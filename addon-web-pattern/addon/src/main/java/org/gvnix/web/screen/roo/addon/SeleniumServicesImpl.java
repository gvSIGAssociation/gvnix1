/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2012 CIT - Generalitat
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
package org.gvnix.web.screen.roo.addon;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.details.JavaTypePersistenceMetadataDetails;
import org.springframework.roo.addon.web.mvc.controller.details.WebMetadataService;
import org.springframework.roo.addon.web.mvc.controller.scaffold.WebScaffoldMetadata;
import org.springframework.roo.addon.web.mvc.jsp.menu.MenuOperations;
import org.springframework.roo.addon.web.selenium.SeleniumOperationsImpl;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.operations.DateTime;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Selenium tests generation and configurations for screen patterns.
 * 
 * @see SeleniumOperationsImpl
 * @author Mario Martínez Sánchez (mmartinez at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.9
 */
@Component
@Service
public class SeleniumServicesImpl implements SeleniumServices {

    private static final String CONTR_TYPE_REQ = "Controller type required";
    private static final String TEST_DASH = "test-";
    private static final String SELENIUM_SLASH = "selenium/";
    private static final String GVNIX_CHECKBOX = "gvnix_checkbox_fu_";
    private static final String INPUT_AT_PROC = "//input[@id='proceed']";

    private static final String TBODY_XPATH = "/html/body/table/tbody";

    private static final String CONTROLLER_NO_CREATION_WARN = "The controller you specified does not allow the creation of new instances of the form backing object. No Selenium tests created.";

    private static final Random RANDOM_GENERATOR = new Random(
            new Date().getTime());

    /**
     * MetadataService offers access to Roo's metadata model, use it to retrieve
     * any available metadata by its MID
     */
    @Reference
    private MetadataService metadataService;
    @Reference
    private ProjectOperations projectOperations;
    @Reference
    PropFileOperations propFileOperations;
    @Reference
    protected FileManager fileManager;
    @Reference
    private MemberDetailsScanner memberDetailsScanner;
    @Reference
    private WebMetadataService webMetadataService;
    @Reference
    private MenuOperations menuOperations;
    @Reference
    private PatternService patternService;

    private static Logger logger = Logger.getLogger(SeleniumServicesImpl.class
            .getName());

    /**
     * Creates a new Selenium test for a master register pattern.
     * 
     * @param controller the JavaType of the controller under test (required)
     * @param name the name of the test case (optional)
     * @param serverURL the application address (optional)
     */
    public void generateTestMasterRegister(JavaType controller, String name,
            String serverURL) {

        Validate.notNull(controller, CONTR_TYPE_REQ);

        // Get web scaffold annotation from controller
        WebScaffoldMetadata webScaffoldMetadata = getWebScaffoldMetadata(controller);

        // We abort the creation of a selenium test if the controller does not
        // allow the creation of new instances for the form backing object
        if (!webScaffoldMetadata.getAnnotationValues().isCreate()) {

            logger.warning(CONTROLLER_NO_CREATION_WARN);
            return;
        }

        String id = webScaffoldMetadata.getId();

        // Get selenium template
        Document document = getSeleniumTemplate();

        // Get template html content and update with current name
        Element root = getHtmlElement(name, document);

        // Get table body element to include test operations
        Element tbody = XmlUtils.findRequiredElement(TBODY_XPATH, root);

        // Add pattern URL to open for test
        if (!serverURL.endsWith("/")) {
            serverURL = serverURL + "/";
        }
        String baseURL = serverURL
                + projectOperations.getProjectName(projectOperations
                        .getFocusedModuleName()) + "/"
                + webScaffoldMetadata.getAnnotationValues().getPath();

        // Add test operations
        JavaType formBackingType = webScaffoldMetadata.getAnnotationValues()
                .getFormBackingObject();
        addTestMasterRegister(formBackingType, document, tbody, baseURL, name,
                id);

        // Store the test file into project
        String testName = TEST_DASH + name + "-master-register";
        String relativeTestFilePath = SELENIUM_SLASH + testName + ".xhtml";
        installTest(testName, serverURL, document, relativeTestFilePath);
    }

    /**
     * Creates a new Selenium test for a master tabular pattern.
     * 
     * @param controller the JavaType of the controller under test (required)
     * @param name the name of the test case (optional)
     * @param serverURL the application address (optional)
     */
    public void generateTestMasterTabular(JavaType controller, String name,
            String serverURL) {

        Validate.notNull(controller, CONTR_TYPE_REQ);

        // Get web scaffold annotation from controller
        WebScaffoldMetadata webScaffoldMetadata = getWebScaffoldMetadata(controller);

        // We abort the creation of a selenium test if the controller does not
        // allow the creation of new instances for the form backing object
        if (!webScaffoldMetadata.getAnnotationValues().isCreate()) {

            logger.warning(CONTROLLER_NO_CREATION_WARN);
            return;
        }

        String id = webScaffoldMetadata.getId();

        // Get selenium template
        Document document = getSeleniumTemplate();

        // Get template html content and update with current name
        Element root = getHtmlElement(name, document);

        // Get table body element to include test operations
        Element tbody = XmlUtils.findRequiredElement(TBODY_XPATH, root);

        // Add pattern URL to open for test
        if (!serverURL.endsWith("/")) {
            serverURL = serverURL + "/";
        }
        String baseURL = serverURL
                + projectOperations.getProjectName(projectOperations
                        .getFocusedModuleName()) + "/"
                + webScaffoldMetadata.getAnnotationValues().getPath();

        // Open tabular pattern URL
        tbody.appendChild(openCommandTabular(document, baseURL, name));

        // Add test operations
        JavaType formBackingType = webScaffoldMetadata.getAnnotationValues()
                .getFormBackingObject();
        addTestTabular(formBackingType, document, tbody, baseURL, name, id);

        // Store the test file into project
        String testName = TEST_DASH + name + "-master-tabular";
        String relativeTestFilePath = SELENIUM_SLASH + testName + ".xhtml";
        installTest(testName, serverURL, document, relativeTestFilePath);
    }

    /**
     * Creates a new Selenium test for a master tabular with register edit
     * pattern.
     * 
     * @param controller the JavaType of the controller under test (required)
     * @param name the name of the test case (optional)
     * @param serverURL the application address (optional)
     */
    public void generateTestMasterTabularEditRegister(JavaType controller,
            String name, String serverURL) {

        Validate.notNull(controller, CONTR_TYPE_REQ);

        // Get web scaffold annotation from controller
        WebScaffoldMetadata webScaffoldMetadata = getWebScaffoldMetadata(controller);

        // We abort the creation of a selenium test if the controller does not
        // allow the creation of new instances for the form backing object
        if (!webScaffoldMetadata.getAnnotationValues().isCreate()) {

            logger.warning(CONTROLLER_NO_CREATION_WARN);
            return;
        }

        String id = webScaffoldMetadata.getId();

        // Get selenium template
        Document document = getSeleniumTemplate();

        // Get template html content and update with current name
        Element root = getHtmlElement(name, document);

        // Get table body element to include test operations
        Element tbody = XmlUtils.findRequiredElement(TBODY_XPATH, root);

        // Add pattern URL to open for test
        if (!serverURL.endsWith("/")) {
            serverURL = serverURL + "/";
        }
        String baseURL = serverURL
                + projectOperations.getProjectName(projectOperations
                        .getFocusedModuleName()) + "/"
                + webScaffoldMetadata.getAnnotationValues().getPath();

        // Open tabular pattern URL
        tbody.appendChild(openCommandTabular(document, baseURL, name));

        // Add test operations
        JavaType formBackingType = webScaffoldMetadata.getAnnotationValues()
                .getFormBackingObject();
        addTestTabularEditRegister(formBackingType, document, tbody, baseURL,
                name, id);

        // Store the test file into project
        String testName = TEST_DASH + name + "-master-tabular";
        String relativeTestFilePath = SELENIUM_SLASH + testName + ".xhtml";
        installTest(testName, serverURL, document, relativeTestFilePath);
    }

    /**
     * Creates a new Selenium test for a detail tabular pattern.
     * 
     * @param controller the JavaType of the controller under test (required)
     * @param fieldName Related controller entity field name (required)
     * @param name the name of the test case (optional)
     * @param serverURL the application address (optional)
     */
    public void generateTestDetailTabular(JavaType controller,
            WebPatternType type, JavaSymbolName fieldName, String name,
            String serverURL) {

        Validate.notNull(controller, CONTR_TYPE_REQ);
        Validate.notNull(fieldName, "Field name required");

        // Get web scaffold annotation from controller
        WebScaffoldMetadata webScaffoldMetadata = getWebScaffoldMetadata(controller);

        // TODO Create validation should be from field web scaffold metadata
        // We abort the creation of a selenium test if the controller does not
        // allow the creation of new instances for the form backing object
        if (!webScaffoldMetadata.getAnnotationValues().isCreate()) {

            logger.warning(CONTROLLER_NO_CREATION_WARN);
            return;
        }

        String id = webScaffoldMetadata.getId();

        // Get selenium template
        Document document = getSeleniumTemplate();

        // Get template html content and update with current name
        Element root = getHtmlElement(name, document);

        // Get table body element to include test operations
        Element tbody = XmlUtils.findRequiredElement(TBODY_XPATH, root);

        // Add pattern URL to open for test
        if (!serverURL.endsWith("/")) {
            serverURL = serverURL + "/";
        }
        String baseURL = serverURL
                + projectOperations.getProjectName(projectOperations
                        .getFocusedModuleName()) + "/"
                + webScaffoldMetadata.getAnnotationValues().getPath();

        // Add test operations
        JavaType formBackingType = webScaffoldMetadata.getAnnotationValues()
                .getFormBackingObject();
        JavaType fieldType = getEntityFieldNameJavaType(fieldName,
                formBackingType);
        List<FieldMetadata> parentFields = webMetadataService
                .getScaffoldEligibleFieldMetadata(formBackingType,
                        getMemberDetails(formBackingType), null);

        String patternType = patternService.getControllerMasterPatternType(
                controller, new JavaSymbolName(name));
        if (patternType.equalsIgnoreCase(WebPatternType.register.name())
                && type.equals(WebPatternType.tabular)) {

            // Master register / detail tabular

            // Open master pattern URL
            tbody.appendChild(openCommandRegister(document, baseURL, name));

            // Add master register
            addTestRegisterAdd(formBackingType, document, tbody, parentFields,
                    id);

            addTestTabular(fieldType.getParameters().get(0), document, tbody,
                    baseURL, name, id);

            // Delete master register
            // TODO Commented because test fails in hudson
            // addTestRegisterDelete(formBackingType, document, tbody);
        }
        if (patternType.equalsIgnoreCase(WebPatternType.register.name())
                && type.equals(WebPatternType.tabular_edit_register)) {

            // Master register / detail tabular edit register

            // Open master pattern URL
            tbody.appendChild(openCommandRegister(document, baseURL, name));

            // Add master register
            addTestRegisterAdd(formBackingType, document, tbody, parentFields,
                    id);

            addTestTabularEditRegister(fieldType.getParameters().get(0),
                    document, tbody, baseURL, name, id);

            // Delete master register
            // TODO Commented because test fails in hudson
            // addTestRegisterDelete(formBackingType, document, tbody);
        }
        else if (patternType.equalsIgnoreCase(WebPatternType.tabular.name())
                && type.equals(WebPatternType.tabular)) {

            // Master tabular / detail tabular

            // Open master pattern URL
            tbody.appendChild(openCommandTabular(document, baseURL, name));

            // Add master register
            addTestTabularAdd(formBackingType, document, tbody, parentFields,
                    id);

            tbody.appendChild(clickAndWaitCommand(
                    document,
                    GVNIX_CHECKBOX
                            + XmlUtils.convertId(formBackingType
                                    .getFullyQualifiedTypeName()) + "_0"));

            addTestTabular(fieldType.getParameters().get(0), document, tbody,
                    baseURL, name, id);

            // Delete master register
            // TODO Commented because test fails in hudson
            // addTestTabularDelete(formBackingType, document, tbody);
        }
        else if (patternType.equalsIgnoreCase(WebPatternType.tabular.name())
                && type.equals(WebPatternType.tabular_edit_register)) {

            // Master tabular / detail tabular edit register

            // Open master pattern URL
            tbody.appendChild(openCommandTabular(document, baseURL, name));

            // Add master register
            addTestTabularAdd(formBackingType, document, tbody, parentFields,
                    id);

            tbody.appendChild(clickAndWaitCommand(
                    document,
                    GVNIX_CHECKBOX
                            + XmlUtils.convertId(formBackingType
                                    .getFullyQualifiedTypeName()) + "_0"));

            addTestTabularEditRegister(fieldType.getParameters().get(0),
                    document, tbody, baseURL, name, id);

            // Delete master register
            // TODO Commented because test fails in hudson
            // addTestTabularDelete(formBackingType, document, tbody);
        }
        else if (patternType
                .equalsIgnoreCase(WebPatternType.tabular_edit_register.name())
                && type.equals(WebPatternType.tabular)) {

            // Master tabular edit register / detail tabular

            tbody.appendChild(openCommandTabular(document, baseURL, name));

            addTestTabularEditRegisterAdd(formBackingType, document, tbody,
                    parentFields, id);

            tbody.appendChild(clickAndWaitCommand(
                    document,
                    GVNIX_CHECKBOX
                            + XmlUtils.convertId(formBackingType
                                    .getFullyQualifiedTypeName()) + "_0"));

            addTestTabular(fieldType.getParameters().get(0), document, tbody,
                    baseURL, name, id);

            // TODO Commented because test fails in hudson
            // addTestTabularDelete(formBackingType, document, tbody);
        }
        else if (patternType
                .equalsIgnoreCase(WebPatternType.tabular_edit_register.name())
                && type.equals(WebPatternType.tabular_edit_register)) {

            // Master tabular edit register / detail tabular edit register

            tbody.appendChild(openCommandTabular(document, baseURL, name));

            addTestTabularEditRegisterAdd(formBackingType, document, tbody,
                    parentFields, id);

            tbody.appendChild(clickAndWaitCommand(
                    document,
                    GVNIX_CHECKBOX
                            + XmlUtils.convertId(formBackingType
                                    .getFullyQualifiedTypeName()) + "_0"));

            addTestTabularEditRegister(fieldType.getParameters().get(0),
                    document, tbody, baseURL, name, id);

            // TODO Commented because test fails in hudson
            // addTestTabularDelete(formBackingType, document, tbody);
        }

        // Store the test file into project
        String testName = TEST_DASH + name + "-detail-tabular" + "-"
                + fieldName;
        String relativeTestFilePath = SELENIUM_SLASH + testName + ".xhtml";
        installTest(testName, serverURL, document, relativeTestFilePath);
    }

    /**
     * Get web scaffold annotation from controller.
     * 
     * @param controller Controller java type
     * @return Web scaffold metadata from annotation
     */
    private WebScaffoldMetadata getWebScaffoldMetadata(JavaType controller) {

        String identifier = WebScaffoldMetadata.createIdentifier(controller,
                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""));
        WebScaffoldMetadata metadata = (WebScaffoldMetadata) metadataService
                .get(identifier);
        Validate.notNull(
                metadata,
                "Web controller '"
                        + controller.getFullyQualifiedTypeName()
                        + "' does not appear to be an automatic, scaffolded controller");

        return metadata;
    }

    /**
     * Get document from selenium template.
     * 
     * @return Selenium template document
     */
    protected Document getSeleniumTemplate() {

        try {

            InputStream inputStream = FileUtils.getInputStream(
                    SeleniumOperationsImpl.class, "selenium-template.xhtml");
            Validate.notNull(inputStream,
                    "Could not acquire selenium.xhtml template");
            return XmlUtils.readXml(inputStream);

        }
        catch (Exception e) {

            throw new IllegalStateException(e);
        }
    }

    /**
     * Get template html content and update with current name.
     * 
     * @param name Name to write into html content
     * @param document Document to write into
     * @return Html element with required name
     */
    protected Element getHtmlElement(String name, Document document) {

        Element root = (Element) document.getLastChild();
        if (root == null || !"html".equals(root.getNodeName())) {

            throw new IllegalArgumentException(
                    "Could not parse selenium test case template file!");
        }
        XmlUtils.findRequiredElement("/html/head/title", root).setTextContent(
                name);
        XmlUtils.findRequiredElement("/html/body/table/thead/tr/td", root)
                .setTextContent(name);

        return root;
    }

    /**
     * Create a html section to open register pattern URL.
     * 
     * @param document Document where create section
     * @param linkTarget Base URL to the server
     * @param name Name of pattern
     * @return Html node with register pattern open URL
     */
    protected Node openCommandRegister(Document document, String linkTarget,
            String name) {

        Node tr = document.createElement("tr");

        Node td1 = tr.appendChild(document.createElement("td"));
        td1.setTextContent("open");

        // Add pattern request attributes
        Node td2 = tr.appendChild(document.createElement("td"));

        td2.setTextContent(linkTarget + (linkTarget.contains("?") ? "&" : "?")
                + "gvnixform" + "&gvnixpattern=" + name + "&index=1" + "&lang="
                + Locale.getDefault());

        Node td3 = tr.appendChild(document.createElement("td"));
        td3.setTextContent(" ");

        return tr;
    }

    /**
     * Create a html section to open tabular pattern URL.
     * 
     * @param document Document where create section
     * @param linkTarget Base URL to the server
     * @param name Name of pattern
     * @return Html node with tabular pattern open URL
     */
    protected Node openCommandTabular(Document document, String linkTarget,
            String name) {

        Node tr = document.createElement("tr");

        Node td1 = tr.appendChild(document.createElement("td"));
        td1.setTextContent("open");

        // Add pattern request attributes
        Node td2 = tr.appendChild(document.createElement("td"));

        td2.setTextContent(linkTarget + (linkTarget.contains("?") ? "&" : "?")
                + "gvnixpattern=" + name + "&lang=" + Locale.getDefault());

        Node td3 = tr.appendChild(document.createElement("td"));
        td3.setTextContent(" ");

        return tr;
    }

    /**
     * Add Selenium commands for master register pattern test.
     * 
     * @param entity Entity to test
     * @param document Document to write commands
     * @param element Element where store commands
     * @param baseURL Server and application URL path
     * @param name Pattern name
     */
    protected void addTestMasterRegister(JavaType entity, Document document,
            Element element, String baseURL, String name, String id) {

        List<FieldMetadata> fields = webMetadataService
                .getScaffoldEligibleFieldMetadata(entity,
                        getMemberDetails(entity), null);

        // Open register pattern URL
        element.appendChild(openCommandRegister(document, baseURL, name));

        // Add register
        addTestRegisterAdd(entity, document, element, fields, id);

        // Update link access and submit opened form
        element.appendChild(clickAndWaitCommand(document, "//a[@id='ps_"
                + XmlUtils.convertId(entity.getFullyQualifiedTypeName())
                + "_update'" + "]"));
        element.appendChild(clickAndWaitCommand(document, INPUT_AT_PROC));

        // Update register fields verification
        addVerificationRegister(entity, document, element, fields);

        addTestRegisterDelete(entity, document, element);
    }

    /**
     * Add Selenium commands for master tabular with register edit pattern test.
     * 
     * @param entity Entity to test
     * @param document Document to write commands
     * @param element Element where store commands
     * @param baseURL Server and application URL path
     * @param name Pattern name
     */
    protected void addTestTabularEditRegister(JavaType entity,
            Document document, Element element, String baseURL, String name,
            String id) {

        List<FieldMetadata> fields = webMetadataService
                .getScaffoldEligibleFieldMetadata(entity,
                        getMemberDetails(entity), null);

        // Add register
        addTestTabularEditRegisterAdd(entity, document, element, fields, id);

        // Update check table first row
        element.appendChild(checkCommand(document, entity));

        // Update link access
        element.appendChild(clickAndWaitCommand(document, "//img[@id='fu_"
                + XmlUtils.convertId(entity.getFullyQualifiedTypeName())
                + "_update'" + "]"));
        element.appendChild(clickAndWaitCommand(document, INPUT_AT_PROC));

        addTestTabularDelete(entity, document, element);
    }

    /**
     * Add Selenium commands for master tabular with register edit pattern add
     * test.
     * 
     * @param entity Entity to test
     * @param document Document to write commands
     * @param element Element where store commands
     * @param fields Entity fields to add values
     */
    protected void addTestTabularEditRegisterAdd(JavaType entity,
            Document document, Element element, List<FieldMetadata> fields,
            String id) {

        // Add image push to access creation
        String imgId = "fu_"
                + XmlUtils.convertId(entity.getFullyQualifiedTypeName())
                + "_create";
        element.appendChild(clickAndWaitCommand(document, "//img[@id='" + imgId
                + "']"));

        // Add register identifier fields
        addCompositeIdentifierFieldsRegister(entity, document, element, id);

        // Add register fields
        addFieldsRegister(fields, document, element);

        // Add submit
        element.appendChild(clickAndWaitCommand(document, INPUT_AT_PROC));

        // Add register fields verification
        addVerificationTabular(entity, document, element, fields);
    }

    /**
     * Add Selenium commands for master register pattern add test.
     * 
     * @param entity Entity to test
     * @param document Document to write commands
     * @param element Element where store commands
     * @param fields Entity fields to add values
     */
    protected void addTestRegisterAdd(JavaType entity, Document document,
            Element element, List<FieldMetadata> fields, String id) {

        // Add link
        element.appendChild(clickAndWaitCommand(document, "//a[@id='ps_"
                + XmlUtils.convertId(entity.getFullyQualifiedTypeName())
                + "_create'" + "]"));

        // Add register identifier fields
        addCompositeIdentifierFieldsRegister(entity, document, element, id);

        // Add register fields
        addFieldsRegister(fields, document, element);

        // Add submit
        element.appendChild(clickAndWaitCommand(document, INPUT_AT_PROC));

        // Add register fields verification
        addVerificationRegister(entity, document, element, fields);
    }

    /**
     * Add Selenium commands for master register pattern delete test.
     * 
     * @param entity Entity to test
     * @param document Document to write commands
     * @param element Element where store commands
     */
    protected void addTestRegisterDelete(JavaType entity, Document document,
            Element element) {

        // Delete form submit
        element.appendChild(clickCommand(document, "//input[@id='ps_"
                + XmlUtils.convertId(entity.getFullyQualifiedTypeName())
                + "_delete']"));

        // Delete javascript confirmation store required (but value not used)
        element.appendChild(storeConfirmationCommand(document, "var"));

        // TODO Delete validation ?
    }

    /**
     * Add Selenium commands for master tabular pattern test.
     * 
     * @param entity Entity to test
     * @param document Document to write commands
     * @param element Element where store commands
     * @param baseURL Server and application URL path
     * @param name Pattern name
     */
    protected void addTestTabular(JavaType entity, Document document,
            Element element, String baseURL, String name, String id) {

        List<FieldMetadata> fields = webMetadataService
                .getScaffoldEligibleFieldMetadata(entity,
                        getMemberDetails(entity), null);

        addTestTabularAdd(entity, document, element, fields, id);

        // Update check table first row
        element.appendChild(checkCommand(document, entity));

        // Update link access and submit enabled form
        element.appendChild(clickCommand(
                document,
                "//img[@id='fu_"
                        + XmlUtils.convertId(entity.getFullyQualifiedTypeName())
                        + "_update'" + "]"));
        element.appendChild(clickAndWaitCommand(
                document,
                "//input[@id='gvnix_control_update_save_fu_"
                        + XmlUtils.convertId(entity.getFullyQualifiedTypeName())
                        + "']"));

        // Update register fields verification
        addVerificationTabular(entity, document, element, fields);

        addTestTabularDelete(entity, document, element);
    }

    protected void addTestTabularDelete(JavaType entity, Document document,
            Element element) {

        // Delete check table first row
        element.appendChild(checkCommand(document, entity));

        // Delete form submit
        element.appendChild(clickCommand(
                document,
                "//img[@id='fu_"
                        + XmlUtils.convertId(entity.getFullyQualifiedTypeName())
                        + "_delete']"));

        // Delete javascript confirmation store required (but value not used)
        element.appendChild(storeConfirmationCommand(document, "var"));

        // TODO Delete validation ?
    }

    protected void addTestTabularAdd(JavaType entity, Document document,
            Element element, List<FieldMetadata> fields, String id) {

        // Add image push to access creation
        String imgId = "fu_"
                + XmlUtils.convertId(entity.getFullyQualifiedTypeName())
                + "_create";
        element.appendChild(clickCommand(document, "//img[@id='" + imgId + "']"));

        // Add register identifier fields
        addCompositeIdentifierFieldsTabular(entity, document, element, id);

        // TODO Check if other fields are editable (storeEditable)

        // Add tabular fields
        addFieldsTabular(fields, entity, document, element);

        // Add submit
        String inputId = "gvnix_control_add_save_fu_"
                + XmlUtils.convertId(entity.getFullyQualifiedTypeName());
        element.appendChild(clickAndWaitCommand(document, "//input[@id='"
                + inputId + "']"));

        // Add tabular fields verification
        addVerificationTabular(entity, document, element, fields);
    }

    /**
     * Get java type for field.
     * 
     * @param fieldName Field name of entity
     * @param entity Entity
     * @return Java type of field in entity
     */
    protected JavaType getEntityFieldNameJavaType(JavaSymbolName fieldName,
            JavaType entity) {

        JavaType fieldType = null;
        MemberDetails memberDetails = getMemberDetails(entity);
        Iterator<FieldMetadata> fieldsMetadata = webMetadataService
                .getScaffoldEligibleFieldMetadata(entity, memberDetails, null)
                .iterator();
        while (fieldsMetadata.hasNext() && fieldType == null) {
            FieldMetadata fieldMetadata = fieldsMetadata.next();
            if (fieldMetadata.getFieldName().equals(fieldName)) {
                fieldType = fieldMetadata.getFieldType();
            }
        }

        return fieldType;
    }

    /**
     * Get member details from java type.
     * 
     * @param javaType Java type for get member details
     * @return Member details from required java type
     */
    private MemberDetails getMemberDetails(JavaType javaType) {

        PhysicalTypeMetadata physicalTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(PhysicalTypeIdentifier.createIdentifier(javaType,
                        LogicalPath.getInstance(Path.SRC_MAIN_JAVA, "")));
        Validate.notNull(
                physicalTypeMetadata,
                "Unable to obtain physical type metadata for type "
                        + javaType.getFullyQualifiedTypeName());
        ClassOrInterfaceTypeDetails classOrInterfaceTypeDetails = (ClassOrInterfaceTypeDetails) physicalTypeMetadata
                .getMemberHoldingTypeDetails();

        return memberDetailsScanner.getMemberDetails(getClass().getName(),
                classOrInterfaceTypeDetails);
    }

    /**
     * Add composite PK identifier fields if needed into register pattern.
     * 
     * @param javaType Java type to get register pattern identifier fields
     * @param document Document to write fields commands
     * @param element Element where add fields commands
     * @param memberDetails Java type member details
     */
    protected void addCompositeIdentifierFieldsRegister(JavaType javaType,
            Document document, Element element, String id) {

        MemberDetails memberDetails = getMemberDetails(javaType);
        JavaTypePersistenceMetadataDetails jTPersistMDDetails = webMetadataService
                .getJavaTypePersistenceMetadataDetails(javaType, memberDetails,
                        id);
        if (jTPersistMDDetails != null
                && !jTPersistMDDetails.getRooIdentifierFields().isEmpty()) {
            for (FieldMetadata field : jTPersistMDDetails
                    .getRooIdentifierFields()) {
                if (!field.getFieldType().isCommonCollectionType()
                        && !isSpecialType(field.getFieldType())) {

                    FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                            field);
                    fieldBuilder.setFieldName(new JavaSymbolName(
                            jTPersistMDDetails.getIdentifierField()
                                    .getFieldName().getSymbolName()
                                    + "."
                                    + field.getFieldName().getSymbolName()));
                    element.appendChild(typeCommandRegister(document,
                            fieldBuilder.build(), true));
                }
            }
        }
    }

    /**
     * Add composite PK identifier fields if needed into tabular pattern.
     * 
     * @param javaType Java type to get register pattern identifier fields
     * @param document Document to write fields commands
     * @param element Element where add fields commands
     * @param memberDetails Java type member details
     */
    protected void addCompositeIdentifierFieldsTabular(JavaType javaType,
            Document document, Element element, String id) {

        MemberDetails memberDetails = getMemberDetails(javaType);
        JavaTypePersistenceMetadataDetails jTPersistMDDetails = webMetadataService
                .getJavaTypePersistenceMetadataDetails(javaType, memberDetails,
                        id);
        if (jTPersistMDDetails != null
                && !jTPersistMDDetails.getRooIdentifierFields().isEmpty()) {
            for (FieldMetadata field : jTPersistMDDetails
                    .getRooIdentifierFields()) {
                if (!field.getFieldType().isCommonCollectionType()
                        && !isSpecialType(field.getFieldType())) {

                    FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                            field);
                    fieldBuilder.setFieldName(new JavaSymbolName(
                            jTPersistMDDetails.getIdentifierField()
                                    .getFieldName().getSymbolName()
                                    + "."
                                    + field.getFieldName().getSymbolName()));
                    element.appendChild(typeCommandTabular(document,
                            fieldBuilder.build(), javaType, true));
                }
            }
        }
    }

    /**
     * Is special type ?
     * 
     * @param javaType Java type
     * @return Is special type
     */
    private boolean isSpecialType(JavaType javaType) {

        String physicalTypeIdentifier = PhysicalTypeIdentifier
                .createIdentifier(javaType,
                        LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""));

        // We are only interested if the type is part of our application and if
        // no editor exists for it already
        if (metadataService.get(physicalTypeIdentifier) != null) {

            return true;
        }

        return false;
    }

    /**
     * Add command click and wait for a URL.
     * 
     * @param document Document to write command
     * @param linkTarget Destination URL
     * @return Click and wait node
     */
    protected Node clickAndWaitCommand(Document document, String linkTarget) {

        Node tr = document.createElement("tr");

        Node td1 = tr.appendChild(document.createElement("td"));
        td1.setTextContent("clickAndWait");

        Node td2 = tr.appendChild(document.createElement("td"));
        td2.setTextContent(linkTarget);

        Node td3 = tr.appendChild(document.createElement("td"));
        td3.setTextContent(" ");

        return tr;
    }

    /**
     * Add command click for a URL.
     * 
     * @param document Document to write command
     * @param linkTarget Destination URL
     * @return Click node
     */
    protected Node clickCommand(Document document, String linkTarget) {

        Node tr = document.createElement("tr");

        Node td1 = tr.appendChild(document.createElement("td"));
        td1.setTextContent("click");

        Node td2 = tr.appendChild(document.createElement("td"));
        td2.setTextContent(linkTarget);

        Node td3 = tr.appendChild(document.createElement("td"));
        td3.setTextContent(" ");

        return tr;
    }

    /**
     * Add fields commands for register pattern.
     * 
     * @param fields Fields to add as selenium command
     * @param document Document to write into
     * @param element Element to add commands
     */
    protected void addFieldsRegister(List<FieldMetadata> fields,
            Document document, Element element) {

        // Add all other fields
        for (FieldMetadata field : fields) {
            if (!field.getFieldType().isCommonCollectionType()
                    && !isSpecialType(field.getFieldType())) {
                element.appendChild(typeCommandRegister(document, field, false));
            }
        }
    }

    /**
     * Add fields commands for tabular pattern.
     * 
     * @param fields Fields to add as selenium command
     * @param entity Parent entity of fields
     * @param document Document to write into
     * @param element Element to add commands
     */
    protected void addFieldsTabular(List<FieldMetadata> fields,
            JavaType entity, Document document, Element element) {

        // Add all other fields
        for (FieldMetadata field : fields) {
            if (!field.getFieldType().isCommonCollectionType()
                    && !isSpecialType(field.getFieldType())) {
                element.appendChild(typeCommandTabular(document, field, entity,
                        false));
            }
        }
    }

    /**
     * Add type value command for register pattern.
     * 
     * @param document Document to write command
     * @param field Field to add value
     * @return Type node
     */
    protected Node typeCommandRegister(Document document, FieldMetadata field,
            boolean random) {

        Node tr = document.createElement("tr");

        Node td1 = tr.appendChild(document.createElement("td"));
        td1.setTextContent("type");

        Node td2 = tr.appendChild(document.createElement("td"));

        td2.setTextContent("_" + field.getFieldName().getSymbolName() + "_id");

        Node td3 = tr.appendChild(document.createElement("td"));
        td3.setTextContent(convertToInitializer(field, random));

        return tr;
    }

    /**
     * Add type value command for tabular pattern.
     * 
     * @param document Document to write command
     * @param field Field to add value
     * @param entity Field parent entity
     * @return Type node
     */
    protected Node typeCommandTabular(Document document, FieldMetadata field,
            JavaType entity, boolean random) {

        Node tr = document.createElement("tr");

        Node td1 = tr.appendChild(document.createElement("td"));
        td1.setTextContent("type");

        Node td2 = tr.appendChild(document.createElement("td"));

        String id = "_"
                + XmlUtils
                        .convertId("fu:" + entity.getFullyQualifiedTypeName())
                + "[0]_" + field.getFieldName() + "_id_create";
        td2.setTextContent(id);

        Node td3 = tr.appendChild(document.createElement("td"));
        td3.setTextContent(convertToInitializer(field, random));

        return tr;
    }

    /**
     * Add verification value command for register pattern.
     * 
     * @param entity Field parent entity
     * @param document Document to write command
     * @param element Element to add commands
     * @param fields Fields to verify value
     */
    protected void addVerificationRegister(JavaType entity, Document document,
            Element element, List<FieldMetadata> fields) {

        // Add verifications for all other fields
        for (FieldMetadata field : fields) {
            if (!field.getFieldType().isCommonCollectionType()
                    && !isSpecialType(field.getFieldType())) {
                element.appendChild(verifyCommandRegister(document, entity,
                        field));
            }
        }
    }

    /**
     * Add verification value command for tabular pattern.
     * 
     * @param entity Field parent entity
     * @param document Document to write command
     * @param element Element to add commands
     * @param fields Fields to verify value
     */
    protected void addVerificationTabular(JavaType entity, Document document,
            Element element, List<FieldMetadata> fields) {

        // Add verifications for all other fields
        for (FieldMetadata field : fields) {
            if (!field.getFieldType().isCommonCollectionType()
                    && !isSpecialType(field.getFieldType())) {
                element.appendChild(verifyCommandTabular(document, entity,
                        field));
            }
        }
    }

    /**
     * Add verification value command for register pattern field.
     * 
     * @param document Document to write command
     * @param entity Field parent entity
     * @param field Field to verify value
     * @return Verification node
     */
    protected Node verifyCommandRegister(Document document, JavaType entity,
            FieldMetadata field) {

        Node tr = document.createElement("tr");

        Node td1 = tr.appendChild(document.createElement("td"));
        td1.setTextContent("verifyText");

        Node td2 = tr.appendChild(document.createElement("td"));
        td2.setTextContent(XmlUtils.convertId("_s_"
                + entity.getFullyQualifiedTypeName() + "_"
                + field.getFieldName().getSymbolName() + "_"
                + field.getFieldName().getSymbolName() + "_id"));

        Node td3 = tr.appendChild(document.createElement("td"));
        td3.setTextContent(convertToInitializer(field));

        return tr;
    }

    /**
     * Add verification value command for tabular pattern field.
     * 
     * @param document Document to write command
     * @param entity Field parent entity
     * @param field Field to verify value
     * @return Verification node
     */
    protected Node verifyCommandTabular(Document document, JavaType entity,
            FieldMetadata field) {

        Node tr = document.createElement("tr");

        Node td1 = tr.appendChild(document.createElement("td"));
        td1.setTextContent("verifyValue");

        Node td2 = tr.appendChild(document.createElement("td"));
        String id = "_"
                + XmlUtils
                        .convertId("fu:" + entity.getFullyQualifiedTypeName())
                + "[0]_" + field.getFieldName() + "_id_update";
        td2.setTextContent(XmlUtils.convertId(id));

        Node td3 = tr.appendChild(document.createElement("td"));
        td3.setTextContent(convertToInitializer(field));

        return tr;
    }

    /**
     * Add store confirmation command, required before a javascript
     * confirmation.
     * 
     * @param document Document to write command
     * @param varName Name of variable to store confirmation value
     * @return Confirmation node
     */
    protected Node storeConfirmationCommand(Document document, String varName) {

        Node tr = document.createElement("tr");

        Node td1 = tr.appendChild(document.createElement("td"));
        td1.setTextContent("storeConfirmation");

        Node td2 = tr.appendChild(document.createElement("td"));
        td2.setTextContent(XmlUtils.convertId(varName));

        Node td3 = tr.appendChild(document.createElement("td"));
        td3.setTextContent(" ");

        return tr;
    }

    /**
     * Add check command, to check a checkbox or radio.
     * 
     * @param document Document to write command
     * @param entity Entity owner of check property
     * @return Check node
     */
    protected Node checkCommand(Document document, JavaType entity) {

        Node tr = document.createElement("tr");

        Node td1 = tr.appendChild(document.createElement("td"));
        td1.setTextContent("check");

        Node td2 = tr.appendChild(document.createElement("td"));
        String id = GVNIX_CHECKBOX
                + XmlUtils.convertId(entity.getFullyQualifiedTypeName()) + "_0";
        td2.setTextContent(id);

        Node td3 = tr.appendChild(document.createElement("td"));
        td3.setTextContent(" ");

        return tr;
    }

    /**
     * Write new test to disk, reference it from Seleniun and install maven
     * plugin.
     * 
     * @param name Test name
     * @param serverURL Application base URL
     * @param document Document to write
     * @param relativePath Destination path where write
     */
    protected void installTest(String name, String serverURL,
            Document document, String relativePath) {

        // Store new test
        String path = projectOperations.getPathResolver()
                .getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        relativePath);
        fileManager.createOrUpdateTextFileIfRequired(path,
                XmlUtils.nodeToString(document), false);

        // Store or update the selenium test suite (all tests list)
        manageTestSuite(relativePath, name, serverURL);

        // Install selenium maven plugin
        installMavenPlugin();
    }

    /**
     * Install central Selenium configuration if not already and reference a new
     * test.
     * 
     * @param testPath New test path
     * @param name Test name
     * @param serverURL Application base URL
     */
    protected void manageTestSuite(String testPath, String name,
            String serverURL) {

        String relativePath = "selenium/test-suite.xhtml";
        String seleniumPath = projectOperations.getPathResolver()
                .getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        relativePath);

        Document suite;
        try {

            if (fileManager.exists(seleniumPath)) {

                suite = XmlUtils.readXml(fileManager
                        .getInputStream(seleniumPath));

            }
            else {

                InputStream templateInputStream = FileUtils.getInputStream(
                        SeleniumOperationsImpl.class,
                        "selenium-test-suite-template.xhtml");
                Validate.notNull(templateInputStream,
                        "Could not acquire selenium test suite template");
                suite = XmlUtils.readXml(templateInputStream);
            }
        }
        catch (Exception e) {

            throw new IllegalStateException(e);
        }

        ProjectMetadata projectMetadata = projectOperations
                .getProjectMetadata(projectOperations.getFocusedModuleName());
        Validate.notNull(projectMetadata, "Unable to obtain project metadata");

        Element root = (Element) suite.getLastChild();

        XmlUtils.findRequiredElement("/html/head/title", root).setTextContent(
                "Test suite for "
                        + projectOperations.getProjectName(projectOperations
                                .getFocusedModuleName()) + "project");

        Element tr = suite.createElement("tr");
        Element td = suite.createElement("td");
        tr.appendChild(td);
        Element a = suite.createElement("a");
        a.setAttribute(
                "href",
                serverURL
                        + projectOperations.getProjectName(projectOperations
                                .getFocusedModuleName()) + "/resources/"
                        + testPath);
        a.setTextContent(name);
        td.appendChild(a);

        XmlUtils.findRequiredElement("/html/body/table", root).appendChild(tr);

        fileManager.createOrUpdateTextFileIfRequired(seleniumPath,
                XmlUtils.nodeToString(suite), false);

        menuOperations.addMenuItem(new JavaSymbolName("SeleniumTests"),
                new JavaSymbolName("Test"), "Test", "selenium_menu_test_suite",
                "/resources/" + relativePath, "si_",
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""));
    }

    /**
     * Install into Maven if not already the Selenium plugin.
     */
    protected void installMavenPlugin() {

        PathResolver pathResolver = projectOperations.getPathResolver();
        String pom = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.ROOT, ""), "/pom.xml");
        Document document = XmlUtils.readXml(fileManager.getInputStream(pom));
        Element root = (Element) document.getLastChild();

        // Stop if the plugin is already installed
        if (XmlUtils
                .findFirstElement(
                        "/project/build/plugins/plugin[artifactId = 'selenium-maven-plugin']",
                        root) != null) {
            return;
        }

        Element configuration = XmlUtils
                .getConfiguration(SeleniumOperationsImpl.class);
        Element plugin = XmlUtils.findFirstElement(
                "/configuration/selenium/plugin", configuration);

        // Now install the plugin itself
        if (plugin != null) {
            projectOperations.addBuildPlugin(
                    projectOperations.getFocusedModuleName(),
                    new Plugin(plugin));
        }
    }

    /**
     * Get test value for a field.
     * 
     * @param field Field to auto generate a value
     * @return Field test value
     */
    private String convertToInitializer(FieldMetadata field) {
        return convertToInitializer(field, false);
    }

    /**
     * Get test value for a field.
     * 
     * @param field Field to auto generate a value
     * @param random Should be the initialized value generated randomly ?
     * @return Field test value
     */
    private String convertToInitializer(FieldMetadata field, boolean random) {

        String initializer = " ";

        short index = 1;
        if (random) {
            index = (short) RANDOM_GENERATOR.nextInt();
        }

        AnnotationMetadata min = MemberFindingUtils.getAnnotationOfType(field
                .getAnnotations(), new JavaType(
                "javax.validation.constraints.Min"));
        if (min != null) {
            AnnotationAttributeValue<?> value = min
                    .getAttribute(new JavaSymbolName("value"));
            if (value != null) {
                index = Short.valueOf(value.getValue().toString());
            }
        }
        if (field.getFieldName().getSymbolName().contains("email")
                || field.getFieldName().getSymbolName().contains("Email")) {
            initializer = "some@email.com";
        }
        else if (field.getFieldType().equals(JavaType.STRING)) {
            initializer = "some"
                    + field.getFieldName()
                            .getSymbolNameCapitalisedFirstLetter() + index;
        }
        else if (field.getFieldType()
                .equals(new JavaType(Date.class.getName()))
                || field.getFieldType().equals(
                        new JavaType(Calendar.class.getName()))) {
            Calendar cal = Calendar.getInstance();
            AnnotationMetadata dateTimeFormat = null;
            String style = null;
            if (null != (dateTimeFormat = MemberFindingUtils
                    .getAnnotationOfType(
                            field.getAnnotations(),
                            new JavaType(
                                    "org.springframework.format.annotation.DateTimeFormat")))) {
                AnnotationAttributeValue<?> value = dateTimeFormat
                        .getAttribute(new JavaSymbolName("style"));
                if (value != null) {
                    style = value.getValue().toString();
                }
            }
            if (null != MemberFindingUtils.getAnnotationOfType(field
                    .getAnnotations(), new JavaType(
                    "javax.validation.constraints.Past"))) {
                cal.add(Calendar.YEAR, -1);
                cal.add(Calendar.MONTH, -1);
                cal.add(Calendar.DAY_OF_MONTH, -1);
            }
            else if (null != MemberFindingUtils.getAnnotationOfType(field
                    .getAnnotations(), new JavaType(
                    "javax.validation.constraints.Future"))) {
                cal.add(Calendar.YEAR, +1);
                cal.add(Calendar.MONTH, +1);
                cal.add(Calendar.DAY_OF_MONTH, +1);
            }
            if (style != null) {
                if (style.startsWith("-")) {
                    initializer = ((SimpleDateFormat) DateFormat
                            .getTimeInstance(
                                    DateTime.parseDateFormat(style.charAt(1)),
                                    Locale.getDefault())).format(cal.getTime());
                }
                else if (style.endsWith("-")) {
                    initializer = ((SimpleDateFormat) DateFormat
                            .getDateInstance(
                                    DateTime.parseDateFormat(style.charAt(0)),
                                    Locale.getDefault())).format(cal.getTime());
                }
                else {
                    initializer = ((SimpleDateFormat) DateFormat
                            .getDateTimeInstance(
                                    DateTime.parseDateFormat(style.charAt(0)),
                                    DateTime.parseDateFormat(style.charAt(1)),
                                    Locale.getDefault())).format(cal.getTime());
                }
            }
            else {
                initializer = ((SimpleDateFormat) DateFormat.getDateInstance(
                        DateFormat.SHORT, Locale.getDefault())).format(cal
                        .getTime());
            }

        }
        else if (field.getFieldType().equals(JavaType.BOOLEAN_OBJECT)
                || field.getFieldType().equals(JavaType.BOOLEAN_PRIMITIVE)) {
            initializer = Boolean.FALSE.toString();
        }
        else if (field.getFieldType().equals(JavaType.INT_OBJECT)
                || field.getFieldType().equals(JavaType.INT_PRIMITIVE)) {
            initializer = Integer.valueOf(index).toString();
        }
        else if (field.getFieldType().equals(JavaType.DOUBLE_OBJECT)
                || field.getFieldType().equals(JavaType.DOUBLE_PRIMITIVE)) {
            initializer = Double.valueOf(index).toString();
        }
        else if (field.getFieldType().equals(JavaType.FLOAT_OBJECT)
                || field.getFieldType().equals(JavaType.FLOAT_PRIMITIVE)) {
            initializer = Float.valueOf(index).toString();
        }
        else if (field.getFieldType().equals(JavaType.LONG_OBJECT)
                || field.getFieldType().equals(JavaType.LONG_PRIMITIVE)) {
            initializer = Long.valueOf(index).toString();
        }
        else if (field.getFieldType().equals(JavaType.SHORT_OBJECT)
                || field.getFieldType().equals(JavaType.SHORT_PRIMITIVE)) {
            initializer = Short.valueOf(index).toString();
        }
        else if (field.getFieldType().equals(
                new JavaType("java.math.BigDecimal"))) {
            initializer = BigDecimal.valueOf(index).toString();
        }
        return initializer;
    }

}
