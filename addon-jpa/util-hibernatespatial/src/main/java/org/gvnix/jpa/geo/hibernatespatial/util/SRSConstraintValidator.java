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
package org.gvnix.jpa.geo.hibernatespatial.util;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author gvNIX Team
 * @since 1.5
 */
public class SRSConstraintValidator implements
        ConstraintValidator<SRS, Geometry> {

    private int srsCode;
    private String msg;

    public void initialize(SRS srs) {
        this.srsCode = srs.value();
        this.msg = srs.message();
    }

    public boolean isValid(Geometry target, ConstraintValidatorContext cxt) {
        if (target == null) {
            return true;
        }
        return srsCode == target.getSRID();
    }
}
