package org.gvnix.addon.fancytree.addon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.fancytree.annotations.GvNIXFancyTree;
import org.gvnix.support.OperationUtils;
import org.gvnix.support.WebProjectUtils;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.addon.web.mvc.jsp.tiles.TilesOperations;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.operations.AbstractOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@FancytreeOperations}
 * 
 * @author gvNIX Team
 * @since 1.5.0
 */
@Component
@Service
public class FancytreeOperationsImpl extends AbstractOperations implements
        FancytreeOperations {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(FancytreeOperationsImpl.class);

    private static final String TREE_UTILS = "org.gvnix.util.fancytree.TreeUtils";
    private static final String WEB_INF_VIEWS = "/WEB-INF/views";

    private ProjectOperations projectOperations;
    private TypeLocationService typeLocationService;
    private ComponentContext cContext;
    private TypeManagementService typeManagementService;
    private MetadataService metadataService;
    private PathResolver pathResolver;
    private PropFileOperations propFileOperations;
    private ImportRegistrationResolver importResolver;
    private TilesOperations tilesOperations;
    private MenuOperations menuOperations;

    protected void activate(final ComponentContext componentContext) {
        cContext = componentContext;
        context = cContext.getBundleContext();
    }

    /** {@inheritDoc} */
    public boolean isSetupAvailable() {
        return getProjectOperations().isFeatureInstalledInFocusedModule(
                "gvnix-jquery")
                && !getProjectOperations().isFeatureInstalledInFocusedModule(
                        FANCY_TREE_FEATURE);
    }

    /** {@inheritDoc} */
    public boolean isAddAvailable() {
        return getProjectOperations().isFeatureInstalledInFocusedModule(
                FANCY_TREE_FEATURE);
    }

    /** {@inheritDoc} */
    public void setup() {
        // Add necessary dependencies to project pom
        updatePomDependencies();
        // Add all necessary resources to project
        installComponents();
        // Add bean to treeUtils class in webmvc-config.xml
        updateWebMvcConfig();
    }

    /** {@inheritDoc} */
    public void updateTags() {
        // Installing all necessary components
        installComponents();
    }

    /** {@inheritDoc} */
    public void addFancyTree(JavaType controller, String page, String mapping,
            Boolean editable) {

        // Check that controller exists
        Validate.notNull(controller,
                "Controller is necessary to execute this operation");

        final ClassOrInterfaceTypeDetails cidController = getTypeLocationService()
                .getTypeDetails(controller);

        // Check that current JavaType is a valid controller
        AnnotationMetadata controllerAnnotation = cidController
                .getAnnotation(SpringJavaType.CONTROLLER);

        Validate.notNull(controllerAnnotation, String.format(
                "Class %s is not a valid controller. Only classes annotated with @Controller "
                        + "are able to add Fancytree components.",
                controller.getFullyQualifiedTypeName()));

        ClassOrInterfaceTypeDetailsBuilder detailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                cidController);

        // Adding @GvNIXFancyTree annotation with necessary attributes if not
        // exists

        AnnotationMetadata fancyTreeAnnotation = cidController
                .getAnnotation(new JavaType(GvNIXFancyTree.class));
        AnnotationMetadataBuilder fancyTreeAnnotationBuilder = null;

        if (fancyTreeAnnotation == null) {
            fancyTreeAnnotationBuilder = new AnnotationMetadataBuilder(
                    new JavaType(GvNIXFancyTree.class));
        }
        else {
            fancyTreeAnnotationBuilder = new AnnotationMetadataBuilder(
                    fancyTreeAnnotation);
        }

        // Adding editable attribute
        if (editable) {
            fancyTreeAnnotationBuilder
                    .addBooleanAttribute("editable", editable);
        }

        // Adding mapping attribute
        String reqValue = "tree";

        if (StringUtils.isNotBlank(mapping)) {
            reqValue = mapping;
        }
        else if (StringUtils.isNotBlank(page)) {
            reqValue = page;
        }

        fancyTreeAnnotationBuilder.addStringAttribute("mapping", reqValue);

        // Adding page attribute
        if (StringUtils.isNotBlank(page)) {
            fancyTreeAnnotationBuilder.addStringAttribute("page", page);
        }

        if (fancyTreeAnnotation == null) {
            detailsBuilder.addAnnotation(fancyTreeAnnotationBuilder.build());
        }
        else {
            detailsBuilder.updateTypeAnnotation(fancyTreeAnnotationBuilder
                    .build());
        }

        // Updating controller class with @GvNIXFancyTree annotation
        getTypeManagementService().createOrUpdateTypeOnDisk(
                detailsBuilder.build());

        // Create view if page value is defined
        String controllerPath = (String) cidController
                .getAnnotation(SpringJavaType.REQUEST_MAPPING)
                .getAttribute("value").getValue();

        controllerPath = controllerPath.replace("/", "").trim();

        // Create view if "page" value is defined
        if (StringUtils.isNotBlank(page)) {
            // Creating ajax request path
            String reqMap = controllerPath;
            if (StringUtils.isBlank(mapping)) {
                // Use "page" value
                reqMap = String.format(controllerPath.concat("/%s"), page);
            }
            else {
                // Use "mapping" value if defined
                reqMap = String.format(controllerPath.concat("/%s"), mapping);
            }
            createView(page, reqMap, new JavaSymbolName(controllerPath),
                    controller.getPackage().getFullyQualifiedPackageName(),
                    editable);
        }

        LOGGER.log(
                Level.INFO,
                String.format(
                        "FancyTree component was added correctly! "
                                + "Remember that generated controller '%s' should "
                                + "be customized by developer with his own implementation.",
                        controller.getSimpleTypeName()));

    }

    /**
     * Adds a new view to show fancytree element linked with controller requests
     * 
     * @param name Custom view name
     * @param mapping URL to send Fancytree requests from view
     * @param path Simple controller path
     * @param controllerPackage Controller package name
     * @param editable
     */
    private void createView(String name, String mapping, JavaSymbolName path,
            String controllerPackage, Boolean editable) {

        // Controller path
        String finalPath = path.getReadableSymbolName().toLowerCase();

        final String showPath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP,
                WEB_INF_VIEWS.concat(String.format("/%s/%s.jspx", finalPath,
                        name)));

        // View file already exists
        if (!fileManager.exists(showPath)) {
            // Views.xml file path
            final LogicalPath webappPath = LogicalPath.getInstance(
                    Path.SRC_MAIN_WEBAPP, "");

            getTilesOperations().addViewDefinition(
                    finalPath,
                    webappPath,
                    finalPath.concat("/").concat(name),
                    TilesOperations.DEFAULT_TEMPLATE,
                    String.format("%s/%s/%s.jspx", WEB_INF_VIEWS, finalPath,
                            name));

            // Copying jspx file
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                inputStream = FileUtils.getInputStream(getClass(),
                        String.format("views/show.jspx"));
                outputStream = fileManager.createFile(showPath)
                        .getOutputStream();

                IOUtils.copy(inputStream, outputStream);
            }
            catch (final IOException ioe) {
                throw new IllegalStateException(ioe);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }

        // If new jspx file doesn't exists, show an error
        if (!fileManager.exists(showPath)) {
            throw new RuntimeException(String.format(
                    "ERROR. Not exists '%s.jspx' file on 'views/%s' folder",
                    name, finalPath));
        }

        // Getting document and adding definition
        Document docXml = WebProjectUtils
                .loadXmlDocument(showPath, fileManager);

        Element docRoot = docXml.getDocumentElement();

        // Creating tree element
        String treeId = String.format("ps_%s_%s_%s",
                controllerPackage.replaceAll("[.]", "_"),
                path.getSymbolNameCapitalisedFirstLetter(), name);

        Element tree = docXml.createElement("fancytree:tree");

        tree.setAttribute("id", treeId);
        tree.setAttribute("path", ("/").concat(mapping));

        if (editable) {
            tree.setAttribute("editable", "true");
        }
        tree.setAttribute("z", "user-managed");

        docRoot.appendChild(tree);

        // Write on new jspx file
        fileManager.createOrUpdateTextFileIfRequired(showPath,
                XmlUtils.nodeToString(docXml), true);

        // Adding tree view id and menu category label to application.properties
        Map<String, String> propertyList = new HashMap<String, String>();

        propertyList.put("label".concat(treeId.substring(2).toLowerCase()),
                name);

        getPropFileOperations().addProperties(getWebappPath(),
                "WEB-INF/i18n/application.properties", propertyList, true,
                false);

        // Add new menu entry for this new fancytree view
        String menuUrl = "/".concat(mapping);

        getMenuOperations().addMenuItem(
                new JavaSymbolName("fancytree_menu_category"),
                new JavaSymbolName(path.getReadableSymbolName()
                        .concat("_fancytree_menu_entry_").concat(name)),
                path.getReadableSymbolName().concat("/").concat(name),
                "global_generic", menuUrl, null, getWebappPath());

        // Updating menu labels
        String categoryMenuPropertyKey = "menu_category_fancytree_menu_category_label";
        String categoryMenuPropertyValue = "TreeView";

        getPropFileOperations().changeProperty(getWebappPath(),
                "WEB-INF/i18n/application.properties", categoryMenuPropertyKey,
                categoryMenuPropertyValue, true);

        String viewMenuPropertyKey = String
                .format("menu_item_fancytree_menu_category_"
                        .concat(path.getReadableSymbolName().toLowerCase())
                        .concat("_fancytree_menu_entry_").concat(name)
                        .concat("_label"));
        String viewMenuPropertyLabel = String.format("Show %s tree", finalPath);

        getPropFileOperations().changeProperty(getWebappPath(),
                "WEB-INF/i18n/application.properties", viewMenuPropertyKey,
                viewMenuPropertyLabel, true);

    }

    /**
     * This method adds necessary dependencies to project pom.xml
     */
    public void updatePomDependencies() {
        // Get add-on configuration file
        Element configuration = XmlUtils.getConfiguration(getClass());

        // Install the add-on repository needed
        List<Element> repos = XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", configuration);
        for (Element repo : repos) {
            getProjectOperations().addRepositories(
                    getProjectOperations().getFocusedModuleName(),
                    Collections.singleton(new Repository(repo)));
        }

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
     * This method installs necessary components on correct folders
     */
    public void installComponents() {
        PathResolver pathResolver = getProjectOperations().getPathResolver();
        LogicalPath webappPath = getWebappPath();

        // Copy Javascript files and related resources
        OperationUtils.updateDirectoryContents("scripts/fancytree/*.js",
                pathResolver.getIdentifier(webappPath, "/scripts/fancytree"),
                fileManager, cContext, getClass());
        OperationUtils.updateDirectoryContents("scripts/fancytree/README.txt",
                pathResolver.getIdentifier(webappPath, "/scripts/fancytree"),
                fileManager, cContext, getClass());

        // Copy tags into tags folder
        OperationUtils.updateDirectoryContents("tags/fancytree/*.tagx",
                pathResolver.getIdentifier(webappPath,
                        "/WEB-INF/tags/fancytree"), fileManager, cContext,
                getClass());

        // Copy stylesheets and images into styles folder
        OperationUtils.updateDirectoryContents("styles/fancytree/*.css",
                pathResolver.getIdentifier(webappPath, "styles/fancytree"),
                fileManager, cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-awesome/*.css", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-awesome"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-awesome/*.less", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-awesome"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-bootstrap/*.css", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-bootstrap"),
                fileManager, cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-bootstrap/*.less", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-bootstrap"),
                fileManager, cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-bootstrap-n/*.css", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-bootstrap-n"),
                fileManager, cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-bootstrap-n/*.less", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-bootstrap-n"),
                fileManager, cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-custom-1/README.md", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-custom-1"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-lion/*.css", pathResolver.getIdentifier(
                        webappPath, "styles/fancytree/skin-lion"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-lion/*.less",
                pathResolver.getIdentifier(webappPath,
                        "styles/fancytree/skin-lion"), fileManager, cContext,
                getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-lion/*.gif", pathResolver.getIdentifier(
                        webappPath, "styles/fancytree/skin-lion"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-themeroller/*.css", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-themeroller"),
                fileManager, cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-themeroller/*.less", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-themeroller"),
                fileManager, cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-themeroller/*.gif", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-themeroller"),
                fileManager, cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-vista/*.css", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-vista"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-vista/*.less", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-vista"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-vista/*.gif", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-vista"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-win7/*.css", pathResolver.getIdentifier(
                        webappPath, "styles/fancytree/skin-win7"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-win7/*.less",
                pathResolver.getIdentifier(webappPath,
                        "styles/fancytree/skin-win7"), fileManager, cContext,
                getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-win7/*.gif", pathResolver.getIdentifier(
                        webappPath, "styles/fancytree/skin-win7"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-win8/*.css", pathResolver.getIdentifier(
                        webappPath, "styles/fancytree/skin-win8"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-win8/*.less",
                pathResolver.getIdentifier(webappPath,
                        "styles/fancytree/skin-win8"), fileManager, cContext,
                getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-win8/*.gif", pathResolver.getIdentifier(
                        webappPath, "styles/fancytree/skin-win8"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-win8-n/*.css", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-win8-n"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-win8-n/*.less", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-win8-n"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-win8-n/*.gif", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-win8-n"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-win8-xxl/*.css", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-win8-xxl"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-win8-xxl/*.less", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-win8-xxl"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-win8-xxl/*.gif", pathResolver
                        .getIdentifier(webappPath,
                                "styles/fancytree/skin-win8-xxl"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-xp/*.css", pathResolver.getIdentifier(
                        webappPath, "styles/fancytree/skin-xp"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-xp/*.less", pathResolver.getIdentifier(
                        webappPath, "styles/fancytree/skin-xp"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "styles/fancytree/skin-xp/*.gif", pathResolver.getIdentifier(
                        webappPath, "styles/fancytree/skin-xp"), fileManager,
                cContext, getClass());
        OperationUtils.updateDirectoryContents("styles/images/*.png",
                pathResolver.getIdentifier(webappPath,
                        "styles/bootstrap/images"), fileManager, cContext,
                getClass());

        // Add JS sources to Load Scripts
        addToLoadScripts("fancytree_url",
                "/resources/scripts/fancytree/jquery.fancytree-all.js", false);
        addToLoadScripts("fancytree_ext_gvnix_url",
                "/resources/scripts/fancytree/fancytree.ext.gvnix.js", false);
        addToLoadScripts("fancytree_jquery_contextmenu_url",
                "/resources/scripts/fancytree/jquery.ui-contextmenu.js", false);

        // Add Default skin CSS to Load Scripts
        addToLoadScripts("css_fancytree_default",
                "/resources/styles/fancytree/skin-lion/ui.fancytree.css", true);
    }

    /**
     * Updates the webmvc-config.xml file to register TreeUtils
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

        Element bean = webMvcXml.createElement("bean");
        bean.setAttribute("id", "treeUtilsBean");
        bean.setAttribute("class", TREE_UTILS);

        root.appendChild(bean);

        XmlUtils.writeXml(webMvcXmlMutableFile.getOutputStream(), webMvcXml);
    }

    public void addToLoadScripts(String varName, String url, boolean isCSS) {
        // Modify Roo load-scripts.tagx
        String docTagxPath = getPathResolver().getIdentifier(getWebappPath(),
                "WEB-INF/tags/util/load-scripts.tagx");

        Validate.isTrue(fileManager.exists(docTagxPath),
                "load-script.tagx not found: ".concat(docTagxPath));

        MutableFile docTagxMutableFile = null;
        Document docTagx;

        try {
            docTagxMutableFile = fileManager.updateFile(docTagxPath);
            docTagx = XmlUtils.getDocumentBuilder().parse(
                    docTagxMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = docTagx.getDocumentElement();

        boolean modified = false;

        if (isCSS) {
            modified = WebProjectUtils.addCssToTag(docTagx, root, varName, url)
                    || modified;
        }
        else {
            modified = WebProjectUtils.addJSToTag(docTagx, root, varName, url)
                    || modified;
        }

        if (modified) {
            XmlUtils.writeXml(docTagxMutableFile.getOutputStream(), docTagx);
        }
    }

    // Methods to obtain service implementation

    public ProjectOperations getProjectOperations() {
        if (projectOperations == null) {
            // Get all Services implement ProjectOperations interface
            try {
                ServiceReference<?>[] references = context
                        .getAllServiceReferences(
                                ProjectOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    projectOperations = (ProjectOperations) context
                            .getService(ref);
                    return projectOperations;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load ProjectOperations on FancytreeOperationsImpl.");
                return null;
            }
        }
        else {
            return projectOperations;
        }
    }

    public TypeManagementService getTypeManagementService() {
        if (typeManagementService == null) {
            // Get all Services implement TypeManagementService interface
            try {
                ServiceReference<?>[] references = context
                        .getAllServiceReferences(
                                TypeManagementService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    typeManagementService = (TypeManagementService) context
                            .getService(ref);
                    return typeManagementService;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load TypeManagementService on FancytreeOperationsImpl.");
                return null;
            }
        }
        else {
            return typeManagementService;
        }
    }

    public MetadataService getMetadataService() {
        if (metadataService == null) {
            // Get all Services MetadataService
            // interface
            try {
                ServiceReference<?>[] references = context
                        .getAllServiceReferences(
                                MetadataService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    metadataService = (MetadataService) context.getService(ref);
                    return metadataService;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MetadataService on FancytreeOperationsImpl.");
                return null;
            }
        }
        else {
            return metadataService;
        }

    }

    public PathResolver getPathResolver() {
        if (pathResolver == null) {
            // Get all Services implement PathResolver interface
            try {
                ServiceReference<?>[] references = context
                        .getAllServiceReferences(PathResolver.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    pathResolver = (PathResolver) context.getService(ref);
                    return pathResolver;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load PathResolver on FancytreeOperationsImpl.");
                return null;
            }
        }
        else {
            return pathResolver;
        }
    }

    public TypeLocationService getTypeLocationService() {
        if (typeLocationService == null) {
            // Get all Services implement TypeLocationService interface
            try {
                ServiceReference<?>[] references = context
                        .getAllServiceReferences(
                                TypeLocationService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    typeLocationService = (TypeLocationService) context
                            .getService(ref);
                    return typeLocationService;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load TypeLocationService on FancytreeOperationsImpl.");
                return null;
            }
        }
        else {
            return typeLocationService;
        }
    }

    public PropFileOperations getPropFileOperations() {
        if (propFileOperations == null) {
            // Get all Services implement PropFileOperations interface
            try {
                ServiceReference<?>[] references = context
                        .getAllServiceReferences(
                                PropFileOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    propFileOperations = (PropFileOperations) context
                            .getService(ref);
                    return propFileOperations;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load PropFileOperations on FancytreeOperationsImpl.");
                return null;
            }
        }
        else {
            return propFileOperations;
        }
    }

    public TilesOperations getTilesOperations() {
        if (tilesOperations == null) {
            // Get all Services implement TilesOperations interface
            try {
                ServiceReference<?>[] references = context
                        .getAllServiceReferences(
                                TilesOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    tilesOperations = (TilesOperations) context.getService(ref);
                    return tilesOperations;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load TilesOperations on FancytreeOperationsImpl.");
                return null;
            }
        }
        else {
            return tilesOperations;
        }
    }

    public MenuOperations getMenuOperations() {
        if (menuOperations == null) {
            // Get all Services implement MenuOperations interface
            try {
                ServiceReference<?>[] references = context
                        .getAllServiceReferences(
                                MenuOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    menuOperations = (MenuOperations) context.getService(ref);
                    return menuOperations;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MenuOperations on FancytreeOperationsImpl.");
                return null;
            }
        }
        else {
            return menuOperations;
        }
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

    // FEATURE METHODS

    @Override
    public String getName() {
        return FANCY_TREE_FEATURE;
    }

    @Override
    public boolean isInstalledInModule(String moduleName) {
        // Getting fancy tree script
        final String scriptPath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP,
                "/scripts/fancytree/fancytree.ext.gvnix.js");

        if (fileManager.exists(scriptPath)) {
            return true;
        }

        return false;
    }
}