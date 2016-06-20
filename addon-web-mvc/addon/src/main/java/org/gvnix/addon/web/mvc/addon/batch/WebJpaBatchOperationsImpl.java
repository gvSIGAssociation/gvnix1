/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana     
 * Copyright (C) 2013 Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/copyleft/gpl.html>.
 */
package org.gvnix.addon.web.mvc.addon.batch;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.addon.JpaOperations;
import org.gvnix.addon.jpa.addon.batch.JpaBatchAnnotationValues;
import org.gvnix.addon.jpa.annotations.batch.GvNIXJpaBatch;
import org.gvnix.addon.web.mvc.addon.MvcOperations;
import org.gvnix.addon.web.mvc.annotations.batch.GvNIXWebJpaBatch;
import org.gvnix.support.WebProjectUtils;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.operations.AbstractOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.RooJavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.maven.Pom;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link WebJpaBatchOperations}
 * 
 * @since 1.1
 */
@Component
@Service
public class WebJpaBatchOperationsImpl extends AbstractOperations implements
        WebJpaBatchOperations {

    protected final static Logger LOGGER = HandlerUtils
            .getLogger(WebJpaBatchOperationsImpl.class);

    private static final String JACKSON2_RM_HANDLER_ADAPTER = "org.gvnix.web.json.Jackson2RequestMappingHandlerAdapter";
    private static final String OBJECT_MAPPER = "org.gvnix.web.json.ConversionServiceObjectMapper";

    private static final JavaType JPA_BATCH_ANNOTATION = new JavaType(
            GvNIXJpaBatch.class);

    private static final JavaType WEB_JPA_BATCH_ANNOTATION = new JavaType(
            GvNIXWebJpaBatch.class);

    private static final List<JavaType> JPA_BATCH_SERVICE_ANNOTATIONS = Arrays
            .asList(JPA_BATCH_ANNOTATION, SpringJavaType.SERVICE);

    private static final String WEBMCV_DATABINDER_BEAN_ID = "dataBinderRequestMappingHandlerAdapter";

    private ProjectOperations projectOperations;

    private MvcOperations mvcOperations;

    private TypeLocationService typeLocationService;

    private TypeManagementService typeManagementService;

    private MetadataService metadataService;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Commands are available if Spring MVC, gvNIX JPA dependencies and gvNIX
     * MVC dependencies are installed
     */
    public boolean isCommandAvailable() {
        return getProjectOperations().isFeatureInstalledInFocusedModule(
                FeatureNames.MVC)
                && getProjectOperations().isFeatureInstalledInFocusedModule(
                        JpaOperations.FEATURE_NAME_GVNIX_JPA)
                && getProjectOperations().isFeatureInstalledInFocusedModule(
                        FEATURE_NAME_GVNIX_MVC_BATCH);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Setup is available if Spring MVC and gvNIX JPA dependencies are installed
     * and gvNIX MVC dependencies have not been installed yet.
     * <p/>
     * Note if gvNIX MVC dependencies are installed there is no need to run
     * setup and it will be not available in
     */
    public boolean isSetupAvailable() {
        return getProjectOperations().isFeatureInstalledInFocusedModule(
                FeatureNames.MVC)
                && getProjectOperations().isFeatureInstalledInFocusedModule(
                        JpaOperations.FEATURE_NAME_GVNIX_JPA)
                && !getProjectOperations().isFeatureInstalledInFocusedModule(
                        FEATURE_NAME_GVNIX_MVC_BATCH);
    }

    /** {@inheritDoc} */
    public void setup() {
        // If gvNIX MVC dependencies are not installed, install them
        if (!getProjectOperations().isFeatureInstalledInFocusedModule(
                MvcOperations.FEATURE_NAME_GVNIX_MVC)) {
            getMvcOperations().setup();
        }

        installDependencies();
        updateWebMvcConfig();
    }

    /**
     * Install jackson 2 dependencies on project pom
     */
    private void installDependencies() {
        // Get add-on configuration file
        Element configuration = XmlUtils.getConfiguration(getClass());
        // Install properties
        List<Element> properties = XmlUtils.findElements(
                "/configuration/gvnix/properties/*", configuration);
        for (Element property : properties) {
            getProjectOperations().addProperty(
                    getProjectOperations().getFocusedModuleName(),
                    new Property(property));
        }

        // Install dependencies
        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", configuration);

        DependenciesVersionManager.manageDependencyVersion(
                getMetadataService(), getProjectOperations(), depens);
    }

    /**
     * Update the webmvc-config.xml file in order to register
     * Jackson2RequestMappingHandlerAdapter
     * 
     * @param targetPackage
     */
    private void updateWebMvcConfig() {
        LogicalPath webappPath = WebProjectUtils
                .getWebappPath(getProjectOperations());
        String webMvcXmlPath = getProjectOperations().getPathResolver()
                .getIdentifier(webappPath, "WEB-INF/spring/webmvc-config.xml");
        Validate.isTrue(fileManager.exists(webMvcXmlPath),
                "webmvc-config.xml not found");

        MutableFile webMvcXmlMutableFile = null;
        Document webMvcXml;

        try {
            webMvcXmlMutableFile = fileManager.updateFile(webMvcXmlPath);
            webMvcXml = XmlUtils.getDocumentBuilder().parse(
                    webMvcXmlMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = webMvcXml.getDocumentElement();

        Element dataBinder = XmlUtils.findFirstElement("bean[@id='"
                + WEBMCV_DATABINDER_BEAN_ID + "']", root);
        if (dataBinder != null) {
            root.removeChild(dataBinder);
        }

        // add bean tag to argument-resolvers
        Element bean = webMvcXml.createElement("bean");
        bean.setAttribute("id", WEBMCV_DATABINDER_BEAN_ID);
        bean.setAttribute("p:order", "1");
        bean.setAttribute("class", JACKSON2_RM_HANDLER_ADAPTER);

        Element property = webMvcXml.createElement("property");
        property.setAttribute("name", "objectMapper");

        Element objectMapperBean = webMvcXml.createElement("bean");
        objectMapperBean.setAttribute("class", OBJECT_MAPPER);
        property.appendChild(objectMapperBean);
        bean.appendChild(property);
        root.appendChild(bean);

        XmlUtils.writeXml(webMvcXmlMutableFile.getOutputStream(), webMvcXml);
    }

    /** {@inheritDoc} */
    public void addAll() {
        Set<JavaType> jpaBatchServices = new HashSet<JavaType>(
                getJpaBatchServices());
        for (JavaType controller : getTypeLocationService()
                .findTypesWithAnnotation(RooJavaType.ROO_WEB_SCAFFOLD)) {
            ClassOrInterfaceTypeDetails controllerDetails = getTypeLocationService()
                    .getTypeDetails(controller);

            // check for if there is jpa batch service
            JavaType entity = getFormBackingObject(controllerDetails);

            if (entity == null) {
                continue;
            }

            // look for jpaBatchSevice for entity
            JavaType service = findJpaServiceForEntity(entity, jpaBatchServices);
            if (service == null) {
                continue;
            }
            // remove service from list (this service is not needed any more)
            jpaBatchServices.remove(service);

            // Add annotations
            add(controller, service);
        }
    }

    private Set<JavaType> getJpaBatchServices() {
        return getTypeLocationService().findTypesWithAnnotation(
                JPA_BATCH_SERVICE_ANNOTATIONS);
    }

    /**
     * Find the Spring service which entity match with required
     * 
     * @param entity
     * @param jpaBatchServices list of class annotated with
     *        {@link #JPA_BATCH_SERVICE_ANNOTATIONS}
     * @return
     */
    private JavaType findJpaServiceForEntity(JavaType entity,
            Set<JavaType> jpaBatchServices) {
        JavaType serviceEntity;
        for (JavaType service : jpaBatchServices) {
            serviceEntity = getJpaBatchEntity(service);
            if (ObjectUtils.equals(serviceEntity, entity)) {
                return service;
            }
        }
        return null;
    }

    /**
     * Gets the entity value of {@link GvNIXJpaBatch}
     * 
     * @param service
     * @return
     */
    private JavaType getJpaBatchEntity(JavaType service) {
        ClassOrInterfaceTypeDetails serviceDatils = getTypeLocationService()
                .getTypeDetails(service);
        JpaBatchAnnotationValues jpaBatchValues = new JpaBatchAnnotationValues(
                serviceDatils);
        return jpaBatchValues.getEntity();
    }

    /**
     * Gets the formBackingObject value of a {@link RooWebScaffold} annotation.
     * 
     * @param controllerDetails
     * @return
     */
    private JavaType getFormBackingObject(
            ClassOrInterfaceTypeDetails controllerDetails) {
        WebScaffoldAnnotationValues annotationValues = new WebScaffoldAnnotationValues(
                controllerDetails);
        return annotationValues.getFormBackingObject();
    }

    /**
     * Gets the formBackingObject value of a {@link RooWebScaffold} annotation.
     * 
     * @param controllerDetails
     * @return
     */
    private JavaType getFormBackingObject(JavaType controller) {
        ClassOrInterfaceTypeDetails controllerDetails = getTypeLocationService()
                .getTypeDetails(controller);
        return getFormBackingObject(controllerDetails);
    }

    /** {@inheritDoc} */
    @Override
    public void add(JavaType controller, JavaType service) {
        Validate.notNull(controller, "Controller required");
        if (service != null) {
            annotateController(controller, service);
            return;
        }

        // check for if there is jpa batch service suitable
        JavaType entity = getFormBackingObject(controller);
        service = findJpaServiceForEntity(entity, getJpaBatchServices());
        Validate.notNull(service,
                "No spring service suitable found for Controller: %s",
                controller);

        annotateController(controller, service);
    }

    private void annotateController(JavaType controller, JavaType service) {
        Validate.notNull(controller, "Controller required");
        Validate.notNull(service, "Service required");

        ClassOrInterfaceTypeDetails existing = getTypeLocationService()
                .getTypeDetails(controller);

        // Get controller annotation
        JavaType entity = getFormBackingObject(existing);
        Validate.notNull(entity, "Operation only supported for controllers");

        final boolean isAlreadyAnnotated = MemberFindingUtils
                .getAnnotationOfType(existing.getAnnotations(),
                        WEB_JPA_BATCH_ANNOTATION) != null;

        // Test if the annotation already exists on the target type
        if (!isAlreadyAnnotated) {
            ClassOrInterfaceTypeDetailsBuilder detailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                    existing);

            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                    WEB_JPA_BATCH_ANNOTATION);

            annotationBuilder.addClassAttribute("service", service);

            // Add annotation to target type
            detailsBuilder.addAnnotation(annotationBuilder.build());

            // Save changes to disk
            getTypeManagementService().createOrUpdateTypeOnDisk(
                    detailsBuilder.build());
        }
    }

    @Override
    public String getName() {
        return FEATURE_NAME_GVNIX_MVC_BATCH;
    }

    /**
     * Returns true if gvNIX Web MVC dependency is installed in current project.
     * 
     * @param moduleName feature name to check in current project
     * @return true if given feature name is installed, otherwise returns false
     */
    public boolean isInstalledInModule(final String moduleName) {
        final Pom pom = getProjectOperations().getPomFromModuleName(moduleName);
        if (pom == null) {
            return false;
        }

        // Look for gvnix web mvc dependency
        for (final Dependency dependency : pom.getDependencies()) {
            if ("org.gvnix.web.json.binding".equals(dependency.getArtifactId())) {
                return true;
            }
        }
        return false;
    }

    public ProjectOperations getProjectOperations() {
        if (projectOperations == null) {
            // Get all Services implement ProjectOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                ProjectOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (ProjectOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load ProjectOperations on WebJpaBatchOperationsImpl.");
                return null;
            }
        }
        else {
            return projectOperations;
        }
    }

    public MvcOperations getMvcOperations() {
        if (mvcOperations == null) {
            // Get all Services implement MvcOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(MvcOperations.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (MvcOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MvcOperations on WebJpaBatchOperationsImpl.");
                return null;
            }
        }
        else {
            return mvcOperations;
        }
    }

    public TypeLocationService getTypeLocationService() {
        if (typeLocationService == null) {
            // Get all Services implement TypeLocationService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                TypeLocationService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (TypeLocationService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load TypeLocationService on WebJpaBatchOperationsImpl.");
                return null;
            }
        }
        else {
            return typeLocationService;
        }
    }

    public TypeManagementService getTypeManagementService() {
        if (typeManagementService == null) {
            // Get all Services implement TypeManagementService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                TypeManagementService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (TypeManagementService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load TypeManagementService on WebJpaBatchOperationsImpl.");
                return null;
            }
        }
        else {
            return typeManagementService;
        }
    }

    public MetadataService getMetadataService() {
        if (metadataService == null) {
            // Get all Services implement MetadataService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MetadataService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (MetadataService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MetadataService on WebJpaBatchOperationsImpl.");
                return null;
            }
        }
        else {
            return metadataService;
        }
    }
}