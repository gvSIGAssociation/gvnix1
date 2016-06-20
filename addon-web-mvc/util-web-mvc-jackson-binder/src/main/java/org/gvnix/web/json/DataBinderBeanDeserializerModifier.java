/*
 * gvNIX. Spring Roo based RAD tool for Generalitat Valenciana
 * Copyright (C) 2013 Generalitat Valenciana
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gvnix.web.json;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

/**
 * Jackson {@link BeanDeserializerModifier} which return
 * {@link DataBinderDeserializer}.
 * 
 * @author gvNIX Team
 * @since TODO: Class version
 */
public class DataBinderBeanDeserializerModifier extends
        BeanDeserializerModifier {

    public DataBinderBeanDeserializerModifier() {
        super();
    }

    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
            BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        if (deserializer instanceof BeanDeserializerBase) {
            return new DataBinderDeserializer(
                    (BeanDeserializerBase) deserializer);
        }
        // When there is no custom-deserializer implementation returns the
        // default jackson deserializer
        return deserializer;
    }

}
