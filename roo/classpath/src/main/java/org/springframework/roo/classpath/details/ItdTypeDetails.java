package org.springframework.roo.classpath.details;

import java.util.List;
import java.util.Set;

import org.springframework.roo.model.JavaType;

/**
 * Provides information about an ITD.
 * <p>
 * For simplicity of implementation this is not a complete representation of all
 * members and other information available via Java bytecode. For example,
 * static initialisers and inner classes are unsupported.
 * 
 * @author Ben Alex
 * @author Stefan Schmidt
 * @since 1.0
 */
public interface ItdTypeDetails extends MemberHoldingTypeDetails {

    /**
     * Returns the name of type which holds the aspect itself.
     * <p>
     * Note that the type receiving the introductions can be determined via
     * {@link #getName()}.
     * 
     * @return the aspect {@link JavaType} (never null)
     */
    JavaType getAspect();

    /**
     * Lists the field-level annotations.
     * <p>
     * This includes those annotations declared on the field, together with
     * those defined via the ITD "declare @field: DestinationType: @Annotation"
     * feature.
     * 
     * @return an unmodifiable representation of the field and the annotations
     *         declared on this field (may be empty, but never null)
     */
    List<DeclaredFieldAnnotationDetails> getFieldAnnotations();

    /**
     * Returns the {@link ClassOrInterfaceTypeDetails} representing the governor
     * of this ITD.
     * 
     * @return the governor {@link ClassOrInterfaceTypeDetails} (never null)
     */
    ClassOrInterfaceTypeDetails getGovernor();

    /**
     * Lists the inner types.
     * 
     * @return an unmodifiable representation of the inner types (may be empty
     *         but never null)
     */
    List<ClassOrInterfaceTypeDetails> getInnerTypes();

    /**
     * Lists the method-level annotations.
     * <p>
     * This includes those annotations declared on the method, together with
     * those defined via the ITD "declare @field: DestinationType: @Annotation"
     * feature.
     * 
     * @return an unmodifiable representation of the method and the annotations
     *         declared on this method (may be empty, but never null)
     */
    List<DeclaredMethodAnnotationDetails> getMethodAnnotations();

    /**
     * @return the explicitly-registered imports this user wishes to have
     *         defined in the ITD (cannot be null, but may be empty)
     */
    Set<JavaType> getRegisteredImports();

    boolean isPrivilegedAspect();
    
    /**
     * Set of aspect declared on {@code declare precedence}
     * AspectJ declaration.
     * 
     * @return ordered aspect declared
     */
    Set<JavaType> getDeclarePrecedence();
}
