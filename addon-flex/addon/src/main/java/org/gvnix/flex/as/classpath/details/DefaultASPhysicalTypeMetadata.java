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

package org.gvnix.flex.as.classpath.details;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.gvnix.flex.as.classpath.ASPhysicalTypeDetails;
import org.gvnix.flex.as.classpath.ASPhysicalTypeIdentifier;
import org.gvnix.flex.as.classpath.ASPhysicalTypeMetadata;
import org.springframework.roo.metadata.AbstractMetadataItem;

/**
 * Default metadata representation of an ActionScript source file.
 * 
 * @author Jeremy Grelle
 */
public class DefaultASPhysicalTypeMetadata extends AbstractMetadataItem
        implements ASPhysicalTypeMetadata {

    private final ASPhysicalTypeDetails physicalTypeDetails;

    private final String physicalLocationCanonicalPath;

    public DefaultASPhysicalTypeMetadata(String metadataIdentificationString,
            String physicalLocationCanonicalPath,
            ASPhysicalTypeDetails physicalTypeDetails) {
        super(metadataIdentificationString);
        Validate.isTrue(
                ASPhysicalTypeIdentifier.isValid(metadataIdentificationString),
                "Metadata identification string '"
                        + metadataIdentificationString
                        + "' does not appear to be a valid physical type identifier");
        StringUtils.isNotBlank(physicalLocationCanonicalPath);
        Validate.notNull(physicalTypeDetails, "Physical type details required");
        this.physicalTypeDetails = physicalTypeDetails;
        this.physicalLocationCanonicalPath = physicalLocationCanonicalPath;
    }

    public ASPhysicalTypeDetails getPhysicalTypeDetails() {
        return this.physicalTypeDetails;
    }

    public String getPhysicalLocationCanonicalPath() {
        return this.physicalLocationCanonicalPath;
    }

}
