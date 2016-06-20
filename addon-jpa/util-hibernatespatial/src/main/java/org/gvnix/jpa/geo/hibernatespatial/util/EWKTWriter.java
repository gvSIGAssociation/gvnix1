package org.gvnix.jpa.geo.hibernatespatial.util;

import java.io.IOException;
import java.io.Writer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Extends {@link WKTWriter} to include SRID (if any)
 * 
 * @author gvNIX Team
 * @version 1.5
 */
public class EWKTWriter extends WKTWriter {

    public EWKTWriter() {
        super();
    }

    public EWKTWriter(int outputDimension) {
        super(outputDimension);
    }

    private String getSRID(Geometry geom) {
        int srid = geom.getSRID();
        if (srid != 0) {
            // Include SRID in wkt
            return "SRID=".concat(String.valueOf(srid)).concat(";");
        }
        return "";
    }

    @Override
    public void write(Geometry geometry, Writer writer) throws IOException {
        writer.write(getSRID(geometry));
        super.write(geometry, writer);
    }

    @Override
    public String write(Geometry geometry) {
        return getSRID(geometry).concat(super.write(geometry));
    }

    @Override
    public String writeFormatted(Geometry geometry) {
        return getSRID(geometry).concat(super.writeFormatted(geometry));
    }

    @Override
    public void writeFormatted(Geometry geometry, Writer writer)
            throws IOException {
        writer.write(getSRID(geometry));
        super.writeFormatted(geometry, writer);
    }
}
