package org.gvnix.addon.jpa.addon.geo.providers.hibernatespatial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.bouncycastle.asn1.gnu.GNUObjectIdentifiers;
import org.gvnix.addon.jpa.addon.JpaOperations;
import org.gvnix.addon.jpa.addon.geo.FieldGeoTypes;
import org.gvnix.addon.jpa.addon.geo.providers.GeoProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.*;
import org.springframework.roo.classpath.details.annotations.*;
import org.springframework.roo.classpath.operations.jsr303.FieldDetails;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.ReservedWords;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="http://www.disid.com">DISID Corporation S.L.</a> made for <a
 *         href="http://www.dgti.gva.es">General Directorate for Information
 *         Technologies (DGTI)</a>
 */

@Component
@Service
public class HibernateSpatialGeoProvider implements GeoProvider {

    private static final JavaType JPA_ACTIVE_RECORD_ANNOTATION = new JavaType(
            RooJpaActiveRecord.class);

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    private FileManager fileManager;

    private PathResolver pathResolver;

    private TypeLocationService typeLocationService;

    private TypeManagementService typeManagementService;

    private ProjectOperations projectOperations;

    private JpaOperations jpaOperations;

    private MemberDetailsScanner memberDetailsScanner;

    /**
     * DECLARING CONSTANTS
     */

    public static final String NAME = "HIBERNATE_SPATIAL";

    public static final String DESCRIPTION = "Use HibernateSpatial in your project";

    private static final Logger LOGGER = Logger
            .getLogger(HibernateSpatialGeoProvider.class.getName());

    private static final JavaType HIBERNATE_TYPE_ANNOTATION = new JavaType(
            "org.hibernate.annotations.Type");

    private static final JavaType GVNIX_JPA_GEO_HIBERNATESPATIAL_SRS_ANNOTATION = new JavaType(
            "org.gvnix.jpa.geo.hibernatespatial.util.SRS");

    private static final JavaType GVNIX_ENTITY_MAP_LAYER_ANNOTATION = new JavaType(
            "org.gvnix.addon.jpa.annotations.geo.GvNIXEntityMapLayer");

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    /**
     * If HIBERNATE is setted as persistence provider, the command will be
     * available
     */
    @Override
    public boolean isAvailablePersistence(FileManager fileManager,
            PathResolver pathResolver) {
        return HibernateSpatialGeoUtils.isHibernateProviderPersistence(
                getFileManager(), getPathResolver());
    }

    /**
     * This method checks if field geo is available to execute
     */
    @Override
    public boolean isGeoPersistenceInstalled(FileManager fileManager,
            PathResolver pathResolver) {
        return HibernateSpatialGeoUtils.isHibernateSpatialPersistenceInstalled(
                getFileManager(), getPathResolver(), getClass());
    }

    /**
     * This method configure your project to works using hibernate spatial
     */
    @Override
    public void setup() {
        // Setup gvNIX JPA
        getJpaOperations().setup();
        // Updating Persistence dialect
        updatePersistenceDialect();
        // Adding hibernate-spatial dependencies
        addHibernateSpatialDependencies();
    }

    /**
     * This method adds new file to the specified Entity.
     * 
     * @param JavaSymbolName
     * @param fieldGeoTypes
     * @param entity
     * 
     */
    @Override
    public void addField(JavaSymbolName fieldName, FieldGeoTypes fieldGeoType,
            JavaType entity, int srs) {
        final ClassOrInterfaceTypeDetails cid = getTypeLocationService()
                .getTypeDetails(entity);
        final String physicalTypeIdentifier = cid.getDeclaredByMetadataId();

        // Getting fieldType and fieldDetails
        JavaType fieldType = new JavaType(fieldGeoType.toString());
        FieldDetails fieldDetails = new FieldDetails(physicalTypeIdentifier,
                fieldType, fieldName);

        // Checking not reserved words on fieldName
        ReservedWords
                .verifyReservedWordsNotPresent(fieldDetails.getFieldName());

        // Generating package-info.java if necessary
        JavaPackage entityPackage = entity.getPackage();
        HibernateSpatialGeoUtils.generatePackageInfoIfNotExists(entityPackage,
                getPathResolver(), getFileManager());

        // Adding Annotation @Type
        List<AnnotationMetadataBuilder> fieldAnnotations = new ArrayList<AnnotationMetadataBuilder>();
        AnnotationMetadataBuilder typeAnnotation = new AnnotationMetadataBuilder(
                HIBERNATE_TYPE_ANNOTATION);
        typeAnnotation.addStringAttribute("type",
                "org.hibernate.spatial.GeometryType");
        fieldAnnotations.add(typeAnnotation);

        // Adding Annotation @SRS (if needed)
        if (srs != 0) {
            AnnotationMetadataBuilder srsAnnotation = new AnnotationMetadataBuilder(
                    GVNIX_JPA_GEO_HIBERNATESPATIAL_SRS_ANNOTATION);
            srsAnnotation.addIntegerAttribute("value", srs);
            fieldAnnotations.add(srsAnnotation);
        }

        fieldDetails.setAnnotations(fieldAnnotations);

        // Adding Modifier
        fieldDetails.setModifiers(Modifier.PRIVATE);

        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                fieldDetails);

        // Adding field to entity
        getTypeManagementService().addField(fieldBuilder.build());

    }

    /**
     * This method checks all Entities with GEO fields and annotate it with @GvNIXEntityMapLayer
     */
    @Override
    public void addFinderGeoAll() {
        // Getting all entities annotated with @RooJpaActiveRecord
        Set<ClassOrInterfaceTypeDetails> entities = getTypeLocationService()
                .findClassesOrInterfaceDetailsWithAnnotation(
                        JPA_ACTIVE_RECORD_ANNOTATION);

        for (ClassOrInterfaceTypeDetails entity : entities) {
            MemberDetails details = getMemberDetailsScanner().getMemberDetails(
                    getClass().getName(), entity);
            annotateWithAllGeoFinders(entity, details.getFields(),
                    entity.getType(), true);
        }
    }

    /**
     * This method add @GvNIXEntityMapLayer annotation to selected Entity
     */
    @Override
    public void addFinderGeoAdd(JavaType entity, JavaSymbolName fieldName) {
        // Validating not null entity
        Validate.notNull(entity, "The entity specified, '%s', doesn't exist",
                entity);

        // Validate @RooJpaActiveRecord annotation
        final ClassOrInterfaceTypeDetails entityDetails = getTypeLocationService()
                .getTypeDetails(entity);

        AnnotationMetadata activeRecordAnnotation = entityDetails
                .getAnnotation(JPA_ACTIVE_RECORD_ANNOTATION);

        Validate.notNull(
                activeRecordAnnotation,
                String.format(
                        "The entity specified, %s doesn't have @RooJpaActiveRecord annotation",
                        entity));

        // Get all entity details for finding fields
        FieldMetadata field = null;
        MemberDetails details = getMemberDetailsScanner().getMemberDetails(
                getClass().getName(), entityDetails);
        List<FieldMetadata> fields = details.getFields();

        // Check if user provided field for finder
        if (fieldName != null) {
            // Check if field exists in entity
            for (FieldMetadata entityField : fields) {
                if (entityField.getFieldName().equals(fieldName)) {
                    field = entityField;
                    break;
                }
            }
            Validate.notNull(
                    field,
                    "The field specified, '%s', doesn't exist in the entity '%s'",
                    fieldName, entity.getSimpleTypeName());

            // Getting field type to get package
            JavaType fieldType = field.getFieldType();
            JavaPackage fieldPackage = fieldType.getPackage();

            if (fieldPackage.toString().equals("com.vividsolutions.jts.geom")) {
                // Check if entity already has @GvNIXEntityMapLayer and add new
                // finder
                AnnotationMetadata mapLayerAnnotation = entityDetails
                        .getAnnotation(GvNIXEntityMapLayerValues.ENTITY_MAP_LAYER_ANNOTATION);
                if (mapLayerAnnotation != null
                        && mapLayerAnnotation.getAttribute("finders") != null) {
                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    // Get annotation attribute values and store them in ordered
                    // Set
                    ArrayAttributeValue<StringAttributeValue> oldAttributesArray = (ArrayAttributeValue) mapLayerAnnotation
                            .getAttribute("finders");
                    final Set<StringAttributeValue> attributes = new LinkedHashSet<StringAttributeValue>();
                    if (oldAttributesArray.getValue() != null) {
                        attributes.addAll(oldAttributesArray.getValue());
                    }

                    // Create new attribute value
                    final StringAttributeValue fieldAttributeValue = new StringAttributeValue(
                            new JavaSymbolName("__ARRAY_ELEMENT__"),
                            fieldName.toString());

                    // Add new value to Set
                    if (!attributes.contains(fieldAttributeValue)) {
                        attributes.add(fieldAttributeValue);
                    }
                    else {
                        throw new IllegalArgumentException(
                                String.format(
                                        "Finder for field %s was already included in the entity.",
                                        fieldName));
                    }

                    // Convert LinkedHashSet into List
                    List<StringAttributeValue> attributesList = new ArrayList<StringAttributeValue>(
                            attributes);

                    // Create annotation builder
                    AnnotationMetadataBuilder mapLayerAnnotationBuilder = new AnnotationMetadataBuilder(
                            mapLayerAnnotation);

                    addEntityMapLayerAnnotation(entityDetails, attributesList,
                            mapLayerAnnotationBuilder);
                }
                else {
                    // Create attributes list
                    List<StringAttributeValue> attributesList = new ArrayList<StringAttributeValue>();

                    // Add finder to attributes list
                    attributesList.add(new StringAttributeValue(
                            new JavaSymbolName("__ARRAY_ELEMENT__"), fieldName
                                    .toString()));

                    // Create annotation builder
                    AnnotationMetadataBuilder mapLayerAnnotBuilder = new AnnotationMetadataBuilder(
                            new JavaType(
                                    "org.gvnix.addon.jpa.annotations.geo.GvNIXEntityMapLayer"));

                    // Annotate entity with new annotation
                    addEntityMapLayerAnnotation(entityDetails, attributesList,
                            mapLayerAnnotBuilder);
                }
            }
            else {
                throw new IllegalStateException(
                        String.format(
                                "Couldn't add finder, the field '%s' is not a geo field",
                                fieldName));
            }

        }
        else {
            // Annotate entity with all geo finders

            annotateWithAllGeoFinders(entityDetails, fields, entity, false);
        }

    }

    /**
     * Annotate an entity with @GvNIXEntityMapLayer with all his geo finders
     * 
     * @param entityDetails
     * @param fields
     */
    private void annotateWithAllGeoFinders(
            final ClassOrInterfaceTypeDetails entityDetails,
            List<FieldMetadata> fields, JavaType entity,
            boolean ignoreExistingAnnotation) {
        // Create attributes list
        List<StringAttributeValue> attributesList = new ArrayList<StringAttributeValue>();
        AnnotationMetadataBuilder mapLayerAnnotationBuilder = null;
        ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder(
                entityDetails);

        if (entityDetails.getAnnotation(GVNIX_ENTITY_MAP_LAYER_ANNOTATION) != null) {
            if (ignoreExistingAnnotation == false) {
                mapLayerAnnotationBuilder = new AnnotationMetadataBuilder(
                        entityDetails
                                .getAnnotation(GVNIX_ENTITY_MAP_LAYER_ANNOTATION));

                Set<JavaSymbolName> attributesToDelete = new LinkedHashSet<JavaSymbolName>();
                attributesToDelete.add(new JavaSymbolName("finders"));
                // Annotate the entity with void annotation
                builder.updateTypeAnnotation(mapLayerAnnotationBuilder.build(),
                        attributesToDelete);
                getTypeManagementService().createOrUpdateTypeOnDisk(
                        builder.build());
            }
        }
        else {
            boolean hasGeoFields = false;
            // Check entity for its geo fields
            for (FieldMetadata entityField : fields) {

                // Getting field type to get package
                JavaType fieldType = entityField.getFieldType();
                JavaPackage fieldPackage = fieldType.getPackage();

                // If entity field is geo field, add it to attributes list for
                // including in annotation
                if (fieldPackage.toString().equals(
                        "com.vividsolutions.jts.geom")) {
                    hasGeoFields = true;
                    break;
                }
            }

            // If entity has geo fields, annotate it with void annotation
            if (hasGeoFields) {
                mapLayerAnnotationBuilder = new AnnotationMetadataBuilder(
                        GVNIX_ENTITY_MAP_LAYER_ANNOTATION);

                // Annotate the entity with void annotation
                builder.addAnnotation(mapLayerAnnotationBuilder.build());
                getTypeManagementService().createOrUpdateTypeOnDisk(
                        builder.build());
            }
            else {
                LOGGER.info(String
                        .format("The entity specified, %s doesn't have geo fields. Use \"field geo\" command to add new geo fields on current entity",
                                entity.getSimpleTypeName()));
            }
        }
    }

    /**
     * This method adds hibernate spatial dependencies and repositories to
     * pom.xml
     */
    public void addHibernateSpatialDependencies() {
        // Parse the configuration.xml file
        final Element configuration = XmlUtils.getConfiguration(getClass());
        // Add POM Repositories
        HibernateSpatialGeoUtils.updateRepositories(configuration,
                "/configuration/repositories/repository",
                getProjectOperations());
        // Add POM dependencies
        HibernateSpatialGeoUtils.updateDependencies(configuration,
                "/configuration/dependencies/dependency",
                getProjectOperations());
    }

    /**
     * This method update Pesistence Dialect to use the required one to the
     * selected database.
     * 
     */
    public void updatePersistenceDialect() {
        boolean runTime = false;
        // persistence.xml file
        final String persistenceFile = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES, "META-INF/persistence.xml");
        // if persistence.xml doesn't exists show a WARNING
        if (getFileManager().exists(persistenceFile)) {
            // Getting document
            final Document persistenceXmlDocument = XmlUtils
                    .readXml(getFileManager().getInputStream(persistenceFile));
            final Element persistenceElement = persistenceXmlDocument
                    .getDocumentElement();
            // Getting all persistence-unit
            NodeList nodes = persistenceElement.getChildNodes();
            // Saving in variables, which nodes could be changed
            int totalModified = 0;
            for (int i = 0; i < nodes.getLength(); i++) {
                Node persistenceUnit = nodes.item(i);
                // Get all items of current persistence-unit
                NodeList childNodes = persistenceUnit.getChildNodes();
                for (int x = 0; x < childNodes.getLength(); x++) {
                    Node childNode = childNodes.item(x);
                    String nodeName = childNode.getNodeName();
                    if ("properties".equals(nodeName)) {
                        // Getting all properties
                        NodeList properties = childNode.getChildNodes();
                        for (int y = 0; y < properties.getLength(); y++) {
                            Node property = properties.item(y);
                            // Getting attribute properties
                            NamedNodeMap attributes = property.getAttributes();
                            if (attributes != null) {
                                Node propertyName = attributes
                                        .getNamedItem("name");
                                String propertyNameValue = propertyName
                                        .getNodeValue();
                                if ("hibernate.dialect"
                                        .equals(propertyNameValue)) {
                                    Node propertyValue = attributes
                                            .getNamedItem("value");

                                    String value = propertyValue.getNodeValue();
                                    // If is replaced on runtime,
                                    // we alert to the user to change manually
                                    if (value.startsWith("${")) {
                                        runTime = true;
                                        showRuntimeMessage(value);
                                    }
                                    else {
                                        final Element configuration = XmlUtils
                                                .getConfiguration(getClass());
                                        if (!HibernateSpatialGeoUtils
                                                .isGeoDialect(configuration,
                                                        value)) {
                                            // Transform current Dialect to
                                            // valid
                                            // GEO dialect depens of the
                                            // selected
                                            // Database
                                            // Parse the configuration.xml file
                                            String geoDialect = HibernateSpatialGeoUtils
                                                    .convertToGeoDialect(
                                                            configuration,
                                                            value);
                                            // If geo Dialect exists, modify
                                            // value
                                            // with
                                            // the
                                            // valid GEO dialect
                                            if (geoDialect != null) {
                                                propertyValue
                                                        .setNodeValue(geoDialect);
                                                totalModified++;
                                            }
                                        }
                                        else {
                                            // If is geo dialect, mark as
                                            // modified
                                            totalModified++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (totalModified != 0) {
                getFileManager().createOrUpdateTextFileIfRequired(
                        persistenceFile,
                        XmlUtils.nodeToString(persistenceXmlDocument), false);

                // Showing WARNING informing that if you install a different
                // persistence, you must to execute this
                // command again
                LOGGER.log(
                        Level.INFO,
                        "WARNING: If you install a new persistence, you must to execute 'jpa geo setup' again to modify Persistence Dialects.");
            }
            else if (!runTime) {
                showRuntimeMessage("");
            }

        }
        else {
            throw new RuntimeException(
                    "ERROR: Error getting persistence.xml file");
        }
    }

    /**
     * This method creates types.xml file into src/main/resources/*
     * 
     * TODO: Improve <!ENTITY declaration on DOCTYPE
     * 
     * @param entity
     */
    private void addTypesXmlFile(JavaType entity) {
        // Getting current entity package
        String entityPackage = entity.getPackage()
                .getFullyQualifiedPackageName();
        String entityPackageFolder = entityPackage.replaceAll("[.]", "/");

        // Setting types.xml location using entity package
        final String typesXmlPath = getPathResolver().getFocusedIdentifier(
                Path.SRC_MAIN_RESOURCES,
                String.format("/%s/types.xml", entityPackageFolder));

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = FileUtils.getInputStream(getClass(), "types.xml");
            if (!getFileManager().exists(typesXmlPath)) {
                outputStream = getFileManager().createFile(typesXmlPath)
                        .getOutputStream();
            }
            if (outputStream != null) {
                IOUtils.copy(inputStream, outputStream);
            }
        }
        catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            if (outputStream != null) {
                IOUtils.closeQuietly(outputStream);
            }

        }
        // Modifying created file
        if (outputStream != null) {
            PrintWriter writer = new PrintWriter(outputStream);
            writer.println(" <!DOCTYPE hibernate-mapping PUBLIC");
            writer.println("\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"");
            writer.println("\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\" [");
            writer.println(String.format(
                    "<!ENTITY types SYSTEM \"classpath://%s/types.xml\">",
                    entityPackageFolder));
            writer.println("]>");
            writer.println("");
            writer.println(String.format("<hibernate-mapping package=\"%s\">",
                    entityPackage));
            writer.println("<typedef name=\"geometry\" class=\"org.hibernate.spatial.GeometryType\"/>");
            writer.println("</hibernate-mapping>");
            writer.flush();
            writer.close();

        }
    }

    /**
     * Adds annotation to a existing entity
     * 
     * @param cid
     * @param attributesList
     * @param annotationBuilder
     */
    private void addEntityMapLayerAnnotation(
            final ClassOrInterfaceTypeDetails cid,
            List<StringAttributeValue> attributesList,
            AnnotationMetadataBuilder annotationBuilder) {
        // Add attributes list to new Attributes array
        ArrayAttributeValue<StringAttributeValue> newAttributesArray = new ArrayAttributeValue<StringAttributeValue>(
                new JavaSymbolName("finders"), attributesList);

        // Create builder to edit annotation
        ClassOrInterfaceTypeDetailsBuilder builder = new ClassOrInterfaceTypeDetailsBuilder(
                cid);

        // Build annotation
        annotationBuilder.addAttribute(newAttributesArray);
        annotationBuilder.build();

        // Replace existing annotation with new one
        builder.updateTypeAnnotation(annotationBuilder);

        // Save changes to disk
        getTypeManagementService().createOrUpdateTypeOnDisk(builder.build());
    }

    /**
     * Method to show which possibilities has the developer to implement new
     * dialect
     * 
     * @param value
     */
    public void showRuntimeMessage(String value) {

        if (StringUtils.isBlank(value)) {
            LOGGER.log(
                    Level.INFO,
                    "There's not any valid database to apply GEO persistence support. GEO is only supported for POSTGRES, ORACLE, MYSQL and MSSQL databases. You must change it following the next instructions:");
        }
        else {
            LOGGER.log(
                    Level.INFO,
                    String.format(
                            "Cannot replace '%s' on 'src/main/resources/META-INF/persistence.xml' with a valid Hibernate Spatial Dialect. You must change it manually following the next instructions:",
                            value));
        }

        LOGGER.log(Level.INFO, "");
        LOGGER.log(Level.INFO,
                "Replace your current dialect with the correct one: ");
        LOGGER.log(Level.INFO, "");
        LOGGER.log(
                Level.INFO,
                "org.hibernate.dialect.H2Dialect ==> org.hibernate.spatial.dialect.h2geodb.GeoDBDialect");
        LOGGER.log(
                Level.INFO,
                "org.hibernate.dialect.PostgreSQLDialect ==> org.hibernate.spatial.dialect.postgis.PostgisDialect");
        LOGGER.log(
                Level.INFO,
                "org.hibernate.dialect.MySQLDialect ==> org.hibernate.spatial.dialect.mysql.MySQLSpatialDialect");
        LOGGER.log(
                Level.INFO,
                "org.hibernate.dialect.MySQL5Dialect ==> org.hibernate.spatial.dialect.mysql.MySQLSpatialDialect");
        LOGGER.log(
                Level.INFO,
                "org.hibernate.dialect.MySQLInnoDBDialect ==> org.hibernate.spatial.dialect.mysql.MySQLSpatialInnoDBDialect");
        LOGGER.log(
                Level.INFO,
                "org.hibernate.dialect.MySQL5InnoDBDialect ==> org.hibernate.spatial.dialect.mysql.MySQLSpatialInnoDBDialect");
        LOGGER.log(
                Level.INFO,
                "org.hibernate.dialect.MySQL5DBDialect ==> org.hibernate.spatial.dialect.mysql.MySQLSpatial56Dialect");
        LOGGER.log(
                Level.INFO,
                "org.hibernate.dialect.MySQLSpatial56Dialect ==> org.hibernate.spatial.dialect.mysql.MySQLSpatial5InnoDBDialect");
        LOGGER.log(
                Level.INFO,
                "org.hibernate.dialect.Oracle10gDialect ==> org.hibernate.spatial.dialect.oracle.OracleSpatial10gDialect");
        LOGGER.log(
                Level.INFO,
                "org.hibernate.dialect.OracleDialect ==> org.hibernate.spatial.dialect.oracle.OracleSpatial10gDialect");
        LOGGER.log(
                Level.INFO,
                "org.hibernate.dialect.SQLServerDialect ==> org.hibernate.spatial.dialect.SQLServer2008Dialect");
    }

    /**
     * PROVIDER CONFIGURATION METHODS
     */

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public FileManager getFileManager() {
        if (fileManager == null) {
            // Get all Services implement FileManager interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(FileManager.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (FileManager) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load FileManager on HibernateSpatialGeoProvider.");
                return null;
            }
        }
        else {
            return fileManager;
        }

    }

    public PathResolver getPathResolver() {
        if (pathResolver == null) {
            // Get all Services implement PathResolver interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(PathResolver.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (PathResolver) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load PathResolver on HibernateSpatialGeoProvider.");
                return null;
            }
        }
        else {
            return pathResolver;
        }

    }

    public TypeLocationService getTypeLocationService() {
        if (typeLocationService == null) {
            // Get all Services implement TypeLocationService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                TypeLocationService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (TypeLocationService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load TypeLocationService on HibernateSpatialGeoProvider.");
                return null;
            }
        }
        else {
            return typeLocationService;
        }

    }

    public TypeManagementService getTypeManagementService() {
        if (typeManagementService == null) {
            // Get all Services implement TypeManagementService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                TypeManagementService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (TypeManagementService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load TypeManagementService on HibernateSpatialGeoProvider.");
                return null;
            }
        }
        else {
            return typeManagementService;
        }

    }

    public ProjectOperations getProjectOperations() {
        if (projectOperations == null) {
            // Get all Services implement ProjectOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                ProjectOperations.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (ProjectOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load ProjectOperations on HibernateSpatialGeoProvider.");
                return null;
            }
        }
        else {
            return projectOperations;
        }

    }

    public JpaOperations getJpaOperations() {
        if (jpaOperations == null) {
            // Get all Services implement ProjectOperations interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(JpaOperations.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (JpaOperations) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load gvnix JpaOperations on HibernateSpatialGeoProvider.");
                return null;
            }
        }
        else {
            return jpaOperations;
        }

    }

    /**
     * Lazy provides a MemberDetailsScanner.
     * 
     * @return MemberDetailsScanner
     */
    private MemberDetailsScanner getMemberDetailsScanner() {
        if (memberDetailsScanner == null) {
            // Get all Services implement MemberDetailsScanner interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MemberDetailsScanner.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (MemberDetailsScanner) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MemberDetailsScanner on JpaGeoOperationsImpl"
                        .concat(getClass().getSimpleName()));
                return null;
            }
        }
        else {
            return memberDetailsScanner;
        }
    }
}
