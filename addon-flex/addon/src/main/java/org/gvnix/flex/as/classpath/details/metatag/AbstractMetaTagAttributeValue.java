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

package org.gvnix.flex.as.classpath.details.metatag;

import org.apache.commons.lang3.Validate;
import org.gvnix.flex.as.model.ActionScriptSymbolName;

/**
 * Abstract base class for ActionScript meta-tag attribute types.
 * 
 * @author Jeremy Grelle
 */
public abstract class AbstractMetaTagAttributeValue<T extends Object>
        implements MetaTagAttributeValue<T> {

    private final ActionScriptSymbolName name;

    public AbstractMetaTagAttributeValue(ActionScriptSymbolName name) {
        Validate.notNull(name, "Meta Tag attribute name required");
        this.name = name;
    }

    public ActionScriptSymbolName getName() {
        return this.name;
    }
}
