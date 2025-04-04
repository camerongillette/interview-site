package com.cam.interview.core.workflows.process;

import com.adobe.granite.asset.api.Asset;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ModifiableValueMap;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowProcess;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.HashMap;

@Component(service = WorkflowProcess.class,
    property = {"process.label=Update Image Metadata"})
public class UpdateImageMetadataProcess implements WorkflowProcess {

    private static final Logger log = LoggerFactory.getLogger(UpdateImageMetadataProcess.class);

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private static final String SERVICE_USER_ID = "camserviceuser";

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, com.day.cq.workflow.metadata.MetaDataMap processArgs) throws WorkflowException {
        ResourceResolver resourceResolver = null;
        Session session = null;
        try {
            Map<String, Object> param = new HashMap<>();
            param.put(ResourceResolverFactory.SUBSERVICE, SERVICE_USER_ID);
            resourceResolver = resourceResolverFactory.getServiceResourceResolver(param);

            if (resourceResolver == null) {
                log.error("Could not obtain ResourceResolver for service user: {}", SERVICE_USER_ID);
                return;
            }

            session = resourceResolver.adaptTo(Session.class);
            if (session == null) {
                log.error("Could not obtain JCR Session for service user: {}", SERVICE_USER_ID);
                return;
            }

            String payloadPath = workItem.getWorkflowData().getPayload().toString();
            Resource assetResource = resourceResolver.getResource(payloadPath);

            if (assetResource != null) {
                Calendar createdDate = assetResource.getValueMap().get("jcr:created", Calendar.class);
                Asset asset = assetResource.adaptTo(Asset.class);
                if (asset != null) {
                    Resource metadataResource = assetResource.getChild("jcr:content/metadata");
                    if (metadataResource != null) {
                        ModifiableValueMap metadata = metadataResource.adaptTo(ModifiableValueMap.class);
                        if (metadata != null) {
                            // 1. Generate Description from Filename
                            String fileName = asset.getName();
                            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                            Pattern pattern = Pattern.compile("[^a-zA-Z0-9\\s]");
                            Matcher matcher = pattern.matcher(baseName);
                            String description = matcher.replaceAll(" ").trim();
                            description = description.replaceAll("\\s+", " ");

                            metadata.put("dc:description", description);
                            log.debug("Generated description for {}: {}", fileName, description);

                            // 2. Set Expiry Date (1 year from upload)
                            Calendar expiryDate = createdDate;
                            expiryDate.add(Calendar.YEAR, 1);
                            ZonedDateTime zdt = expiryDate.toInstant().atZone(expiryDate.getTimeZone().toZoneId());

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

                            metadata.put("prism:expirationDate", zdt.format(formatter));
                            log.debug("Set expiry date for {} to: {}", fileName, expiryDate.getTime());

                            // Persist the changes
                            if (session.hasPendingChanges()) {
                                session.save();
                                log.info("Metadata updated for asset: {}", payloadPath);
                            } else {
                                log.warn("No pending changes to save for asset: {}", payloadPath);
                            }
                        } else {
                            log.warn("Could not adapt metadata resource to ModifiableValueMap for: {}", payloadPath);
                        }
                    } else {
                        log.warn("Could not find metadata resource (jcr:content/metadata) for: {}", payloadPath);
                    }
                } else {
                    log.warn("Could not adapt resource to Asset for: {}", payloadPath);
                }
            } else {
                log.warn("Payload path {} does not point to a valid asset.", payloadPath);
            }

        } catch (Exception e) {
            log.error("Error during Update Image Metadata workflow process for payload: {}", workItem.getWorkflowData().getPayload(), e);
            throw new WorkflowException("Error updating image metadata: " + e.getMessage());
        } finally {
            if (resourceResolver != null && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
    }
}