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
package org.gvnix.addon.web.mvc.addon.jquery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.web.mvc.addon.MvcOperations;
import org.gvnix.addon.web.mvc.annotations.jquery.GvNIXWebJQuery;
import org.gvnix.support.OperationUtils;
import org.gvnix.support.WebProjectUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.controller.details.FinderMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.finder.WebFinderMetadata;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldAnnotationValues;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.operations.AbstractOperations;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.maven.Pom;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of operations this add-on offers.
 * 
 * @author gvNIX Team
 * @since 1.1
 */
@Component
@Service
public class JQueryOperationsImpl extends AbstractOperations implements
        JQueryOperations {

    protected final static Logger LOGGER = HandlerUtils
            .getLogger(JQueryOperationsImpl.class);

    private static final String PATH_IS_NOT_SPECIFIED = "Path is not specified in the @RooWebScaffold annotation for '";

    private static final String JQUERY_FORM_PATH = "urn:jsptagdir:/WEB-INF/tags/jquery/form";

    private static final String FORM_FIELDS_PATH = "urn:jsptagdir:/WEB-INF/tags/form/fields";

    private static final String XMLNS_UTIL = "xmlns:util";

    private static final String XMLNS_PAGE = "xmlns:page";

    private static final String XMLNS_TABLE = "xmlns:table";

    private static final String XMLNS_FORM = "xmlns:form";

    private static final String XMLNS_FIELD = "xmlns:field";

    private ComponentContext cContext;

    protected void activate(final ComponentContext componentContext) {
        cContext = componentContext;
        context = cContext.getBundleContext();
    }

    private static final JavaType SCAFFOLD_ANNOTATION = new JavaType(
            RooWebScaffold.class.getName());

    private static final JavaType JQUERY_ANNOTATION = new JavaType(
            GvNIXWebJQuery.class.getName());

    private ProjectOperations projectOperations;

    private MvcOperations mvcOperations;

    private TypeLocationService typeLocationService;

    private TypeManagementService typeManagementService;

    private WebMetadataService webMetadataService;

    /**
     * {@inheritDoc}
     * <p/>
     * Setup is available if Spring MVC and gvNIX MVC jQuery files have not been
     * installed yet.
     * <p/>
     * Note gvNIX MVC dependencies aren't checked because {@link #setup()} will
     * install them if they are missing
     */
    public boolean isSetupAvailable() {
        return getProjectOperations().isFeatureInstalledInFocusedModule(
                FeatureNames.MVC)
                && !getProjectOperations().isFeatureInstalledInFocusedModule(
                        FEATURE_NAME_GVNIX_JQUERY);
    }

    public boolean isAddAvailable() {
        return getProjectOperations().isFeatureInstalledInFocusedModule(
                FEATURE_NAME_GVNIX_JQUERY);
    }

    public boolean isUpdateTagsAvailable() {
        return isAddAvailable();
    }

    public void annotateAll() {

        // Locate all controllers and annotate it
        for (JavaType type : getTypeLocationService().findTypesWithAnnotation(
                SCAFFOLD_ANNOTATION)) {
            annotateController(type);
        }
    }

    public void annotateController(JavaType javaType) {
        Validate.notNull(javaType, "Controller required");

        ClassOrInterfaceTypeDetails existing = getControllerDetails(javaType);

        // Only for @RooWebScaffold annotated controllers
        final AnnotationMetadata controllerAnnotation = MemberFindingUtils
                .getAnnotationOfType(existing.getAnnotations(),
                        SCAFFOLD_ANNOTATION);

        Validate.isTrue(controllerAnnotation != null,
                "Operation for @RooWebScaffold annotated controllers only.");

        final boolean isJQueryAnnotated = MemberFindingUtils
                .getAnnotationOfType(existing.getAnnotations(),
                        JQUERY_ANNOTATION) != null;

        // If annotation already exists on the target type do nothing
        if (isJQueryAnnotated) {
            return;
        }

        ClassOrInterfaceTypeDetailsBuilder detailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                existing);

        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                JQUERY_ANNOTATION);

        // Add annotation to target type
        detailsBuilder.addAnnotation(annotationBuilder.build());

        // Save changes to disk
        getTypeManagementService().createOrUpdateTypeOnDisk(
                detailsBuilder.build());
    }

    /**
     * Updates all JSP pages of target controller to use JQuery
     * 
     * @param controller
     * @param controllerAnnotation
     */
    public void updateCommonJsp() {

        // List of pages to update
        List<String> pageList = new ArrayList<String>();
        Collections.addAll(pageList, "dataAccessFailure", "resourceNotFound",
                "uncaughtException", "index", "login");

        // 3rd party add-ons could customize default Roo tags as gvNIX does,
        // to avoid to overwrite them with jQuery namespaces we will update
        // default Roo namespaces only
        Map<String, String> rooUriMap = new HashMap<String, String>();
        rooUriMap.put(XMLNS_FIELD, FORM_FIELDS_PATH);
        rooUriMap.put(XMLNS_FORM, "urn:jsptagdir:/WEB-INF/tags/form");
        rooUriMap.put(XMLNS_TABLE, FORM_FIELDS_PATH);
        rooUriMap.put(XMLNS_PAGE, "urn:jsptagdir:/WEB-INF/tags/form");
        rooUriMap.put(XMLNS_UTIL, "urn:jsptagdir:/WEB-INF/tags/util");

        // new jQuery namespaces
        Map<String, String> uriMap = new HashMap<String, String>();
        uriMap.put(XMLNS_FIELD,
                "urn:jsptagdir:/WEB-INF/tags/jquery/form/fields");
        uriMap.put(XMLNS_FORM, JQUERY_FORM_PATH);
        uriMap.put(XMLNS_TABLE,
                "urn:jsptagdir:/WEB-INF/tags/jquery/form/fields");
        uriMap.put(XMLNS_PAGE, JQUERY_FORM_PATH);
        uriMap.put(XMLNS_UTIL, "urn:jsptagdir:/WEB-INF/tags/jquery/util");

        // do the update
        for (String jspxName : pageList) {
            WebProjectUtils.updateTagxUriInJspx(
                    "WEB-INF/views/".concat(jspxName).concat(".jspx"),
                    rooUriMap, uriMap, getProjectOperations(), fileManager);
        }
    }

    /**
     * Updates all JSP pages of target controller to use JQuery
     * 
     * @param controller
     * @param controllerAnnotation
     */
    public void updateCrudJsp(JavaType controller, JQueryMetadata jqueryMetadata) {
        Validate.notNull(jqueryMetadata, "JQuery metadata required");

        String controllerPath = jqueryMetadata.getWebScaffoldAnnotationValues()
                .getPath();

        Validate.notBlank(controllerPath,
                PATH_IS_NOT_SPECIFIED.concat(controller.getSimpleTypeName())
                        .concat("'"));
        Validate.isTrue(controllerPath != null && !controllerPath.isEmpty(),
                PATH_IS_NOT_SPECIFIED.concat(controller.getSimpleTypeName())
                        .concat("'"));
        if (controllerPath != null && !controllerPath.startsWith("/")) {
            controllerPath = "/".concat(controllerPath);
        }

        // List of pages to update
        List<String> pageList = new ArrayList<String>();
        Collections.addAll(pageList, "create", "list", "show", "update");

        // 3rd party add-ons could customize default Roo tags as gvNIX does,
        // to avoid to overwrite them with jQuery namespaces we will update
        // default Roo namespaces only
        Map<String, String> rooUriMap = new HashMap<String, String>();
        rooUriMap.put(XMLNS_FIELD, FORM_FIELDS_PATH);
        rooUriMap.put(XMLNS_FORM, "urn:jsptagdir:/WEB-INF/tags/form");
        rooUriMap.put(XMLNS_TABLE, FORM_FIELDS_PATH);
        rooUriMap.put(XMLNS_PAGE, "urn:jsptagdir:/WEB-INF/tags/form");
        rooUriMap.put(XMLNS_UTIL, "urn:jsptagdir:/WEB-INF/tags/util");

        // new jQuery namespaces
        Map<String, String> uriMap = new HashMap<String, String>();
        uriMap.put(XMLNS_FIELD,
                "urn:jsptagdir:/WEB-INF/tags/jquery/form/fields");
        uriMap.put(XMLNS_FORM, JQUERY_FORM_PATH);
        uriMap.put(XMLNS_TABLE,
                "urn:jsptagdir:/WEB-INF/tags/jquery/form/fields");
        uriMap.put(XMLNS_PAGE, JQUERY_FORM_PATH);
        uriMap.put(XMLNS_UTIL, "urn:jsptagdir:/WEB-INF/tags/jquery/util");

        // do the update
        for (String jspxName : pageList) {
            WebProjectUtils.updateTagxUriInJspx(controllerPath, jspxName,
                    rooUriMap, uriMap, getProjectOperations(), fileManager);
        }
    }

    /**
     * Updates all JSP pages of target controller to use JQuery
     * 
     * @param controller
     * @param controllerAnnotation
     */
    public void updateFindJsp(JavaType controller,
            WebFinderMetadata finderMetadata) {
        Validate.notNull(finderMetadata, "Finder metadata required");

        WebScaffoldAnnotationValues annotationValues = finderMetadata
                .getAnnotationValues();

        // Get view path for success file access in "WEB-INF/views/"
        String controllerPath = annotationValues.getPath();
        Validate.notBlank(controllerPath,
                PATH_IS_NOT_SPECIFIED.concat(controller.getSimpleTypeName())
                        .concat("'"));
        Validate.isTrue(controllerPath != null && !controllerPath.isEmpty(),
                PATH_IS_NOT_SPECIFIED.concat(controller.getSimpleTypeName())
                        .concat("'"));

        // Get Java type details
        JavaType formBackingType = annotationValues.getFormBackingObject();
        MemberDetails memberDetails = getWebMetadataService().getMemberDetails(
                formBackingType);

        // This controller is annotated with @RooWebFinder
        final Set<FinderMetadataDetails> finderMethodsDetails = getWebMetadataService()
                .getDynamicFinderMethodsAndFields(formBackingType,
                        memberDetails, finderMetadata.getId());

        if (finderMethodsDetails == null) {
            // No finders found, do nothing
            return;
        }

        // 3rd party add-ons could customize default Roo tags as gvNIX does,
        // to avoid to overwrite them with jQuery namespaces we will update
        // default Roo namespaces only
        Map<String, String> rooUriMap = new HashMap<String, String>();
        rooUriMap.put(XMLNS_FIELD, FORM_FIELDS_PATH);
        rooUriMap.put(XMLNS_FORM, "urn:jsptagdir:/WEB-INF/tags/form");
        rooUriMap.put(XMLNS_TABLE, FORM_FIELDS_PATH);
        rooUriMap.put(XMLNS_PAGE, "urn:jsptagdir:/WEB-INF/tags/form");
        rooUriMap.put(XMLNS_UTIL, "urn:jsptagdir:/WEB-INF/tags/util");

        // new jQuery namespaces
        Map<String, String> uriMap = new HashMap<String, String>();
        uriMap.put(XMLNS_FIELD,
                "urn:jsptagdir:/WEB-INF/tags/jquery/form/fields");
        uriMap.put(XMLNS_FORM, JQUERY_FORM_PATH);
        uriMap.put(XMLNS_TABLE,
                "urn:jsptagdir:/WEB-INF/tags/jquery/form/fields");
        uriMap.put(XMLNS_PAGE, JQUERY_FORM_PATH);
        uriMap.put(XMLNS_UTIL, "urn:jsptagdir:/WEB-INF/tags/jquery/util");

        // do the update
        for (final FinderMetadataDetails finderDetails : finderMethodsDetails) {
            final String finderName = finderDetails.getFinderMethodMetadata()
                    .getMethodName().getSymbolName();
            WebProjectUtils.updateTagxUriInJspx(
                    "WEB-INF/views/".concat(controllerPath).concat("/")
                            .concat(finderName).concat(".jspx"), rooUriMap,
                    uriMap, getProjectOperations(), fileManager);
        }
    }

    /**
     * @param controller
     * @return
     */
    protected ClassOrInterfaceTypeDetails getControllerDetails(
            JavaType controller) {
        ClassOrInterfaceTypeDetails existing = getTypeLocationService()
                .getTypeDetails(controller);

        Validate.notNull(existing, "Can't get Type details");
        return existing;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Copy jQuery JS, CSS, images, customized TAGx, et al files to current
     * project.
     * <p/>
     * Note this method copies needed artifacts only, to transform current views
     * to jQuery views you must annotate manually the Controller with
     * {@link GvNIXWebJQuery} or use {@code web mvc jquery add} and
     * {@code web mvc jquery all} commands to annotate the Controller.
     */
    public void setup() {

        // If gvNIX MVC dependencies are not installed, install them
        if (!getProjectOperations().isFeatureInstalledInFocusedModule(
                MvcOperations.FEATURE_NAME_GVNIX_MVC)) {
            getMvcOperations().setup();
        }

        // Install tags modified for jQuery support
        updateTags();

        // Install common JSP modified for jQuery support
        updateCommonJsp();

        // Configure load-scripts.tagx
        updateLoadScriptsTag();
    }

    public void updateLoadScriptsTag() {

        // Modify Roo load-scripts.tagx
        PathResolver pathResolver = getProjectOperations().getPathResolver();
        String docTagxPath = pathResolver.getIdentifier(getWebappPath(),
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

        // Add jquery-ui.css
        modified = WebProjectUtils.addCssToTag(docTagx, root,
                "jquery_ui_css_url", "/resources/styles/jquery/jquery-ui.css")
                || modified;

        // Add jquery.js
        modified = WebProjectUtils.addJSToTag(docTagx, root, "jquery_js_url",
                "/resources/scripts/jquery/jquery-min.js") || modified;

        // Add jquery-ui.js
        modified = WebProjectUtils.addJSToTag(docTagx, root,
                "jquery_ui_js_url",
                "/resources/scripts/jquery/jquery-ui.min.js")
                || modified;

        // Add jquery.base64.js
        modified = WebProjectUtils.addJSToTag(docTagx, root,
                "jquery_b64_js_url",
                "/resources/scripts/jquery/jquery.base64.js")
                || modified;

        // Add tinymce.js
        modified = WebProjectUtils.addJSToTag(docTagx, root, "tinymce_js_url",
                "/resources/scripts/jquery/tinymce.min.js") || modified;

        // Add jQuery tinymce.js
        modified = WebProjectUtils.addJSToTag(docTagx, root,
                "jquery_tinymce_js_url",
                "/resources/scripts/jquery/jquery.tinymce.min.js")
                || modified;

        // Add jQuery validate.js
        modified = WebProjectUtils.addJSToTag(docTagx, root,
                "jquery_validate_js_url",
                "/resources/scripts/jquery/jquery.validate-min.js")
                || modified;

        // Add jQuery application JS init
        modified = WebProjectUtils.addJSToTag(docTagx, root, "app_js_url",
                "/resources/scripts/jquery/application.js") || modified;

        // Add jQuery cookie.js
        modified = WebProjectUtils.addJSToTag(docTagx, root,
                "jquery_cookie_js_url",
                "/resources/scripts/jquery/jquery.cookie.js")
                || modified;

        // Add i18n customization var
        modified = WebProjectUtils.addLocaleVarToTag(docTagx, root,
                "jqueryLocale") || modified;

        // Add jQuery UI datepicker i18n
        modified = WebProjectUtils
                .addJSToTag(docTagx, root, "jquery_ui_i18n_js_url",
                        "/resources/scripts/jquery/i18n/jquery.ui.datepicker-${jqueryLocale}.js")
                || modified;

        // Add jQuery validate i18n
        modified = WebProjectUtils
                .addJSToTag(docTagx, root, "jquery_validate_i18n_js_url",
                        "/resources/scripts/jquery/i18n/jquery.validate-${jqueryLocale}.js")
                || modified;

        // Add jQuery hastable.js
        modified = WebProjectUtils.addJSToTag(docTagx, root,
                "hashtable_js_url", "/resources/scripts/jquery/hashtable.js")
                || modified;

        // Add jQuery numberformatter.js
        modified = WebProjectUtils.addJSToTag(docTagx, root,
                "jquery_numberformatter_js_url",
                "/resources/scripts/jquery/jquery.numberformatter.min.js")
                || modified;

        // Add jQuery.print.js
        modified = WebProjectUtils.addJSToTag(docTagx, root,
                "jquery_print_js_url",
                "/resources/scripts/jquery/jquery.print.js")
                || modified;

        // Add jquery.dialogextend.min.js
        modified = WebProjectUtils.addJSToTag(docTagx, root, "moment_js_url",
                "/resources/scripts/jquery/moment.min.js") || modified;

        // Add moment.min.js
        modified = WebProjectUtils.addJSToTag(docTagx, root,
                "jquery_dialog_extend_js_url",
                "/resources/scripts/jquery/jquery.dialogextend.min.js")
                || modified;

        // Add jquery-datetimepicker.css
        modified = WebProjectUtils.addCssToTag(docTagx, root,
                "jquery_datetimepicker_css_url",
                "/resources/styles/jquery/jquery.datetimepicker.css")
                || modified;

        // Add jquery-datetimepicker.min.js
        modified = WebProjectUtils.addJSToTag(docTagx, root,
                "jquery_datetimepicker_js_url",
                "/resources/scripts/jquery/jquery.datetimepicker.full.min.js")
                || modified;

        modified = addMomentAndDatepickerConfig(docTagx, root,
                "moment_js_locale_url",
                "/resources/scripts/jquery/i18n/moment-${jqueryLocale}.js");

        if (modified) {
            XmlUtils.writeXml(docTagxMutableFile.getOutputStream(), docTagx);
        }
    }

    // Feature methods -----

    /**
     * Gets the feature name managed by this operations class.
     * 
     * @return feature name
     */
    public String getName() {
        return FEATURE_NAME_GVNIX_JQUERY;
    }

    /**
     * Returns true if gvNIX Web MVC dependency and gvNIX MVC jQuery files are
     * installed in current project.
     * 
     * @param moduleName feature name to check in current project
     * @return true if given feature name is installed, otherwise returns false
     */
    public boolean isInstalledInModule(final String moduleName) {
        final Pom pom = getProjectOperations().getPomFromModuleName(moduleName);
        if (pom == null) {
            return false;
        }
        return getProjectOperations().isFeatureInstalledInFocusedModule(
                MvcOperations.FEATURE_NAME_GVNIX_MVC)
                && hasJQueryTags();
    }

    // Helper methods -----

    /**
     * Creates an instance with the {@code src/main/webapp} path in the current
     * module
     * 
     * @return
     */
    private LogicalPath getWebappPath() {
        return WebProjectUtils.getWebappPath(getProjectOperations());
    }

    /**
     * Check if {@code WEB-INF/tags/jquery} and
     * {@code scripts/jquery/jquery-min.js} exist
     * 
     * @return
     */
    private boolean hasJQueryTags() {
        PathResolver pathResolver = getProjectOperations().getPathResolver();
        String dirPath = pathResolver.getIdentifier(getWebappPath(),
                "WEB-INF/tags/jquery");
        String jsPath = pathResolver.getIdentifier(getWebappPath(),
                "scripts/jquery/jquery-min.js");
        return fileManager.exists(dirPath) && fileManager.exists(jsPath);
    }

    @Override
    public void updateTags() {
        PathResolver pathResolver = getProjectOperations().getPathResolver();
        LogicalPath webappPath = getWebappPath();

        // Copy Javascript files and related resources
        copyDirectoryContents("scripts/jquery/*.js",
                pathResolver.getIdentifier(webappPath, "/scripts/jquery"), true);
        copyDirectoryContents("scripts/jquery/i18n/*.js",
                pathResolver.getIdentifier(webappPath, "/scripts/jquery/i18n"),
                true);
        copyDirectoryContents("scripts/jquery/themes/modern/*.js",
                pathResolver.getIdentifier(webappPath,
                        "/scripts/jquery/themes/modern"), true);
        copyDirectoryContents("scripts/jquery/skins/lightgray/*.css",
                pathResolver.getIdentifier(webappPath,
                        "/scripts/jquery/skins/lightgray"), true);
        copyDirectoryContents("scripts/jquery/README.txt",
                pathResolver.getIdentifier(webappPath, "/scripts/jquery"), true);
        OperationUtils.updateDirectoryContents(
                "scripts/jquery/skins/lightgray/fonts/*.*", pathResolver
                        .getIdentifier(webappPath,
                                "/scripts/jquery/skins/lightgray/fonts"),
                fileManager, cContext, getClass());
        OperationUtils.updateDirectoryContents(
                "scripts/jquery/skins/lightgray/img/*.*", pathResolver
                        .getIdentifier(webappPath,
                                "/scripts/jquery/skins/lightgray/img"),
                fileManager, cContext, getClass());

        // Copy CSS files and related resources
        copyDirectoryContents("styles/jquery/*.css",
                pathResolver.getIdentifier(webappPath, "/styles/jquery"), true);
        OperationUtils
                .updateDirectoryContents("styles/jquery/images/*.*",
                        pathResolver.getIdentifier(webappPath,
                                "/styles/jquery/images"), fileManager,
                        cContext, getClass());

        // Copy Tagx files
        copyDirectoryContents("tags/jquery/form/*.tagx",
                pathResolver.getIdentifier(webappPath,
                        "/WEB-INF/tags/jquery/form"), true);
        copyDirectoryContents("tags/jquery/form/fields/*.tagx",
                pathResolver.getIdentifier(webappPath,
                        "/WEB-INF/tags/jquery/form/fields"), true);
        copyDirectoryContents("tags/jquery/util/*.tagx",
                pathResolver.getIdentifier(webappPath,
                        "/WEB-INF/tags/jquery/util"), true);
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
                LOGGER.warning("Cannot load ProjectOperations on JQueryOperationsImpl.");
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
                LOGGER.warning("Cannot load MvcOperations on JQueryOperationsImpl.");
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
                LOGGER.warning("Cannot load TypeLocationService on JQueryOperationsImpl.");
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
                LOGGER.warning("Cannot load TypeManagementService on JQueryOperationsImpl.");
                return null;
            }
        }
        else {
            return typeManagementService;
        }
    }

    public WebMetadataService getWebMetadataService() {
        if (webMetadataService == null) {
            // Get all Services implement WebMetadataService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                WebMetadataService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (WebMetadataService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load WebMetadataService on JQueryOperationsImpl.");
                return null;
            }
        }
        else {
            return webMetadataService;
        }
    }

    private boolean addMomentAndDatepickerConfig(Document docTagx,
            Element root, String varName, String location) {

        boolean modified = false;

        // add url resolution
        modified = WebProjectUtils
                .addUrlToTag(docTagx, root, varName, location);

        // Add script
        Element scriptElement = XmlUtils.findFirstElement(
                String.format("script[@src='${%s}']", varName), root);
        if (scriptElement == null) {
            scriptElement = docTagx.createElement("script");
            scriptElement.setAttribute("src", "${".concat(varName).concat("}"));
            scriptElement.setAttribute("type", "text/javascript");
            scriptElement.appendChild(docTagx
                    .createComment("required for FF3 and Opera"));
            root.appendChild(scriptElement);
            modified = true;

            // Add configuration script
            Element scriptConfigElement = docTagx.createElement("script");
            scriptConfigElement.setAttribute("type", "text/javascript");
            StringBuilder strb = new StringBuilder();
            strb.append("// set momentjs  and jQuery datetimepicker locale\n");
            strb.append("moment.locale('${jqueryLocale}');\n");
            strb.append("jQuery.datetimepicker.setLocale('${jqueryLocale}');\n");
            strb.append("\n");
            strb.append("// Define parse/format date using moment library\n");
            strb.append("Date.parseDate = function( input, format ){\n");
            strb.append("    return moment(input,format).toDate();\n");
            strb.append("};\n");
            strb.append("Date.prototype.dateFormat = function( format ){\n");
            strb.append("    return moment(this).format(format);\n");
            strb.append("};\n");
            scriptConfigElement.appendChild(docTagx.createTextNode(strb
                    .toString()));
            root.appendChild(scriptConfigElement);
        }

        return modified;
    }
}
