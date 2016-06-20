/*
 * Copyright 2002-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.gvnix.flex.as.classpath.as3parser;

import java.io.File;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.flex.as.classpath.ASMutablePhysicalTypeMetadataProvider;
import org.gvnix.flex.as.classpath.ASPhysicalTypeCategory;
import org.gvnix.flex.as.classpath.ASPhysicalTypeDetails;
import org.gvnix.flex.as.classpath.ASPhysicalTypeIdentifier;
import org.gvnix.flex.as.classpath.ASPhysicalTypeMetadata;
import org.gvnix.flex.as.classpath.details.ASClassOrInterfaceTypeDetails;
import org.gvnix.flex.as.model.ActionScriptType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileEventListener;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Parser-based {@link MetadataProvider} for ActionScript source files.
 * 
 * @author Jeremy Grelle
 */
@Component
@Service
public class As3ParserMetadataProvider implements
        ASMutablePhysicalTypeMetadataProvider, FileEventListener {

    protected final static Logger LOGGER = HandlerUtils
            .getLogger(As3ParserMetadataProvider.class);

    // ------------ OSGi component attributes ----------------
    private BundleContext context;

    private FileManager fileManager;

    private MetadataService metadataService;

    private MetadataDependencyRegistry metadataDependencyRegistry;

    private PathResolver pathResolver;

    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
    }

    public void createPhysicalType(ASPhysicalTypeMetadata toCreate) {
        Validate.notNull(toCreate, "Metadata to create is required");
        ASPhysicalTypeDetails physicalTypeDetails = toCreate
                .getPhysicalTypeDetails();
        Validate.notNull(physicalTypeDetails, "Unable to parse '" + toCreate
                + "'");
        Validate.isInstanceOf(ASClassOrInterfaceTypeDetails.class,
                physicalTypeDetails,
                "This implementation can only create class or interface types");
        ASClassOrInterfaceTypeDetails cit = (ASClassOrInterfaceTypeDetails) physicalTypeDetails;
        String fileIdentifier = toCreate.getPhysicalLocationCanonicalPath();
        As3ParserMutableClassOrInterfaceTypeDetails.createType(
                getFileManager(), cit, fileIdentifier);
    }

    public String findIdentifier(ActionScriptType actionScriptType) {
        Validate.notNull(actionScriptType,
                "ActionScript type to locate is required");
        // TODO Removed for, it's ok ?
        // for (LogicalPath sourcePath : getPathResolver().getFlexSourcePaths())
        // {
        String relativePath = actionScriptType.getFullyQualifiedTypeName()
                .replace('.', File.separatorChar) + ".as";
        String fileIdentifier = getPathResolver().getIdentifier(
                LogicalPath.getInstance(Path.ROOT, ""),
                "src/main/flex/" + relativePath);
        if (getFileManager().exists(fileIdentifier)) {
            // found the file, so use this one
            return ASPhysicalTypeIdentifier.createIdentifier(actionScriptType,
                    "src/main/flex");
        }
        // }
        return null;
    }

    public MetadataItem get(String metadataIdentificationString) {
        Validate.isTrue(
                ASPhysicalTypeIdentifier.isValid(metadataIdentificationString),
                "Metadata identification string '"
                        + metadataIdentificationString
                        + "' is not valid for this metadata provider");
        String fileIdentifier = obtainPathToIdentifier(metadataIdentificationString);
        getMetadataDependencyRegistry().deregisterDependencies(
                metadataIdentificationString);
        if (!getFileManager().exists(fileIdentifier)) {
            // Couldn't find the file, so return null to distinguish from a file
            // that was found but could not be parsed
            return null;
        }
        As3ParserClassMetadata result = new As3ParserClassMetadata(
                getFileManager(), fileIdentifier, metadataIdentificationString,
                getMetadataService(), this);
        if (result.getPhysicalTypeDetails() != null
                && result.getPhysicalTypeDetails() instanceof ASClassOrInterfaceTypeDetails) {
            ASClassOrInterfaceTypeDetails details = (ASClassOrInterfaceTypeDetails) result
                    .getPhysicalTypeDetails();
            if (details.getPhysicalTypeCategory() == ASPhysicalTypeCategory.CLASS
                    && details.getExtendsTypes().size() == 1) {
                // This is a class, and it extends another class

                if (details.getSuperClass() != null) {
                    // We have a dependency on the superclass, and there is
                    // metadata available for the superclass
                    // We won't implement the full MetadataNotificationListener
                    // here, but rely on MetadataService's
                    // fallback
                    // (which is to evict from cache and call get again given
                    // As3ParserMetadataProvider doesn't
                    // implement MetadataNotificationListener, then notify
                    // everyone we've changed)
                    String superclassId = details.getSuperClass()
                            .getDeclaredByMetadataId();
                    getMetadataDependencyRegistry().registerDependency(
                            superclassId, result.getId());
                }
                else {
                    // We have a dependency on the superclass, but no metadata
                    // is available
                    // We're left with no choice but to register for every
                    // physical type change, in the hope we discover
                    // our parent someday (sad, isn't it? :-) )
                    // TODO Removed for, it's ok ?
                    // for (LogicalPath sourcePath :
                    // getPathResolver().getSourcePaths()) {
                    String possibleSuperclass = ASPhysicalTypeIdentifier
                            .createIdentifier(details.getExtendsTypes().get(0),
                                    "src/main/flex");
                    getMetadataDependencyRegistry().registerDependency(
                            possibleSuperclass, result.getId());
                    // }
                }
            }
        }
        return result;
    }

    public String getProvidesType() {
        return ASPhysicalTypeIdentifier.getMetadataIdentiferType();
    }

    public void onFileEvent(FileEvent fileEvent) {
        String fileIdentifier = fileEvent.getFileDetails().getCanonicalPath();

        if (fileIdentifier.endsWith(".as")
                && fileEvent.getOperation() != FileOperation.MONITORING_FINISH) {
            // file is of interest

            // figure out the ActionScriptType this should be
            LogicalPath sourcePath = null;
            // TODO Removed for, it's ok ?
            // for (LogicalPath path : getPathResolver().getFlexSourcePaths()) {
            LogicalPath path = LogicalPath.getInstance(Path.ROOT, "");
            if (new FileDetails(new File(getPathResolver().getRoot(path)), null)
                    .isParentOf(fileIdentifier)) {
                sourcePath = path;
                // break;
            }
            // }
            if (sourcePath == null) {
                // the .as file is not under a source path, so ignore it
                return;
            }
            // determine the ActionScriptType for this file
            String relativePath = getPathResolver().getRelativeSegment(
                    fileIdentifier);
            StringUtils.isNotBlank(relativePath);
            Validate.isTrue(relativePath.startsWith(File.separator),
                    "Relative path unexpectedly dropped the '" + File.separator
                            + "' prefix (received '" + relativePath
                            + "' from '" + fileIdentifier + "'");
            relativePath = relativePath.substring(1);
            Validate.isTrue(relativePath.endsWith(".as"),
                    "The relative path unexpectedly dropped the .as extension for file '"
                            + fileIdentifier + "'");
            relativePath = relativePath.substring(0,
                    relativePath.lastIndexOf(".as"));

            ActionScriptType actionScriptType = new ActionScriptType(
                    relativePath.replace(File.separatorChar, '.'));

            // figure out the PhysicalTypeIdentifier
            String id = ASPhysicalTypeIdentifier.createIdentifier(
                    actionScriptType, "src/main/flex");

            // Now we've worked out the id, we can publish the event in case
            // others were interested
            getMetadataService().evict(id);
            getMetadataDependencyRegistry().notifyDownstream(id);
        }

    }

    /*
     * private FlexPathResolver getPathResolver() { ProjectMetadata
     * projectMetadata = (ProjectMetadata)
     * metadataService.get(ProjectMetadata.getProjectIdentifier());
     * Validate.notNull(projectMetadata, "Project metadata unavailable");
     * PathResolver pathResolver = projectMetadata.getPathResolver();
     * Validate.notNull(pathResolver,
     * "Path resolver unavailable because valid project metadata not currently available"
     * ); Validate.isInstanceOf(FlexPathResolver.class,
     * "Path resolver is of an unexpected type, not appropriate for a Flex project."
     * ); return (FlexPathResolver) pathResolver; }
     */

    private String obtainPathToIdentifier(String physicalTypeIdentifier) {
        Validate.isTrue(
                ASPhysicalTypeIdentifier.isValid(physicalTypeIdentifier),
                "Metadata identification string '" + physicalTypeIdentifier
                        + "' is not valid for this metadata provider");
        LogicalPath path = ASPhysicalTypeIdentifier
                .getPath(physicalTypeIdentifier);
        ActionScriptType type = ASPhysicalTypeIdentifier
                .getActionScriptType(physicalTypeIdentifier);
        String relativePath = type.getFullyQualifiedTypeName().replace('.',
                File.separatorChar)
                + ".as";
        String fileIdentifier = getPathResolver().getIdentifier(path,
                relativePath);
        return fileIdentifier;
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
                LOGGER.warning("Cannot load FileManager on As3ParserMetadataProvider.");
                return null;
            }
        }
        else {
            return fileManager;
        }
    }

    public MetadataService getMetadataService() {
        if (metadataService == null) {
            // Get all Services implement MetadataService interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MetadataService.class.getName(), null);

                for (ServiceReference<?> ref : references) {
                    return (MetadataService) this.context.getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MetadataService on As3ParserMetadataProvider.");
                return null;
            }
        }
        else {
            return metadataService;
        }
    }

    public MetadataDependencyRegistry getMetadataDependencyRegistry() {
        if (metadataDependencyRegistry == null) {
            // Get all Services implement MetadataDependencyRegistry interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                MetadataDependencyRegistry.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (MetadataDependencyRegistry) this.context
                            .getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load MetadataDependencyRegistry on As3ParserMetadataProvider.");
                return null;
            }
        }
        else {
            return metadataDependencyRegistry;
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
                LOGGER.warning("Cannot load PathResolver on As3ParserMetadataProvider.");
                return null;
            }
        }
        else {
            return pathResolver;
        }
    }

}
