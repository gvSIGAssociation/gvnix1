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
package org.gvnix.addon.geo.addon;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.addon.geo.annotations.GvNIXWebEntityMapLayer;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXWebEntityMapLayer} annotation.
 * 
 * @author gvNIX Team
 * @since 1.4.0
 */
public class GvNIXWebEntityMapLayerMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final JavaSymbolName LIST_GEO_ENTITY_ON_MAP_VIEWER = new JavaSymbolName(
            "listGeoEntityOnMapViewer");
    private static final JavaSymbolName SHOW_ONLY_LIST = new JavaSymbolName(
            "showOnlyListForMap");

    private final ItdBuilderHelper helper;
    private final ImportRegistrationResolver importResolver;

    private static final String PROVIDES_TYPE_STRING = GvNIXWebEntityMapLayerMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    public GvNIXWebEntityMapLayerMetadata(String identifier,
            JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            TypeLocationService typeLocationService,
            TypeManagementService typeManagementService, JavaType controller,
            JavaType entity, String entityPlural) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        // Generate necessary methods

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());
        this.importResolver = builder.getImportRegistrationResolver();

        // Adding list method
        builder.addMethod(getListGeoEntityOnMapViewerMethod(entity,
                entityPlural));

        // Adding showOnlyList method
        builder.addMethod(getShowOnlyListMethod(entityPlural));

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();

    }

    /**
     * Gets <code>listGeoEntityOnMapViewer</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getListGeoEntityOnMapViewerMethod(JavaType entity,
            String plural) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // @RequestParam(required = false, value = "GeometryFilter")
        // GeometryFilter geomFilter
        AnnotationMetadataBuilder geometryAnnotation = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        geometryAnnotation.addBooleanAttribute("required", false);
        geometryAnnotation.addStringAttribute("value", "GeometryFilter");
        parameterTypes.add(new AnnotatedJavaType(new JavaType(
                "org.gvnix.jpa.geo.hibernatespatial.util.GeometryFilter"),
                geometryAnnotation.build()));

        // @RequestParam(required = false, value="fields") String[] fields
        AnnotationMetadataBuilder fieldsAnnotation = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        fieldsAnnotation.addBooleanAttribute("required", false);
        fieldsAnnotation.addStringAttribute("value", "fields");
        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING_ARRAY,
                fieldsAnnotation.build()));

        // @RequestParam(required = false, value="scale") String scale
        AnnotationMetadataBuilder scaleAnnotation = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        scaleAnnotation.addBooleanAttribute("required", false);
        scaleAnnotation.addStringAttribute("value", "scale");
        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                scaleAnnotation.build()));

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("geomFilter"));
        parameterNames.add(new JavaSymbolName("fields"));
        parameterNames.add(new JavaSymbolName("scale"));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(
                LIST_GEO_ENTITY_ON_MAP_VIEWER, parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        AnnotationMetadataBuilder requestMappingMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_MAPPING);

        // @RequestMapping(params = "entityMapList", headers =
        // "Accept=application/json", produces = "application/json", consumes =
        // "application/json")
        requestMappingMetadataBuilder.addStringAttribute("params",
                "entityMapList");
        requestMappingMetadataBuilder.addStringAttribute("headers",
                "Accept=application/json");
        requestMappingMetadataBuilder.addStringAttribute("produces",
                "application/json");
        requestMappingMetadataBuilder.addStringAttribute("consumes",
                "application/json");

        // @ResponseBody
        AnnotationMetadataBuilder responseBodyMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.RESPONSE_BODY);

        annotations.add(requestMappingMetadataBuilder);
        annotations.add(responseBodyMetadataBuilder);

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildlistGeoEntityOnMapViewerMethodBody(entity, plural, bodyBuilder);

        // Return type: ResponseEntity<List<Owner>>
        JavaType responseEntityJavaType = new JavaType(
                SpringJavaType.RESPONSE_ENTITY.getFullyQualifiedTypeName(), 0,
                DataType.TYPE, null, Arrays.asList(new JavaType(
                        "java.util.List", 0, DataType.TYPE, null, Arrays
                                .asList(entity))));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, LIST_GEO_ENTITY_ON_MAP_VIEWER,
                responseEntityJavaType, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>showOnlyList</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getShowOnlyListMethod(String plural) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(SpringJavaType.MODEL));
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
                "javax.servlet.http.HttpServletRequest")));
        // @RequestParam("path") String listPath
        AnnotationMetadataBuilder listPathAnnotation = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_PARAM);
        listPathAnnotation.addStringAttribute("value", "path");
        parameterTypes.add(new AnnotatedJavaType(JavaType.STRING,
                listPathAnnotation.build()));

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("uiModel"));
        parameterNames.add(new JavaSymbolName("request"));
        parameterNames.add(new JavaSymbolName("listPath"));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(SHOW_ONLY_LIST,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        AnnotationMetadataBuilder requestMappingMetadataBuilder = new AnnotationMetadataBuilder(
                SpringJavaType.REQUEST_MAPPING);

        // @RequestMapping(params = "selector", produces = "text/html")
        requestMappingMetadataBuilder.addStringAttribute("params",
                "mapselector");
        requestMappingMetadataBuilder.addStringAttribute("produces",
                "text/html");

        annotations.add(requestMappingMetadataBuilder);

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildShowOnlyListMethodBody(plural, bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, SHOW_ONLY_LIST, JavaType.STRING,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * 
     * Builds body method for <code>showOnlyList</code> method <br>
     * 
     * @param plural
     * @param bodyBuilder
     */
    private void buildShowOnlyListMethodBody(String plural,
            InvocableMemberBodyBuilder bodyBuilder) {
        // Do common datatables operations: get entity list filtered by request
        // parameters
        bodyBuilder
                .appendFormalLine("// Do common datatables operations: get entity list filtered by request parameters");

        // Map<String, String> params = populateParametersMap(request);
        bodyBuilder.appendFormalLine(String.format(
                "%s<String, String> params = populateParametersMap(request);",
                new JavaType("java.util.Map").getNameIncludingTypeParameters(
                        false, importResolver)));
        bodyBuilder.appendFormalLine("");

        // if (!params.isEmpty()) {
        bodyBuilder.appendFormalLine("if (!params.isEmpty()) {");
        bodyBuilder.indent();

        // uiModel.addAttribute("baseFilter", params);
        bodyBuilder
                .appendFormalLine("uiModel.addAttribute(\"baseFilter\", params);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("");

        // uiModel.addAttribute("dtt_ignoreParams",
        // Arrays.asList("selector","path"));
        bodyBuilder
                .appendFormalLine(String
                        .format("uiModel.addAttribute(\"dtt_ignoreParams\", %s.asList(\"mapselector\",\"path\"));",
                                helper.getFinalTypeName(new JavaType(
                                        "java.util.Arrays"))));
        bodyBuilder.appendFormalLine("");

        // Show only the list fragment (without footer, header, menu, etc.)
        bodyBuilder
                .appendFormalLine("// Show only the list fragment (without footer, header, menu, etc.)");

        // return "forward:/WEB-INF/views/entity/" + listPath + ".jspx";
        bodyBuilder
                .appendFormalLine(String
                        .format("return \"forward:/WEB-INF/views/%s/\" + listPath + \".jspx\";",
                                plural.toLowerCase()));

    }

    /**
     * Builds body method for <code>showMap</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildlistGeoEntityOnMapViewerMethodBody(JavaType entity,
            String plural, InvocableMemberBodyBuilder bodyBuilder) {
        // HttpHeaders headers = new HttpHeaders();
        bodyBuilder.appendFormalLine(String.format(
                "%s headers = new HttpHeaders();",
                helper.getFinalTypeName(SpringJavaType.HTTP_HEADERS)));

        // headers.add("Content-Type", "application/json; charset=utf-8");
        bodyBuilder
                .appendFormalLine("headers.add(\"Content-Type\", \"application/json; charset=utf-8\");");
        bodyBuilder.newLine();

        // Generating empty result list
        // List<Owner> result = new ArrayList<Owner>();
        bodyBuilder.appendFormalLine("// Generating empty result list");
        bodyBuilder.appendFormalLine(String.format(
                "%s<%s> result = new %s<%s>();",
                helper.getFinalTypeName(JdkJavaType.LIST),
                helper.getFinalTypeName(entity),
                helper.getFinalTypeName(JdkJavaType.ARRAY_LIST),
                helper.getFinalTypeName(entity)));

        // List <String> fieldsList = null;
        bodyBuilder.appendFormalLine(String.format("%s fieldsList = null;",
                helper.getFinalTypeName(JavaType.listOf(JavaType.STRING))));

        // if (fields != null && fields.length > 0) {
        bodyBuilder
                .appendFormalLine("if (fields != null && fields.length > 0) {");

        // fieldsList = new ArrayList<String>();
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(String.format(
                "fieldsList = new %s<%s>();",
                helper.getFinalTypeName(new JavaType(ArrayList.class)),
                helper.getFinalTypeName(JavaType.STRING)));

        // fieldsList.addAll(Arrays.asList(fields));
        bodyBuilder.appendFormalLine(String.format(
                "fieldsList.addAll(%s.asList(fields));",
                helper.getFinalTypeName(new JavaType(Arrays.class))));
        bodyBuilder.indentRemove();

        // }
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.newLine();
        bodyBuilder
                .appendFormalLine("// Looking for all entries on map bounding box");

        // result = Owner.findAllOwnerByGeoFilter(geomFilter, fieldsList,
        // Owner.class, null, scale);
        bodyBuilder
                .appendFormalLine(String
                        .format("result = %s.findAll%sByGeoFilter(geomFilter, %s.class, null, fieldsList, scale);",
                                helper.getFinalTypeName(entity), plural,
                                helper.getFinalTypeName(entity)));

        // return new ResponseEntity<List<Owner>>(result, headers,
        // org.springframework.http.HttpStatus.OK);
        // Return type
        JavaType responseEntityJavaType = new JavaType(
                SpringJavaType.RESPONSE_ENTITY.getFullyQualifiedTypeName(), 0,
                DataType.TYPE, null, Arrays.asList(new JavaType(
                        "java.util.List", 0, DataType.TYPE, null, Arrays
                                .asList(entity))));
        bodyBuilder
                .appendFormalLine(String
                        .format("return new %s(result, headers, org.springframework.http.HttpStatus.OK);",
                                helper.getFinalTypeName(responseEntityJavaType)));
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

    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {
        return MemberFindingUtils.getDeclaredMethod(governorTypeDetails,
                methodName,
                AnnotatedJavaType.convertFromAnnotatedJavaTypes(paramTypes));
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType,
            LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }
}
