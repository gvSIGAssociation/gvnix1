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
package org.gvnix.addon.jpa.addon.audit;

import static org.springframework.roo.classpath.customdata.CustomDataKeys.PERSISTENT_TYPE;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.gvnix.addon.jpa.addon.entitylistener.JpaOrmEntityListenerRegistry;
import org.gvnix.addon.jpa.annotations.audit.GvNIXJpaAudit;
import org.gvnix.addon.jpa.annotations.audit.GvNIXJpaAuditListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ItdTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.itd.AbstractMemberDiscoveringItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Provides {@link JpaAuditListenerMetadata}. Prepares all required information
 * to construct a new instance of {@link JpaAuditListenerMetadata}. Also
 * register metadata dependencies.
 * 
 * @author gvNIX Team
 * @since 1.3.0
 */
@Component
@Service
public final class JpaAuditListenerMetadataProvider extends
        AbstractMemberDiscoveringItdMetadataProvider {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(JpaAuditListenerMetadataProvider.class);

    private JpaOrmEntityListenerRegistry entityListenerRegistry;

    private JpaAuditOperationsMetadata operations;

    private Map<JavaType, String> entityToAuditMidMap = new HashMap<JavaType, String>();

    /**
     * The activate method for this OSGi component, this will be called by the
     * OSGi container upon bundle activation (result of the 'addon install'
     * command)
     * 
     * @param context the component context can be used to get access to the
     *        OSGi container (ie find out if certain bundles are active)
     */
    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
        getMetadataDependencyRegistry().registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        getEntityListenerRegistry().registerListenerMetadata(getProvidesType());
        addMetadataTrigger(new JavaType(GvNIXJpaAuditListener.class.getName()));
    }

    /**
     * The deactivate method for this OSGi component, this will be called by the
     * OSGi container upon bundle deactivation (result of the 'addon uninstall'
     * command)
     * 
     * @param context the component context can be used to get access to the
     *        OSGi container (ie find out if certain bundles are active)
     */
    protected void deactivate(ComponentContext context) {
        getMetadataDependencyRegistry().deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        getEntityListenerRegistry().deregisterListenerMetadata(
                getProvidesType());
        removeMetadataTrigger(new JavaType(
                GvNIXJpaAuditListener.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        // Get annotations values
        final JpaAuditListenerAnnotationValues annotationValues = new JpaAuditListenerAnnotationValues(
                governorPhysicalTypeMetadata);

        LogicalPath path = JpaAuditListenerMetadata
                .getPath(metadataIdentificationString);

        JavaType userService = getOperations().getUserServiceType();

        if (userService == null) {
            // No user type defined
            return null;
        }

        String userServiceId = JpaAuditUserServiceMetadata.createIdentifier(
                userService, path);

        JpaAuditUserServiceMetadata userServiceMetadata = (JpaAuditUserServiceMetadata) getMetadataService()
                .get(userServiceId);

        if (userServiceMetadata == null) {
            // No user type: do nothing
            return null;
        }

        // get target entity class details
        final JavaType targetEntity = annotationValues.getEntity();
        if (!annotationValues.isAnnotationFound() || targetEntity == null) {
            return null;
        }
        final MemberDetails targetEntityMemberDetails = getMemberDetails(targetEntity);
        if (targetEntityMemberDetails == null) {
            return null;
        }

        // Check than target entity class is a entity
        final MemberHoldingTypeDetails tarMemHoldTDetails = MemberFindingUtils
                .getMostConcreteMemberHoldingTypeDetailsWithTag(
                        targetEntityMemberDetails, PERSISTENT_TYPE);
        if (tarMemHoldTDetails == null) {
            return null;
        }

        // get target entity class MID and Logical path
        final String domainTypeMid = getTypeLocationService()
                .getPhysicalTypeIdentifier(targetEntity);
        if (domainTypeMid == null) {
            return null;
        }

        // Create MID to get JpaAudit Metadata info for target entity
        String auditMetadataKey = JpaAuditMetadata.createIdentifier(
                targetEntity, path);

        // get JpaAudit metadata of target entity
        JpaAuditMetadata auditMetadata = (JpaAuditMetadata) getMetadataService()
                .get(auditMetadataKey);

        if (auditMetadata == null) {
            LOGGER.severe(String.format(
                    "%s: Can't found %s annotation on related entity %s",
                    aspectName, GvNIXJpaAudit.class.getSimpleName(),
                    targetEntity));
            return null;
        }

        // register downstream dependency (JpaAudit --> JpaAuditListener)
        getMetadataDependencyRegistry().registerDependency(auditMetadataKey,
                metadataIdentificationString);

        // Generate metadata
        return new JpaAuditListenerMetadata(metadataIdentificationString,
                aspectName, governorPhysicalTypeMetadata, annotationValues,
                targetEntity, auditMetadata, userServiceMetadata.userType(),
                userService);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXJpaAuditListener.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXJpaAuditListener";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = JpaAuditListenerMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = JpaAuditListenerMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return JpaAuditListenerMetadata.createIdentifier(javaType, path);
    }

    public String getProvidesType() {
        return JpaAuditListenerMetadata.getMetadataIdentiferType();
    }

    @Override
    protected String getLocalMidToRequest(final ItdTypeDetails itdTypeDetails) {
        final JavaType governor = itdTypeDetails.getName();

        // If the governor is a form backing object, refresh its local metadata
        final String localMid = entityToAuditMidMap.get(governor);
        if (localMid != null) {
            return localMid;
        }

        // If the governor is a layer component that manages a form backing
        // object, refresh that object's local metadata
        return getRelatedEntityComponent(governor);
    }

    /**
     * If the given governor is a layer component (service, repository, etc.)
     * that manages an entity for which we maintain web scaffold metadata,
     * returns the ID of that metadata, otherwise returns <code>null</code>.
     * TODO doesn't handle the case where the governor is a component that
     * manages multiple entities, as it always returns the MID for the first
     * entity found (in annotation order) for which we provide web metadata. We
     * would need to enhance
     * {@link AbstractMemberDiscoveringItdMetadataProvider#getLocalMidToRequest}
     * to return a list of MIDs, rather than only one.
     * 
     * @param governor the governor to check (required)
     * @return see above
     */
    private String getRelatedEntityComponent(final JavaType governor) {
        final ClassOrInterfaceTypeDetails governorTypeDetails = getTypeLocationService()
                .getTypeDetails(governor);
        if (governorTypeDetails != null) {
            for (final JavaType type : governorTypeDetails.getLayerEntities()) {
                final String localMid = entityToAuditMidMap.get(type);
                if (localMid != null) {
                    /*
                     * The ITD's governor is a layer component that manages an
                     * entity for which we maintain web scaffold metadata =>
                     * refresh that MD in case a layer has appeared or gone
                     * away.
                     */
                    return localMid;
                }
            }
        }
        return null;
    }

    public JpaOrmEntityListenerRegistry getEntityListenerRegistry() {
        if (entityListenerRegistry == null) {
            // Get all Services implement JpaOrmEntityListenerRegistry interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                JpaOrmEntityListenerRegistry.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    entityListenerRegistry = (JpaOrmEntityListenerRegistry) this.context
                            .getService(ref);
                    return entityListenerRegistry;
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load JpaOrmEntityListenerRegistry on JpaAuditListenerMetadataProvider.");
                return null;
            }
        }
        else {
            return entityListenerRegistry;
        }

    }

    public JpaAuditOperationsMetadata getOperations() {
        if (operations == null) {
            // Get all Services implement JpaAuditOperationsMetadata interface
            try {
                ServiceReference<?>[] references = this.context
                        .getAllServiceReferences(
                                JpaAuditOperationsMetadata.class.getName(),
                                null);

                for (ServiceReference<?> ref : references) {
                    return (JpaAuditOperationsMetadata) this.context
                            .getService(ref);
                }

                return null;

            }
            catch (InvalidSyntaxException e) {
                LOGGER.warning("Cannot load JpaAuditOperationsMetadata on JpaAuditListenerMetadataProvider.");
                return null;
            }
        }
        else {
            return operations;
        }

    }
}