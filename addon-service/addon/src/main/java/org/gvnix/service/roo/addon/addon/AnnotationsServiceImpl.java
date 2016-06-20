package org.gvnix.service.roo.addon.addon;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * Utilities to manage annotations.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class AnnotationsServiceImpl implements AnnotationsService {

    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private JavaParserService javaParserService;
    @Reference
    private MetadataService metadataService;
    @Reference
    private TypeLocationService typeLocationService;
    @Reference
    private TypeManagementService typeManagementService;

    private static Logger logger = Logger.getLogger(AnnotationsService.class
            .getName());

    /**
     * {@inheritDoc}
     */
    public void addAddonDependency() {

        // Get configuration (repository and dependency) XML element
        Element conf = XmlUtils.getConfiguration(this.getClass());

        // Find repository elements and add them to the project
        for (Element repo : XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", conf)) {
            projectOperations.addRepository(projectOperations
                    .getFocusedModuleName(), new Repository(repo));
        }

        // Install properties
        List<Element> properties = XmlUtils.findElements(
                "/configuration/gvnix/properties/*", conf);
        for (Element property : properties) {
            projectOperations.addProperty(projectOperations
                    .getFocusedModuleName(), new Property(property));
        }

        // Find dependency elements and update them into the project
        DependenciesVersionManager.manageDependencyVersion(metadataService,
                projectOperations, XmlUtils.findElements(
                        "/configuration/gvnix/dependencies/dependency", conf));
    }

    /**
     * {@inheritDoc}
     */
    public void addJavaTypeAnnotation(JavaType serviceClass, String annotation,
            List<AnnotationAttributeValue<?>> annotationAttributeValues,
            boolean forceUpdate) {

        // Load class or interface details.
        // If class not found an exception will be raised.
        ClassOrInterfaceTypeDetails typeDetails = typeLocationService
                .getTypeDetails(serviceClass);

        // Check and get mutable instance
        Validate.isInstanceOf(ClassOrInterfaceTypeDetails.class, typeDetails,
                "Can't modify " + typeDetails.getName());
        ClassOrInterfaceTypeDetails mutableTypeDetails = (ClassOrInterfaceTypeDetails) typeDetails;

        // Check annotation defined.
        // The annotation can't be updated.
        if (javaParserService.isAnnotationIntroduced(annotation,
                mutableTypeDetails)) {

            if (forceUpdate) {
                logger.log(
                        Level.INFO,
                        "The annotation " + annotation
                                + " is already defined in '"
                                + serviceClass.getFullyQualifiedTypeName()
                                + "' and will be updated.");

                ClassOrInterfaceTypeDetailsBuilder mutableTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                        mutableTypeDetails);
                mutableTypeDetailsBuilder.removeAnnotation(new JavaType(
                        annotation));
                typeManagementService
                        .createOrUpdateTypeOnDisk(mutableTypeDetailsBuilder
                                .build());
            }
            else {
                logger.log(
                        Level.FINE,
                        "The annotation " + annotation
                                + " is already defined in '"
                                + serviceClass.getFullyQualifiedTypeName()
                                + "'.");
                return;

            }
        }

        // Add annotation
        // If attributes are null, create an empty list to avoid
        // NullPointerException
        if (annotationAttributeValues == null) {

            annotationAttributeValues = new ArrayList<AnnotationAttributeValue<?>>();
        }

        // Define annotation.
        AnnotationMetadata defaultAnnotationMetadata = new AnnotationMetadataBuilder(
                new JavaType(annotation), annotationAttributeValues).build();

        // Adds annotation to the entity
        ClassOrInterfaceTypeDetailsBuilder mutableTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                mutableTypeDetails);
        mutableTypeDetailsBuilder.addAnnotation(defaultAnnotationMetadata);
        typeManagementService
                .createOrUpdateTypeOnDisk(mutableTypeDetailsBuilder.build());

        // Delete from chache to update class values.
        metadataService.evict(typeDetails.getDeclaredByMetadataId());

    }

}
