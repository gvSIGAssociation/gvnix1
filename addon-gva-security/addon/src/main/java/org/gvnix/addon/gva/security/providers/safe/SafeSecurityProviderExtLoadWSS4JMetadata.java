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
package org.gvnix.addon.gva.security.providers.safe;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ConstructorMetadata;
import org.springframework.roo.classpath.details.ConstructorMetadataBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.comments.AbstractComment;
import org.springframework.roo.classpath.details.comments.CommentStructure;
import org.springframework.roo.classpath.details.comments.JavadocComment;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXExtLoadWSS4JOutInterceptor} annotation.
 * 
 * @author gvNIX Team
 * @since 1.5
 */
public class SafeSecurityProviderExtLoadWSS4JMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final JavaType FAULT_EXCEPTION = new JavaType(
            "org.apache.cxf.interceptor.Fault");

    private static final JavaType SOAP_MESSAGE_PARAM_TYPE = new JavaType(
            "org.apache.cxf.binding.soap.SoapMessage");

    private static final JavaType SOAP_MESSAGE_PARAM_ARRAY_TYPE = new JavaType(
            SOAP_MESSAGE_PARAM_TYPE.getFullyQualifiedTypeName(), 0,
            DataType.TYPE, null, null);

    private static final JavaSymbolName HANDLE_MESSAGE_METHOD = new JavaSymbolName(
            "handleMessage");

    private static final JavaType JAVA_TYPE_STRING = new JavaType(
            "java.lang.String");

    // Constants
    private static final String PROVIDES_TYPE_STRING = SafeSecurityProviderExtLoadWSS4JMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);
    private static final String TRACEABILITY = "traceabilityId";

    /**
     * Itd builder herlper
     */
    private ItdBuilderHelper helper;

    public SafeSecurityProviderExtLoadWSS4JMetadata(String identifier,
            JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        // Adding Fields
        builder.addField(getField("LOGGER",
                "Logger.getLogger(ExtLoadWSS4JOutInterceptor.class)",
                new JavaType("org.apache.log4j.Logger"), Modifier.PRIVATE
                        + Modifier.STATIC + Modifier.FINAL));
        builder.addField(getField(TRACEABILITY, null, JAVA_TYPE_STRING,
                Modifier.PRIVATE));

        // Creating constructors
        builder.addConstructor(getExtLoadWSS4JConst());
        builder.addConstructor(getExtLoadWSS4JConstWithParam());

        // Creating methods
        builder.addMethod(getSetterMethod(TRACEABILITY, JAVA_TYPE_STRING));
        builder.addMethod(getHandleMessageMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Generates constructor method without arguments
     */
    private ConstructorMetadataBuilder getExtLoadWSS4JConst() {

        // Search for an existing constructor
        final ConstructorMetadata existingExplicitConstructor = governorTypeDetails
                .getDeclaredConstructor(null);
        if (existingExplicitConstructor != null) {
            // Found an existing no-arg constructor on this class, so return it
            return new ConstructorMetadataBuilder(existingExplicitConstructor);
        }

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("super();");

        // Use the ConstructorMetadataBuilder for easy creation of
        // MethodMetadata
        ConstructorMetadataBuilder consBuilder = new ConstructorMetadataBuilder(
                getId());
        // Set the modifier public to constructor
        consBuilder.setModifier(Modifier.PUBLIC);
        // Set the body to constructor
        consBuilder.setBodyBuilder(bodyBuilder);

        // return a ConstructorMetadataBuilder instance
        return consBuilder;
    }

    /**
     * Generates constructor method with arguments
     */
    private ConstructorMetadataBuilder getExtLoadWSS4JConstWithParam() {

        // Define method parameter types
        List<JavaType> parameterTypes = new ArrayList<JavaType>();
        parameterTypes.add(new JavaType("java.util.Map", 0, DataType.TYPE,
                null, Arrays.asList(JavaType.STRING, JavaType.OBJECT)));

        // Search for an existing constructor
        final ConstructorMetadata existingExplicitConstructor = governorTypeDetails
                .getDeclaredConstructor(parameterTypes);
        if (existingExplicitConstructor != null) {
            // Found an existing constructor on this class with this params, so
            // return it
            return new ConstructorMetadataBuilder(existingExplicitConstructor);
        }

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("super(props);");

        // Use the ConstructorMetadataBuilder for easy creation of
        // MethodMetadata
        ConstructorMetadataBuilder consBuilder = new ConstructorMetadataBuilder(
                getId());
        // Set parameters
        consBuilder.addParameter("props", parameterTypes.get(0));
        // Set the modifier public to constructor
        consBuilder.setModifier(Modifier.PUBLIC);
        // Set the body to constructor
        consBuilder.setBodyBuilder(bodyBuilder);

        // return a ConstructorMetadataBuilder instance
        return consBuilder;
    }

    /**
     * Generates handleMessage method with SoapMessage argument
     */
    private MethodMetadata getHandleMessageMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(SOAP_MESSAGE_PARAM_ARRAY_TYPE);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(HANDLE_MESSAGE_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Create reference to override annotation
        AnnotationMetadataBuilder overrideAnnotation = new AnnotationMetadataBuilder();
        overrideAnnotation
                .setAnnotationType(new JavaType("java.lang.Override"));

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        throwsTypes.add(FAULT_EXCEPTION);

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("mc"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildHandleMessageMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, HANDLE_MESSAGE_METHOD,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        // Add javadoc
        List<AbstractComment> listComments = new ArrayList<AbstractComment>();
        CommentStructure comStruct = new CommentStructure();
        JavadocComment jvDocCom = new JavadocComment();
        StringBuilder strB = new StringBuilder("Add property Id_trazabilidad");
        strB.append(" head to SOAP message \n\n@param mc SoapMessage where add");
        strB.append(" traceability head");
        jvDocCom.setComment(strB.toString());
        listComments.add(jvDocCom);
        comStruct.setBeginComments(listComments);
        methodBuilder.setCommentStructure(comStruct);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for <code>handleMessage</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildHandleMessageMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // DocumentBuilder builder = null;
        bodyBuilder.appendFormalLine(String.format("%s builder = null;", helper
                .getFinalTypeName(new JavaType(
                        "javax.xml.parsers.DocumentBuilder"))));

        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();

        // builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        bodyBuilder.appendFormalLine(String.format(
                "builder = %s.newInstance().newDocumentBuilder();", helper
                        .getFinalTypeName(new JavaType(
                                "javax.xml.parsers.DocumentBuilderFactory"))));

        // Document doc = builder.newDocument();
        bodyBuilder.appendFormalLine(String.format(
                "%s doc = builder.newDocument();",
                helper.getFinalTypeName(new JavaType("org.w3c.dom.Document"))));
        // Element idTrazabilidad = doc.createElement("Id_trazabilidad");
        bodyBuilder.appendFormalLine(String.format(
                "%s idTrazabilidad = doc.createElement(\"Id_trazabilidad\");",
                helper.getFinalTypeName(new JavaType("org.w3c.dom.Element"))));

        bodyBuilder.appendFormalLine("idTrazabilidad.setAttribute(\"xmlns\",");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("\"http://dgti.gva.es/interoperabilidad\");\n");
        bodyBuilder.indentRemove();

        // Calendar nowDate = Calendar.getInstance();
        bodyBuilder.appendFormalLine(String.format(
                "%s nowDate = Calendar.getInstance();",
                helper.getFinalTypeName(new JavaType("java.util.Calendar"))));

        // SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        bodyBuilder.appendFormalLine(String.format(
                "%s format = new SimpleDateFormat(\"yyyyMMddHHmmssSSS\");",
                helper.getFinalTypeName(new JavaType(
                        "java.text.SimpleDateFormat"))));

        bodyBuilder
                .appendFormalLine("String nowDateString = format.format(nowDate.getTime());\n");

        bodyBuilder
                .appendFormalLine("idTrazabilidad.appendChild(doc.createTextNode(this.traceabilityId");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(".concat(nowDateString)));");
        bodyBuilder.indentRemove();

        // QName qnameIdTrazabilidad = new QName("Id_trazabilidad");
        bodyBuilder.appendFormalLine(String.format(
                "%s qnameIdTrazabilidad = new QName(\"Id_trazabilidad\");\n",
                helper.getFinalTypeName(new JavaType(
                        "javax.xml.namespace.QName"))));

        // Header header = new Header(qnameIdTrazabilidad, idTrazabilidad);
        bodyBuilder.appendFormalLine(String.format(
                "%s header = new Header(qnameIdTrazabilidad, idTrazabilidad);",
                helper.getFinalTypeName(new JavaType(
                        "org.apache.cxf.headers.Header"))));

        bodyBuilder.appendFormalLine("mc.getHeaders().add(header);");

        // close try
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // catch
        bodyBuilder.appendFormalLine(String.format("catch (%s ex) {", helper
                .getFinalTypeName(new JavaType(
                        "javax.xml.parsers.ParserConfigurationException"))));
        bodyBuilder.indent();
        // put exception message in log
        bodyBuilder
                .appendFormalLine("LOGGER.warn(ex.getLocalizedMessage(), ex);");

        // close catch
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}\n");
        // return
        bodyBuilder.appendFormalLine("super.handleMessage(mc);");
    }

    /**
     * Checks if method exists by name and parameters
     * 
     * @param methodName Name of the method
     * @param paramTypes List of the parameters included in the method
     * @return the method if exists, null otherwise
     */
    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {
        return MemberFindingUtils.getDeclaredMethod(governorTypeDetails,
                methodName,
                AnnotatedJavaType.convertFromAnnotatedJavaTypes(paramTypes));
    }

    /**
     * Gets all setters methods. <br>
     * 
     * @return
     */
    private MethodMetadata getSetterMethod(String propertyName,
            JavaType parameterType) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(parameterType);

        // Check if a method with the same signature already exists in the
        // target type
        JavaSymbolName propertyMethodName = new JavaSymbolName(
                "set".concat(Character.toUpperCase(propertyName.charAt(0))
                        + propertyName.substring(1)));
        final MethodMetadata method = methodExists(propertyMethodName,
                parameterTypes);

        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName(propertyName));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildSetterMethodBody(bodyBuilder, propertyName);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, propertyMethodName,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for all setters methods. <br>
     * 
     * @param bodyBuilder
     */
    private void buildSetterMethodBody(InvocableMemberBodyBuilder bodyBuilder,
            String propertyName) {
        bodyBuilder.appendFormalLine("this.".concat(propertyName).concat(" = ")
                .concat(propertyName).concat(";"));

    }

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

    /**
     * Create metadata for a field definition.
     * 
     * @return a FieldMetadata object
     */
    private FieldMetadata getField(String name, String value,
            JavaType javaType, int modifier) {
        JavaSymbolName curName = new JavaSymbolName(name);
        String initializer = value;
        FieldMetadata field = getOrCreateField(curName, javaType, initializer,
                modifier, null);
        return field;
    }

    /**
     * Gets or creates a field based on parameters.<br>
     * First try to get a suitable field (by name and type). If not found create
     * a new one (adding a counter to name if it's needed)
     * 
     * @param fielName
     * @param fieldType
     * @param initializer (String representation)
     * @param modifier See {@link Modifier}
     * @param annotations optional (can be null)
     * @return
     */
    private FieldMetadata getOrCreateField(JavaSymbolName fielName,
            JavaType fieldType, String initializer, int modifier,
            List<AnnotationMetadataBuilder> annotations) {
        JavaSymbolName curName = fielName;

        // Check if field exist
        FieldMetadata currentField = governorTypeDetails
                .getDeclaredField(curName);
        if (currentField != null) {
            if (!currentField.getFieldType().equals(fieldType)) {
                // No compatible field: look for new name
                currentField = null;
                JavaSymbolName newName = curName;
                int i = 1;
                while (governorTypeDetails.getDeclaredField(newName) != null) {
                    newName = new JavaSymbolName(curName.getSymbolName()
                            .concat(StringUtils.repeat('_', i)));
                    i++;
                }
                curName = newName;
            }
        }
        if (currentField == null) {
            // create field
            if (annotations == null) {
                annotations = new ArrayList<AnnotationMetadataBuilder>(0);
            }
            // Using the FieldMetadataBuilder to create the field
            // definition.
            final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                    getId(), modifier, annotations, curName, // Field
                    fieldType); // Field type
            fieldBuilder.setFieldInitializer(initializer);
            currentField = fieldBuilder.build(); // Build and return a
            // FieldMetadata
            // instance
        }
        return currentField;

    }

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType,
            LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }
}
