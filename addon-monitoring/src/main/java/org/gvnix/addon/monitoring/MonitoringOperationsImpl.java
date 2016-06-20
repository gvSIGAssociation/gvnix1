package org.gvnix.addon.monitoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.WebProjectUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Implementation of operations this add-on offers.
 * 
 * @since 1.4.0
 */
@Component
@Service
public class MonitoringOperationsImpl implements MonitoringOperations {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(MonitoringOperationsImpl.class);

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    private static final String MONITORING = "monitoring";
    private static final String BEAN_TAG = "bean";
    private static final String CLASS_TAG = "class";
    private static final String NAME_TAG = "name";
    private static final String JM_ANN_POINTCUT = "net.bull.javamelody.MonitoredWithAnnotationPointcut";

    private static final JavaType SPRING_MONITORING_ANNOTATION = new JavaType(
            "net.bull.javamelody.MonitoredWithSpring");

    private static final JavaType SPRING_CONTROLLER = new JavaType(
            "org.springframework.stereotype.Controller");

    private static final JavaType SPRING_SERVICE = new JavaType(
            "org.springframework.stereotype.Service");

    private MenuOperations menuOperations;

    protected FileManager fileManager;

    private PathResolver pathResolver;

    private PropFileOperations propFileOperations;

    private ProjectOperations projectOperations;

    private TypeLocationService typeLocationService;

    private TypeManagementService typeManagementService;

    /** {@inheritDoc} */
    public boolean isCommandAvailable() {
        // Check if a project has been created
        return getProjectOperations().isFocusedProjectAvailable();
    }

    public boolean isAddAvailable() {
        return getProjectOperations().isFeatureInstalledInFocusedModule(
                FEATURE_NAME_GVNIX_MONITORING);
    }

    /** {@inheritDoc} */
    public void setup(String pathString) {
        // Adding pom.xml dependencies
        addPomDependencies();
        // Modifying web.xml
        updateWebXML(pathString);
        // Adding JavaMelody persistence
        updatePersistence();
        // Modifying ApplicationContext to enable SQL monitoring
        updateAppContextSQL();
        // Add i18n necessary messages
        addI18nControllerProperties();
        // Adding menu entry
        addMenuEntry();
    }

    /**
     * This method adds a new entry menu
     */
    public void addMenuEntry() {
        String finalPath = MONITORING;
        getMenuOperations().addMenuItem(
                new JavaSymbolName("monitoring_menu_category"),
                new JavaSymbolName("monitoring_menu_entry"),
                "JMelody Monitoring", "global_generic", "/" + finalPath, null,
                getWebappPath());
    }

    /**
     * This method add necessary properties to messages.properties for
     * Controller
     */
    public void addI18nControllerProperties() {

        Map<String, String> propertyList = new HashMap<String, String>();

        propertyList.put("menu_category_monitoring_menu_category_label",
                MONITORING);

        getPropFileOperations().addProperties(getWebappPath(),
                "WEB-INF/i18n/application.properties", propertyList, true,
                false);

    }

    /**
     * This method updates ApplicationContext.xml to enable SQL monitoring
     */
    public void updateAppContextSQL() {
        String appContextPath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES,
                "META-INF/spring/applicationContext.xml");

        if (getFileManager().exists(appContextPath)) {
            Document docXml = WebProjectUtils.loadXmlDocument(appContextPath,
                    getFileManager());

            // Getting root element
            Element docRoot = docXml.getDocumentElement();

            // Checking if exist
            NodeList beanElements = docRoot.getElementsByTagName(BEAN_TAG);

            for (int i = 0; i < beanElements.getLength(); i++) {
                Node bean = beanElements.item(i);
                NamedNodeMap beanAttr = bean.getAttributes();
                if (beanAttr != null) {
                    Node idAttr = beanAttr.getNamedItem("id");
                    // Checking if bean exists on current beans
                    if ("springDataSourceBeanPostProcessor".equals(idAttr
                            .getNodeValue())) {
                        return;
                    }
                }
            }

            // Creating new element (bean)
            Element beanElement = docXml.createElement(BEAN_TAG);
            beanElement.setAttribute("id", "springDataSourceBeanPostProcessor");
            beanElement.setAttribute(CLASS_TAG,
                    "net.bull.javamelody.SpringDataSourceBeanPostProcessor");

            docRoot.appendChild(beanElement);

            // Saving changes
            getFileManager().createOrUpdateTextFileIfRequired(appContextPath,
                    XmlUtils.nodeToString(docXml), true);
        }

    }

    /**
     * This method updates persistence.xml to add persistence which is needed by
     * JavaMelody to work
     */
    public void updatePersistence() {
        String persistencePath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES, "META-INF/persistence.xml");

        if (getFileManager().exists(persistencePath)) {
            Document docXml = WebProjectUtils.loadXmlDocument(persistencePath,
                    getFileManager());

            // Getting root element
            Element docRoot = docXml.getDocumentElement();

            // Getting provider
            NodeList allProviders = docRoot.getElementsByTagName("provider");

            // Modifying or creating provider element
            if (allProviders.getLength() > 0) {
                for (int i = 0; i < allProviders.getLength(); i++) {
                    Element provider = (Element) allProviders.item(i);
                    provider.setTextContent("net.bull.javamelody.JpaPersistence");
                }
            }
            else {
                Element providerElement = docXml.createElement("provider");
                providerElement
                        .setTextContent("net.bull.javamelody.JpaPersistence");
                docRoot.appendChild(providerElement);
            }

            // Getting properties node
            NodeList allProperties = docRoot.getElementsByTagName("properties");
            Element propertiesElement = null;

            // Modifying or creating properties element
            if (allProperties.getLength() > 0) {
                for (int i = 0; i < allProperties.getLength(); i++) {
                    propertiesElement = (Element) allProperties.item(i);
                    NodeList propertyElements = propertiesElement
                            .getChildNodes();
                    for (int x = 0; x < propertyElements.getLength(); x++) {
                        Node property = propertyElements.item(x);
                        NamedNodeMap propertyAttr = property.getAttributes();
                        if (propertyAttr != null) {
                            Node nameAttr = propertyAttr.getNamedItem(NAME_TAG);
                            // Checking if property exists on current Properties
                            if ("net.bull.javamelody.jpa.provider"
                                    .equals(nameAttr.getNodeValue())) {
                                return;
                            }
                        }
                    }
                }
            }
            else {
                propertiesElement = docXml.createElement("properties");
                docRoot.appendChild(propertiesElement);
            }

            // Creating provider property
            Element property = docXml.createElement("property");
            property.setAttribute(NAME_TAG, "net.bull.javamelody.jpa.provider");
            property.setAttribute("value",
                    "org.hibernate.ejb.HibernatePersistence");

            // Adding property to properties
            propertiesElement.appendChild(property);

            // Saving result
            getFileManager().createOrUpdateTextFileIfRequired(persistencePath,
                    XmlUtils.nodeToString(docXml), true);

        }
    }

    /**
     * This method updates web.xml to add filter, filter-mapping and listener
     * which are needed to proper functioning of JavaMelody
     * 
     * @param pathString
     */
    public void updateWebXML(String pathString) {
        String webPath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, "WEB-INF/web.xml");

        if (getFileManager().exists(webPath)) {
            Document docXml = WebProjectUtils.loadXmlDocument(webPath,
                    getFileManager());

            // Getting root element
            Element docRoot = docXml.getDocumentElement();

            // Checking if exists
            NodeList allFilters = docRoot.getElementsByTagName("filter-name");
            for (int i = 0; i < allFilters.getLength(); i++) {
                Element filter = (Element) allFilters.item(i);
                if (MONITORING.equals(filter.getTextContent())) {
                    return;
                }
            }

            // Creating filter elements
            Element filterElement = docXml.createElement("filter");
            // filter-name
            Element filterNameElement = docXml.createElement("filter-name");
            filterNameElement.setTextContent(MONITORING);
            filterElement.appendChild(filterNameElement);
            // filter-class
            Element filterClassElement = docXml.createElement("filter-class");
            filterClassElement
                    .setTextContent("net.bull.javamelody.MonitoringFilter");
            filterElement.appendChild(filterClassElement);
            // async-supported
            Element asyncSupportedElement = docXml
                    .createElement("async-supported");
            asyncSupportedElement.setTextContent("true");
            filterElement.appendChild(asyncSupportedElement);

            // Creating filter-mapping element
            Element filterMappingElement = docXml
                    .createElement("filter-mapping");
            // filter-name 2
            Element filterName2Element = docXml.createElement("filter-name");
            filterName2Element.setTextContent(MONITORING);
            filterMappingElement.appendChild(filterName2Element);
            // url-pattern
            Element urlPatternElement = docXml.createElement("url-pattern");
            urlPatternElement.setTextContent("/*");
            filterMappingElement.appendChild(urlPatternElement);
            Element dispatcherElement = docXml.createElement("dispatcher");
            dispatcherElement.setTextContent("REQUEST");
            filterMappingElement.appendChild(dispatcherElement);
            Element dispatcher2Element = docXml.createElement("dispatcher");
            dispatcher2Element.setTextContent("ASYNC");
            filterMappingElement.appendChild(dispatcher2Element);

            // Creating listener
            Element listenerElement = docXml.createElement("listener");
            // listener-class
            Element listenerClassElement = docXml
                    .createElement("listener-class");
            listenerClassElement
                    .setTextContent("net.bull.javamelody.SessionListener");
            listenerElement.appendChild(listenerClassElement);

            // Adding elements
            docRoot.appendChild(filterElement);
            docRoot.appendChild(filterMappingElement);
            docRoot.appendChild(listenerElement);

            // Add javamelody configuration file
            NodeList paramValueNodes = docRoot
                    .getElementsByTagName("param-value");

            for (int i = 0; i < paramValueNodes.getLength(); i++) {
                Node node = paramValueNodes.item(i);
                if (("WEB-INF/spring/webmvc-config.xml").equals(node
                        .getTextContent())) {
                    node.setTextContent("classpath*:net/bull/javamelody/monitoring-spring-aspectj*.xml"
                            .concat(" ").concat(node.getTextContent()));
                }
            }

            getFileManager().createOrUpdateTextFileIfRequired(webPath,
                    XmlUtils.nodeToString(docXml), true);
        }
    }

    /**
     * This method adds pom.xml dependencies
     */
    public void addPomDependencies() {
        List<Dependency> dependencies = new ArrayList<Dependency>();

        // Install dependencies defined in external XML file
        for (Element dependencyElement : XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency",
                XmlUtils.getConfiguration(getClass()))) {
            dependencies.add(new Dependency(dependencyElement));
        }

        // Add all new dependencies to pom.xml
        getProjectOperations().addDependencies("", dependencies);

    }

    /**
     * Creates an instance with the {@code src/main/webapp} path in the current
     * module
     * 
     * @return
     */
    public LogicalPath getWebappPath() {
        return WebProjectUtils.getWebappPath(getProjectOperations());
    }

    /**
     * Add all files to be monitored as a Spring service
     */
    @Override
    public void all() {
        addPackage(getProjectOperations().getFocusedTopLevelPackage());
    }

    /**
     * Add a path which all his child methods will be monitored as a Spring
     * service
     * 
     * @param path Set the package path to be monitored
     */
    @Override
    public void addPackage(JavaPackage path) {
        // Creating annotation
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                SPRING_MONITORING_ANNOTATION);

        Set<ClassOrInterfaceTypeDetails> controllers = getTypeLocationService()
                .findClassesOrInterfaceDetailsWithAnnotation(SPRING_CONTROLLER);

        Set<ClassOrInterfaceTypeDetails> services = getTypeLocationService()
                .findClassesOrInterfaceDetailsWithAnnotation(SPRING_SERVICE);

        // Annotating all controllers if they exists
        if (controllers != null) {
            Iterator<ClassOrInterfaceTypeDetails> it = controllers.iterator();

            while (it.hasNext()) {
                ClassOrInterfaceTypeDetails controller = it.next();
                if (controller.getType().getPackage().isWithin(path)) {

                    // Generating new annotation
                    ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder(
                            controller);

                    // Add annotation to target type
                    builder.updateTypeAnnotation(annotationBuilder.build());

                    // Save changes to disk
                    getTypeManagementService().createOrUpdateTypeOnDisk(
                            builder.build());
                }
            }
        }

        // Annotating all services if they exists
        if (services != null) {
            Iterator<ClassOrInterfaceTypeDetails> it = services.iterator();

            while (it.hasNext()) {
                ClassOrInterfaceTypeDetails service = it.next();
                if (service.getType().getPackage().isWithin(path)) {

                    // Generating new annotation
                    ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder(
                            service);

                    // Add annotation to target type
                    builder.updateTypeAnnotation(annotationBuilder.build());

                    // Save changes to disk
                    getTypeManagementService().createOrUpdateTypeOnDisk(
                            builder.build());
                }
            }
        }
    }

    /**
     * Add a class to be monitored as a Spring service
     * 
     * @param name Set the class name to be monitored
     */
    @Override
    public void addClass(JavaType name) {
        // Get java type controller
        ClassOrInterfaceTypeDetails controller = getControllerDetails(name);

        // Generating new annotation
        ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder(
                controller);
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                SPRING_MONITORING_ANNOTATION);

        // Add annotation to target type
        builder.updateTypeAnnotation(annotationBuilder.build());

        // Save changes to disk
        getTypeManagementService().createOrUpdateTypeOnDisk(builder.build());

    }

    /**
     * Add a method to be monitored as a Spring service
     * 
     * @param methodName Set the method name to be monitored
     * @param className Set the class name of the method to be monitored
     */
    @Override
    public void addMethod(JavaSymbolName methodName, JavaType className) {
        // Get java type controller
        ClassOrInterfaceTypeDetails controller = getControllerDetails(className);

        ClassOrInterfaceTypeDetailsBuilder classBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                controller);

        List<MethodMetadata> methodList = (List<MethodMetadata>) controller
                .getDeclaredMethods();

        for (int i = 0; i < methodList.size(); i++) {
            MethodMetadata method = methodList.get(i);
            if (methodName.equals(method.getMethodName())) {
                MethodMetadataBuilder builder = new MethodMetadataBuilder(
                        method);

                // Generating new annotation
                AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                        SPRING_MONITORING_ANNOTATION);

                // Add annotation to target type
                builder.updateTypeAnnotation(annotationBuilder.build());

                // Save changes to disk
                getTypeManagementService().createOrUpdateTypeOnDisk(
                        classBuilder.build());

            }
        }
        LOGGER.log(
                Level.INFO,
                "[ERROR] This method doesn't exist for this class or maybe it's inside an .aj file. In this case you must to push-in that method and then execute this command again");
    }

    /**
     * This method gets class details
     * 
     * @param controller
     * @return
     */
    private ClassOrInterfaceTypeDetails getControllerDetails(JavaType controller) {
        ClassOrInterfaceTypeDetails existing = getTypeLocationService()
                .getTypeDetails(controller);

        Validate.notNull(existing, "Can't get Type details");
        return existing;
    }

    /*** Feature Methods ***/

    /**
     * Gets the feature name managed by this operations class.
     * 
     * @return feature name
     */
    @Override
    public String getName() {
        return FEATURE_NAME_GVNIX_MONITORING;
    }

    /**
     * Returns true if the given feature is installed in current project.
     * 
     * @param moduleName feature name to check in current project
     * @return true if given feature name is installed, otherwise returns false
     */
    @Override
    public boolean isInstalledInModule(String moduleName) {
        // If its installed provider is net.bull.javamelody.JpaPersistence
        String persistencePath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES, "META-INF/persistence.xml");

        if (getFileManager().exists(persistencePath)) {
            Document docXml = WebProjectUtils.loadXmlDocument(persistencePath,
                    getFileManager());

            // Getting root element
            Element docRoot = docXml.getDocumentElement();

            // Getting provider
            NodeList allProviders = docRoot.getElementsByTagName("provider");

            // Check providers
            if (allProviders.getLength() > 0) {
                for (int i = 0; i < allProviders.getLength(); i++) {
                    Element provider = (Element) allProviders.item(i);
                    if (provider != null
                            && "net.bull.javamelody.JpaPersistence"
                                    .equals(provider.getTextContent())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public MenuOperations getMenuOperations() {
        if (menuOperations == null) {
            // Get all Services implement MenuOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MenuOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (MenuOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MenuOperations on MonitoringOperationsImpl.");
                return null;
            }
        }
        else {
            return menuOperations;
        }

    }

    public FileManager getFileManager() {
        if (fileManager == null) {
            // Get all Services implement FileManager interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(FileManager.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (FileManager) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load FileManager on MonitoringOperationsImpl.");
                return null;
            }
        }
        else {
            return fileManager;
        }

    }

    public PathResolver getPathResolver() {
        if (pathResolver == null) {
            // Get all Services implement PathResolver interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(PathResolver.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (PathResolver) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load PathResolver on MonitoringOperationsImpl.");
                return null;
            }
        }
        else {
            return pathResolver;
        }

    }

    public PropFileOperations getPropFileOperations() {
        if (propFileOperations == null) {
            // Get all Services implement PropFileOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                PropFileOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (PropFileOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load PropFileOperations on MonitoringOperationsImpl.");
                return null;
            }
        }
        else {
            return propFileOperations;
        }

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
                LOGGER.warning("Cannot load ProjectOperations on MonitoringOperationsImpl.");
                return null;
            }
        }
        else {
            return projectOperations;
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
                LOGGER.warning("Cannot load TypeLocationService on MonitoringOperationsImpl.");
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
                LOGGER.warning("Cannot load TypeManagementService on MonitoringOperationsImpl.");
                return null;
            }
        }
        else {
            return typeManagementService;
        }

    }
}
