package org.springframework.roo.classpath.details;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.comments.CommentStructure;
import org.springframework.roo.model.CustomData;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.support.util.CollectionUtils;

/**
 * Abstract implementation of {@link InvocableMemberMetadata}.
 * 
 * @author Ben Alex
 * @author Juan Carlos García
 * @since 1.0
 */
public abstract class AbstractInvocableMemberMetadata extends
        AbstractIdentifiableAnnotatedJavaStructureProvider implements
        InvocableMemberMetadata {

    private final String body;
    private final List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
    private final List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
    private final List<JavaType> throwsTypes = new ArrayList<JavaType>();
    private CommentStructure commentStructure;
    private String genericDefinition;

    /**
     * Constructor
     * 
     * @param customData
     * @param declaredByMetadataId
     * @param modifier
     * @param annotations
     * @param parameterTypes
     * @param parameterNames
     * @param throwsTypes
     * @param body
     */
    protected AbstractInvocableMemberMetadata(final CustomData customData,
            final String declaredByMetadataId, final int modifier,
            final List<AnnotationMetadata> annotations,
            final List<AnnotatedJavaType> parameterTypes,
            final List<JavaSymbolName> parameterNames,
            final List<JavaType> throwsTypes, final String body) {
        super(customData, declaredByMetadataId, modifier, annotations);
        this.body = body;
        CollectionUtils.populate(this.parameterNames, parameterNames);
        CollectionUtils.populate(this.parameterTypes, parameterTypes);
        CollectionUtils.populate(this.throwsTypes, throwsTypes);
    }

    public final String getBody() {
        return body;
    }

    public final List<JavaSymbolName> getParameterNames() {
        return Collections.unmodifiableList(parameterNames);
    }

    public final List<AnnotatedJavaType> getParameterTypes() {
        return Collections.unmodifiableList(parameterTypes);
    }

    public final List<JavaType> getThrowsTypes() {
        return Collections.unmodifiableList(throwsTypes);
    }

    @Override
    public CommentStructure getCommentStructure() {
        return commentStructure;
    }

    @Override
    public void setCommentStructure(CommentStructure commentStructure) {
        this.commentStructure = commentStructure;
    }

    @Override
	public String getGenericDefinition() {
		return genericDefinition;
	}

    @Override
	public void setGenericDefinition(String genericDefinition) {
		this.genericDefinition = genericDefinition;
	}

}
