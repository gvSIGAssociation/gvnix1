package org.gvnix.jpa.geo.hibernatespatial.util;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Object which contains a filter for a geometric column.<br/>
 * Supported filters:
 * <ul>
 * <li>equals: set the <code>equals</code> value</li>
 * <li>disjoint: set the <code>disjoint</code> value</li>
 * <li>intersects: set the <code>intersects</code> value</li>
 * <li>touches: set the <code>touches</code> value</li>
 * <li>crosses: set the <code>crosses</code> value</li>
 * <li>within: set the <code>within</code> value</li>
 * <li>contains: set the <code>contains</code> value</li>
 * <li>overlaps: set the <code>overlaps</code> value</li>
 * <li>relate: set the <code>relateGeom</code> value and
 * <code>relateString</code></li>
 * <li>dwithin: set the <code>dwithinGeom</code> value and
 * <code>dwithinUnits</code></li>
 * </ul>
 * <br/>
 * If more than one is specified, all filters will be applied using
 * <code>and</code> connector <br/>
 * Also contains logic to generate JPQL expressions to execute required filter.
 * 
 * 
 * @author gvNIX Team
 * @since 1.5
 */
public class GeometryFilter {

    private Geometry equals;

    private Geometry disjoint;

    private Geometry intersects;

    private Geometry touches;

    private Geometry crosses;

    private Geometry within;

    private Geometry contains;

    private Geometry containedOn;

    private Geometry overlaps;

    private Geometry relateGeom;

    private String relateString;

    private Geometry dwithinGeom;

    private Double dwithinUnits;

    /**
     * @return equals geometry operator
     */
    public Geometry getEquals() {
        return equals;
    }

    /**
     * @param equals geometry operator
     */
    public void setEquals(Geometry equals) {
        this.equals = equals;
    }

    /**
     * @return disjoint geometry operator
     */
    public Geometry getDisjoint() {
        return disjoint;
    }

    /**
     * @param disjoint geometry operator
     */
    public void setDisjoint(Geometry disjoint) {
        this.disjoint = disjoint;
    }

    /**
     * @return intersects geometry operator
     */
    public Geometry getIntersects() {
        return intersects;
    }

    /**
     * @param intersects geometry operator
     */
    public void setIntersects(Geometry intersects) {
        this.intersects = intersects;
    }

    /**
     * @return touches geometry operator
     */
    public Geometry getTouches() {
        return touches;
    }

    /**
     * @param touches geometry operator
     */
    public void setTouches(Geometry touches) {
        this.touches = touches;
    }

    /**
     * @return crosses geometry operator
     */
    public Geometry getCrosses() {
        return crosses;
    }

    /**
     * @param crosses geometry operator
     */
    public void setCrosses(Geometry crosses) {
        this.crosses = crosses;
    }

    /**
     * @return geometry operator
     */
    public Geometry getWithin() {
        return within;
    }

    /**
     * @param within geometry operator
     */
    public void setWithin(Geometry within) {
        this.within = within;
    }

    /**
     * @return contains geometry operator
     */
    public Geometry getContains() {
        return contains;
    }

    /**
     * @param contains geometry operator
     */
    public void setContains(Geometry contains) {
        this.contains = contains;
    }

    /**
     * @return overlaps geometry operator
     */
    public Geometry getOverlaps() {
        return overlaps;
    }

    /**
     * @param overlaps geometry operator
     */
    public void setOverlaps(Geometry overlaps) {
        this.overlaps = overlaps;
    }

    /**
     * @return relate geometry operator
     */
    public Geometry getRelateGeom() {
        return relateGeom;
    }

    /**
     * @param relateGeom relate geometry operator
     */
    public void setRelateGeom(Geometry relateGeom) {
        this.relateGeom = relateGeom;
    }

    /**
     * @return relate string operator
     */
    public String getRelateString() {
        return relateString;
    }

    /**
     * @param relateString relate string operator
     */
    public void setRelateString(String relateString) {
        this.relateString = relateString;
    }

    public Geometry getDwithinGeom() {
        return dwithinGeom;
    }

    /**
     * @param dwithinGeom dwithin geometry operator
     */
    public void setDwithinGeom(Geometry dwithinGeom) {
        this.dwithinGeom = dwithinGeom;
    }

    /**
     * @return dwithin units operator
     */
    public Double getDwithinUnits() {
        return dwithinUnits;
    }

    /**
     * @param dwithinUnits dwithin units geometry operator
     */
    public void setDwithinUnits(Double dwithinUnits) {
        this.dwithinUnits = dwithinUnits;
    }

    /**
     * @return
     */
    public Geometry getContainedOn() {
        return containedOn;
    }

    /**
     * @param containedOn
     */
    public void setContainedOn(Geometry containedOn) {
        this.containedOn = containedOn;
    }

    /**
     * @return true if no operator set
     */
    public boolean isEmpty() {
        return equals == null && disjoint == null && intersects == null
                && touches == null && crosses == null && within == null
                && contains == null && overlaps == null && relateGeom == null
                && dwithinGeom == null && containedOn == null;
    }

    /**
     * Generate the JPQL string filter for current filter
     * 
     * @param fieldName
     * @param srid
     * @return JPQL filter expression
     * @see #loadJPQLParams(Query, String, String)
     */
    public String toJPQLString(String fieldName, int srid) {
        return toJPQLString(fieldName, null, srid);
    }

    /**
     * Generate a <em>Order by</em> expression to order query by distance
     * 
     * @param fieldName
     * @param parameterPrefix
     * @param sridField
     * @param distanceTo
     * @return
     * @see #loadJPQLParamsDistanceOrder(Query, String, Geometry)
     */
    public String generateDistanceJPQLOrderItem(String fieldName,
            String parameterPrefix, int sridField, Geometry distanceTo) {
        return getFunctCall("distance", fieldName,
                StringUtils.defaultString(parameterPrefix), "_order",
                sridField, distanceTo.getSRID());
    }

    /**
     * Generate the JPQL string filter for current filter
     * 
     * @param fieldName
     * @param parameterPrefix
     * @param srid
     * 
     * @return JPQL filter expression
     * @see #loadJPQLParams(Query, String, String)
     */
    public String toJPQLString(String fieldName, String parameterPrefix,
            int srid) {
        if (parameterPrefix == null) {
            parameterPrefix = "";
        }
        if (isEmpty()) {
            throw new IllegalStateException(
                    "Check empty filter before use toJPQLString.");
        }
        List<String> conditions = new ArrayList<String>();
        if (equals != null) {
            conditions.add(getFunctCondition("equals", fieldName,
                    parameterPrefix, "", srid, equals.getSRID()));
        }
        if (disjoint != null) {
            conditions.add(getFunctCondition("disjoint", fieldName,
                    parameterPrefix, "", srid, disjoint.getSRID()));
        }
        if (intersects != null) {
            conditions.add(getFunctCondition("intersects", fieldName,
                    parameterPrefix, "", srid, intersects.getSRID()));
        }
        if (touches != null) {
            conditions.add(getFunctCondition("touches", fieldName,
                    parameterPrefix, "", srid, touches.getSRID()));
        }
        if (crosses != null) {
            conditions.add(getFunctCondition("crosses", fieldName,
                    parameterPrefix, "", srid, crosses.getSRID()));
        }
        if (within != null) {
            conditions.add(getFunctCondition("within", fieldName,
                    parameterPrefix, "", srid, within.getSRID()));
        }
        if (contains != null) {
            conditions.add(getFunctCondition("contains", fieldName,
                    parameterPrefix, "", srid, contains.getSRID()));
        }
        if (containedOn != null) {
            conditions.add(getFunctConditionInverse("contains", fieldName,
                    parameterPrefix, "", srid, containedOn.getSRID()));
        }
        if (overlaps != null) {
            conditions.add(getFunctCondition("overlaps", fieldName,
                    parameterPrefix, "", srid, overlaps.getSRID()));
        }
        if (relateGeom != null) {
            if (StringUtils.isBlank(relateString)) {
                throw new IllegalArgumentException(
                        "missing relateString parameter");
            }
            boolean reproject = mustReproject(srid, relateGeom.getSRID());
            if (reproject) {
                conditions.add(String.format(
                        " ( relate(%s,%s,:%s) = true ) ",
                        fieldName,
                        getTransformCall(
                                formatParamName("relate", fieldName,
                                        parameterPrefix), srid, true),
                        formatParamName("relate", fieldName, parameterPrefix,
                                "_str")));
            }
            else {
                conditions.add(String.format(
                        " ( relate(%s,:%s,:%s) = true ) ",
                        fieldName,
                        formatParamName("relate", fieldName, parameterPrefix),
                        formatParamName("relate", fieldName, parameterPrefix,
                                "_str")));
            }
        }
        if (dwithinGeom != null) {
            if (dwithinUnits == null) {
                throw new IllegalArgumentException(
                        "missing dwithinUnits parameter");
            }
            boolean reproject = mustReproject(srid, dwithinGeom.getSRID());
            if (reproject) {
                conditions.add(String.format(
                        " ( intersects(%s,buffer(%s,:%s)) = true ) ",
                        fieldName,
                        getTransformCall(
                                formatParamName("dwithin", fieldName,
                                        parameterPrefix), srid, true),
                        formatParamName("dwithin", fieldName, parameterPrefix,
                                "_d")));
            }
            else {
                conditions.add(String.format(
                        " ( intersects(%s, buffer(:%s,:%s)) = true ) ",
                        fieldName,
                        formatParamName("dwithin", fieldName, parameterPrefix),
                        formatParamName("dwithin", fieldName, parameterPrefix,
                                "_d")));
            }
        }

        // Format final condition
        StringBuilder builder = new StringBuilder();
        if (conditions.size() > 1) {
            // Join expression using "and"
            builder.append(" ( ");
            builder.append(StringUtils.join(conditions, " and "));
            builder.append(" ) ");
        }
        else {
            // Single expression
            builder.append(conditions.get(0));
        }

        return builder.toString();
    }

    /**
     * Load the filter parameters for filter query
     * 
     * @param query
     * @param fieldName target of current filter
     * @see #toJPQLString(String, String)
     */
    public void loadJPQLParams(Query query, String fieldName) {
        loadJPQLParams(query, fieldName, null);
    }

    /**
     * Load the parameter for a
     * {@link #generateDistanceJPQLOrderItem(String, String)} call
     * 
     * @param query
     * @param fieldName
     * @param geom
     * @see #generateDistanceJPQLOrderItem(String, String)
     */
    public void loadJPQLParamsDistanceOrder(Query query, String fieldName,
            Geometry geom) {
        loadJPQLParamsDistanceOrder(query, fieldName, null, geom);
    }

    /**
     * Load the filter parameters for filter query
     * 
     * @param query
     * @param fieldName target of current filter
     * @param parameterPrefix to use for parameters
     * @see #toJPQLString(String, String)
     */
    public void loadJPQLParams(Query query, String fieldName,
            String parameterPrefix) {
        if (parameterPrefix == null) {
            parameterPrefix = "";
        }
        if (isEmpty()) {
            throw new IllegalStateException(
                    "Check empty filter before use toJPQLString.");
        }
        if (equals != null) {
            query.setParameter(
                    formatParamName("equals", fieldName, parameterPrefix),
                    equals);
        }
        if (disjoint != null) {
            query.setParameter(
                    formatParamName("disjoint", fieldName, parameterPrefix),
                    disjoint);
        }
        if (intersects != null) {
            query.setParameter(
                    formatParamName("intersects", fieldName, parameterPrefix),
                    intersects);
        }
        if (touches != null) {
            query.setParameter(
                    formatParamName("touches", fieldName, parameterPrefix),
                    touches);
        }
        if (crosses != null) {
            query.setParameter(
                    formatParamName("touches", fieldName, parameterPrefix),
                    crosses);
        }
        if (within != null) {
            query.setParameter(
                    formatParamName("touches", fieldName, parameterPrefix),
                    within);
        }
        if (contains != null) {
            query.setParameter(
                    formatParamName("contains", fieldName, parameterPrefix),
                    contains);
        }
        if (overlaps != null) {
            query.setParameter(
                    formatParamName("overlaps", fieldName, parameterPrefix),
                    overlaps);
        }
        if (relateGeom != null) {
            query.setParameter(
                    formatParamName("realte", fieldName, parameterPrefix),
                    relateGeom);
            query.setParameter(
                    formatParamName("relate", fieldName, parameterPrefix,
                            "_str"), relateString);
        }
        if (dwithinGeom != null) {
            query.setParameter(
                    formatParamName("dwithin", fieldName, parameterPrefix),
                    dwithinGeom);
            query.setParameter(
                    formatParamName("dwithin", fieldName, parameterPrefix, "_d"),
                    dwithinUnits);
        }
    }

    /**
     * Load parameters for distance in order
     * 
     * @param query
     * @param fieldName
     * @param parameterPrefix
     * @see #generateDistanceJPQLOrderItem(String, String)
     */
    public void loadJPQLParamsDistanceOrder(Query query, String fieldName,
            String parameterPrefix, Geometry geom) {
        query.setParameter(
                formatParamName("distance", fieldName,
                        StringUtils.defaultString(parameterPrefix), "_order"),
                geom);
    }

    /**
     * Generate a parameter name for a filter function
     * 
     * @param functionName
     * @param fieldName
     * @param prefix
     * @return
     */
    private String formatParamName(String functionName, String fieldName,
            String prefix) {
        return formatParamName(functionName, fieldName, prefix, "");
    }

    /**
     * Generate a parameter name for a filter function
     * 
     * @param functionName
     * @param fieldName
     * @param prefix
     * @param sufix
     * @return
     */
    private String formatParamName(String functionName, String fieldName,
            String prefix, String sufix) {
        return prefix
                .concat(StringUtils.replaceChars(fieldName, ".", "_"))
                .concat("_")
                .concat(StringUtils.substring(functionName, 0, 4).concat(sufix));
    }

    /**
     * Generate filter for a function where first parameter is the field
     * 
     * @param functionName
     * @param fieldName
     * @param prefix
     * @param sufix
     * @param sridField
     * @param sridValue
     * @return
     */
    public String getFunctCondition(String functionName, String fieldName,
            String prefix, String sufix, int sridField, int sridValue) {
        boolean reproject = mustReproject(sridField, sridValue);
        if (reproject) {
            return String.format(
                    " ( %s(%s,  %s ) = true )",
                    functionName,
                    fieldName,
                    getTransformCall(
                            formatParamName(functionName, fieldName, prefix,
                                    sufix), sridField, true));
        }
        else {
            return String.format(" ( %s(%s, :%s) = true )", functionName,
                    fieldName,
                    formatParamName(functionName, fieldName, prefix, sufix));
        }
    }

    /**
     * Generate filter for a function where first parameter is the field
     * 
     * @param functionName
     * @param fieldName
     * @param prefix
     * @param sufix
     * @param sridField
     * @param sridValue
     * @return
     */
    public String getFunctCall(String functionName, String fieldName,
            String prefix, String sufix, int sridField, int sridValue) {
        boolean reproject = mustReproject(sridField, sridValue);
        if (reproject) {
            return String.format(
                    " %s(%s, %s) ",
                    functionName,
                    fieldName,
                    getTransformCall(
                            formatParamName(functionName, fieldName, prefix,
                                    sufix), sridField, true));
        }
        else {
            return String.format(" %s(%s, :%s) ", functionName, fieldName,
                    formatParamName(functionName, fieldName, prefix, sufix));
        }
    }

    /**
     * Generate a JPQL expression to transform geometry
     * 
     * @param fieldOrVarName
     * @param toSRID
     * @param isVariable
     * @return
     */
    public String getTransformCall(String fieldOrVarName, int toSRID,
            boolean isVariable) {
        return String.format(" transform(%s,%s) ",
                isVariable ? ":".concat(fieldOrVarName) : fieldOrVarName,
                toSRID);
    }

    /**
     * Check if must include a reprojection for a value based on its SRID and
     * field SRID
     * 
     * @param sridField
     * @param sridValue
     * @return
     */
    private boolean mustReproject(int sridField, int sridValue) {
        return sridValue != 0 && sridField != 0 && sridField != sridValue;
    }

    /**
     * Generate filter for a function where first parameter is the parameter
     * 
     * @param functionName
     * @param fieldName
     * @param prefix
     * @return
     */
    private String getFunctConditionInverse(String functionName,
            String fieldName, String prefix, String sufix, int sridField,
            int sridValue) {
        boolean reproject = mustReproject(sridField, sridValue);
        if (reproject) {
            return String.format(
                    " ( %s(%s, %s) = true )",
                    functionName,
                    getTransformCall(
                            formatParamName(functionName, fieldName, prefix),
                            sridField, true), fieldName);
        }
        else {
            return String
                    .format(" ( %s(:%s, %s) = true )", functionName,
                            formatParamName(functionName, fieldName, prefix),
                            fieldName);
        }
    }

    @Override
    public String toString() {
        // Use toJPQL as can be the best content representation
        return toJPQLString("geom", null, 0);
    }

}
