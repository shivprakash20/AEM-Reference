package com.training.core.workflows;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.DamConstants;
import com.day.cq.dam.commons.util.AssetReferenceSearch;
import com.day.cq.wcm.commons.ReferenceSearch;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.servlet.Servlet;
import java.util.*;

/**
 * @author Shiv Prakash
 * http://localhost:4502/bin/referenceSearch?pagePath=/content/training/en/testing-page
 */

@Component(service = Servlet.class, property = { Constants.SERVICE_DESCRIPTION + "=Reference Search",
        "sling.servlet.paths=" + "/bin/referenceSearch", "sling.servlet.methods=" + HttpConstants.METHOD_GET })
public class ReferenceSearchClass extends SlingAllMethodsServlet {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceSearchClass.class);

    ResourceResolver resourceResolver;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {

        try {
            resourceResolver = request.getResourceResolver();
            String pagePath = request.getParameter("pagePath");

            /* Reference Search of Page
            * Provide list of all pages where current page are being Used */
            ReferenceSearch referenceSearch = new ReferenceSearch();
            referenceSearch.setExact(true);
            referenceSearch.setHollow(true);
            referenceSearch.setMaxReferencesPerPage(-1);

            Collection<ReferenceSearch.Info> resultSet = referenceSearch.search(resourceResolver, pagePath).values();
            for (ReferenceSearch.Info info : resultSet) {
                String currentPage = info.getPagePath();
            }

           /* All Asset Available on Current Page
           * Provide List of All Asset Available on Current Page */
            Resource resource = resourceResolver.getResource(pagePath+"/"+ JcrConstants.JCR_CONTENT);
            assert resource != null;
            Node node = resource.adaptTo(Node.class);

            AssetReferenceSearch assetReferenceSearch = new AssetReferenceSearch(node, DamConstants.MOUNTPOINT_ASSETS,resourceResolver);
            Map<String, Asset> allReference = new HashMap<String, Asset>(assetReferenceSearch.search());

            for (Map.Entry<String, Asset> entry : allReference.entrySet()) {
                String val = entry.getKey();
                logger.debug("Key :" + val);

                Asset asset = entry.getValue();
                logger.debug("Value "+ asset);
            }

        }catch (Exception e){
           logger.error("Exception in Reference Search :" + e);
        }
    }
}
