package com.training.core.services;

import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Session;
import java.util.List;

/**
 * @author Shiv Prakash
 */
public interface ReferencePageAssetService {
    List<String> getPageAssetReference(String rootPath, ResourceResolver resourceResolver, Session session);
}
