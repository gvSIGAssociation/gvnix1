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
package org.gvnix.addon.jpa.addon.geo.providers.hibernatespatial;

import org.gvnix.addon.jpa.annotations.geo.GvNIXEntityMapLayer;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.model.JavaType;

/**
 * Represents {@link GvNIXEntityMapLayer} annotation value
 * 
 * @author gvNIX Team
 * @since 1.5
 * 
 */
public class GvNIXEntityMapLayerValues extends AbstractAnnotationValues {

    public static final JavaType ENTITY_MAP_LAYER_ANNOTATION = new JavaType(
            GvNIXEntityMapLayer.class);

    @AutoPopulate
    private String[] finders = {};

    /**
     * Constructor
     * 
     * @param memberHoldingTypeDetails
     */
    protected GvNIXEntityMapLayerValues(
            MemberHoldingTypeDetails memberHoldingTypeDetails) {
        super(memberHoldingTypeDetails, ENTITY_MAP_LAYER_ANNOTATION);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    /**
     * 
     * @return finders
     */
    public String[] getFinders() {
        return finders;
    }

}
