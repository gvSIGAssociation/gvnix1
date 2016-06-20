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
package org.gvnix.addon.jpa.addon.geo.providers.hibernatespatial;

import static org.springframework.roo.model.JdkJavaType.MAP;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.addon.jpa.annotations.geo.GvNIXEntityMapLayer;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXEntityMapLayer} annotation.
 * 
 * @author gvNIX Team
 * @since 1.4.0
 */
public class GvNIXEntityMapLayerMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private final ItdBuilderHelper helper;

    private static final String PROVIDES_TYPE_STRING = GvNIXEntityMapLayerMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    private final GvNIXEntityMapLayerValues annotationValues;

    public GvNIXEntityMapLayerMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            TypeLocationService typeLocationService,
            TypeManagementService typeManagementService, JavaType entity,
            String entityPlural,
            Map<JavaSymbolName, AnnotationAttributeValue<Integer>> geoFields,
            GvNIXEntityMapLayerValues annotationValues) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        // Generate necessary methods

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        this.annotationValues = annotationValues;

        builder.addMethod(getfindAllTheEntityByAllGeoms(entity, entityPlural,
                geoFields));

        if (annotationValues.getFinders() != null
                && annotationValues.getFinders().length != 0) {
            if (annotationValues.getFinders().length == 1) {
                for (String finder : annotationValues.getFinders()) {
                    builder.addMethod(getfindAllTheEntityByGeomFilter(entity,
                            finder, entityPlural, geoFields));
                }
            }
            else {
                builder.addMethod(getfindAllTheEntityByFilters(entity,
                        annotationValues.getFinders(), entityPlural, geoFields));
            }
        }
        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();

    }

    /**
     * Create a method to filter by two or more geometry fields from an entity
     * 
     * @param entity
     * @param finders
     * @param plural
     * @param geoFieldNames
     * @return
     */
    private MethodMetadata getfindAllTheEntityByFilters(JavaType entity,
            String finders[], String plural,
            Map<JavaSymbolName, AnnotationAttributeValue<Integer>> geoFields) {

        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Adding parameter types
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
                "org.gvnix.jpa.geo.hibernatespatial.util.GeometryFilter")));
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
                "java.lang.Class", 0, DataType.TYPE, null, Arrays
                        .asList(new JavaType("T")))));
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
                MAP.getFullyQualifiedTypeName(), 0, DataType.TYPE, null, Arrays
                        .asList(JavaType.STRING, JavaType.OBJECT))));

        // Getting method name
        StringBuilder methodNameBuilder = new StringBuilder(String.format(
                "findAll%sBy", plural));
        for (String field : finders) {
            methodNameBuilder
                    .append(StringUtils.capitalize(field).concat("Or"));
        }
        JavaSymbolName methodName = new JavaSymbolName(
                methodNameBuilder.substring(0, methodNameBuilder.length() - 2));

        // Check if a method with the same signature already exists in the
        // target type

        final MethodMetadata method = methodExists(methodName, parameterTypes);
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

        parameterNames.add(new JavaSymbolName("geomFilter"));
        parameterNames.add(new JavaSymbolName("klass"));
        parameterNames.add(new JavaSymbolName("hints"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildfindAllTheEntityByGeoFiltersMethodBody(entity, plural, finders,
                bodyBuilder, geoFields);

        // Return type
        JavaType responseEntityJavaType = new JavaType("java.util.List", 0,
                DataType.TYPE, null, Arrays.asList(new JavaType("T")));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC, methodName,
                responseEntityJavaType, parameterTypes, parameterNames,
                bodyBuilder);

        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);
        methodBuilder.setGenericDefinition("T");

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Create a method to filter by a single geometry field
     * 
     * @param entity
     * @param finder
     * @param plural
     * @param geoFieldNames
     * @return
     */
    private MethodMetadata getfindAllTheEntityByGeomFilter(JavaType entity,
            String finder, String plural,
            Map<JavaSymbolName, AnnotationAttributeValue<Integer>> geoFields) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Adding parameter types
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
                "org.gvnix.jpa.geo.hibernatespatial.util.GeometryFilter")));
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
                "java.lang.Class", 0, DataType.TYPE, null, Arrays
                        .asList(new JavaType("T")))));
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
                MAP.getFullyQualifiedTypeName(), 0, DataType.TYPE, null, Arrays
                        .asList(JavaType.STRING, JavaType.OBJECT))));

        // Getting method name
        JavaSymbolName methodName = new JavaSymbolName(String.format(
                "findAll%sBy%s", plural, StringUtils.capitalize(finder)));

        // Check if a method with the same signature already exists in the
        // target type

        final MethodMetadata method = methodExists(methodName, parameterTypes);
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

        parameterNames.add(new JavaSymbolName("geomFilter"));
        parameterNames.add(new JavaSymbolName("klass"));
        parameterNames.add(new JavaSymbolName("hints"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildfindAllTheEntityByGeomFilterMethodBody(entity, plural, finder,
                bodyBuilder, geoFields);

        // Return type
        JavaType responseEntityJavaType = new JavaType("java.util.List", 0,
                DataType.TYPE, null, Arrays.asList(new JavaType("T")));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC, methodName,
                responseEntityJavaType, parameterTypes, parameterNames,
                bodyBuilder);

        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);
        methodBuilder.setGenericDefinition("T");

        return methodBuilder.build(); // Build and return a MethodMetadata
                                      // instance
    }

    /**
     * Create a generic method to filter by all geometric fields from an entity
     * 
     * @param entity
     * @param plural
     * @param geoFieldNames
     * @return
     */
    private MethodMetadata getfindAllTheEntityByAllGeoms(JavaType entity,
            String plural,
            Map<JavaSymbolName, AnnotationAttributeValue<Integer>> geoFields) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Adding param types
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
                "org.gvnix.jpa.geo.hibernatespatial.util.GeometryFilter")));
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
                "java.lang.Class", 0, DataType.TYPE, null, Arrays
                        .asList(new JavaType("T")))));
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
                MAP.getFullyQualifiedTypeName(), 0, DataType.TYPE, null, Arrays
                        .asList(JavaType.STRING, JavaType.OBJECT))));
        parameterTypes.add(AnnotatedJavaType.convertFromJavaType(new JavaType(
                "java.lang.Iterable", 0, DataType.TYPE, null, Arrays
                        .asList(JavaType.STRING))));
        parameterTypes.add(AnnotatedJavaType
                .convertFromJavaType(JavaType.STRING));

        // Getting method name
        JavaSymbolName methodName = new JavaSymbolName(String.format(
                "findAll%sByGeoFilter", plural));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(methodName, parameterTypes);
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

        // Adding parameter names
        parameterNames.add(new JavaSymbolName("geomFilter"));
        parameterNames.add(new JavaSymbolName("klass"));
        parameterNames.add(new JavaSymbolName("hints"));
        parameterNames.add(new JavaSymbolName("fields"));
        parameterNames.add(new JavaSymbolName("scale"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildfindAllTheEntityByAllGeomMethodBody(entity, plural, bodyBuilder,
                geoFields);

        // Return type: ResponseEntity<List<T>>
        JavaType responseEntityJavaType = new JavaType(new JavaType(
                "java.util.List").getFullyQualifiedTypeName(), 0,
                DataType.TYPE, null, Arrays.asList(new JavaType("T")));

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC + Modifier.STATIC, methodName,
                responseEntityJavaType, parameterTypes, parameterNames,
                bodyBuilder);

        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);
        methodBuilder.setGenericDefinition("T");

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * 
     * 
     * @param entity
     * @param Plural
     * @param finders
     * @param bodyBuilder
     * @param geoFieldName
     */
    private void buildfindAllTheEntityByGeoFiltersMethodBody(JavaType entity,
            String Plural, String[] finders,
            InvocableMemberBodyBuilder bodyBuilder,
            Map<JavaSymbolName, AnnotationAttributeValue<Integer>> geoFields) {

        final String strBuilderTypeName = helper.getFinalTypeName(new JavaType(
                "java.lang.StringBuilder"));

        // StringBuilder jpql = new StringBuilder("SELECT ");
        bodyBuilder.appendFormalLine(String.format(
                "%s jpql = new %s(\"SELECT \");", strBuilderTypeName,
                strBuilderTypeName));

        // if (klass == TheEntity.class) {
        bodyBuilder.appendFormalLine(String.format("if (klass == %s.class) {",
                helper.getFinalTypeName(entity)));

        // jpql.append(" o ");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("jpql.append(\" ( o ) \");");

        // }
        // else {
        // jpql.append(" new ");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("else {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("jpql.append(\" new \");");

        // jpql.append(klass.getName());
        bodyBuilder.appendFormalLine("jpql.append(klass.getName());");

        // jpql.append(" ( o ) ");
        // }
        bodyBuilder.appendFormalLine("jpql.append(\" ( o ) \");");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // jpql.append("FROM Owner o WHERE");
        bodyBuilder.appendFormalLine("jpql.append(\"FROM Owner o WHERE\");");

        // if (geomFilter == null || geomFilter.isEmpty()) {
        bodyBuilder
                .appendFormalLine("if (geomFilter == null || geomFilter.isEmpty()) {");
        bodyBuilder.indent();

        // Set<String> filters = new LinkedHashSet<String>();
        bodyBuilder.appendFormalLine(String
                .format("%s filters = new %s();", helper
                        .getFinalTypeName(new JavaType("java.util.Set", 0,
                                DataType.TYPE, null, Arrays
                                        .asList(JavaType.STRING))), helper
                        .getFinalTypeName(new JavaType(
                                "java.util.LinkedHashSet", 0, DataType.TYPE,
                                null, Arrays.asList(JavaType.STRING)))));

        // filters.add(" o.geom IS NOT NULL");
        for (String filter : finders) {
            bodyBuilder.appendFormalLine(String.format(
                    "filters.add(\"o.%s IS NOT NULL\");", filter));
        }

        // jpql.append(StringUtils.join(filters, " or "));
        bodyBuilder.appendFormalLine(String.format(
                "jpql.append(%s.join(filters, \" or \"));", helper
                        .getFinalTypeName(new JavaType(
                                "org.apache.commons.lang3.StringUtils"))));

        bodyBuilder.indentRemove();

        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("else {");
        bodyBuilder.indent();

        // Set<String> filters = new LinkedHashSet<String>();
        bodyBuilder.appendFormalLine(String
                .format("%s filters = new %s();", helper
                        .getFinalTypeName(new JavaType("java.util.Set", 0,
                                DataType.TYPE, null, Arrays
                                        .asList(JavaType.STRING))), helper
                        .getFinalTypeName(new JavaType(
                                "java.util.LinkedHashSet", 0, DataType.TYPE,
                                null, Arrays.asList(JavaType.STRING)))));

        for (String filter : finders) {

            int srsVal = 0;

            for (Map.Entry<JavaSymbolName, AnnotationAttributeValue<Integer>> field : geoFields
                    .entrySet()) {

                if (filter.equals(field.getKey().getSymbolName())
                        && field.getValue() != null) {
                    srsVal = field.getValue().getValue();
                }
            }

            bodyBuilder.appendFormalLine(String.format(
                    "filters.add(geomFilter.toJPQLString(\"o.%s\", %s));",
                    filter, srsVal));

        }
        // jpql.append(StringUtils.join(filters, " or "));
        bodyBuilder.appendFormalLine(String.format(
                "jpql.append(%s.join(filters, \" or \"));", helper
                        .getFinalTypeName(new JavaType(
                                "org.apache.commons.lang3.StringUtils"))));

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // TypedQuery<T> q = entityManager().createQuery(jpql.toString(),
        // klass);
        bodyBuilder
                .appendFormalLine(String
                        .format("%s<%s> q = entityManager().createQuery(jpql.toString(), klass);",
                                helper.getFinalTypeName(new JavaType(
                                        "javax.persistence.TypedQuery")),
                                helper.getFinalTypeName(new JavaType("T"))));

        // if (!geomFilter.isEmpty()) {
        bodyBuilder.appendFormalLine("if (!geomFilter.isEmpty()) {");
        bodyBuilder.indent();

        // geomFilter.loadJPQLParams(q, "geom");
        for (String filter : finders) {
            bodyBuilder.appendFormalLine(String.format(
                    "geomFilter.loadJPQLParams(q, \"%s\");", filter));
        }
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // if (hints != null && !hints.isEmpty()) {
        bodyBuilder
                .appendFormalLine("if (hints != null && !hints.isEmpty()) {");
        bodyBuilder.indent();

        // for (Entry<String, Object> entry : hints.entrySet()) {
        bodyBuilder.appendFormalLine(String.format(
                "for (%s entry : hints.entrySet()) {", helper
                        .getFinalTypeName(new JavaType("java.util.Map.Entry",
                                0, DataType.TYPE, null, Arrays.asList(
                                        JavaType.STRING, JavaType.OBJECT)))));

        // q.setHint(entry.getKey(), entry.getValue());
        // }
        // }
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("q.setHint(entry.getKey(), entry.getValue());");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // return q.getResultList()
        bodyBuilder.appendFormalLine("return q.getResultList();");
    }

    /**
     * build
     * 
     * @param entity
     * @param Plural
     * @param bodyBuilder
     * @param geoFieldNames
     */
    private void buildfindAllTheEntityByGeomFilterMethodBody(JavaType entity,
            String Plural, String finder,
            InvocableMemberBodyBuilder bodyBuilder,
            Map<JavaSymbolName, AnnotationAttributeValue<Integer>> geoFields) {

        final String strBuilderTypeName = helper.getFinalTypeName(new JavaType(
                "java.lang.StringBuilder"));

        // StringBuilder jpql = new StringBuilder("SELECT ");
        bodyBuilder.appendFormalLine(String.format(
                "%s jpql = new %s(\"SELECT \");", strBuilderTypeName,
                strBuilderTypeName));

        // if (klass == TheEntity.class) {
        bodyBuilder.appendFormalLine(String.format("if (klass == %s.class) {",
                helper.getFinalTypeName(entity)));

        // jpql.append(\" o \"); }
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("jpql.append(\" o \");");
        bodyBuilder.indentRemove();

        // else {
        // jpql.append(" new ");
        bodyBuilder.appendFormalLine("} else {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("jpql.append(\" new \");");

        // jpql.append(klass.getName());
        bodyBuilder.appendFormalLine("jpql.append(klass.getName());");

        // jpql.append(" ( o ) ");
        bodyBuilder.appendFormalLine("jpql.append(\" ( o ) \");");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // jpql.append("FROM TheEntity o WHERE");
        bodyBuilder.appendFormalLine(String.format(
                "jpql.append(\"FROM %s o WHERE\");",
                helper.getFinalTypeName(entity)));

        // if (geomFilter == null || geomFilter.isEmpty()) {
        bodyBuilder
                .appendFormalLine("if (geomFilter == null || geomFilter.isEmpty()) {");
        bodyBuilder.indent();

        // jpql.append(" o.geom IS NOT NULL");
        bodyBuilder.appendFormalLine(String.format(
                "jpql.append(\" o.%s IS NOT NULL\");", finder));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // else {
        // jpql.append(geomFilter.toJPQLString("o.geom", 0));
        // }
        bodyBuilder.appendFormalLine("else {");
        bodyBuilder.indent();

        for (Map.Entry<JavaSymbolName, AnnotationAttributeValue<Integer>> field : geoFields
                .entrySet()) {
            String fieldName = field.getKey().getSymbolName();
            int srsVal = 0;
            if (field.getValue() != null) {
                srsVal = field.getValue().getValue();
            }
            if (fieldName.equalsIgnoreCase(finder)) {
                bodyBuilder.appendFormalLine(String.format(
                        "jpql.append(geomFilter.toJPQLString(\"o.%s\", %s));",
                        finder, srsVal));
            }
        }

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // TypedQuery<T> q = entityManager().createQuery(jpql.toString(),
        // klass);
        bodyBuilder
                .appendFormalLine(String
                        .format("%s<%s> q = entityManager().createQuery(jpql.toString(), klass);",
                                helper.getFinalTypeName(new JavaType(
                                        "javax.persistence.TypedQuery")),
                                helper.getFinalTypeName(new JavaType("T"))));

        // if (!geomFilter.isEmpty()) {
        bodyBuilder.appendFormalLine("if (!geomFilter.isEmpty()) {");

        // geomFilter.loadJPQLParams(q, "o.geom");
        // }
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(String.format(
                "geomFilter.loadJPQLParams(q, \"o.%s\");", finder));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // if (hints != null && !hints.isEmpty()) {
        bodyBuilder
                .appendFormalLine("if (hints != null && !hints.isEmpty()) {");

        // for (Entry<String, Object> entry : hints.entrySet()) {
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(String.format(
                "for (%s entry : hints.entrySet()) {", helper
                        .getFinalTypeName(new JavaType("java.util.Map.Entry",
                                0, DataType.TYPE, null, Arrays.asList(
                                        JavaType.STRING, JavaType.OBJECT)))));

        // q.setHint(entry.getKey(), entry.getValue());
        // }
        // }
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("q.setHint(entry.getKey(), entry.getValue());");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // return q.getResultList()
        bodyBuilder.appendFormalLine("return q.getResultList();");
    }

    private void buildfindAllTheEntityByAllGeomMethodBody(JavaType entity,
            String Plural, InvocableMemberBodyBuilder bodyBuilder,
            Map<JavaSymbolName, AnnotationAttributeValue<Integer>> geoFields) {

        final String strBuilderTypeName = helper.getFinalTypeName(new JavaType(
                "java.lang.StringBuilder"));

        // StringBuilder jpql = new StringBuilder("SELECT ");
        bodyBuilder.appendFormalLine(String.format(
                "%s jpql = new %s(\"SELECT \");", strBuilderTypeName,
                strBuilderTypeName));

        // if (klass == TheEntity.class) {
        bodyBuilder.appendFormalLine(String.format("if (klass == %s.class) {",
                helper.getFinalTypeName(entity)));

        // pql.append(" o ");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("jpql.append(\" o \");");

        // }
        // else {
        // jpql.append(" new ");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} else {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("jpql.append(\" new \");");

        // jpql.append(klass.getName());
        bodyBuilder.appendFormalLine("jpql.append(klass.getName());");

        // jpql.append(" ( o ) ")
        bodyBuilder.appendFormalLine("jpql.append(\" ( o ) \");");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // jpql.append("FROM Owner o WHERE");
        bodyBuilder.appendFormalLine(String.format(
                "jpql.append(\"FROM %s o WHERE\");",
                helper.getFinalTypeName(entity)));

        // if (geomFilter == null || geomFilter.isEmpty())
        bodyBuilder
                .appendFormalLine("if (geomFilter == null || geomFilter.isEmpty()) {");
        bodyBuilder.indent();

        // Set<String> filters = new HashSet<String>();
        bodyBuilder.appendFormalLine(String
                .format("%s filters = new %s();", helper
                        .getFinalTypeName(new JavaType("java.util.Set", 0,
                                DataType.TYPE, null, Arrays
                                        .asList(JavaType.STRING))), helper
                        .getFinalTypeName(new JavaType(
                                "java.util.LinkedHashSet", 0, DataType.TYPE,
                                null, Arrays.asList(JavaType.STRING)))));

        // if (fields == null || !fields.iterator().hasNext()) {
        bodyBuilder
                .appendFormalLine("if (fields == null || !fields.iterator().hasNext()) {");
        bodyBuilder.indent();

        StringBuilder fieldsList = new StringBuilder(String.format(
                "fields = %s.asList(",
                helper.getFinalTypeName(new JavaType("java.util.Arrays"))));
        // Adding all geo fields to field array
        for (Map.Entry<JavaSymbolName, AnnotationAttributeValue<Integer>> field : geoFields
                .entrySet()) {
            String fieldName = field.getKey().getSymbolName();
            fieldsList.append("\"".concat(fieldName).concat("\", "));
        }

        // Removing last comma and closing expression
        fieldsList = new StringBuilder(fieldsList.substring(0,
                fieldsList.length() - 2).concat(");"));

        bodyBuilder.appendFormalLine(fieldsList.toString());
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // for(String field : fields) {
        bodyBuilder.appendFormalLine("for(String field : fields) {");
        bodyBuilder.indent();

        // if ("field".equalsIgnoreCase(field))
        // filters.add(" o.field IS NOT NULL")
        String sentence = "if";
        for (Map.Entry<JavaSymbolName, AnnotationAttributeValue<Integer>> field : geoFields
                .entrySet()) {
            String fieldName = field.getKey().getSymbolName();
            bodyBuilder.appendFormalLine(String.format(
                    " %s (\"%s\".equalsIgnoreCase(field)) {", sentence,
                    fieldName));
            bodyBuilder.indent();
            bodyBuilder.appendFormalLine(String.format(
                    "filters.add(\" o.%s IS NOT NULL\");", fieldName));
            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine("}");

            sentence = "else if";
        }
        // } else {
        // throw new
        // IllegalArgumentException(field.concat(" is not a geometry field"));
        bodyBuilder.appendFormalLine("else {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("throw new IllegalArgumentException(field.concat(\" is not a geometry field\"));");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // jpql.append(StringUtils.join(filters," or "));
        bodyBuilder
                .appendFormalLine("jpql.append(StringUtils.join(filters,\" or \"));");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // else {
        // Set<String> filters = new HashSet<String>();
        bodyBuilder.appendFormalLine("else {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(String
                .format("%s filters = new %s();", helper
                        .getFinalTypeName(new JavaType("java.util.Set", 0,
                                DataType.TYPE, null, Arrays
                                        .asList(JavaType.STRING))), helper
                        .getFinalTypeName(new JavaType(
                                "java.util.LinkedHashSet", 0, DataType.TYPE,
                                null, Arrays.asList(JavaType.STRING)))));

        // if (fields == null || !fields.iterator().hasNext()) {

        bodyBuilder
                .appendFormalLine("if (fields == null || !fields.iterator().hasNext()) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine(fieldsList.toString());
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // for (String field : fields) {
        bodyBuilder.appendFormalLine("for (String field : fields) {");
        bodyBuilder.indent();

        // if ("field".equalsIgnoreCase(field))
        // filters.add(geomFilter.toJPQLString("o.filter", 0));

        sentence = "if";
        for (Map.Entry<JavaSymbolName, AnnotationAttributeValue<Integer>> field : geoFields
                .entrySet()) {
            String fieldName = field.getKey().getSymbolName();

            int srsVal = 0;
            if (field.getValue() != null) {
                srsVal = field.getValue().getValue();
            }
            bodyBuilder.appendFormalLine(String
                    .format("%s(\"%s\".equalsIgnoreCase(field)) {", sentence,
                            fieldName));
            bodyBuilder.indent();
            bodyBuilder.appendFormalLine(String.format(
                    "filters.add(geomFilter.toJPQLString(\"%s\", %s));",
                    fieldName, srsVal));

            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine("}");

            sentence = "else if";
        }

        // } else {
        // throw new
        // IllegalArgumentException(field.concat(" is not a geometry field"));
        bodyBuilder.appendFormalLine("else {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("throw new IllegalArgumentException(field.concat(\" is not a geometry field\"));");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // jpql.append(StringUtils.join(filters," or "));
        bodyBuilder.appendFormalLine(String.format(
                "jpql.append(%s.join(filters,\" or \"));", helper
                        .getFinalTypeName(new JavaType(
                                "org.apache.commons.lang3.StringUtils"))));
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // TypedQuery<T> q = entityManager().createQuery(jpql.toString(),
        // klass);
        bodyBuilder
                .appendFormalLine(String
                        .format("%s<%s>  q = entityManager().createQuery(jpql.toString(), klass);",
                                helper.getFinalTypeName(new JavaType(
                                        "javax.persistence.TypedQuery")),
                                helper.getFinalTypeName(new JavaType("T"))));

        // if (!geomFilter.isEmpty()) {
        bodyBuilder
                .appendFormalLine("if (geomFilter != null && !geomFilter.isEmpty()) {");
        bodyBuilder.indent();

        // for (String field : fields) {
        bodyBuilder.appendFormalLine("for (String field : fields) {");
        bodyBuilder.indent();

        // if ("field".equalsIgnoreCase(field)) {
        // geomFilter.loadJPQLParams(q, "o.field");
        sentence = "if";
        for (Map.Entry<JavaSymbolName, AnnotationAttributeValue<Integer>> field : geoFields
                .entrySet()) {
            String fieldName = field.getKey().getSymbolName();
            bodyBuilder.appendFormalLine(String.format(
                    "%s (\"%s\".equalsIgnoreCase(field)) {", sentence,
                    fieldName));
            bodyBuilder.indent();
            bodyBuilder.appendFormalLine(String.format(
                    "geomFilter.loadJPQLParams(q, \"o.%s\");", fieldName));
            bodyBuilder.indentRemove();
            bodyBuilder.appendFormalLine("}");
            sentence = "else if";
        }
        // } else {
        // throw new
        // IllegalArgumentException(field.concat(" is not a geometry field"));
        bodyBuilder.appendFormalLine("else {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("throw new IllegalArgumentException(field.concat(\" is not a geometry field\"));");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // if (hints != null && !hints.isEmpty()) {
        bodyBuilder
                .appendFormalLine("if (hints != null && !hints.isEmpty()) {");
        bodyBuilder.indent();

        // for (Entry<String, Object> entry : hints.entrySet()) {
        bodyBuilder.appendFormalLine(String.format(
                "for (%s entry : hints.entrySet()) {", helper
                        .getFinalTypeName(new JavaType("java.util.Map.Entry",
                                0, DataType.TYPE, null, Arrays.asList(
                                        JavaType.STRING, JavaType.OBJECT)))));
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine(" q.setHint(entry.getKey(), entry.getValue());");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // return q.getResultList()
        bodyBuilder.appendFormalLine("return q.getResultList();");

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
