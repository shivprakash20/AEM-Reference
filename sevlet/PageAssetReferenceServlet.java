package com.training.core.workflows;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.Servlet;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Shiv Prakash
 * http://localhost:4502/bin/pageReference?pagePath=/content/training/en/testing-page&domainName=http://localhost:4502
 */

@Component(service = Servlet.class, property = {Constants.SERVICE_DESCRIPTION + "=Page Reference Search",
        "sling.servlet.paths=" + "/bin/pageReference", "sling.servlet.methods=" + HttpConstants.METHOD_GET})
public class PageAssetReferenceServlet extends SlingAllMethodsServlet {

    private static final Logger logger = LoggerFactory.getLogger(PageAssetReferenceServlet.class);
    public static final String CONTENT_START = "/content/training/";
    public static final String ASSET_START = "/content/dam/";
    public static final String HTTP_START = "http://";
    public static final String HTTPS_START = "https://";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36";

    ResourceResolver resourceResolver;

    @Reference
    private QueryBuilder queryBuilder;

    Session session;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {
        try {
            List<String> internalList = new ArrayList<String>();
            List<String> externalList = new ArrayList<String>();
            List<String> assetList = new ArrayList<String>();

            resourceResolver = request.getResourceResolver();
            session = resourceResolver.adaptTo(Session.class);
            String pagePath = request.getParameter("pagePath");
            String domainName = request.getParameter("domainName");

            /* Checking all Properties  on Root Node */
            String queryPath = pagePath + "/" + JcrConstants.JCR_CONTENT;
            Resource rootResource = resourceResolver.getResource(queryPath);
            assert rootResource != null;
            ValueMap valueMap = rootResource.getValueMap();
            for (String key : valueMap.keySet()) {
                String[] value = valueMap.get(key, String[].class);
                assert value != null;
                if (value.length == 1) {
                    if (value[0].startsWith(CONTENT_START))
                        internalList.add(value[0]);
                    if (value[0].startsWith(ASSET_START))
                        assetList.add(value[0]);
                    if (value[0].startsWith(HTTP_START) || value[0].startsWith(HTTPS_START))
                        externalList.add(value[0]);
                }
                if (value.length > 1) {
                    for (String currentValue : value) {
                        if (currentValue.startsWith(CONTENT_START))
                            internalList.add(currentValue);
                        if (currentValue.startsWith(ASSET_START))
                            assetList.add(currentValue);
                        if (currentValue.startsWith(HTTP_START) || currentValue.startsWith(HTTPS_START))
                            externalList.add(currentValue);
                    }
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
                    if (value.length == 1) {
                        if (value[0].startsWith(CONTENT_START))
                            internalList.add(value[0]);
                        if (value[0].startsWith(ASSET_START))
                            assetList.add(value[0]);
                        if (value[0].startsWith(HTTP_START) || value[0].startsWith(HTTPS_START))
                            externalList.add(value[0]);
                    }
                    if (value.length > 1) {
                        for (String currentValue : value) {
                            if (currentValue.startsWith(CONTENT_START))
                                internalList.add(currentValue);
                            if (currentValue.startsWith(ASSET_START))
                                assetList.add(currentValue);
                            if (currentValue.startsWith(HTTP_START) || currentValue.startsWith(HTTPS_START))
                                externalList.add(currentValue);
                        }
                    }
                }
            }

            /* Writing ALL URL and Response Code on Response */
            response.getWriter().append("List of All Reference Resource \n\n");

            /* Writing Internal Url */
            if (!internalList.isEmpty()) {
                response.getWriter().append("Internal Url-------------------------------------------- \n\n");
                internalList.forEach(relativePath -> {
                    String fullUrl = domainName + relativePath + ".html";
                    int respCode = responseCode(fullUrl);
                    try {
                        response.getWriter().append("Relative Url :").append(relativePath).append("\n");
                        response.getWriter().append("Full Url :").append(fullUrl).append(" Status :").append(String.valueOf(respCode)).append("\n\n");
                    } catch (IOException e) {
                        logger.error("Error In Writing Local Url " + e);
                    }
                });
            }

            /* Writing External Url */
            if (!externalList.isEmpty()) {
                response.getWriter().append("External Url-------------------------------------------- \n\n");
                externalList.forEach(relativePath -> {
                    int respCode = responseCode(relativePath);
                    try {
                        response.getWriter().append("Full Url :").append(relativePath).append(" Status : ").append(String.valueOf(respCode)).append("\n\n");
                    } catch (IOException e) {
                        logger.error("Error In Writing Local Url " + e);
                    }
                });
            }
            /* Writing Asset Url */
            if (!assetList.isEmpty()) {
                response.getWriter().append("Asset Url-------------------------------------------- \n\n");
                assetList.forEach(relativePath -> {
                    String fullUrl = domainName + relativePath;
                    int respCode = responseCode(fullUrl);
                    try {
                        response.getWriter().append("Relative Url :").append(relativePath).append("\n");
                        response.getWriter().append("Full Url :").append(fullUrl).append(" Status :").append(String.valueOf(respCode)).append("\n\n");
                    } catch (IOException e) {
                        logger.error("Error In Writing Local Url " + e);
                    }
                });
            }


        } catch (Exception e) {
            logger.error("Exception in Reference Search :" + e);
        }
    }

    int responseCode(String finalUrl) {
        int respCode = 0;
        try {
            URL url = new URL(finalUrl);
            if (finalUrl.startsWith(HTTP_START)) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("HEAD");
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);
                httpURLConnection.connect();
                respCode = httpURLConnection.getResponseCode();
                httpURLConnection.disconnect();
            }
            if (finalUrl.startsWith(HTTPS_START)) {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
                httpsURLConnection.setRequestMethod("HEAD");
                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.setRequestProperty("User-Agent", USER_AGENT);
                httpsURLConnection.connect();
                respCode = httpsURLConnection.getResponseCode();
                httpsURLConnection.disconnect();
            }
        } catch (IOException e) {
            logger.error("Error While Making Connection :" + e);
        }
        return respCode;
    }
}
