package com.training.core.workflows;

import com.day.cq.commons.jcr.JcrConstants;
import com.training.core.services.ReferencePageAssetService;

import com.day.cq.replication.*;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.HistoryItem;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.*;

/**
 * @author Shiv Prakash
 */

@Component(service = WorkflowProcess.class, property = {Constants.SERVICE_DESCRIPTION + "= Training Replication Agent Selection Process Step",
        Constants.SERVICE_VENDOR + "= training.com",
        "process.label" + "= Selected Replication Agent Process Step"})
public class ActivateWithSelectedAgent implements WorkflowProcess {

    private static final Logger logger = LoggerFactory.getLogger(ActivateWithSelectedAgent.class);

    public static final String LOCAL_PUBLISH_AGENT_ID = "local-test";
    public static final String CONTENT_START = "/content/testing/";
    public static final String ASSET_START = "/content/dam/";

    @Reference
    Replicator replicator;

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    @Reference
    ReferencePageAssetService referencePageAssetService;

    ResourceResolver resourceResolver;
    Session session;
    List<String> pageAssetList;
    List<String> pageList;
    List<String> assetList;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {

        try {
                WorkflowData workflowData = workItem.getWorkflowData();
                String payloadPath = workflowData.getPayload().toString();
                session = workflowSession.getSession();
                resourceResolver = resourceResolverFactory.getResourceResolver(Collections.singletonMap("user.jcr.session", (Object) session));

                /* Accessing History of Workflow Metadata */
                String pageActivation = StringUtils.EMPTY;
                String assetActivation = StringUtils.EMPTY;

                List<HistoryItem> historyItemList = workflowSession.getHistory(workItem.getWorkflow());
                MetaDataMap historyMetaDataMap = historyItemList.get(historyItemList.size() - 1).getWorkItem().getMetaDataMap();
                if (historyMetaDataMap.containsKey("refPageActivation"))
                    pageActivation = historyMetaDataMap.get("refPageActivation").toString();
                if (historyMetaDataMap.containsKey("refPageActivation"))
                    assetActivation = historyMetaDataMap.get("refPageActivation").toString();

                /* List of all Reference Page and Asset on Current Page */
                pageAssetList = new ArrayList<String>();
                pageList = new ArrayList<String>();
                assetList = new ArrayList<String>();

                if (!assetPromotion.isEmpty() || !pagePromotion.isEmpty())
                    pageAssetList = referencePageAssetService.getPageAssetReference(payloadPath, resourceResolver, session);

                if (!pageAssetList.isEmpty()) {
                    pageAssetList.forEach(pageAsset -> {
                        if (pageAsset.startsWith(CONTENT_START))
                            pageList.add(pageAsset);
                        if (pageAsset.startsWith(ASSET_START))
                            assetList.add(pageAsset);
                    });
                }

                /* Selecting Replication Agent for Publication */
                ReplicationOptions replicationOptions = new ReplicationOptions();
                replicationOptions.setFilter(agent -> LOCAL_PUBLISH_AGENT_ID.equals(agent.getId()));

                /* PayLoad Replication*/
                replicator.replicate(session, ReplicationActionType.ACTIVATE, payloadPath, replicationOptions);

                /* Reference Page Replication */
                if (!pageActivation.isEmpty() && pageActivation.equals("true") && !pageList.isEmpty()) {
                    pageList.forEach(localPagePath -> {
                        try {
                            Resource pageResource = resourceResolver.getResource(localPagePath + "/" + JcrConstants.JCR_CONTENT);
                            if (pageResource != null) {
                                replicator.replicate(session, ReplicationActionType.ACTIVATE, localPagePath, replicationOptions);
                            }
                        } catch (ReplicationException e) {
                            logger.error("Exception in Replication :" + e);
                        }
                    });
                }

                /* Reference Asset Replication */
                if (!pageActivation.isEmpty() && pageActivation.equals("true") && !assetList.isEmpty()) {
                    assetList.forEach(assetPagePath -> {
                        try {
                            Resource assetResource = resourceResolver.getResource(assetPagePath + "/" + JcrConstants.JCR_CONTENT);
                            if (assetResource != null) {
                                replicator.replicate(session, ReplicationActionType.ACTIVATE, assetPagePath, replicationOptions);
                            }
                        } catch (ReplicationException e) {
                            logger.error("Exception in Replication :" + e);
                        }
                    });
                }
        } catch (ReplicationException | LoginException e) {
            logger.error("Error in replication :" + e);
        }
    }
}
