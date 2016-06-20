package org.springframework.roo.addon.solr;

import static org.springframework.roo.model.RooJavaType.ROO_SOLR_WEB_SEARCHABLE;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadataProvider;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Provides {@link SolrWebSearchMetadata}.
 * 
 * @author Stefan Schmidt
 * @since 1.1
 */
@Component
@Service
public class SolrWebSearchMetadataProvider extends AbstractItdMetadataProvider {
	
	protected final static Logger LOGGER = HandlerUtils.getLogger(SolrWebSearchMetadataProvider.class);
	
    private WebScaffoldMetadataProvider webScaffoldMetadataProvider;

    protected void activate(final ComponentContext cContext) {
    	context = cContext.getBundleContext();
        getMetadataDependencyRegistry().registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        getWebScaffoldMetadataProvider().addMetadataTrigger(ROO_SOLR_WEB_SEARCHABLE);
        addMetadataTrigger(ROO_SOLR_WEB_SEARCHABLE);
    }

    @Override
    protected String createLocalIdentifier(final JavaType javaType,
            final LogicalPath path) {
        return SolrWebSearchMetadata.createIdentifier(javaType, path);
    }

    /**
     * OSGi bundle deactivation callback
     * 
     * @param context
     * @since 1.2.0
     */
    protected void deactivate(final ComponentContext context) {
        getMetadataDependencyRegistry().deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        getWebScaffoldMetadataProvider()
                .removeMetadataTrigger(ROO_SOLR_WEB_SEARCHABLE);
        removeMetadataTrigger(ROO_SOLR_WEB_SEARCHABLE);
    }

    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            final String metadataIdentificationString) {
        final JavaType javaType = SolrWebSearchMetadata
                .getJavaType(metadataIdentificationString);
        final LogicalPath path = SolrWebSearchMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    public String getItdUniquenessFilenameSuffix() {
        return "SolrWebSearch";
    }

    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            final String metadataIdentificationString,
            final JavaType aspectName,
            final PhysicalTypeMetadata governorPhysicalTypeMetadata,
            final String itdFilename) {
        // We need to parse the annotation, which we expect to be present
        final SolrWebSearchAnnotationValues annotationValues = new SolrWebSearchAnnotationValues(
                governorPhysicalTypeMetadata);
        if (!annotationValues.isAnnotationFound()
                || annotationValues.getSearchMethod() == null) {
            return null;
        }

        // Acquire bean info (we need getters details, specifically)
        final JavaType javaType = SolrWebSearchMetadata
                .getJavaType(metadataIdentificationString);
        final LogicalPath path = SolrWebSearchMetadata
                .getPath(metadataIdentificationString);
        final String webScaffoldMetadataKey = WebScaffoldMetadata
                .createIdentifier(javaType, path);

        // We want to be notified if the getter info changes in any way
        getMetadataDependencyRegistry().registerDependency(webScaffoldMetadataKey,
                metadataIdentificationString);
        final WebScaffoldMetadata webScaffoldMetadata = (WebScaffoldMetadata) metadataService
                .get(webScaffoldMetadataKey);

        // Abort if we don't have getter information available
        if (webScaffoldMetadata == null || !webScaffoldMetadata.isValid()) {
            return null;
        }

        final JavaType targetObject = webScaffoldMetadata.getAnnotationValues()
                .getFormBackingObject();
        Validate.notNull(
                targetObject,
                "Could not acquire form backing object for the '%s' controller",
                WebScaffoldMetadata.getJavaType(webScaffoldMetadata.getId())
                        .getFullyQualifiedTypeName());

        final String targetObjectMid = typeLocationService
                .getPhysicalTypeIdentifier(targetObject);
        final LogicalPath targetObjectPath = PhysicalTypeIdentifier
                .getPath(targetObjectMid);

        final SolrMetadata solrMetadata = (SolrMetadata) metadataService
                .get(SolrMetadata.createIdentifier(targetObject,
                        targetObjectPath));
        Validate.notNull(solrMetadata,
                "Could not determine SolrMetadata for type '%s'",
                targetObject.getFullyQualifiedTypeName());

        // Otherwise go off and create the to String metadata
        return new SolrWebSearchMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, annotationValues,
                webScaffoldMetadata.getAnnotationValues(),
                solrMetadata.getAnnotationValues());
    }

    public String getProvidesType() {
        return SolrWebSearchMetadata.getMetadataIdentiferType();
    }
    
    public WebScaffoldMetadataProvider getWebScaffoldMetadataProvider(){
    	if(webScaffoldMetadataProvider == null){
    		// Get all Services implement WebScaffoldMetadataProvider interface
    		try {
    			ServiceReference<?>[] references = this.context.getAllServiceReferences(WebScaffoldMetadataProvider.class.getName(), null);
    			
    			for(ServiceReference<?> ref : references){
    				return (WebScaffoldMetadataProvider) this.context.getService(ref);
    			}
    			
    			return null;
    			
    		} catch (InvalidSyntaxException e) {
    			LOGGER.warning("Cannot load WebScaffoldMetadataProvider on SolrWebSearchMetadataProvider.");
    			return null;
    		}
    	}else{
    		return webScaffoldMetadataProvider;
    	}
    }
}