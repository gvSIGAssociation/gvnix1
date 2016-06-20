package org.gvnix.addon.fancytree.addon;

import static org.springframework.roo.model.SpringJavaType.REQUEST_MAPPING;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.BooleanAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.details.comments.AbstractComment;
import org.springframework.roo.classpath.details.comments.CommentStructure;
import org.springframework.roo.classpath.details.comments.JavadocComment;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Metadata for {@link GvNIXFancyTreeMetadataProvider}.
 * 
 * @author gvNIX Team
 * @since 1.5
 */
public class GvNIXFancyTreeMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String PROVIDES_TYPE_STRING = GvNIXFancyTreeMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    private static final JavaSymbolName TREE_UTILS_FIELD = new JavaSymbolName(
            "treeUtils");
    private static final String TREE_UTILS = "org.gvnix.util.fancytree.TreeUtils";
    private static final JavaType TREE_UTILS_TYPE = new JavaType(TREE_UTILS);
    private static final JavaType TREENODE_TYPE = new JavaType(
            "org.gvnix.util.fancytree.TreeNode");

    public static final String GET_ACTION = "get";
    public static final String UPDATE_ACTION = "update";
    public static final String DELETE_ACTION = "delete";
    public static final String CREATE_ACTION = "create";
    private static final String GET_METHOD_COMMENT = "Returns the data that will be loaded by Fancytree. It can be added by"
            + "\n"
            + "custom implementation."
            + "\n \n"
            + "@param id (optional) Tree node id to be loaded"
            + "\n \n"
            + "@return Data to be loaded by Fancytree";

    private static final String UPDATE_METHOD_COMMENT = "Renames a node or changes its position into the tree. Also additional data"
            + "\n"
            + "can be updated by custom method implementation."
            + "\n \n"
            + "@param id Tree node id to be updated"
            + "\n"
            + "@param parent (optional) New parent of updated node"
            + "\n"
            + "@param position (optional) New position on the tree of updated node"
            + "\n"
            + "@param data (optional) Custom data of updated node"
            + "\n"
            + "@param text (optional) New title of updated node"
            + "\n \n" + "@return OK if update succeed";

    private static final String DELETE_METHOD_COMMENT = "Removes a element from the tree"
            + "\n \n"
            + "@param id Identifier of node to be deleted"
            + "\n \n"
            + "@return OK if operation succeed";

    private static final String CREATE_METHOD_COMMENT = "Creates a new tree element to add into the tree."
            + "\n \n"
            + "@param parent Parent of new element to be created. It can be root."
            + "\n"
            + "@param position (optional) Initial position on the tree of new element"
            + "\n"
            + "@param data (optional) Custom element data"
            + "\n"
            + "@param text (optional) Initial element title "
            + "\n \n"
            + "@return OK if operation succeed";

    private Boolean editable;
    private String mapping;
    private String page;
    private JavaType controller;
    private String controllerPath;
    private JavaPackage controllerPackage;
    private ImportRegistrationResolver resolver;

    public static String createIdentifier(final JavaType javaType,
            final LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static JavaType getJavaType(final String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static LogicalPath getPath(final String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static boolean isValid(final String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    /**
     * Constructor
     * 
     * @param identifier the ID of the metadata to create (must be a valid ID)
     * @param aspectName the name of the ITD to be created (required)
     * @param governorPhysicalTypeMetadata the governor (required)
     * @param editable boolean that indicates if is necessary to generate edit
     *        methods
     */
    public GvNIXFancyTreeMetadata(final String identifier,
            final JavaType aspectName,
            final PhysicalTypeMetadata governorPhysicalTypeMetadata,
            boolean editable, String mapping, String page, JavaType controller,
            String controllerPath, JavaPackage controllerPackage) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(
                isValid(identifier),
                "Metadata identification string '%s' does not appear to be a valid",
                identifier);

        this.editable = editable;
        this.mapping = mapping;
        this.page = page;
        this.controller = controller;
        this.controllerPath = controllerPath;
        this.controllerPackage = controllerPackage;
        this.resolver = builder.getImportRegistrationResolver();

        // Generating @autowired utilsField
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        AnnotationMetadataBuilder autowiredAnnotation = new AnnotationMetadataBuilder(
                SpringJavaType.AUTOWIRED);
        annotations.add(autowiredAnnotation);

        final FieldMetadataBuilder utilsFieldBuilder = new FieldMetadataBuilder(
                getId(), Modifier.PUBLIC, annotations, TREE_UTILS_FIELD,
                TREE_UTILS_TYPE);
        builder.addField(utilsFieldBuilder);

        // Create view if "page" value is defined
        if (StringUtils.isNotBlank(page)) {
            // Add method to load the view
            builder.addMethod(getShowMethod(getId(), mapping, controllerPath,
                    page));
        }

        builder.addMethod(getMethodByAction(controller, getId(), GET_ACTION,
                mapping, false, GET_METHOD_COMMENT));

        if (editable) {
            builder.addMethod(getMethodByAction(controller, getId(),
                    UPDATE_ACTION, mapping, true, UPDATE_METHOD_COMMENT));
            builder.addMethod(getMethodByAction(controller, getId(),
                    DELETE_ACTION, mapping, true, DELETE_METHOD_COMMENT));
            builder.addMethod(getMethodByAction(controller, getId(),
                    CREATE_ACTION, mapping, true, CREATE_METHOD_COMMENT));
        }

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();

    }

    /**
     * Adds default method to controller that returns the page
     * 
     * @param declaredByMetadataId physical type metadata ID
     * @param request RequestMapping value to custom view
     * @param path target controller path
     * @return
     */
    private MethodMetadata getShowMethod(String declaredByMetadataId,
            String request, String path, String page) {
        // Define method name
        JavaSymbolName methodName = new JavaSymbolName("showTree");

        // Define method parameter types
        final List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(SpringJavaType.MODEL));

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("uiModel"));

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // @RequestMapping
        AnnotationMetadataBuilder requestMappingAnnotation = getRequestMappingAnnotation(
                ("/").concat(request), null, null, null, null, null);
        annotations.add(requestMappingAnnotation);

        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildShowMethodBody(bodyBuilder, path, page);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                declaredByMetadataId, Modifier.PUBLIC, methodName,
                JavaType.STRING, parameterTypes, parameterNames, bodyBuilder);

        methodBuilder.setAnnotations(annotations);

        return methodBuilder.build();
    }

    /**
     * Method that generates show method body
     * 
     * @param bodyBuilder
     * @param path
     * @param page
     */
    private void buildShowMethodBody(InvocableMemberBodyBuilder bodyBuilder,
            String path, String page) {
        bodyBuilder.appendFormalLine(String.format("return \"%s\";", path
                .concat("/").concat(page)));
    }

    /**
     * Writes a method template chaning content depending of the specified
     * action
     * 
     * @param controller Controller class target to write
     * @param declaredByMetadataId physical type metadata ID
     * @param action expected method action
     * @param path default requestmapping path
     * @param editable if method to generate is to edit nodes
     */
    private MethodMetadata getMethodByAction(JavaType controller,
            String declaredByMetadataId, String action, String path,
            Boolean editable, String doc) {

        // Define method name
        JavaSymbolName methodName = new JavaSymbolName(action.concat(
                StringUtils.capitalize(path)).concat("Data"));

        // Define method parameter types
        final List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Define method parameter names (none in this case)
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        if (!action.equalsIgnoreCase(CREATE_ACTION)) {
            // parameter "id" for any action but "create". Not required for
            // "get" actio
            parameterTypes.add(createRequestParam(JavaType.STRING, "id",
                    editable, null));
            parameterNames.add(new JavaSymbolName("id"));
        }

        if (action.equalsIgnoreCase(UPDATE_ACTION)
                || action.equalsIgnoreCase(CREATE_ACTION)) {
            // "parent" parameter is required for "create" action, not for
            // "update"
            parameterTypes.add(createRequestParam(JavaType.STRING, "parent",
                    action.equalsIgnoreCase(CREATE_ACTION), null));
            parameterNames.add(new JavaSymbolName("parent"));

            parameterTypes.add(createRequestParam(JavaType.INT_PRIMITIVE,
                    "position", false, null));
            parameterNames.add(new JavaSymbolName("position"));

            parameterTypes.add(createRequestParam(JavaType.STRING, "data",
                    false, null));
            parameterNames.add(new JavaSymbolName("data"));

            parameterTypes.add(createRequestParam(JavaType.STRING, "text",
                    false, null));
            parameterNames.add(new JavaSymbolName("text"));
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // @RequestMapping
        String requestMappingValue = ("/").concat(path);

        if (!action.equalsIgnoreCase(GET_ACTION)) {
            requestMappingValue = requestMappingValue.concat("/")
                    .concat(action).toLowerCase();
        }

        AnnotationMetadataBuilder requestMappingAnnotation = getRequestMappingAnnotation(
                requestMappingValue, null, null, "application/json", null,
                "Accept=application/json");
        if (editable) {
            requestMappingAnnotation.addEnumAttribute("method",
                    SpringJavaType.REQUEST_METHOD, "POST");
        }
        annotations.add(requestMappingAnnotation);

        // @ResponseBody
        AnnotationMetadataBuilder responseBodyAnnotation = new AnnotationMetadataBuilder();
        responseBodyAnnotation.setAnnotationType(SpringJavaType.RESPONSE_BODY);
        annotations.add(responseBodyAnnotation);

        // Sample method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildSampleMethodBody(bodyBuilder, editable);

        // Return type
        final JavaType RETURN_TYPE;
        if (editable) {
            RETURN_TYPE = new JavaType(
                    "org.springframework.http.ResponseEntity", 0,
                    DataType.TYPE, null, Arrays.asList(JavaType.STRING));
        }
        else {
            final JavaType nodeList = new JavaType(
                    new JavaType(List.class).getFullyQualifiedTypeName(), 0,
                    DataType.TYPE, null, Arrays.asList(TREENODE_TYPE));
            RETURN_TYPE = new JavaType(
                    "org.springframework.http.ResponseEntity", 0,
                    DataType.TYPE, null, Arrays.asList(nodeList));
        }

        // Adding Javadoc to generated methods
        List<AbstractComment> javadocComments = new ArrayList<AbstractComment>();
        javadocComments.add(new JavadocComment(doc));

        CommentStructure commentStructure = new CommentStructure();
        commentStructure.setBeginComments(javadocComments);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                declaredByMetadataId, Modifier.PUBLIC, methodName, RETURN_TYPE,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setCommentStructure(commentStructure);

        return methodBuilder.build();
    }

    /**
     * Builds sample implementation for method template
     * 
     * @param bodyBuilder
     * @param editable
     */
    private void buildSampleMethodBody(InvocableMemberBodyBuilder bodyBuilder,
            Boolean editable) {

        bodyBuilder.appendFormalLine("//FIXME to be implemented");

        // Return statement to edition methods
        // No sample implementation
        if (editable) {
            // return new ResponseEntity<String>(HttpStatus.OK);
            bodyBuilder.appendFormalLine(String.format(
                    "return new %s<%s>(%s.OK);", SpringJavaType.RESPONSE_ENTITY
                            .getSimpleTypeName(), JavaType.STRING
                            .getSimpleTypeName(), SpringJavaType.HTTP_STATUS
                            .getNameIncludingTypeParameters(false, resolver)));
        }
        else {
            // Build sample method body to show some data on Fancytree
            // List<TreeNode> data = new ArrayList<TreeNode>();
            bodyBuilder.appendFormalLine(String.format(
                    "List<TreeNode> data = new %s<TreeNode>();", new JavaType(
                            "java.util.ArrayList")
                            .getNameIncludingTypeParameters(false, resolver)));
            // if (treeUtils.isRootNode(id) || id == null) {
            bodyBuilder
                    .appendFormalLine("if (treeUtils.isRootNode(id) || id == null) {");
            bodyBuilder.indent();

            // TreeNode node1 = new TreeNode("Node 1", true);
            bodyBuilder
                    .appendFormalLine("TreeNode node1 = new TreeNode(\"Node 1\", true);");

            // node1.addChild(new TreeNode("subnode1"));
            bodyBuilder
                    .appendFormalLine("node1.addChild(new TreeNode(\"subnode1\"));");
            bodyBuilder
                    .appendFormalLine("node1.addChild(new TreeNode(\"subnode2\"));");

            // data.add(node1);
            bodyBuilder.appendFormalLine("data.add(node1);");

            // TreeNode node1 = new TreeNode("Node 2");
            bodyBuilder
                    .appendFormalLine("TreeNode node2 = new TreeNode(\"Node 2\");");

            // data.add(node2);
            bodyBuilder.appendFormalLine("data.add(node2);");

            bodyBuilder.indentRemove();

            // } else if ("Node 1".equals(id)) {
            bodyBuilder.appendFormalLine("} else if (\"Node 1\".equals(id)) {");
            bodyBuilder.indent();

            // data.add(new TreeNode("subnode1"));
            // data.add(new TreeNode("subnode2"));
            bodyBuilder
                    .appendFormalLine("data.add(new TreeNode(\"subnode1\"));");
            bodyBuilder
                    .appendFormalLine("data.add(new TreeNode(\"subnode2\"));");

            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine("}");

            // ResponseEntity<List<TreeNode>>(HttpStatus.OK);
            bodyBuilder.appendFormalLine(String.format(
                    "return new %s <List<TreeNode>>(data, %s.OK);",
                    SpringJavaType.RESPONSE_ENTITY.getSimpleTypeName(),
                    SpringJavaType.HTTP_STATUS.getNameIncludingTypeParameters(
                            false, resolver)));
        }
    }

    /**
     * Create a RequestMapping annotation
     * 
     * @param value (optional) attribute value
     * @param method (optional) attribute value
     * @param consumes (optional) attribute value
     * @param produces (optional) attribute value
     * @param params (optional) attribute value
     * @param headers (optional) attribute value
     * @return
     */
    public AnnotationMetadataBuilder getRequestMappingAnnotation(String value,
            String method, String consumes, String produces, String params,
            String headers) {
        // @RequestMapping
        AnnotationMetadataBuilder methodAnnotation = new AnnotationMetadataBuilder();
        methodAnnotation.setAnnotationType(REQUEST_MAPPING);

        if (value != null) {
            // @RequestMapping(value = "xx")
            methodAnnotation.addStringAttribute("value", value);
        }

        if (produces != null) {
            // @RequestMapping(... produces = "application/json")
            methodAnnotation.addStringAttribute("produces", produces);
        }
        if (consumes != null) {
            // @RequestMapping(... consumes = "application/json")
            methodAnnotation.addStringAttribute("consumes", consumes);
        }
        if (method != null) {
            // @RequestMapping(... method = "POST")
            methodAnnotation.addEnumAttribute("method",
                    SpringJavaType.REQUEST_METHOD, new JavaSymbolName(method));
        }
        if (params != null) {
            // @RequestMapping(... produces = "application/json")
            methodAnnotation.addStringAttribute("params", params);
        }
        if (headers != null) {
            // @RequestMapping(... produces = "application/json")
            methodAnnotation.addStringAttribute("headers", headers);
        }

        return methodAnnotation;
    }

    /**
     * Creates a "RequestParam" annotated type
     * 
     * @param paramType
     * @param value (optional) "value" attribute value
     * @param required (optional) attribute value
     * @param defaultValue (optional) attribute value
     * @return
     */
    public AnnotatedJavaType createRequestParam(JavaType paramType,
            String value, Boolean required, String defaultValue) {
        // create annotation values
        final List<AnnotationAttributeValue<?>> annotationAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        if (StringUtils.isNotBlank(value)) {
            annotationAttributes.add(new StringAttributeValue(
                    new JavaSymbolName("value"), value));
        }
        if (required != null) {
            annotationAttributes.add(new BooleanAttributeValue(
                    new JavaSymbolName("required"), required.booleanValue()));
        }
        if (defaultValue != null) {
            annotationAttributes.add(new StringAttributeValue(
                    new JavaSymbolName("defaultValue"), defaultValue));
        }
        AnnotationMetadataBuilder paramAnnotationBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM, annotationAttributes);
        return new AnnotatedJavaType(paramType, paramAnnotationBuilder.build());
    }

    @Override
    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identifier", getId());
        builder.append("valid", valid);
        builder.append("aspectName", aspectName);
        builder.append("destinationType", destination);
        builder.append("governor", governorPhysicalTypeMetadata.getId());
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }
}