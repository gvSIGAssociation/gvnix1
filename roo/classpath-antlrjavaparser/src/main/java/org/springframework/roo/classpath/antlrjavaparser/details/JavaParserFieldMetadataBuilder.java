package org.springframework.roo.classpath.antlrjavaparser.details;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.roo.classpath.antlrjavaparser.CompilationUnitServices;
import org.springframework.roo.classpath.antlrjavaparser.JavaParserUtils;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.Builder;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

import com.github.antlrjavaparser.ASTHelper;
import com.github.antlrjavaparser.JavaParser;
import com.github.antlrjavaparser.ParseException;
import com.github.antlrjavaparser.api.CompilationUnit;
import com.github.antlrjavaparser.api.body.BodyDeclaration;
import com.github.antlrjavaparser.api.body.FieldDeclaration;
import com.github.antlrjavaparser.api.body.TypeDeclaration;
import com.github.antlrjavaparser.api.body.VariableDeclarator;
import com.github.antlrjavaparser.api.expr.AnnotationExpr;
import com.github.antlrjavaparser.api.expr.Expression;
import com.github.antlrjavaparser.api.expr.NameExpr;
import com.github.antlrjavaparser.api.expr.ObjectCreationExpr;
import com.github.antlrjavaparser.api.type.ClassOrInterfaceType;
import com.github.antlrjavaparser.api.type.Type;

/**
 * Java Parser implementation of {@link FieldMetadata}.
 * 
 * @author Ben Alex
 * @since 1.0
 */
public class JavaParserFieldMetadataBuilder implements Builder<FieldMetadata> {

    public static void addField(
            final CompilationUnitServices compilationUnitServices,
            final List<BodyDeclaration> members, final FieldMetadata field) {
        Validate.notNull(compilationUnitServices,
                "Flushable compilation unit services required");
        Validate.notNull(members, "Members required");
        Validate.notNull(field, "Field required");

        JavaParserUtils.importTypeIfRequired(
                compilationUnitServices.getEnclosingTypeName(),
                compilationUnitServices.getImports(), field.getFieldType());
        final Type initType = JavaParserUtils.getResolvedName(
                compilationUnitServices.getEnclosingTypeName(),
                field.getFieldType(), compilationUnitServices);
        final ClassOrInterfaceType finalType = JavaParserUtils
                .getClassOrInterfaceType(initType);

        final FieldDeclaration newField = ASTHelper.createFieldDeclaration(
                JavaParserUtils.getJavaParserModifier(field.getModifier()),
                initType, field.getFieldName().getSymbolName());

        // Add parameterized types for the field type (not initializer)
        if (field.getFieldType().getParameters().size() > 0) {
            final List<Type> fieldTypeArgs = new ArrayList<Type>();
            finalType.setTypeArgs(fieldTypeArgs);
            for (final JavaType parameter : field.getFieldType()
                    .getParameters()) {
                fieldTypeArgs.add(JavaParserUtils.importParametersForType(
                        compilationUnitServices.getEnclosingTypeName(),
                        compilationUnitServices.getImports(), parameter));
            }
        }

        final List<VariableDeclarator> vars = newField.getVariables();
        Validate.notEmpty(vars,
                "Expected ASTHelper to have provided a single VariableDeclarator");
        Validate.isTrue(vars.size() == 1,
                "Expected ASTHelper to have provided a single VariableDeclarator");
        final VariableDeclarator vd = vars.iterator().next();

        if (StringUtils.isNotBlank(field.getFieldInitializer())) {
            // There is an initializer.
            // We need to make a fake field that we can have JavaParser parse.
            // Easiest way to do that is to build a simple source class
            // containing the required field and re-parse it.
            final StringBuilder sb = new StringBuilder();
            sb.append("class TemporaryClass {\n");
            sb.append("  private " + field.getFieldType() + " "
                    + field.getFieldName() + " = "
                    + field.getFieldInitializer() + ";\n");
            sb.append("}\n");
            final ByteArrayInputStream bais = new ByteArrayInputStream(sb
                    .toString().getBytes());
            CompilationUnit ci;
            try {
                ci = JavaParser.parse(bais);
            }
            catch (final IOException e) {
                throw new IllegalStateException(
                        "Illegal state: Unable to parse input stream", e);
            }
            catch (final ParseException pe) {
                throw new IllegalStateException(
                        "Illegal state: JavaParser did not parse correctly", pe);
            }
            final List<TypeDeclaration> types = ci.getTypes();
            if (types == null || types.size() != 1) {
                throw new IllegalArgumentException("Field member invalid");
            }
            final TypeDeclaration td = types.get(0);
            final List<BodyDeclaration> bodyDeclarations = td.getMembers();
            if (bodyDeclarations == null || bodyDeclarations.size() != 1) {
                throw new IllegalStateException(
                        "Illegal state: JavaParser did not return body declarations correctly");
            }
            final BodyDeclaration bd = bodyDeclarations.get(0);
            if (!(bd instanceof FieldDeclaration)) {
                throw new IllegalStateException(
                        "Illegal state: JavaParser did not return a field declaration correctly");
            }
            final FieldDeclaration fd = (FieldDeclaration) bd;
            if (fd.getVariables() == null || fd.getVariables().size() != 1) {
                throw new IllegalStateException(
                        "Illegal state: JavaParser did not return a field declaration correctly");
            }

            final Expression init = fd.getVariables().get(0).getInit();

            // Resolve imports (ROO-1505)
            if (init instanceof ObjectCreationExpr) {
                final ObjectCreationExpr ocr = (ObjectCreationExpr) init;
                final JavaType typeToImport = JavaParserUtils.getJavaTypeNow(
                        compilationUnitServices, ocr.getType(), null);
                final NameExpr nameExpr = JavaParserUtils.importTypeIfRequired(
                        compilationUnitServices.getEnclosingTypeName(),
                        compilationUnitServices.getImports(), typeToImport);
                final ClassOrInterfaceType classOrInterfaceType = JavaParserUtils
                        .getClassOrInterfaceType(nameExpr);
                ocr.setType(classOrInterfaceType);

                if (typeToImport.getParameters().size() > 0) {
                    final List<Type> initTypeArgs = new ArrayList<Type>();
                    finalType.setTypeArgs(initTypeArgs);
                    for (final JavaType parameter : typeToImport
                            .getParameters()) {
                        initTypeArgs.add(JavaParserUtils
                                .importParametersForType(
                                        compilationUnitServices
                                                .getEnclosingTypeName(),
                                        compilationUnitServices.getImports(),
                                        parameter));
                    }
                    classOrInterfaceType.setTypeArgs(initTypeArgs);
                }
            }

            vd.setInit(init);
        }

        // Add annotations
        final List<AnnotationExpr> annotations = new ArrayList<AnnotationExpr>();
        newField.setAnnotations(annotations);
        for (final AnnotationMetadata annotation : field.getAnnotations()) {
            JavaParserAnnotationMetadataBuilder.addAnnotationToList(
                    compilationUnitServices, annotations, annotation);
        }

        // Locate where to add this field; also verify if this field already
        // exists
        int nextFieldIndex = 0;
        int i = -1;
        for (final BodyDeclaration bd : members) {
            i++;
            if (bd instanceof FieldDeclaration) {
                // Next field should appear after this current field
                nextFieldIndex = i + 1;
                final FieldDeclaration bdf = (FieldDeclaration) bd;
                for (final VariableDeclarator v : bdf.getVariables()) {
                    Validate.isTrue(!field.getFieldName().getSymbolName()
                            .equals(v.getId().getName()),
                            "A field with name '%s' already exists", field
                                    .getFieldName().getSymbolName());
                }
            }
        }

        if (field.getCommentStructure() != null) {

            // if the field has annotations, add JavaDoc comments to the first
            // annotation
            if (annotations != null && annotations.size() > 0) {
                AnnotationExpr firstAnnotation = annotations.get(0);

                JavaParserCommentMetadataBuilder.updateCommentsToJavaParser(
                        firstAnnotation, field.getCommentStructure());

                // Otherwise, add comments to the field declaration line
            }
            else {
                JavaParserCommentMetadataBuilder.updateCommentsToJavaParser(
                        newField, field.getCommentStructure());
            }
        }

        // Add the field to the compilation unit
        members.add(nextFieldIndex, newField);
    }

    public static JavaParserFieldMetadataBuilder getInstance(
            final String declaredByMetadataId,
            final FieldDeclaration fieldDeclaration,
            final VariableDeclarator var,
            final CompilationUnitServices compilationUnitServices,
            final Set<JavaSymbolName> typeParameters) {
        return new JavaParserFieldMetadataBuilder(declaredByMetadataId,
                fieldDeclaration, var, compilationUnitServices, typeParameters);
    }

    public static void removeField(
            final CompilationUnitServices compilationUnitServices,
            final List<BodyDeclaration> members, final JavaSymbolName fieldName) {
        Validate.notNull(compilationUnitServices,
                "Flushable compilation unit services required");
        Validate.notNull(members, "Members required");
        Validate.notNull(fieldName, "Field name to remove is required");

        // Locate the field
        int i = -1;
        int toDelete = -1;
        for (final BodyDeclaration bd : members) {
            i++;
            if (bd instanceof FieldDeclaration) {
                final FieldDeclaration fieldDeclaration = (FieldDeclaration) bd;
                for (final VariableDeclarator var : fieldDeclaration
                        .getVariables()) {
                    if (var.getId().getName().equals(fieldName.getSymbolName())) {
                        toDelete = i;
                        break;
                    }
                }
            }
        }

        Validate.isTrue(toDelete > -1, "Could not locate field '%s' to delete",
                fieldName);

        // Do removal outside iteration of body declaration members, to avoid
        // concurrent modification exceptions
        members.remove(toDelete);
    }

    private final List<AnnotationMetadata> annotations = new ArrayList<AnnotationMetadata>();
    private final String declaredByMetadataId;
    private String fieldInitializer;

    private final JavaSymbolName fieldName;

    private JavaType fieldType;

    private final int modifier;

    private JavaParserFieldMetadataBuilder(final String declaredByMetadataId,
            final FieldDeclaration fieldDeclaration,
            final VariableDeclarator var,
            final CompilationUnitServices compilationUnitServices,
            final Set<JavaSymbolName> typeParameters) {
        Validate.notNull(declaredByMetadataId,
                "Declared by metadata ID required");
        Validate.notNull(fieldDeclaration, "Field declaration is mandatory");
        Validate.notNull(var, "Variable declarator required");
        Validate.isTrue(fieldDeclaration.getVariables().contains(var),
                "Cannot request a variable not already in the field declaration");
        Validate.notNull(compilationUnitServices,
                "Compilation unit services are required");

        // Convert Java Parser modifier into JDK modifier
        modifier = JavaParserUtils.getJdkModifier(fieldDeclaration
                .getModifiers());

        this.declaredByMetadataId = declaredByMetadataId;

        final Type type = fieldDeclaration.getType();
        fieldType = JavaParserUtils.getJavaType(compilationUnitServices, type,
                typeParameters);

        // Convert into an array if this variable ID uses array notation
        if (var.getId().getArrayCount() > 0) {
            fieldType = new JavaType(fieldType.getFullyQualifiedTypeName(), var
                    .getId().getArrayCount() + fieldType.getArray(),
                    fieldType.getDataType(), fieldType.getArgName(),
                    fieldType.getParameters());
        }

        fieldName = new JavaSymbolName(var.getId().getName());

        // Lookup initializer, if one was requested and easily determinable
        final Expression e = var.getInit();
        if (e != null) {
            fieldInitializer = e.toString();
        }

        final List<AnnotationExpr> annotations = fieldDeclaration
                .getAnnotations();
        if (annotations != null) {
            for (final AnnotationExpr annotation : annotations) {
                this.annotations.add(JavaParserAnnotationMetadataBuilder
                        .getInstance(annotation, compilationUnitServices)
                        .build());
            }
        }
    }

    @Override
    public FieldMetadata build() {
        final FieldMetadataBuilder fieldMetadataBuilder = new FieldMetadataBuilder(
                declaredByMetadataId);
        fieldMetadataBuilder.setAnnotations(annotations);
        fieldMetadataBuilder.setFieldInitializer(fieldInitializer);
        fieldMetadataBuilder.setFieldName(fieldName);
        fieldMetadataBuilder.setFieldType(fieldType);
        fieldMetadataBuilder.setModifier(modifier);
        return fieldMetadataBuilder.build();
    }
}
