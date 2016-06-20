/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2011 CIT - Generalitat
 * Valenciana
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.web.exception.handler.roo.addon.addon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.transform.Transformer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.MessageBundleUtils;
import org.gvnix.support.OperationUtils;
import org.gvnix.support.WebProjectUtils;
import org.gvnix.support.dependenciesmanager.DependenciesVersionManager;
import org.gvnix.web.exception.handler.roo.addon.annotations.GvNIXModalDialogs;
import org.gvnix.web.i18n.roo.addon.ValencianCatalanLanguage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18nSupport;
import org.springframework.roo.addon.web.mvc.jsp.i18n.languages.SpanishLanguage;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.osgi.OSGiUtils;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of Operations of Dialogs .
 * 
 * @author Óscar Rovira ( orovira at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class WebModalDialogOperationsImpl implements WebModalDialogOperations {

    private static final JavaSymbolName VALUE = new JavaSymbolName("value");
    private static final JavaSymbolName ARRAY_ELEMENT = new JavaSymbolName(
            "__ARRAY_ELEMENT__");

    private static final JavaType MODAL_DIALOGS = new JavaType(
            GvNIXModalDialogs.class.getName());

    private static Logger LOGGER = HandlerUtils
            .getLogger(WebModalDialogOperationsImpl.class);

    @Reference
    private ProjectOperations projectOperations;
    @Reference
    private FileManager fileManager;
    @Reference
    private MetadataService metadataService;
    @Reference
    private TypeLocationService typeLocationService;
    @Reference
    private PathResolver pathResolver;
    @Reference
    private WebExceptionHandlerOperations exceptionOperations;
    @Reference
    private I18nSupport i18nSupport;
    @Reference
    private PropFileOperations propFileOperations;
    @Reference
    private TypeManagementService typeManagementService;

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * isProjectAvailable()
     */
    public boolean isProjectAvailable() {
        return OperationUtils.isProjectAvailable(metadataService,
                projectOperations)
                && WebProjectUtils.isSpringMvcProject(metadataService,
                        fileManager, projectOperations);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * addModalDialogAnnotation(org.springframework.roo.model.JavaType,
     * org.springframework.roo.model.JavaSymbolName)
     */
    public void addModalDialogAnnotation(JavaType controllerClass,
            JavaSymbolName name) {
        Validate.notNull(controllerClass, "controller is required");
        Validate.notNull(name, "name is required");
        // setup maven dependency
        setupMavenDependency();

        annotateWithModalDialog(controllerClass, name);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * addDefaultModalDialogAnnotation(org.springframework.roo.model.JavaType)
     */
    public void addDefaultModalDialogAnnotation(JavaType controllerClass) {
        Validate.notNull(controllerClass, "controller is required");

        annotateWithModalDialog(controllerClass, null);
    }

    /**
     * Annotates given controller class with {@link GvNIXModalDialogs} with
     * value name if informed
     * 
     * @param controllerClass
     * @param name name of the modal dialog. May be null.
     */
    private void annotateWithModalDialog(JavaType controllerClass,
            JavaSymbolName name) {
        Validate.notNull(controllerClass, "controller is required");

        // Get mutableTypeDetails from controllerClass. Also checks javaType is
        // a controller
        ClassOrInterfaceTypeDetails controllerDetails = typeLocationService
                .getTypeDetails(controllerClass);
        // Test if has the @Controller
        Validate.notNull(
                MemberFindingUtils.getAnnotationOfType(controllerDetails
                        .getAnnotations(), new JavaType(
                        "org.springframework.stereotype.Controller")),
                controllerClass.getSimpleTypeName().concat(
                        " has not @Controller annotation"));

        // Test if the annotation already exists on the target type
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(controllerDetails.getAnnotations(),
                        MODAL_DIALOGS);

        // List of pattern to use
        List<StringAttributeValue> modalDialogsList = new ArrayList<StringAttributeValue>();

        boolean isAlreadyAnnotated = false;
        if (annotationMetadata != null) {
            // @GvNIXModalDialog already exists

            // Loads previously registered modal dialog into modalDialogsList
            // Also checks if name is used previously
            AnnotationAttributeValue<?> previousAnnotationValues = annotationMetadata
                    .getAttribute(VALUE);

            if (previousAnnotationValues != null) {

                @SuppressWarnings("unchecked")
                List<StringAttributeValue> previousValues = (List<StringAttributeValue>) previousAnnotationValues
                        .getValue();

                if (previousValues != null && !previousValues.isEmpty()) {
                    for (StringAttributeValue value : previousValues) {
                        if (!modalDialogsList.contains(value)) {
                            modalDialogsList.add(value);
                        }
                    }
                }
            }
            isAlreadyAnnotated = true;
        }

        // If no changes will make (annotation exists and no name
        // must be added)
        if (isAlreadyAnnotated && name == null) {
            // All done
            return;
        }

        // Prepare annotation builder
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                MODAL_DIALOGS);

        if (name != null) {
            StringAttributeValue newModalDialogValue = new StringAttributeValue(
                    ARRAY_ELEMENT, name.getSymbolName());
            if (!modalDialogsList.contains(newModalDialogValue)) {
                modalDialogsList.add(newModalDialogValue);
            }

            // Add attribute values
            annotationBuilder
                    .addAttribute(new ArrayAttributeValue<StringAttributeValue>(
                            VALUE, modalDialogsList));
        }

        ClassOrInterfaceTypeDetailsBuilder mutableTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                controllerDetails);
        if (isAlreadyAnnotated) {
            mutableTypeDetailsBuilder.updateTypeAnnotation(
                    annotationBuilder.build(), new HashSet<JavaSymbolName>());
        }
        else {
            mutableTypeDetailsBuilder.addAnnotation(annotationBuilder.build());
        }
        typeManagementService
                .createOrUpdateTypeOnDisk(mutableTypeDetailsBuilder.build());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * setupModalDialogsSupport()
     */
    public void setupModalDialogsSupport() {
        // setup maven dependency
        setupMavenDependency();

        // install Dialog Bean
        WebProjectUtils.installWebDialogClass(
                getControllerFullyQualifiedPackage().concat(".dialog"),
                pathResolver, fileManager);

        // install MessageMappingBeanResolver
        String messageMappingResolverClass = installWebServletMessageMappingExceptionResolverClass();
        updateExceptionResolverBean(messageMappingResolverClass);

        // install gvNIX excpections
        exceptionOperations.setUpGvNIXExceptions();

        // install MVC artifacts
        installMvcArtifacts();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * setupMavenDependency()
     */
    public void setupMavenDependency() {
        Element configuration = XmlUtils.getConfiguration(getClass());

        // Install the add-on Google code repository and dependency needed to
        // get the annotations

        List<Element> repos = XmlUtils.findElements(
                "/configuration/gvnix/repositories/repository", configuration);
        for (Element repo : repos) {
            projectOperations.addRepository(projectOperations
                    .getFocusedModuleName(), new Repository(repo));
        }

        List<Element> depens = XmlUtils.findElements(
                "/configuration/gvnix/dependencies/dependency", configuration);
        DependenciesVersionManager.manageDependencyVersion(metadataService,
                projectOperations, depens);

        depens = XmlUtils.findElements(
                "/configuration/dependencies/dependency", configuration);
        for (Element depen : depens) {
            projectOperations.addDependency(projectOperations
                    .getFocusedModuleName(), new Dependency(depen));
        }

        // Install properties
        List<Element> properties = XmlUtils.findElements(
                "/configuration/gvnix/properties/*", configuration);
        for (Element property : properties) {
            projectOperations.addProperty(projectOperations
                    .getFocusedModuleName(), new Property(property));
        }
    }

    /**
     * Installs Java classes for MessageMappapingExceptionResolver support
     * 
     * @return string as fully qualified name of MessageMappingExceptionResolver
     *         Java class installed
     */
    private String installWebServletMessageMappingExceptionResolverClass() {
        String className = "MessageMappingExceptionResolver";
        String classPackage = getControllerFullyQualifiedPackage().concat(
                ".servlet.handler");

        String classPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""),
                classPackage.concat(".").concat(className)
                        .replace(".", File.separator).concat(".java"));

        MutableFile mutableClass = null;
        if (!fileManager.exists(classPath)) {
            InputStream template = FileUtils.getInputStream(
                    getClass(),
                    "web/servlet/handler/".concat(className).concat(
                            ".java-template"));

            String javaTemplate;
            try {
                javaTemplate = IOUtils
                        .toString(new InputStreamReader(template));

                // Replace package definition
                javaTemplate = StringUtils.replace(javaTemplate, "${PACKAGE}",
                        classPackage);
                javaTemplate = StringUtils.replace(javaTemplate,
                        "${PACKAGE_DIALOG}",
                        getControllerFullyQualifiedPackage().concat(".dialog"));

                // Write final java file
                mutableClass = fileManager.createFile(classPath);
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = IOUtils.toInputStream(javaTemplate);
                    outputStream = mutableClass.getOutputStream();
                    IOUtils.copy(inputStream, outputStream);
                }
                finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }
            catch (IOException ioe) {
                throw new IllegalStateException("Unable load "
                        .concat(className).concat(".java-template template"),
                        ioe);
            }
            finally {
                try {
                    template.close();
                }
                catch (IOException e) {
                    throw new IllegalStateException("Error creating ".concat(
                            className).concat(".java in project"), e);
                }
            }
        }
        return classPackage.concat(".").concat(className);
    }

    /**
     * Returns the fully qualified name of the Controllers package of the
     * App.(classes annotated with @Controller)
     * 
     * @param className
     * @return
     */
    private String getControllerFullyQualifiedPackage() {
        // Search for @Controller annotated class and get its package
        Set<ClassOrInterfaceTypeDetails> webMcvControllers = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(new JavaType(
                        "org.springframework.stereotype.Controller"));
        String controllerPackageName = null;
        if (!webMcvControllers.isEmpty()) {
            JavaPackage controllerPackage = webMcvControllers.iterator().next()
                    .getName().getPackage();
            controllerPackageName = controllerPackage
                    .getFullyQualifiedPackageName();
        }
        Validate.notNull(controllerPackageName,
                "Can not get a fully qualified name for Controllers package");

        return controllerPackageName;
    }

    /**
     * Change the class of the bean MappingExceptionResolver by gvNIX's resolver
     * class. The gvNIX resolver class supports redirect calls and messages in a
     * modal dialog.
     * 
     * @param beanClassName the name of the new ExceptionResolver Bean
     */
    private void updateExceptionResolverBean(String beanClassName) {
        String webXmlPath = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/spring/webmvc-config.xml");

        if (!fileManager.exists(webXmlPath)) {
            return;
        }

        MutableFile webXmlMutableFile = null;
        Document webXml;

        try {
            webXmlMutableFile = fileManager.updateFile(webXmlPath);
            webXml = XmlUtils.getDocumentBuilder().parse(
                    webXmlMutableFile.getInputStream());
        }
        catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element root = webXml.getDocumentElement();

        Element sMapExcepResBean = XmlUtils
                .findFirstElement(
                        "/beans/bean[@class='org.springframework.web.servlet.handler.SimpleMappingExceptionResolver']",
                        root);

        // We'll replace the class just if SimpleMappingExceptionResolver is set
        if (sMapExcepResBean != null) {
            sMapExcepResBean.setAttribute("class", beanClassName);
            sMapExcepResBean.setAttribute("id",
                    "messageMappingExceptionResolverBean");
            XmlUtils.writeXml(webXmlMutableFile.getOutputStream(), webXml);
        }

        // Here we need MessageMappingExceptionResolver set as ExceptionResolver
        Element mMapExcepResBean = XmlUtils
                .findFirstElement("/beans/bean[@class='".concat(beanClassName)
                        .concat("']"), root);
        Validate.notNull(mMapExcepResBean,
                "MessageMappingExceptionResolver is not configured. Check webmvc-config.xml");

    }

    /**
     * Installs MVC Artifacts into current project<br/>
     * Artifacts installed:<br/>
     * <ul>
     * <li>message-box.tagx</li>
     * </ul>
     * Modify default.jspx layout adding in the right position the element
     * &lt;util:message-box /&gt;
     * <p>
     * Also adds needed i18n properties to right message_xx.properties files
     */
    public void installMvcArtifacts() {
        // copy util to tags/util
        copyDirectoryContents("tags/dialog/modal/*.tagx",
                pathResolver.getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        "/WEB-INF/tags/dialog/modal"));
        addMessageBoxInLayout();

        modifyChangesControlDialogNs();

        addI18nProperties();
    }

    /**
     * Takes properties files (messages_xx.properties) and adds their content to
     * i18n message bundle file in current project
     */
    private void addI18nProperties() {
        // Check if Valencian_Catalan language is supported and add properties
        // if so
        Set<I18n> supportedLanguages = i18nSupport.getSupportedLanguages();
        for (I18n i18n : supportedLanguages) {
            if (i18n.getLocale().equals(new Locale("ca"))) {
                MessageBundleUtils.installI18nMessages(
                        new ValencianCatalanLanguage(), projectOperations,
                        fileManager);
                MessageBundleUtils.addPropertiesToMessageBundle("ca",
                        getClass(), propFileOperations, projectOperations,
                        fileManager);
                break;
            }
        }
        // Add properties to Spanish messageBundle
        MessageBundleUtils.installI18nMessages(new SpanishLanguage(),
                projectOperations, fileManager);
        MessageBundleUtils.addPropertiesToMessageBundle("es", getClass(),
                propFileOperations, projectOperations, fileManager);

        // Add properties to default messageBundle
        MessageBundleUtils.addPropertiesToMessageBundle("en", getClass(),
                propFileOperations, projectOperations, fileManager);
    }

    private void modifyChangesControlDialogNs() {
        String changesControlTagx = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/tags/util/changes-control.tagx");

        if (!fileManager.exists(changesControlTagx)) {
            // tags/util/changes-control.tagx doesn't exist, so nothing to do
            return;
        }

        InputStream changesControlTagxIs = fileManager
                .getInputStream(changesControlTagx);

        Document changesControlTagxXml;
        try {
            changesControlTagxXml = XmlUtils.getDocumentBuilder().parse(
                    changesControlTagxIs);
        }
        catch (Exception ex) {
            throw new IllegalStateException("Could not open default.jspx file",
                    ex);
        }

        Element jspRoot = changesControlTagxXml.getDocumentElement();

        // Set dialog tag lib as attribute in html element
        jspRoot.setAttribute("xmlns:dialog",
                "urn:jsptagdir:/WEB-INF/tags/dialog/modal");

        writeToDiskIfNecessary(changesControlTagx,
                changesControlTagxXml.getDocumentElement());

    }

    /**
     * This method will copy the contents of a directory to another if the
     * resource does not already exist in the target directory
     * 
     * @param sourceAntPath the source path
     * @param targetDirectory the target directory
     */
    private void copyDirectoryContents(String sourceAntPath,
            String targetDirectory) {
        Validate.notNull(sourceAntPath, "sourceAntPath required");
        Validate.notBlank(sourceAntPath, "sourceAntPath required");
        Validate.notNull(targetDirectory, "targetDirectory required");
        Validate.notBlank(targetDirectory, "targetDirectory required");

        if (!targetDirectory.endsWith("/")) {
            targetDirectory += "/";
        }

        if (!fileManager.exists(targetDirectory)) {
            fileManager.createDirectory(targetDirectory);
        }

        String path = FileUtils.getPath(getClass(), sourceAntPath);
        Collection<URL> urls = OSGiUtils.findEntriesByPattern(context, path);
        Validate.notNull(urls,
                "Could not search bundles for resources for Ant Path '" + path
                        + "'");
        for (URL url : urls) {
            String fileName = url.getPath().substring(
                    url.getPath().lastIndexOf("/") + 1);
            if (!fileManager.exists(targetDirectory + fileName)) {
                try {
                    InputStream inputStream = null;
                    OutputStream outputStream = null;
                    try {
                        inputStream = url.openStream();
                        outputStream = fileManager.createFile(
                                targetDirectory + fileName).getOutputStream();
                        IOUtils.copy(inputStream, outputStream);
                    }
                    finally {
                        IOUtils.closeQuietly(inputStream);
                        IOUtils.closeQuietly(outputStream);
                    }
                }
                catch (IOException e) {
                    throw new IllegalStateException(
                            "Encountered an error during copying of resources for MVC JSP addon.",
                            e);
                }
            }
        }
    }

    /**
     * Adds the element util:message-box in the right place in default.jspx
     * layout
     */
    private void addMessageBoxInLayout() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        String defaultJspx = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/layouts/default.jspx");

        if (!fileManager.exists(defaultJspx)) {
            // layouts/default.jspx doesn't exist, so nothing to do
            return;
        }

        InputStream defulatJspxIs = fileManager.getInputStream(defaultJspx);

        Document defaultJspxXml;
        try {
            defaultJspxXml = XmlUtils.getDocumentBuilder().parse(defulatJspxIs);
        }
        catch (Exception ex) {
            throw new IllegalStateException("Could not open default.jspx file",
                    ex);
        }

        Element lsHtml = defaultJspxXml.getDocumentElement();

        // Set dialog tag lib as attribute in html element
        lsHtml.setAttribute("xmlns:dialog",
                "urn:jsptagdir:/WEB-INF/tags/dialog/modal");

        Element messageBoxElement = DomUtils.findFirstElementByName(
                "dialog:message-box", lsHtml);
        if (messageBoxElement == null) {
            Element divMain = XmlUtils.findFirstElement(
                    "/html/body/div/div[@id='main']", lsHtml);
            Element insertAttributeBodyElement = XmlUtils.findFirstElement(
                    "/html/body/div/div/insertAttribute[@name='body']", lsHtml);
            Element messageBox = new XmlElementBuilder("dialog:message-box",
                    defaultJspxXml).build();
            divMain.insertBefore(messageBox, insertAttributeBodyElement);
        }

        writeToDiskIfNecessary(defaultJspx, defaultJspxXml.getDocumentElement());

    }

    /**
     * Decides if write to disk is needed (ie updated or created)<br/>
     * Used for TAGx files TODO: candidato a ir al módulo Support
     * 
     * @param filePath
     * @param body
     * @return
     */
    private boolean writeToDiskIfNecessary(String filePath, Element body) {
        // Build a string representation of the JSP
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Transformer transformer = XmlUtils.createIndentingTransformer();
        XmlUtils.writeXml(transformer, byteArrayOutputStream,
                body.getOwnerDocument());
        String viewContent = byteArrayOutputStream.toString();

        // If mutableFile becomes non-null, it means we need to use it to write
        // out the contents of jspContent to the file
        MutableFile mutableFile = null;
        if (fileManager.exists(filePath)) {
            // First verify if the file has even changed
            File newFile = new File(filePath);
            String existing = null;
            try {
                existing = IOUtils.toString(new FileReader(newFile));
            }
            catch (IOException ignoreAndJustOverwriteIt) {
                LOGGER.finest("Problems writting ".concat(newFile
                        .getAbsolutePath()));
            }

            if (!viewContent.equals(existing)) {
                mutableFile = fileManager.updateFile(filePath);
            }
        }
        else {
            mutableFile = fileManager.createFile(filePath);
            Validate.notNull(mutableFile, "Could not create '" + filePath + "'");
        }

        if (mutableFile != null) {
            try {
                // We need to write the file out (it's a new file, or the
                // existing file has different contents)
                InputStream inputStream = null;
                OutputStream outputStream = null;
                try {
                    inputStream = IOUtils.toInputStream(viewContent);
                    outputStream = mutableFile.getOutputStream();
                    IOUtils.copy(inputStream, outputStream);
                }
                finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
                // Return and indicate we wrote out the file
                return true;
            }
            catch (IOException ioe) {
                throw new IllegalStateException("Could not output '"
                        + mutableFile.getCanonicalPath() + "'", ioe);
            }
        }

        // A file existed, but it contained the same content, so we return false
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gvnix.web.exception.handler.roo.addon.WebModalDialogOperations#
     * isMessageBoxOfTypeModal()
     */
    public boolean isMessageBoxOfTypeModal() {
        String defaultJspx = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/layouts/default.jspx");

        if (!fileManager.exists(defaultJspx)) {
            // layouts/default.jspx doesn't exist, so nothing to do
            return false;
        }

        InputStream defulatJspxIs = fileManager.getInputStream(defaultJspx);

        Document defaultJspxXml;
        try {
            defaultJspxXml = XmlUtils.getDocumentBuilder().parse(defulatJspxIs);
        }
        catch (Exception ex) {
            throw new IllegalStateException("Could not open default.jspx file",
                    ex);
        }

        Element lsHtml = defaultJspxXml.getDocumentElement();

        // Check if dialog:message-box is of type modal
        String dialogNS = lsHtml.getAttribute("xmlns:dialog");
        String defaultUrn = "urn:jsptagdir:/WEB-INF/tags/dialog/modal";

        // Check if Bootstrap is installed
        if (projectOperations.isFeatureInstalled("gvnix-bootstrap")) {
            defaultUrn = "urn:jsptagdir:/WEB-INF/tags/bootstrap/dialog/modal";
        }

        if (dialogNS.equals(defaultUrn)) {
            return true;
        }

        return false;
    }
}
