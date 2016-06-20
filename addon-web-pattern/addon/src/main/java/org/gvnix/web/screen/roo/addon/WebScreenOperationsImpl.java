/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2011 CIT - Generalitat
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.transform.Transformer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.support.MessageBundleUtils;
import org.gvnix.support.OperationUtils;
import org.gvnix.web.i18n.roo.addon.ValencianCatalanLanguage;
import org.springframework.roo.addon.jpa.activerecord.JpaActiveRecordMetadata;
import org.springframework.roo.addon.propfiles.PropFileOperations;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18n;
import org.springframework.roo.addon.web.mvc.jsp.i18n.I18nSupport;
import org.springframework.roo.addon.web.mvc.jsp.i18n.languages.SpanishLanguage;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.operations.AbstractOperations;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.osgi.service.component.ComponentContext;

/**
 * Implementation of web MVC screen patterns operations.
 * 
 * @author Jose Manuel Vivó (jmvivo at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Óscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
@Component
@Service
public class WebScreenOperationsImpl extends AbstractOperations implements
        WebScreenOperations {
    private static final String VALUE = "value";
    private static final String FORM_BACKING_OBJ = "formBackingObject";
    private static final String ARRAY_ELEM = "__ARRAY_ELEMENT__";
    private static final String VAR = "var";

    private static Logger logger = Logger
            .getLogger(WebScreenOperationsImpl.class.getName());

    /** Name of {@link GvNIXPattern} attribute value */
    public static final JavaSymbolName PAT_ANN_ATTR_VAL_NAME = new JavaSymbolName(
            VALUE);

    /** {@link GvNIXPattern} JavaType */
    public static final JavaType PATTERN_ANNOTATION = new JavaType(
            GvNIXPattern.class.getName());

    /** Name of {@link GvNIXRelationPattern} attribute value */
    public static final JavaSymbolName REL_PAT_ANN_ATTR_VAL_NAME = new JavaSymbolName(
            VALUE);

    /** {@link GvNIXRelationPattern} JavaType */
    public static final JavaType RELATION_PATTERN_ANNOTATION = new JavaType(
            GvNIXRelationsPattern.class.getName());

    /** {@link GvNIXPattern} JavaType */
    public static final JavaType RELATED_PATTERN_ANNOTATION = new JavaType(
            GvNIXRelatedPattern.class.getName());

    /** Name of {@link GvNIXRelatedPattern} attribute value */
    public static final JavaSymbolName RELTD_PAT_ANN_ATTR_VAL_NAME = new JavaSymbolName(
            VALUE);

    /** Name of {@link RooWebScaffold} attribute formBackingObject */
    public static final JavaSymbolName SCAFF_ANN_ATTR_FORMBOBJ = new JavaSymbolName(
            FORM_BACKING_OBJ);

    /** {@link RooWebScaffold} JavaType */
    public static final JavaType ROOWEBSCAFFOLD_ANNOTATION = new JavaType(
            RooWebScaffold.class.getName());

    /** {@link GvNIXEntityBatch} JavaType */
    public static final JavaType ENTITYBATCH_ANNOTATION = new JavaType(
            GvNIXEntityBatch.class.getName());

    /** {@link OneToMany} JavaType */
    public static final JavaType ONETOMANY_ANNOTATION = new JavaType(
            "javax.persistence.OneToMany");

    /** {@link OneToMany} JavaType */
    public static final JavaType MANYTOMANY_ANNOTATION = new JavaType(
            "javax.persistence.ManyToMany");

    private ComponentContext cContext;

    protected void activate(final ComponentContext componentContext) {
        cContext = componentContext;
        context = cContext.getBundleContext();
    }

    /**
     * MetadataService offers access to Roo's metadata model, use it to retrieve
     * any available metadata by its MID
     */
    @Reference
    private MetadataService metadataService;

    @Reference
    private TypeLocationService typeLocationService;

    @Reference
    private PatternService patternService;

    @Reference
    private WebScreenConfigService configService;

    @Reference
    private ProjectOperations projectOperations;

    @Reference
    PropFileOperations propFileOperations;

    @Reference
    private I18nSupport i18nSupport;

    @Reference
    private TypeManagementService typeManagementService;

    /** {@inheritDoc} */
    public boolean isPatternCommandAvailable() {

        return configService.isSpringMvcProject();
    }

    /** {@inheritDoc} */
    public boolean addPattern(JavaType controllerClass, JavaSymbolName name,
            WebPatternType pattern) {

        Validate.notNull(controllerClass, "controller class is required");
        Validate.notNull(name, "pattern name is required");
        Validate.notNull(pattern, "pattern type is required");

        // Get mutableTypeDetails from controllerClass. Also checks javaType is
        // a controller
        ClassOrInterfaceTypeDetails controllerDetails = patternService
                .getControllerTypeDetails(controllerClass);

        // Check if controller entity is a active-record entity (supported)
        Validate.isTrue(
                isControllerEntityActiveRecord(controllerDetails),
                "This operation only supports controllers of entities with @RooJpaActiveRecord annotation");

        // Check if there are pattern names used more than once in project
        Validate.isTrue(
                !patternService.existsMasterPatternDuplicated()
                        && !patternService.existsMasterPatternDefined(name
                                .getSymbolName()),
                "There is a pattern name used more than once in the project");

        // All checks passed OK

        // Test if the annotation already exists on the target type
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(controllerDetails.getAnnotations(),
                        PATTERN_ANNOTATION);

        // Get pattern attributes of the controller
        List<StringAttributeValue> patternList = patternService
                .getControllerMasterPattern(controllerClass);

        // Build string parameter for the pattern
        String patternParameter = name.toString().concat("=")
                .concat(pattern.toString());

        // Adds new pattern
        patternList.add(new StringAttributeValue(
                new JavaSymbolName(ARRAY_ELEM), patternParameter));

        // Prepare annotation builder
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                PATTERN_ANNOTATION);
        annotationBuilder
                .addAttribute(new ArrayAttributeValue<StringAttributeValue>(
                        PAT_ANN_ATTR_VAL_NAME, patternList));

        // Add or update annotation to target type
        ClassOrInterfaceTypeDetailsBuilder mutableTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                controllerDetails);
        if (annotationMetadata != null) {

            mutableTypeDetailsBuilder.updateTypeAnnotation(
                    annotationBuilder.build(), new HashSet<JavaSymbolName>());

        }
        else {

            mutableTypeDetailsBuilder.addAnnotation(annotationBuilder.build());
        }
        typeManagementService
                .createOrUpdateTypeOnDisk(mutableTypeDetailsBuilder.build());

        // Tabular style patterns requires batch operations
        if (pattern.equals(WebPatternType.tabular)
                || pattern.equals(WebPatternType.tabular_edit_register)) {

            // TODO If tabular_edit_register, only delete entity in batch
            // required
            annotateTypeWithGvNIXEntityBatch(getFormBakingObject(controllerDetails));
        }

        return true;
    }

    /** {@inheritDoc} */
    public boolean isRelationPatternCommandAvailable() {
        return configService.arePattrenArtifactsInstalled();
    }

    /** {@inheritDoc} */
    public boolean addRelationPattern(JavaType controllerClass,
            JavaSymbolName name, JavaSymbolName field, WebPatternType type) {

        Validate.notNull(controllerClass, "controller is required");
        Validate.notNull(name, "name is required");
        Validate.notNull(field, "field is required");
        Validate.notNull(type, "type is required");

        // Get mutableTypeDetails from controllerClass. Also checks javaType is
        // a controller
        ClassOrInterfaceTypeDetails mutableTypeDetails = patternService
                .getControllerTypeDetails(controllerClass);

        // Check if controller entity is a active-record entity (supported)
        Validate.isTrue(
                isControllerEntityActiveRecord(mutableTypeDetails),
                "This operation only supports controllers of entities with @RooJpaActiveRecord annotation");

        // Check if pattern name is already used as value of @GvNIXPattern
        Validate.isTrue(
                patternService.existsControllerMasterPattern(controllerClass,
                        name),
                "Pattern name '".concat(name.getSymbolName())
                        .concat("' not found in values of ")
                        .concat(PATTERN_ANNOTATION.getSimpleTypeName())
                        .concat(" annotation in controller ")
                        .concat(controllerClass.getFullyQualifiedTypeName()));

        // Check if user is setting Detail register
        // if so, this is not supported yet, so abort
        List<StringAttributeValue> patternValues = patternService
                .getControllerMasterPattern(controllerClass);
        if (!patternValues.isEmpty() && type == WebPatternType.register) {

            logger.warning("For pattern name '"
                    .concat(name.getSymbolName())
                    .concat("' you are setting detail as register. Currently gvNIX doesn't support 'Detail register' pattern"));
            return false;
        }

        // Get @RooController annotation to get formbackingObject definition
        AnnotationMetadata controllerAnnotationMetadata = MemberFindingUtils
                .getAnnotationOfType(mutableTypeDetails.getAnnotations(),
                        ROOWEBSCAFFOLD_ANNOTATION);

        // Check if filed exists in formBackingObject entity and is OneToMany or
        // ManyToMany
        JavaType formBackingObject = (JavaType) controllerAnnotationMetadata
                .getAttribute(SCAFF_ANN_ATTR_FORMBOBJ).getValue();
        Validate.notNull(
                patternService.getEntityToManyField(formBackingObject,
                        field.getSymbolName()),
                "Field '".concat(field.getSymbolName())
                        .concat("' not found or is not *ToMany in '")
                        .concat(formBackingObject.getFullyQualifiedTypeName())
                        .concat("' entity."));

        // All checks passed OK.

        // List of pattern to use in annotation
        List<StringAttributeValue> patternList = new ArrayList<StringAttributeValue>();

        // Prepare pattern prefix and previous value
        String patternPrefix = name.getSymbolName().concat(":");
        String patternFieldValue = field.getSymbolName().concat("=")
                .concat(type.name());
        String finalValue = patternPrefix.concat(" ").concat(patternFieldValue);
        int previousValueIndex = -1;

        // Test if the annotation already exists on the target type
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(mutableTypeDetails.getAnnotations(),
                        RELATION_PATTERN_ANNOTATION);

        // Check if @GvNIXRelationPattern
        boolean isAlreadyAnnotated = false;
        if (annotationMetadata != null) {
            // @GvNIXRelationPattern already exists

            // Loads previously registered values into patterList
            // Also identifies previous value of target pattern
            AnnotationAttributeValue<?> previousAnnotationValues = annotationMetadata
                    .getAttribute(REL_PAT_ANN_ATTR_VAL_NAME);

            if (previousAnnotationValues != null) {

                @SuppressWarnings("unchecked")
                List<StringAttributeValue> previousValues = (List<StringAttributeValue>) previousAnnotationValues
                        .getValue();

                if (previousValues != null && !previousValues.isEmpty()) {
                    String strValue;
                    int tmpIndex = 0;
                    for (StringAttributeValue value : previousValues) {
                        strValue = value.getValue();
                        if (strValue.trim().startsWith(patternPrefix)) {
                            // Found previous

                            // Check for duplicates pattern definition
                            Validate.isTrue(
                                    previousValueIndex < 0,
                                    "Duplicate definition for pattern '"
                                            .concat(name.getSymbolName())
                                            .concat("' in ")
                                            .concat(RELATION_PATTERN_ANNOTATION
                                                    .getSimpleTypeName())
                                            .concat(" annotation in ")
                                            .concat(controllerClass
                                                    .getFullyQualifiedTypeName()));

                            // Check if already exist field declaration for
                            // pattern name
                            boolean existsField = existsFieldPatternDeclaration(
                                    strValue, field);
                            Validate.isTrue(
                                    !existsField,
                                    "Field '"
                                            .concat(field.getSymbolName())
                                            .concat("' is already defined for pattern '")
                                            .concat(name.getSymbolName())
                                            .concat("' in controller ")
                                            .concat(controllerClass
                                                    .getFullyQualifiedTypeName()));
                            // Prepare final value
                            finalValue = value.getValue().concat(", ")
                                    .concat(patternFieldValue);
                            // skip from pattern list but store position
                            // to prevent unnecessary changes in file
                            previousValueIndex = tmpIndex;

                        }
                        else {
                            patternList.add(value);
                        }
                        tmpIndex++;
                    }
                }
            }
            isAlreadyAnnotated = true;
        }

        // Adds final pattern value
        if (previousValueIndex >= 0) {
            // Restore previous value position if any
            patternList.add(previousValueIndex, new StringAttributeValue(
                    new JavaSymbolName(ARRAY_ELEM), finalValue));
        }
        else {
            patternList.add(new StringAttributeValue(new JavaSymbolName(
                    ARRAY_ELEM), finalValue));
        }

        // Prepare annotation builder
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                RELATION_PATTERN_ANNOTATION);
        AnnotationAttributeValue<?> annotationValues = new ArrayAttributeValue<StringAttributeValue>(
                REL_PAT_ANN_ATTR_VAL_NAME, patternList);
        annotationBuilder.addAttribute(annotationValues);

        // Add or update annotation to target type
        ClassOrInterfaceTypeDetailsBuilder mutableTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                mutableTypeDetails);
        if (isAlreadyAnnotated) {
            mutableTypeDetailsBuilder.updateTypeAnnotation(
                    annotationBuilder.build(), new HashSet<JavaSymbolName>());
        }
        else {
            mutableTypeDetailsBuilder.addAnnotation(annotationBuilder.build());
        }
        typeManagementService
                .createOrUpdateTypeOnDisk(mutableTypeDetailsBuilder.build());

        annotateFormBackingObjectRelationsControllers(mutableTypeDetails,
                annotationValues);

        return true;
    }

    /**
     * For a given controller, this method inspect the OneToMany and ManyToMany
     * fields in its formBackingObjet and, based on GvNIXRelationsPattern
     * annotationValues, annotates the controllers exposing these entities with
     * the needed GvNIXPattern annotation
     * 
     * @param controllerDetails
     * @param relationsPatternValues
     */
    private void annotateFormBackingObjectRelationsControllers(
            ClassOrInterfaceTypeDetails controllerDetails,
            AnnotationAttributeValue<?> relationsPatternValues) {

        JavaType formBakingObjectType = getFormBakingObject(controllerDetails);

        // Retrieve metadata for the Java source type the annotation is being
        // added to
        String formBackingTypeId = JpaActiveRecordMetadata.createIdentifier(
                formBakingObjectType,
                LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""));
        JpaActiveRecordMetadata formBackingTypeMetadata = (JpaActiveRecordMetadata) metadataService
                .evictAndGet(formBackingTypeId);
        if (formBackingTypeMetadata == null) {
            throw new IllegalArgumentException("Cannot locate Metadata for '"
                    + formBakingObjectType.getFullyQualifiedTypeName() + "'");
        }

        List<FieldMetadata> toManyFields = patternService
                .getEntityToManyFields(formBakingObjectType);
        Map<String, String> fieldsPatternIdAndType = patternService
                .getRelationsPatternFieldAndRelatedPattern(relationsPatternValues);
        if (!fieldsPatternIdAndType.keySet().isEmpty()) {
            for (FieldMetadata field : toManyFields) {
                if (fieldsPatternIdAndType.keySet().contains(
                        field.getFieldName().getSymbolName())
                        && field.getFieldType().isCommonCollectionType()) {
                    AnnotationMetadata relatedPattern = getGvNIXRelatedPattern(fieldsPatternIdAndType
                            .get(field.getFieldName().getSymbolName()));
                    if (relatedPattern != null) {
                        if (!annotateEntityController(field.getFieldType()
                                .getParameters().get(0), relatedPattern)) {
                            // Related entity has not a controller: Alert
                            // message and rollback
                            throw new IllegalArgumentException(
                                    "Cannot add a detail pattern into a field whose entity type has not a controller."
                                            + " Please, run command 'web mvc scaffold' for field entity type first.");
                        }
                    }
                }
            }
        }

    }

    /**
     * Given a Entity this method look for the WebScaffold exposing it and adds
     * {@link GvNIXRelatedPattern} in parameter annotation
     * <p>
     * The entity controller can't be annotated if entity has not a controller.
     * Then this method return false.
     * </p>
     * 
     * @param entity
     * @param annotation
     * @return Entity controller annotated ?
     */
    private boolean annotateEntityController(JavaType entity,
            AnnotationMetadata annotation) {

        // If entity has not a controller, 'success' will be false
        boolean success = false;

        AnnotationMetadata annotationMetadata = null;
        for (ClassOrInterfaceTypeDetails cid : typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROOWEBSCAFFOLD_ANNOTATION)) {
            annotationMetadata = MemberFindingUtils.getAnnotationOfType(
                    cid.getAnnotations(), ROOWEBSCAFFOLD_ANNOTATION);
            if (annotationMetadata != null) {
                AnnotationAttributeValue<?> annotationValues = annotationMetadata
                        .getAttribute(new JavaSymbolName(FORM_BACKING_OBJ));
                if (annotationValues != null) {
                    if (annotationValues.getName().compareTo(
                            new JavaSymbolName(FORM_BACKING_OBJ)) == 0
                            && ((JavaType) annotationValues.getValue())
                                    .getFullyQualifiedTypeName()
                                    .equalsIgnoreCase(
                                            entity.getFullyQualifiedTypeName())) {
                        addOrUpdateGvNIXRelatedPatternToController(
                                cid.getName(), annotation);
                        success = true;
                    }
                }
            }
        }
        return success;
    }

    /**
     * Annotates with GvNIXRelatedPattern (or updates the annotation value) the
     * given controller
     * 
     * @param controllerClass
     * @param annotation Instace of GvNIXPattern annotation metadata
     */
    private void addOrUpdateGvNIXRelatedPatternToController(
            JavaType controllerClass, AnnotationMetadata annotation) {

        ClassOrInterfaceTypeDetails controllerDetails = patternService
                .getControllerTypeDetails(controllerClass);

        // Test if has the @RooWebScaffold
        Validate.notNull(
                MemberFindingUtils.getAnnotationOfType(
                        controllerDetails.getAnnotations(),
                        ROOWEBSCAFFOLD_ANNOTATION),
                controllerClass.getSimpleTypeName().concat(
                        " has not @RooWebScaffold annotation"));

        // All checks passed OK.
        // We don't need to do Setup because it must be done by addPattern call

        // Test if the annotation already exists on the target type
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(controllerDetails.getAnnotations(),
                        RELATED_PATTERN_ANNOTATION);

        // List of pattern to use
        List<StringAttributeValue> patternList = new ArrayList<StringAttributeValue>();

        boolean isAlreadyAnnotated = false;
        if (annotationMetadata != null) {
            // @GvNIXRelatedPattern alredy exists

            // Loads previously registered pattern into patterList
            // Also checks if name is used previously
            AnnotationAttributeValue<?> previousAnnotationValues = annotationMetadata
                    .getAttribute(RELTD_PAT_ANN_ATTR_VAL_NAME);

            if (previousAnnotationValues != null) {

                @SuppressWarnings("unchecked")
                List<StringAttributeValue> previousValues = (List<StringAttributeValue>) previousAnnotationValues
                        .getValue();
                patternList.addAll(previousValues);
            }
            isAlreadyAnnotated = true;
        }

        // We're sure that annotation has set a value since we created this
        // instance
        @SuppressWarnings("unchecked")
        List<StringAttributeValue> newValues = (List<StringAttributeValue>) annotation
                .getAttribute(RELTD_PAT_ANN_ATTR_VAL_NAME).getValue();
        // Check if the same annotation value is already defined avoiding define
        // it twice
        for (StringAttributeValue stringAttributeValue : newValues) {
            if (!patternList.contains(stringAttributeValue)) {
                patternList.add(stringAttributeValue);
            }
        }

        AnnotationAttributeValue<?> gvNIXRelatedPatternValue = new ArrayAttributeValue<StringAttributeValue>(
                RELTD_PAT_ANN_ATTR_VAL_NAME, patternList);

        // TODO: next check must be over if pattern id is already defined in
        // project
        // Assert.isTrue(
        // arePatternsDefinedOnceInController(gvNIXRelatedPatternValue),
        // "Controller ".concat(
        // controllerClass.getFullyQualifiedTypeName()).concat(
        // " has the same pattern defined more than once"));

        // Prepare annotation builder
        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                RELATED_PATTERN_ANNOTATION);
        annotationBuilder.addAttribute(gvNIXRelatedPatternValue);

        // Add or update annotation to target type
        ClassOrInterfaceTypeDetailsBuilder mutableTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                controllerDetails);
        if (isAlreadyAnnotated) {
            mutableTypeDetailsBuilder.updateTypeAnnotation(
                    annotationBuilder.build(), new HashSet<JavaSymbolName>());
        }
        else {
            mutableTypeDetailsBuilder.addAnnotation(annotationBuilder.build());
        }
        typeManagementService
                .createOrUpdateTypeOnDisk(mutableTypeDetailsBuilder.build());

        if (patternService.existsRelatedPatternType(
                WebPatternType.tabular.name(), patternList)
                || patternService.existsRelatedPatternType(
                        WebPatternType.tabular_edit_register.name(),
                        patternList)) {
            annotateTypeWithGvNIXEntityBatch(getFormBakingObject(controllerDetails));
        }
    }

    /**
     * Annotates with GvNIXEntityBatch given JavaType
     * 
     * @param type
     */
    private void annotateTypeWithGvNIXEntityBatch(JavaType type) {

        ClassOrInterfaceTypeDetails typeMutableDetails = typeLocationService
                .getTypeDetails(type);
        AnnotationMetadata annotationMetadata = MemberFindingUtils
                .getAnnotationOfType(typeMutableDetails.getAnnotations(),
                        ENTITYBATCH_ANNOTATION);

        // Annotate type with GvNIXEntityBatch just if is not
        // annotated already. We don't need to update attributes
        if (annotationMetadata == null) {
            // Prepare annotation builder
            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                    ENTITYBATCH_ANNOTATION);
            ClassOrInterfaceTypeDetailsBuilder mutableTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                    typeMutableDetails);
            mutableTypeDetailsBuilder.addAnnotation(annotationBuilder.build());
            typeManagementService
                    .createOrUpdateTypeOnDisk(mutableTypeDetailsBuilder.build());
        }
    }

    /**
     * For given attrValue, returns an instance of GvNIXRelatedPattern
     * annotation <br/>
     * GvNIXRelatedPattern({"pattern_id1=table"})
     * 
     * @param attrValue
     * @return
     */
    private AnnotationMetadata getGvNIXRelatedPattern(String attrValue) {
        List<AnnotationAttributeValue<?>> attributes = new ArrayList<AnnotationAttributeValue<?>>();

        List<StringAttributeValue> patternList = new ArrayList<StringAttributeValue>(
                1);

        patternList.add(new StringAttributeValue(
                new JavaSymbolName(ARRAY_ELEM), attrValue));

        attributes.add(new ArrayAttributeValue<StringAttributeValue>(
                RELTD_PAT_ANN_ATTR_VAL_NAME, patternList));

        AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(
                RELATED_PATTERN_ANNOTATION);
        annotationBuilder.setAttributes(attributes);

        return annotationBuilder.build();
    }

    /**
     * Returns formBackingObject JavaType from the {@link RooWebScaffold}
     * attribute
     * 
     * @param controllerDetails
     * @return
     */
    private JavaType getFormBakingObject(
            ClassOrInterfaceTypeDetails controllerDetails) {
        AnnotationMetadata rooWSacffAnnMdata = MemberFindingUtils
                .getAnnotationOfType(controllerDetails.getAnnotations(),
                        ROOWEBSCAFFOLD_ANNOTATION);

        AnnotationAttributeValue<?> formbakingObjectAttValue = rooWSacffAnnMdata
                .getAttribute(new JavaSymbolName(FORM_BACKING_OBJ));

        JavaType formBakingObjectType = (JavaType) formbakingObjectAttValue
                .getValue();
        Validate.notNull(formBakingObjectType,
                "formBakingObject attribute of RooWebScaffold in "
                        + controllerDetails.getName().getSimpleTypeName()
                        + " must be set");

        return formBakingObjectType;
    }

    private boolean existsFieldPatternDeclaration(String definition,
            JavaSymbolName field) {
        char[] fieldName = field.getSymbolName().toCharArray();
        StringBuilder sb = new StringBuilder((fieldName.length * 3) + 15);
        sb.append("[,: ]");
        for (char ch : fieldName) {
            sb.append('[');
            sb.append(ch);
            sb.append(']');
        }
        sb.append("[ ]*[=]");
        Pattern pattern = Pattern.compile(sb.toString());
        return pattern.matcher(definition).find();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gvnix.web.screen.roo.addon.WebScreenOperations#setup()
     */
    @Override
    public void setup() {
        configService.setup();
        installPatternArtifacts(false);
        modifyLoadScriptsTagx();
        addMessageBoxInLayout();
    }

    /**
     * Updates load-scripts.tagx adding in the right position some elements:
     * <ul>
     * <li><code>spring:url</code> elements for JS and CSS</li>
     * <li><code>link</code> element for CSS</li>
     * <li><code>script</code> element for JS</li>
     * </ul>
     */
    private void modifyLoadScriptsTagx() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        String loadScriptsTagx = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/tags/util/load-scripts.tagx");

        if (!fileManager.exists(loadScriptsTagx)) {
            // load-scripts.tagx doesn't exist, so nothing to do
            return;
        }

        InputStream loadScriptsIs = fileManager.getInputStream(loadScriptsTagx);

        Document loadScriptsXml;
        try {
            loadScriptsXml = XmlUtils.getDocumentBuilder().parse(loadScriptsIs);
        }
        catch (Exception ex) {
            throw new IllegalStateException(
                    "Could not open load-scripts.tagx file", ex);
        }

        Element lsRoot = loadScriptsXml.getDocumentElement();

        Node nextSibiling;

        // spring:url elements
        Element testElement = XmlUtils.findFirstElement(
                "/root/url[@var='pattern_css_url']", lsRoot);
        if (testElement == null) {
            Element urlPatternCss = new XmlElementBuilder("spring:url",
                    loadScriptsXml)
                    .addAttribute(VALUE, "/resources/styles/pattern.css")
                    .addAttribute(VAR, "pattern_css_url").build();
            Element urlQlJs = new XmlElementBuilder("spring:url",
                    loadScriptsXml)
                    .addAttribute(VALUE, "/resources/scripts/quicklinks.js")
                    .addAttribute(VAR, "qljs_url").build();
            // Add i18n messages for quicklinks.js
            List<Element> qlJsI18n = new ArrayList<Element>();
            qlJsI18n.add(new XmlElementBuilder("spring:message", loadScriptsXml)
                    .addAttribute("code", "message_selectrowtodelete_alert")
                    .addAttribute(VAR, "msg_selectrowtodelete_alert")
                    .addAttribute("htmlEscape", "false").build());
            qlJsI18n.add(new XmlElementBuilder("spring:message", loadScriptsXml)
                    .addAttribute("code", "message_selectrowtoupdate_alert")
                    .addAttribute(VAR, "msg_selectrowtoupdate_alert")
                    .addAttribute("htmlEscape", "false").build());
            qlJsI18n.add(new XmlElementBuilder("spring:message", loadScriptsXml)
                    .addAttribute("code", "message_updateonlyonerow_alert")
                    .addAttribute(VAR, "msg_updateonlyonerow_alert")
                    .addAttribute("htmlEscape", "false").build());
            StringBuilder qlJsI18nScriptText = new StringBuilder("<!--\n");
            qlJsI18nScriptText
                    .append("var GVNIX_MSG_SELECT_ROW_TO_DELETE=\"${msg_selectrowtodelete_alert}\";\n");
            qlJsI18nScriptText
                    .append("var GVNIX_MSG_SELECT_ROW_TO_UPDATE=\"${msg_selectrowtoupdate_alert}\";\n");
            qlJsI18nScriptText
                    .append("var GVNIX_MSG_UPDATE_ONLY_ONE_ROW=\"${msg_updateonlyonerow_alert}\";\n");
            qlJsI18nScriptText.append("-->\n");
            Element qlJsI18nScript = new XmlElementBuilder("script",
                    loadScriptsXml).setText(qlJsI18nScriptText.toString())
                    .build();
            List<Element> springUrlElements = XmlUtils.findElements(
                    "/root/url", lsRoot);
            // Element lastSpringUrl = null;
            if (!springUrlElements.isEmpty()) {
                Element lastSpringUrl = springUrlElements.get(springUrlElements
                        .size() - 1);
                if (lastSpringUrl != null) {
                    nextSibiling = lastSpringUrl.getNextSibling()
                            .getNextSibling();
                    lsRoot.insertBefore(urlPatternCss, nextSibiling);
                    lsRoot.insertBefore(urlQlJs, nextSibiling);
                    lsRoot.insertBefore(qlJsI18nScript, nextSibiling);
                    for (Element item : qlJsI18n) {
                        lsRoot.insertBefore(item, qlJsI18nScript);
                    }
                }
            }
            else {
                // Add at the end of the document
                lsRoot.appendChild(urlPatternCss);
                lsRoot.appendChild(urlQlJs);
                for (Element item : qlJsI18n) {
                    lsRoot.appendChild(item);
                }
                lsRoot.appendChild(qlJsI18nScript);
            }
        }

        // pattern.css stylesheet element
        testElement = XmlUtils.findFirstElement(
                "/root/link[@href='${pattern_css_url}']", lsRoot);
        if (testElement == null) {
            Element linkPatternCss = new XmlElementBuilder("link",
                    loadScriptsXml).addAttribute("rel", "stylesheet")
                    .addAttribute("type", "text/css")
                    .addAttribute("media", "screen")
                    .addAttribute("href", "${pattern_css_url}").build();
            linkPatternCss.appendChild(loadScriptsXml
                    .createComment(" required for FF3 and Opera "));
            Node linkTrundraCssNode = XmlUtils.findFirstElement(
                    "/root/link[@href='${tundra_url}']", lsRoot);
            if (linkTrundraCssNode != null) {
                nextSibiling = linkTrundraCssNode.getNextSibling()
                        .getNextSibling();
                lsRoot.insertBefore(linkPatternCss, nextSibiling);
            }
            else {
                // Add ass last link element
                // Element lastLink = null;
                List<Element> linkElements = XmlUtils.findElements(
                        "/root/link", lsRoot);
                if (!linkElements.isEmpty()) {
                    Element lastLink = linkElements
                            .get(linkElements.size() - 1);
                    if (lastLink != null) {
                        nextSibiling = lastLink.getNextSibling()
                                .getNextSibling();
                        lsRoot.insertBefore(linkPatternCss, nextSibiling);
                    }
                }
                else {
                    // Add at the end of document
                    lsRoot.appendChild(linkPatternCss);
                }
            }
        }

        // quicklinks.js script element
        testElement = XmlUtils.findFirstElement(
                "/root/script[@src='${qljs_url}']", lsRoot);
        if (testElement == null) {
            Element scriptQlJs = new XmlElementBuilder("script", loadScriptsXml)
                    .addAttribute("src", "${qljs_url}")
                    .addAttribute("type", "text/javascript").build();
            scriptQlJs.appendChild(loadScriptsXml
                    .createComment(" required for FF3 and Opera "));
            List<Element> scrtiptElements = XmlUtils.findElements(
                    "/root/script", lsRoot);
            // Element lastScript = null;
            if (!scrtiptElements.isEmpty()) {
                Element lastScript = scrtiptElements
                        .get(scrtiptElements.size() - 1);
                if (lastScript != null) {
                    nextSibiling = lastScript.getNextSibling().getNextSibling();
                    lsRoot.insertBefore(scriptQlJs, nextSibiling);
                }
            }
            else {
                // Add at the end of document
                lsRoot.appendChild(scriptQlJs);
            }
        }

        writeToDiskIfNecessary(loadScriptsTagx,
                loadScriptsXml.getDocumentElement());

    }

    /**
     * Decides if write to disk is needed (ie updated or created)<br/>
     * Used for TAGx files
     * 
     * @param filePath
     * @param body
     * @return
     */
    private boolean writeToDiskIfNecessary(String filePath, Element body) {
        // Build a string representation of the JSP
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Transformer transformer = XmlUtils.createIndentingTransformer();
        XmlUtils.writeXml(transformer, byteArrayOutputStream,
                body.getOwnerDocument());
        String viewContent = byteArrayOutputStream.toString();

        // If mutableFile becomes non-null, it means we need to use it to write
        // out the contents of jspContent to the file
        MutableFile mutableFile = null;
        if (fileManager.exists(filePath)) {
            // First verify if the file has even changed
            File newFile = new File(filePath);
            String existing = null;
            try {
                existing = IOUtils.toString(new FileReader(newFile));
            }
            catch (IOException ignoreAndJustOverwriteIt) {
                LOGGER.finest("Problems writing ".concat(newFile
                        .getAbsolutePath()));
            }

            if (!viewContent.equals(existing)) {
                mutableFile = fileManager.updateFile(filePath);
            }
        }
        else {
            mutableFile = fileManager.createFile(filePath);
            Validate.notNull(mutableFile, "Could not create '" + filePath + "'");
        }

        if (mutableFile != null) {
            try {
                // We need to write the file out (it's a new file, or the
                // existing file has different contents)
                InputStream inputStream = null;
                OutputStreamWriter outputStream = null;
                try {
                    inputStream = IOUtils.toInputStream(viewContent);
                    outputStream = new OutputStreamWriter(
                            mutableFile.getOutputStream());
                    IOUtils.copy(inputStream, outputStream);
                }
                finally {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
                // Return and indicate we wrote out the file
                return true;
            }
            catch (IOException ioe) {
                throw new IllegalStateException("Could not output '"
                        + mutableFile.getCanonicalPath() + "'", ioe);
            }
        }

        // A file existed, but it contained the same content, so we return false
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.gvnix.web.screen.roo.addon.WebScreenOperations#updatePattern()
     */
    public void updatePattern() {
        installPatternArtifacts(true);
    }

    /**
     * Installs or updates pattern Artifacts.
     * <ul>
     * <li>images</li>
     * <li>scripts JS</li>
     * <li>CSS</li>
     * <li>TAGx</li>
     * <li>i18n language properties</li>
     * <ul>
     * 
     * @param forceUpdate
     */
    private void installPatternArtifacts(boolean forceUpdate) {
        PathResolver pathResolver = projectOperations.getPathResolver();
        // install pattern images
        // Don't use copyDirectoryContents method with binary files and
        // 'replace' = true: binary files will be corrupted
        if (forceUpdate) {
            OperationUtils.updateDirectoryContents("images/pattern/*.*",
                    pathResolver.getIdentifier(
                            LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                            "/images/pattern"), fileManager, cContext,
                    getClass());
        }
        else {
            copyDirectoryContents("images/pattern/*.*",
                    pathResolver.getIdentifier(
                            LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                            "/images/pattern"), false);
        }
        // install js
        copyDirectoryContents("scripts/*.js", pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""), "/scripts"),
                forceUpdate);
        // install css
        copyDirectoryContents("styles/*.css", pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""), "/styles"),
                forceUpdate);

        // copy util to tags/util
        copyDirectoryContents("tags/util/*.tagx", pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "/WEB-INF/tags/util"), forceUpdate);
        // copy dialog/message to tags/dialog/message
        copyDirectoryContents("tags/dialog/message/*.tagx",
                pathResolver.getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        "/WEB-INF/tags/dialog/message"), forceUpdate);
        // copy pattern to tags/pattern
        copyDirectoryContents("tags/pattern/*.tagx",
                pathResolver.getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        "/WEB-INF/tags/pattern"), forceUpdate);
        copyDirectoryContents("tags/pattern/form/*.tagx",
                pathResolver.getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        "/WEB-INF/tags/pattern/form"), forceUpdate);
        copyDirectoryContents("tags/pattern/form/fields/*.tagx",
                pathResolver.getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                        "/WEB-INF/tags/pattern/form/fields"), forceUpdate);

        addI18nProperties();
    }

    /**
     * Takes properties files (messages_xx.properties) and adds their content to
     * i18n message bundle file in current project
     */
    private void addI18nProperties() {
        // Check if Valencian_Catalan language is supported and add properties
        // if so
        Set<I18n> supportedLanguages = i18nSupport.getSupportedLanguages();
        for (I18n i18n : supportedLanguages) {
            if (i18n.getLocale().equals(new Locale("ca"))) {
                MessageBundleUtils.installI18nMessages(
                        new ValencianCatalanLanguage(), projectOperations,
                        fileManager);
                MessageBundleUtils.addPropertiesToMessageBundle("ca",
                        getClass(), propFileOperations, projectOperations,
                        fileManager);
                break;
            }
        }
        // Add properties to Spanish messageBundle
        MessageBundleUtils.installI18nMessages(new SpanishLanguage(),
                projectOperations, fileManager);
        MessageBundleUtils.addPropertiesToMessageBundle("es", getClass(),
                propFileOperations, projectOperations, fileManager);

        // Add properties to default messageBundle
        MessageBundleUtils.addPropertiesToMessageBundle("en", getClass(),
                propFileOperations, projectOperations, fileManager);
    }

    /**
     * Adds the element dialog:message-box in the right place in default.jspx
     * layout
     */
    private void addMessageBoxInLayout() {
        PathResolver pathResolver = projectOperations.getPathResolver();
        String defaultJspx = pathResolver.getIdentifier(
                LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP, ""),
                "WEB-INF/layouts/default.jspx");

        // TODO: Check if it's necessary to add message-box in home-default.jspx
        // layout (when exists)

        if (!fileManager.exists(defaultJspx)) {
            // layouts/default.jspx doesn't exist, so nothing to do
            return;
        }

        InputStream defulatJspxIs = fileManager.getInputStream(defaultJspx);

        Document defaultJspxXml;
        try {
            defaultJspxXml = XmlUtils.getDocumentBuilder().parse(defulatJspxIs);
        }
        catch (Exception ex) {
            throw new IllegalStateException("Could not open default.jspx file",
                    ex);
        }

        Element lsHtml = defaultJspxXml.getDocumentElement();

        String dialogNsUri = lsHtml.getAttribute("xmlns:dialog");

        if (!dialogNsUri.isEmpty()
                && dialogNsUri
                        .equals("urn:jsptagdir:/WEB-INF/tags/dialog/modal")) {
            // User has applied modal dialog support, so, don't change anything;
            return;
        }

        // Set dialog tag lib as attribute in html element
        lsHtml.setAttribute("xmlns:dialog",
                "urn:jsptagdir:/WEB-INF/tags/dialog/message");

        Element messageBoxElement = DomUtils.findFirstElementByName(
                "dialog:message-box", lsHtml);
        if (messageBoxElement == null) {
            Element divMain = XmlUtils.findFirstElement(
                    "/html/body/div/div[@id='main']", lsHtml);
            Element insertAttributeBodyElement = XmlUtils.findFirstElement(
                    "/html/body/div/div/insertAttribute[@name='body']", lsHtml);
            Element messageBox = new XmlElementBuilder("dialog:message-box",
                    defaultJspxXml).build();
            divMain.insertBefore(messageBox, insertAttributeBodyElement);
        }

        writeToDiskIfNecessary(defaultJspx, defaultJspxXml.getDocumentElement());

    }

    /*
     * (non-Javadoc)
     *
     * @see org.gvnix.web.screen.roo.addon.WebScreenOperations#
     * isControllerEntityActiveRecord(org.springframework.roo.model.JavaType)
     */
    @Override
    public boolean isControllerEntityActiveRecord(JavaType controller) {
        ClassOrInterfaceTypeDetails mutableTypeDetails = patternService
                .getControllerTypeDetails(controller);
        return isControllerEntityActiveRecord(mutableTypeDetails);
    }

    private boolean isControllerEntityActiveRecord(
            ClassOrInterfaceTypeDetails controller) {
        JavaType entityType = getFormBakingObject(controller);

        // Retrieve metadata for the Java source type the annotation is being
        // added to
        String entityTypeId = JpaActiveRecordMetadata.createIdentifier(
                entityType, LogicalPath.getInstance(Path.SRC_MAIN_JAVA, ""));
        JpaActiveRecordMetadata activeRecordMetadata = (JpaActiveRecordMetadata) metadataService
                .get(entityTypeId);

        return activeRecordMetadata != null;
    }
}
