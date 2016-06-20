package org.gvnix.addon.fancytree.addon;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.fancytree.annotations.GvNIXFancyTree;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Provides {@link GvNIXFancyTreeMetadata}.
 * 
 * @author gvNIX Team
 * @since 1.5
 */
@Component
@Service
public class GvNIXFancyTreeMetadataProvider extends AbstractItdMetadataProvider {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(GvNIXFancyTreeMetadataProvider.class);

    public static final JavaType FANCY_TREE_ANNOTATION = new JavaType(
            GvNIXFancyTree.class);

    protected void activate(final ComponentContext cContext) {
        context = cContext.getBundleContext();
        getMetadataDependencyRegistry().addNotificationListener(this);
        getMetadataDependencyRegistry().registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(FANCY_TREE_ANNOTATION);
    }

    @Override
    protected String createLocalIdentifier(final JavaType javaType,
            final LogicalPath path) {
        return GvNIXFancyTreeMetadata.createIdentifier(javaType, path);
    }

    protected void deactivate(final ComponentContext context) {
        getMetadataDependencyRegistry().removeNotificationListener(this);
        getMetadataDependencyRegistry().deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(FANCY_TREE_ANNOTATION);
    }

    @Override
    protected String getGovernorPhysicalTypeIdentifier(
            final String metadataIdentificationString) {
        final JavaType javaType = GvNIXFancyTreeMetadata
                .getJavaType(metadataIdentificationString);
        final LogicalPath path = GvNIXFancyTreeMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXFancyTree";
    }

    @Override
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            final String metadataIdentificationString,
            final JavaType aspectName,
            final PhysicalTypeMetadata governorPhysicalTypeMetadata,
            final String itdFilename) {

        final JavaType controller = GvNIXFancyTreeMetadata
                .getJavaType(metadataIdentificationString);

        // Getting controller package
        JavaPackage controllerPackage = controller.getPackage();

        ClassOrInterfaceTypeDetails controllerDetails = getTypeLocationService()
                .getTypeDetails(controller);

        // Getting @GvNIXFancyTree annotation attributes
        AnnotationMetadata annotation = controllerDetails
                .getAnnotation(new JavaType(GvNIXFancyTree.class));

        AnnotationAttributeValue<Boolean> editableAttr = annotation
                .getAttribute("editable");
        AnnotationAttributeValue<String> mappingAttr = annotation
                .getAttribute("mapping");
        AnnotationAttributeValue<String> pageAttr = annotation
                .getAttribute("page");

        Boolean editableAttrVal = false;
        String mappingAttrVal = "";
        String pageAttrVal = "";

        if (editableAttr != null) {
            editableAttrVal = editableAttr.getValue();
        }

        if (mappingAttr != null) {
            mappingAttrVal = mappingAttr.getValue();
        }

        if (pageAttr != null) {
            pageAttrVal = pageAttr.getValue();
        }

        // Getting Controller path
        String controllerPath = (String) controllerDetails
                .getAnnotation(SpringJavaType.REQUEST_MAPPING)
                .getAttribute("value").getValue();

        controllerPath = controllerPath.replace("/", "").trim();

        return new GvNIXFancyTreeMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, editableAttrVal,
                mappingAttrVal, pageAttrVal, controller, controllerPath,
                controllerPackage);
    }

    public String getProvidesType() {
        return GvNIXFancyTreeMetadata.getMetadataIdentiferType();
    }

}