package org.springframework.roo.classpath;

import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.ReferenceStrategy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.DefaultPhysicalTypeMetadata;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsBuilder;
import org.springframework.roo.classpath.scanner.MemberDetailsDecorator;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileEventListener;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectOperations;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Monitors for *.java files and produces a {@link PhysicalTypeMetadata} for
 * each, also providing type creation and deleting methods. Prior to 1.2.0, the
 * default implementation of PhysicalTypeMetadataProvider was
 * JavaParserMetadataProvider.
 * 
 * @author Ben Alex
 * @author James Tyrrell
 * @since 1.2.0
 */
@Component
@Service
@References(value = { @Reference(name = "memberHoldingDecorator", strategy = ReferenceStrategy.EVENT, policy = ReferencePolicy.DYNAMIC, referenceInterface = MemberDetailsDecorator.class, cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE) })
public class DefaultPhysicalTypeMetadataProvider implements
        PhysicalTypeMetadataProvider, FileEventListener {
	
	protected final static Logger LOGGER = HandlerUtils.getLogger(DefaultPhysicalTypeMetadataProvider.class);
	
	// ------------ OSGi component attributes ----------------
   	private BundleContext context;
   	
   	protected void activate(final ComponentContext context) {
    	this.context = context.getBundleContext();
    }

    private final SortedSet<MemberDetailsDecorator> decorators = new TreeSet<MemberDetailsDecorator>(
            new Comparator<MemberDetailsDecorator>() {
                public int compare(final MemberDetailsDecorator o1,
                        final MemberDetailsDecorator o2) {
                    return o1.getClass().getName()
                            .compareTo(o2.getClass().getName());
                }
            });

    private FileManager fileManager;
    private MetadataDependencyRegistry metadataDependencyRegistry;
    private MetadataService metadataService;
    private ProjectOperations projectOperations;
    private TypeLocationService typeLocationService;
    private TypeParsingService typeParsingService;

    // Mutex
    private final Object lock = new Object();

    protected void bindMemberHoldingDecorator(
            final MemberDetailsDecorator decorator) {
        synchronized (lock) {
            decorators.add(decorator);
        }
    }

    public MetadataItem get(final String metadataIdentificationString) {
    	
    	if(fileManager == null){
    		fileManager = getFileManager();
    	}
    	
    	Validate.notNull(fileManager, "FileManager is required");
    	
    	if (metadataDependencyRegistry == null){
    		metadataDependencyRegistry = getMetadataDependencyRegistry();
    	}
    	
    	Validate.notNull(metadataDependencyRegistry, "MetadataDependencyRegistry is required");
    	
    	if(projectOperations == null){
    		projectOperations = getProjectOperations();
    	}
    	
    	Validate.notNull(projectOperations, "ProjectOperations is required");
    	
    	if(typeLocationService == null){
    		typeLocationService = getTypeLocationService();
    	}
    	
    	Validate.notNull(typeLocationService, "TypeLocationService is required");
    	
    	if(typeParsingService == null){
    		typeParsingService = getTypeParsingService();
    	}
    	
    	Validate.notNull(typeParsingService, "TypeParsingService is required");
    	
    	
        Validate.isTrue(
                PhysicalTypeIdentifier.isValid(metadataIdentificationString),
                "Metadata id '%s' is not valid for this metadata provider",
                metadataIdentificationString);
        final String canonicalPath = typeLocationService
                .getPhysicalTypeCanonicalPath(metadataIdentificationString);
        if (StringUtils.isBlank(canonicalPath)) {
            return null;
        }
        metadataDependencyRegistry
                .deregisterDependencies(metadataIdentificationString);
        if (!fileManager.exists(canonicalPath)) {
            // Couldn't find the file, so return null to distinguish from a file
            // that was found but could not be parsed
            return null;
        }
        final JavaType javaType = PhysicalTypeIdentifier
                .getJavaType(metadataIdentificationString);
        final ClassOrInterfaceTypeDetails typeDetails = typeParsingService
                .getTypeAtLocation(canonicalPath, metadataIdentificationString,
                        javaType);
        if (typeDetails == null) {
            return null;
        }
        final PhysicalTypeMetadata result = new DefaultPhysicalTypeMetadata(
                metadataIdentificationString, canonicalPath, typeDetails);
        final ClassOrInterfaceTypeDetails details = result
                .getMemberHoldingTypeDetails();
        if (details != null
                && details.getPhysicalTypeCategory() == PhysicalTypeCategory.CLASS
                && details.getExtendsTypes().size() == 1) {
            // This is a class, and it extends another class
            if (details.getSuperclass() != null) {
                // We have a dependency on the superclass, and there is metadata
                // available for the superclass
                // We won't implement the full MetadataNotificationListener
                // here, but rely on MetadataService's fallback
                // (which is to evict from cache and call get again given
                // JavaParserMetadataProvider doesn't implement
                // MetadataNotificationListener, then notify everyone we've
                // changed)
                final String superclassId = details.getSuperclass()
                        .getDeclaredByMetadataId();
                metadataDependencyRegistry.registerDependency(superclassId,
                        result.getId());
            }
            else {
                // We have a dependency on the superclass, but no metadata is
                // available
                // We're left with no choice but to register for every physical
                // type change, in the hope we discover our parent someday
                for (final LogicalPath sourcePath : projectOperations
                        .getPathResolver().getSourcePaths()) {
                    final String possibleSuperclass = PhysicalTypeIdentifier
                            .createIdentifier(details.getExtendsTypes().get(0),
                                    sourcePath);
                    metadataDependencyRegistry.registerDependency(
                            possibleSuperclass, result.getId());
                }
            }
        }
        MemberDetails memberDetails = new MemberDetailsBuilder(
                Arrays.asList(details)).build();
        // Loop until such time as we complete a full loop where no changes are
        // made to the result
        boolean additionalLoopRequired = true;
        while (additionalLoopRequired) {
            additionalLoopRequired = false;
            for (final MemberDetailsDecorator decorator : decorators) {
                final MemberDetails newResult = decorator.decorateTypes(
                        DefaultPhysicalTypeMetadataProvider.class.getName(),
                        memberDetails);
                Validate.isTrue(newResult != null,
                        "Decorator '%s' returned an illegal result", decorator
                                .getClass().getName());
                if (!newResult.equals(memberDetails)) {
                    additionalLoopRequired = true;
                    memberDetails = newResult;
                }
            }
        }

        return new DefaultPhysicalTypeMetadata(metadataIdentificationString,
                canonicalPath, (ClassOrInterfaceTypeDetails) memberDetails
                        .getDetails().get(0));
    }

    public String getProvidesType() {
        return PhysicalTypeIdentifier.getMetadataIdentiferType();
    }

    public void onFileEvent(final FileEvent fileEvent) {
    	
    	if (metadataDependencyRegistry == null){
    		metadataDependencyRegistry = getMetadataDependencyRegistry();
    	}
    	
    	Validate.notNull(metadataDependencyRegistry, "MetadataDependencyRegistry is required");
    	
    	if(metadataService == null){
    		metadataService = getMetadataService();
    	}
    	
    	Validate.notNull(metadataService, "MetadataService is required");
    	
    	if(typeLocationService == null){
    		typeLocationService = getTypeLocationService();
    	}
    	
    	Validate.notNull(typeLocationService, "TypeLocationService is required");
    	
        final String fileIdentifier = fileEvent.getFileDetails()
                .getCanonicalPath();

        // Check to see if file is of interest
        if (fileIdentifier.endsWith(".java")
                && fileEvent.getOperation() != FileOperation.MONITORING_FINISH
                && !fileIdentifier.endsWith("package-info.java")) {
            // Figure out the PhysicalTypeIdentifier
            final String id = typeLocationService
                    .getPhysicalTypeIdentifier(fileIdentifier);
            if (id == null) {
                return;
            }
            // Now we've worked out the id, we can publish the event in case
            // others were interested
            metadataService.evictAndGet(id);
            metadataDependencyRegistry.notifyDownstream(id);
        }
    }

    protected void unbindMemberHoldingDecorator(
            final MemberDetailsDecorator decorator) {
        synchronized (lock) {
            decorators.remove(decorator);
        }
    }
    
    public FileManager getFileManager(){
    	// Get all Services implement FileManager interface
		try {
			ServiceReference<?>[] references = this.context.getAllServiceReferences(FileManager.class.getName(), null);
			
			for(ServiceReference<?> ref : references){
				return (FileManager) this.context.getService(ref);
			}
			
			LOGGER.warning("Cannot load FileManager on DefaultPhysicalTypeMetadataProvider.");
			return null;
			
		} catch (InvalidSyntaxException e) {
			LOGGER.warning("Cannot load FileManager on DefaultPhysicalTypeMetadataProvider.");
			return null;
		}
    }
    
    public MetadataDependencyRegistry getMetadataDependencyRegistry(){
    	// Get all Services implement MetadataDependencyRegistry interface
		try {
			ServiceReference<?>[] references = this.context.getAllServiceReferences(MetadataDependencyRegistry.class.getName(), null);
			
			for(ServiceReference<?> ref : references){
				return (MetadataDependencyRegistry) this.context.getService(ref);
			}
			
			LOGGER.warning("Cannot load MetadataDependencyRegistry on DefaultPhysicalTypeMetadataProvider.");
			return null;
			
		} catch (InvalidSyntaxException e) {
			LOGGER.warning("Cannot load MetadataDependencyRegistry on DefaultPhysicalTypeMetadataProvider.");
			return null;
		}
    }
    
    public MetadataService getMetadataService(){
    	// Get all Services implement MetadataService interface
		try {
			ServiceReference<?>[] references = this.context.getAllServiceReferences(MetadataService.class.getName(), null);
			
			for(ServiceReference<?> ref : references){
				return (MetadataService) this.context.getService(ref);
			}
			
			LOGGER.warning("Cannot load MetadataService on DefaultPhysicalTypeMetadataProvider.");
			return null;
			
		} catch (InvalidSyntaxException e) {
			LOGGER.warning("Cannot load MetadataService on DefaultPhysicalTypeMetadataProvider.");
			return null;
		}
    }
    
    public ProjectOperations getProjectOperations(){
    	// Get all Services implement ProjectOperations interface
		try {
			ServiceReference<?>[] references = this.context.getAllServiceReferences(ProjectOperations.class.getName(), null);
			
			for(ServiceReference<?> ref : references){
				return (ProjectOperations) this.context.getService(ref);
			}
			
			LOGGER.warning("Cannot load ProjectOperations on DefaultPhysicalTypeMetadataProvider.");
			return null;
			
		} catch (InvalidSyntaxException e) {
			LOGGER.warning("Cannot load ProjectOperations on DefaultPhysicalTypeMetadataProvider.");
			return null;
		}
    }
    
    public TypeLocationService getTypeLocationService(){
    	// Get all Services implement TypeLocationService interface
		try {
			ServiceReference<?>[] references = this.context.getAllServiceReferences(TypeLocationService.class.getName(), null);
			
			for(ServiceReference<?> ref : references){
				return (TypeLocationService) this.context.getService(ref);
			}
			
			LOGGER.warning("Cannot load TypeLocationService on DefaultPhysicalTypeMetadataProvider.");
			return null;
			
		} catch (InvalidSyntaxException e) {
			LOGGER.warning("Cannot load TypeLocationService on DefaultPhysicalTypeMetadataProvider.");
			return null;
		}
    }
    
    public TypeParsingService getTypeParsingService(){
    	// Get all Services implement TypeParsingService interface
		try {
			ServiceReference<?>[] references = this.context.getAllServiceReferences(TypeParsingService.class.getName(), null);
			
			for(ServiceReference<?> ref : references){
				return (TypeParsingService) this.context.getService(ref);
			}
			
			LOGGER.warning("Cannot load TypeParsingService on DefaultPhysicalTypeMetadataProvider.");
			return null;
			
		} catch (InvalidSyntaxException e) {
			LOGGER.warning("Cannot load TypeParsingService on DefaultPhysicalTypeMetadataProvider.");
			return null;
		}
    }
    
    
}
