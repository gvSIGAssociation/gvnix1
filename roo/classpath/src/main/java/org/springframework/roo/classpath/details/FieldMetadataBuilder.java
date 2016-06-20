package org.springframework.roo.classpath.details;

import java.util.List;

import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.comments.CommentStructure;
import org.springframework.roo.classpath.details.comments.JavadocComment;
import org.springframework.roo.classpath.operations.jsr303.FieldDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Builder for {@link FieldMetadata}.
 * 
 * @author Ben Alex
 * @since 1.1
 */
public class FieldMetadataBuilder extends
        AbstractIdentifiableAnnotatedJavaStructureBuilder<FieldMetadata> {

    private String fieldInitializer;
    private JavaSymbolName fieldName;
    private JavaType fieldType;
    private CommentStructure commentStructure;

    public FieldMetadataBuilder(final FieldMetadata existing) {
        super(existing);
        init(existing.getFieldName(), existing.getFieldType(),
                existing.getFieldInitializer());

        this.commentStructure = existing.getCommentStructure();
    }

    public FieldMetadataBuilder(final String declaredbyMetadataId) {
        super(declaredbyMetadataId);
    }

    public FieldMetadataBuilder(final String declaredbyMetadataId,
            final FieldMetadata existing) {
        super(declaredbyMetadataId, existing);
        init(existing.getFieldName(), existing.getFieldType(),
                existing.getFieldInitializer());

        this.commentStructure = existing.getCommentStructure();
    }

    /**
     * Constructor for a builder with the given field values
     * 
     * @param declaredbyMetadataId a MID for a specific instance
     * @param modifier as per {@link java.lang.reflect.Modifier}
     * @param fieldName the field name (required)
     * @param fieldType the field type (required)
     * @param fieldInitializer the Java expression for the field's initial value
     *            (can be <code>null</code> for none)
     */
    public FieldMetadataBuilder(final String declaredbyMetadataId,
            final int modifier, final JavaSymbolName fieldName,
            final JavaType fieldType, final String fieldInitializer) {
        this(declaredbyMetadataId);
        setModifier(modifier);
        init(fieldName, fieldType, fieldInitializer);
    }

    /**
     * Constructor
     * 
     * @param declaredbyMetadataId
     * @param modifier
     * @param annotations
     * @param fieldName
     * @param fieldType
     */
    public FieldMetadataBuilder(final String declaredbyMetadataId,
            final int modifier,
            final List<AnnotationMetadataBuilder> annotations,
            final JavaSymbolName fieldName, final JavaType fieldType) {
        this(declaredbyMetadataId);
        setModifier(modifier);
        setAnnotations(annotations);
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    /**
     * Constructor
     * 
     * @param fieldDetails
     */
    public FieldMetadataBuilder(final FieldDetails fieldDetails) {
        this(fieldDetails.getPhysicalTypeIdentifier(), fieldDetails
                .getModifiers(), fieldDetails.getAnnotations(), fieldDetails
                .getFieldName(), fieldDetails.getFieldType());

        if (fieldDetails.getComment() != null) {
            commentStructure = new CommentStructure();
            JavadocComment javadocComment = new JavadocComment(
                    fieldDetails.getComment());
            commentStructure.addComment(javadocComment,
                    CommentStructure.CommentLocation.BEGINNING);
        }
    }

    public FieldMetadata build() {
        DefaultFieldMetadata md = new DefaultFieldMetadata(getCustomData()
                .build(), getDeclaredByMetadataId(), getModifier(),
                buildAnnotations(), getFieldName(), getFieldType(),
                getFieldInitializer());
        md.setCommentStructure(getCommentStructure());

        return md;
    }

    public String getFieldInitializer() {
        return fieldInitializer;
    }

    public JavaSymbolName getFieldName() {
        return fieldName;
    }

    public JavaType getFieldType() {
        return fieldType;
    }

    private void init(final JavaSymbolName fieldName, final JavaType fieldType,
            final String fieldInitializer) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.fieldInitializer = fieldInitializer;
    }

    public void setFieldInitializer(final String fieldInitializer) {
        this.fieldInitializer = fieldInitializer;
    }

    public void setFieldName(final JavaSymbolName fieldName) {
        this.fieldName = fieldName;
    }

    public void setFieldType(final JavaType fieldType) {
        this.fieldType = fieldType;
    }

    public CommentStructure getCommentStructure() {
        return commentStructure;
    }

    public void setCommentStructure(CommentStructure commentStructure) {
        this.commentStructure = commentStructure;
    }
}
