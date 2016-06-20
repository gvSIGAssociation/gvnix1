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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.validation.BindingResult;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

/**
 * Configure jackson seralizers for a bean.
 * <p/>
 * If it's possible, register a Serializer based on {@link ConversionService}
 * registered conversions.
 * <p/>
 * If a property already has a serialized configured (normally by
 * {@link JsonSerialize#using()}) this will be use.
 * <p/>
 * Also, if {@link JsonSerialize#as()} is set, it tries to use this class as
 * serialize target class.
 * 
 * @author gvNIX Team
 * @since TODO: Class version
 */
public class ConversionServiceBeanSerializerModifier extends
        BeanSerializerModifier {

    private final ConversionService conversionService;
    private final BindingResultSerializer bindingResultSerializer;

    public ConversionServiceBeanSerializerModifier(
            ConversionService conversionService) {
        super();
        this.conversionService = conversionService;
        this.bindingResultSerializer = new BindingResultSerializer();
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(
            SerializationConfig config, BeanDescription beanDesc,
            List<BeanPropertyWriter> beanProperties) {

        // We need the BeanPropertyDefinition to get the related Field
        List<BeanPropertyDefinition> properties = beanDesc.findProperties();
        Map<String, BeanPropertyDefinition> propertyDefMap = new HashMap<String, BeanPropertyDefinition>();
        for (BeanPropertyDefinition property : properties) {
            propertyDefMap.put(property.getName(), property);
        }

        // iterate over bean's properties to configure serializers
        for (int i = 0; i < beanProperties.size(); i++) {
            BeanPropertyWriter beanPropertyWriter = beanProperties.get(i);
            Class<?> propertyType = beanPropertyWriter.getPropertyType();

            if (beanPropertyWriter.hasSerializer()) {
                continue;
            }

            // For conversion between collection, array, and map types,
            // ConversionService.canConvert() method will return 'true'
            // but better to delegate in default Jackson property writer for
            // right start and ends markers serialization and iteration
            if (propertyType.isArray()
                    || Collection.class.isAssignableFrom(propertyType)
                    || Map.class.isAssignableFrom(propertyType)) {

                // Don't set ConversionService serializer, let Jackson
                // use default Collection serializer
                continue;
            }
            else if (BindingResult.class.isAssignableFrom(propertyType)) {
                // Use BindingResultSerializer
                beanPropertyWriter.assignSerializer(bindingResultSerializer);
            }
            else {

                // ConversionService uses value Class plus related Field
                // annotations to be able to select the right converter,
                // so we must get/ the Field annotations for success
                // formatting
                BeanPropertyDefinition propertyDef = propertyDefMap
                        .get(beanPropertyWriter.getName());
                AnnotatedField annotatedField = propertyDef.getField();
                if (annotatedField == null) {
                    continue;
                }
                AnnotatedElement annotatedEl = annotatedField.getAnnotated();

                // Field contains info about Annotations, info that
                // ConversionService uses for success formatting, use it if
                // available. Otherwise use the class of given value.
                TypeDescriptor sourceType = annotatedEl != null ? new TypeDescriptor(
                        (Field) annotatedEl) : TypeDescriptor
                        .valueOf(propertyType);

                TypeDescriptor targetType = TypeDescriptor
                        .valueOf(String.class);
                if (beanPropertyWriter.getSerializationType() != null) {
                    targetType = TypeDescriptor.valueOf(beanPropertyWriter
                            .getSerializationType().getRawClass());
                }
                if (ObjectUtils.equals(sourceType, targetType)) {
                    // No conversion needed
                    continue;
                }
                else if (sourceType.getObjectType() == Object.class
                        && targetType.getObjectType() == String.class
                        && beanPropertyWriter.getSerializationType() == null) {
                    // Can't determine source type and no target type has been
                    // configure. Delegate on jackson.
                    continue;
                }

                // All other converters must be set in ConversionService
                if (this.conversionService.canConvert(sourceType, targetType)) {

                    // We must create BeanPropertyWriter own Serializer that
                    // has knowledge about the Field related to that
                    // BeanPropertyWriter in order to have access to
                    // Field Annotations for success serialization
                    JsonSerializer<Object> jsonSerializer = new ConversionServicePropertySerializer(
                            this.conversionService, sourceType, targetType);

                    beanPropertyWriter.assignSerializer(jsonSerializer);
                }
                // If no converter set, use default Jackson property writer
                else {
                    continue;
                }
            }
        }
        return beanProperties;
    }
}
