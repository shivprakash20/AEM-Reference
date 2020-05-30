package com.training.core.servlets;

import java.util.HashMap;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.framework.Constants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;

@Component(service = Servlet.class, property = {Constants.SERVICE_DESCRIPTION + "=Property Updater Servlet",
        "sling.servlet.paths=" + "/bin/updateProperties", "sling.servlet.methods=" + HttpConstants.METHOD_GET})
public class PropertyUpdaterServlet extends SlingSafeMethodsServlet {
    /**
     * Generated serialVersionUID
     */
    private static final long serialVersionUID = 3722859543804811132L;

    private static final Logger logger = LoggerFactory.getLogger(PropertyUpdaterServlet.class);

    private static final String SYSTEM_USER = "system-user";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    ResourceResolver resourceResolver;

    Session session;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) {

        Node pageNode = null;

        try {
            Map<String, Object> param = new HashMap<String, Object>();
            param.put(ResourceResolverFactory.SUBSERVICE, SYSTEM_USER);
            resourceResolver = resourceResolverFactory.getServiceResourceResolver(param);
            session = resourceResolver.adaptTo(Session.class);

            Resource resource = resourceResolver.getResource("/content/training/jcr:content");

            if(resource != null)
                pageNode = resource.adaptTo(Node.class);

            if(pageNode != null) {
                if (pageNode.hasProperty("currentDate"))
                    pageNode.getProperty("currentDate").remove();
                pageNode.setProperty("currentDate", "16-04-2020");

             pageNode.getSession().save();
            }
            session.save();

        } catch (LoginException | RepositoryException e) {
            logger.error("Failed to Update the Properties on Node :" + e;
        }

    }

}
