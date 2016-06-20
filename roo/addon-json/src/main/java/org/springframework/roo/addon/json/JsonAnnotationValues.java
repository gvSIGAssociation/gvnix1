package org.springframework.roo.addon.json;

import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.annotations.populator.AbstractAnnotationValues;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulate;
import org.springframework.roo.classpath.details.annotations.populator.AutoPopulationUtils;
import org.springframework.roo.model.RooJavaType;

/**
 * Represents a parsed {@link RooJson} annotation.
 * 
 * @author Stefan Schmidt
 * @since 1.1
 */
public class JsonAnnotationValues extends AbstractAnnotationValues {

    @AutoPopulate boolean deepSerialize;
    @AutoPopulate boolean iso8601Dates;
    @AutoPopulate String fromJsonArrayMethod = "fromJsonArrayTo<TypeNamePlural>";
    @AutoPopulate String fromJsonMethod = "fromJsonTo<TypeName>";
    @AutoPopulate String rootName = "";
    @AutoPopulate String toJsonArrayMethod = "toJsonArray";
    @AutoPopulate String toJsonMethod = "toJson";

    /**
     * Constructor
     * 
     * @param governorPhysicalTypeMetadata
     */
    public JsonAnnotationValues(
            final PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(governorPhysicalTypeMetadata, RooJavaType.ROO_JSON);
        AutoPopulationUtils.populate(this, annotationMetadata);
    }

    public String getFromJsonArrayMethod() {
        return fromJsonArrayMethod;
    }

    public String getFromJsonMethod() {
        return fromJsonMethod;
    }

    public String getRootName() {
        return rootName;
    }

    public String getToJsonArrayMethod() {
        return toJsonArrayMethod;
    }

    public String getToJsonMethod() {
        return toJsonMethod;
    }

    public boolean isDeepSerialize() {
        return deepSerialize;
    }

    public boolean isIso8601Dates() {
        return iso8601Dates;
    }
}
