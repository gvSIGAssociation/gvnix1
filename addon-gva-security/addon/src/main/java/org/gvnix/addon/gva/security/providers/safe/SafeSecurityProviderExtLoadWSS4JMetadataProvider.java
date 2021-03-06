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
package org.gvnix.addon.gva.security.providers.safe;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.itd.AbstractItdMetadataProvider;
import org.springframework.roo.classpath.itd.ItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Provides {@link SafeSecurityProviderExtLoadWSS4JMetadata}.
 * 
 * @author gvNIX Team
 * @since 1.5
 */
@Component
@Service
public final class SafeSecurityProviderExtLoadWSS4JMetadataProvider extends
        AbstractItdMetadataProvider {

    protected final static Logger LOGGER = HandlerUtils
            .getLogger(SafeSecurityProviderExtLoadWSS4JMetadataProvider.class);

    /**
     * Register itself into metadataDependencyRegister and add metadata trigger
     * 
     * @param context the component context
     */
    protected void activate(ComponentContext cContext) {
        context = cContext.getBundleContext();
        getMetadataDependencyRegistry().registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        addMetadataTrigger(new JavaType(
                GvNIXExtLoadWSS4JOutInterceptor.class.getName()));
    }

    /**
     * Unregister this provider
     * 
     * @param context the component context
     */
    protected void deactivate(ComponentContext context) {
        getMetadataDependencyRegistry().deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
        removeMetadataTrigger(new JavaType(
                GvNIXExtLoadWSS4JOutInterceptor.class.getName()));
    }

    /**
     * Return an instance of the Metadata offered by this add-on
     */
    protected ItdTypeDetailsProvidingMetadataItem getMetadata(
            String metadataIdentificationString, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            String itdFilename) {

        return new SafeSecurityProviderExtLoadWSS4JMetadata(
                metadataIdentificationString, aspectName,
                governorPhysicalTypeMetadata);
    }

    /**
     * Define the unique ITD file name extension, here the resulting file name
     * will be **_ROO_GvNIXExtLoadWSS4JOutInterceptor.aj
     */
    public String getItdUniquenessFilenameSuffix() {
        return "GvNIXExtLoadWSS4JOutInterceptor";
    }

    protected String getGovernorPhysicalTypeIdentifier(
            String metadataIdentificationString) {
        JavaType javaType = SafeSecurityProviderExtLoadWSS4JMetadata
                .getJavaType(metadataIdentificationString);
        LogicalPath path = SafeSecurityProviderExtLoadWSS4JMetadata
                .getPath(metadataIdentificationString);
        return PhysicalTypeIdentifier.createIdentifier(javaType, path);
    }

    protected String createLocalIdentifier(JavaType javaType, LogicalPath path) {
        return SafeSecurityProviderExtLoadWSS4JMetadata.createIdentifier(
                javaType, path);
    }

    public String getProvidesType() {
        return SafeSecurityProviderExtLoadWSS4JMetadata
                .getMetadataIdentiferType();
    }
}