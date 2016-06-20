/*
 * Copyright 2015 DiSiD Technologies S.L.L. All rights reserved.
 *
 * Project  : DiSiD org.gvnix.web.geo
 * SVN Id   : $Id$
 */
package org.gvnix.web.geo.util.impl;

import org.gvnix.web.datatables.util.impl.DatatablesUtilsBeanImpl;

/**
 * Custom Datatables utility service implementation for Geo component
 * 
 * @author gvNIX Team
 */
public class DatatablesUtilsBeanGeoImpl extends DatatablesUtilsBeanImpl {

    public static final String BOUNDING_BOX_PARAM = "dtt_bbox";
    public static final String BOUNDING_BOX_FIELDS_PARAM = "dtt_bbox_fields";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSpecialFilterParameters(String name) {
        if (super.isSpecialFilterParameters(name)) {
            return true;
        }
        else if (BOUNDING_BOX_PARAM.equals(name)) {
            return true;
        }
        else if (BOUNDING_BOX_FIELDS_PARAM.equals(name)) {
            return true;
        }
        return false;
    }
}
