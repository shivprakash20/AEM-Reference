package com.training.core.servicesImpl;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.training.core.services.ReferencePageAssetService;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Shiv Prakash
 */
@Component(service = ReferencePageAssetService.class, immediate = true)
public class ReferencePageAssetServiceImpl implements ReferencePageAssetService {

    private static final Logger logger = LoggerFactory.getLogger(ReferencePageAssetServiceImpl.class);
    public static final String CONTENT_START = "/content/training/";
    public static final String ASSET_START = "/content/dam/";

    @Reference
    private QueryBuilder queryBuilder;

    List<String> pageAssetList;

    @Override
    public List<String> getPageAssetReference(String rootPath, ResourceResolver resourceResolver, Session session) {
        try {
            pageAssetList = new ArrayList<String>();

            if (rootPath.startsWith(CONTENT_START)) {
                /* Checking all Properties  on Root Node */
                String queryPath = rootPath + "/" + JcrConstants.JCR_CONTENT;
                Resource rootResource = resourceResolver.getResource(queryPath);
                assert rootResource != null;
                ValueMap valueMap = rootResource.getValueMap();
                for (String key : valueMap.keySet()) {
                    String[] value = valueMap.get(key, String[].class);
                    assert value != null;
                    if (value.length > 1) {
                        for (String currentValue : value) {
                            if (currentValue.startsWith(CONTENT_START) || currentValue.startsWith(ASSET_START))
                                pageAssetList.add(currentValue);
                        }
                    }
                    if (value.length == 1) {
                        if (value[0].startsWith(CONTENT_START) || value[0].startsWith(ASSET_START))
                            pageAssetList.add(value[0]);
                    }
                }

                /* Using Query Builder To Read All author page inside the Current Page */
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("path", queryPath);
                hashMap.put("p.limit", "-1");

                Query query = queryBuilder.createQuery(PredicateGroup.create(hashMap), session);
                query.setStart(0);
                query.setHitsPerPage(20);
                SearchResult result = query.getResult();

                /* Checking all Properties  on Children Node */
                for (Hit hit : result.getHits()) {
                    String currentPath = hit.getPath();
                    Resource currentResource = resourceResolver.getResource(currentPath);
                    assert currentResource != null;
                    ValueMap currentValueMap = currentResource.getValueMap();
                    for (String key : currentValueMap.keySet()) {
                        String[] value = currentValueMap.get(key, String[].class);
                        assert value != null;
                        if (value.length > 1) {
                            for (String currentValue : value) {
                                if (currentValue.startsWith(CONTENT_START) || currentValue.startsWith(ASSET_START))
                                    pageAssetList.add(currentValue);
                            }
                        }
                        if (value.length == 1) {
                            if (value[0].startsWith(CONTENT_START) || value[0].startsWith(ASSET_START))
                                pageAssetList.add(value[0]);
                        }
                    }
                }
            }

            if (rootPath.startsWith(ASSET_START)) {
                /* Checking all Properties  on Asset JCR Node */
                String jcrPath = rootPath + "/" + JcrConstants.JCR_CONTENT;
                Resource jcrResource = resourceResolver.getResource(jcrPath);
                assert jcrResource != null;
                ValueMap valueMap = jcrResource.getValueMap();
                for (String key : valueMap.keySet()) {
                    String[] value = valueMap.get(key, String[].class);
                    assert value != null;
                    if (value.length > 1) {
                        for (String currentValue : value) {
                            if (currentValue.startsWith(CONTENT_START) || currentValue.startsWith(ASSET_START))
                                pageAssetList.add(currentValue);
                        }
                    }
                    if (value.length == 1) {
                        if (value[0].startsWith(CONTENT_START) || value[0].startsWith(ASSET_START))
                            pageAssetList.add(value[0]);
                    }
                }

                /* Checking all Properties  on Asset Metadata Node */
                String metadataPath = rootPath + "/" + JcrConstants.JCR_CONTENT + "/metadata";
                Resource metadataResource = resourceResolver.getResource(metadataPath);
                assert metadataResource != null;
                ValueMap metaValueMap = metadataResource.getValueMap();
                for (String key : metaValueMap.keySet()) {
                    String[] value = metaValueMap.get(key, String[].class);
                    assert value != null;
                    if (value.length > 1) {
                        for (String currentValue : value) {
                            if (currentValue.startsWith(CONTENT_START) || currentValue.startsWith(ASSET_START))
                                pageAssetList.add(currentValue);
                        }
                    }
                    if (value.length == 1) {
                        if (value[0].startsWith(CONTENT_START) || value[0].startsWith(ASSET_START))
                            pageAssetList.add(value[0]);
                    }
                }

            }


        } catch (Exception e) {
            logger.error("Exception in Reading the Reference " + e);
        }

        return pageAssetList;
    }
}
