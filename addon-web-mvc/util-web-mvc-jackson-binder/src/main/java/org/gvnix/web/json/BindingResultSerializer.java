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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Jackson Serializer which generates a tree with binding and validation
 * property errors stored on a {@link BindingResult} object.
 * <p/>
 * Error messages will be {@link FieldError#getDefaultMessage()} so, it is
 * already translated to (current request) language (or supposed to).
 * <p/>
 * JSON generated for {@link List} binding errors:
 * 
 * <pre>
 *   { 
 *     OBJECT_INDEX : { FIELD1_NAME : FIELD_ERROR_MSG, FIELD2_NAME : FIELD_ERROR_MSG, ...}, 
 *     OBJECT_INDEX2 : { FIELD1_NAME : FIELD_ERROR_MSG, 
 *         FIELD_OBJECT_NAME : { SUBOBJECT_FIELD: FIELD_ERROR_MSG, ... }
 *         FIELD_LIST_NAME: {
 *              OBJECT_FIELD_ITEM_INDEX : {ITEM_LIST_FIELD: FIELD_ERROR_MSG, ... },
 *              OBJECT_FIELD_ITEM_INDEX2 : {ITEM_LIST_FIELD: FIELD_ERROR_MSG, ... },
 *         },
 *         ...
 *     },
 *     ... 
 *   }
 * </pre>
 * 
 * JSON for object binding errors:
 * 
 * <pre>
 * { FIELD1_NAME : FIELD_ERROR_MSG, 
 *      FIELD_OBJECT_NAME : { SUBOBJECT_FIELD: FIELD_ERROR_MSG, ... }
 *      FIELD_LIST_NAME: {
 *              OBJECT_FIELD_ITEM_INDEX : {ITEM_LIST_FIELD: FIELD_ERROR_MSG, ... },
 *              OBJECT_FIELD_ITEM_INDEX2 : {ITEM_LIST_FIELD: FIELD_ERROR_MSG, ... },
 *      },
 *      ...
 * }
 * </pre>
 * 
 * 
 * @author gvNIX Team
 * @since TODO: Class version
 */
public class BindingResultSerializer extends JsonSerializer<Object> {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(BindingResultSerializer.class);

    private static final String ERROR_WRITTING_BINDING = "Error writting BindingResult";

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Object value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {

        try {
            // Cast to BindingResult
            BindingResult result = (BindingResult) value;

            // Create the result map
            Map<String, Object> allErrorsMessages = new HashMap<String, Object>();

            // Get field errors
            List<FieldError> fieldErrors = result.getFieldErrors();
            if (fieldErrors.isEmpty()) {
                // Nothing to do
                jgen.writeNull();
                return;
            }

            // Check if target type is an array or a bean
            @SuppressWarnings("rawtypes")
            Class targetClass = result.getTarget().getClass();
            if (targetClass.isArray()
                    || Collection.class.isAssignableFrom(targetClass)) {
                loadListErrors(result.getFieldErrors(), allErrorsMessages);
            }
            else {
                loadObjectErrors(result.getFieldErrors(), allErrorsMessages);
            }

            jgen.writeObject(allErrorsMessages);
        }
        catch (JsonProcessingException e) {
            LOGGER.warn(ERROR_WRITTING_BINDING, e);
            throw e;
        }
        catch (IOException e) {
            LOGGER.warn(ERROR_WRITTING_BINDING, e);
            throw e;
        }
        catch (Exception e) {
            LOGGER.warn(ERROR_WRITTING_BINDING, e);
            throw new IOException(ERROR_WRITTING_BINDING, e);
        }

    }

    /**
     * Iterate over object errors and load it on allErrorsMessages map.
     * <p/>
     * Delegates on {@link #loadObjectError(FieldError, String, Map)}
     * 
     * @param fieldErrors
     * @param allErrorsMessages
     */
    private void loadObjectErrors(List<FieldError> fieldErrors,
            Map<String, Object> allErrorsMessages) {

        for (FieldError error : fieldErrors) {
            loadObjectError(error, error.getField(), allErrorsMessages);
        }

    }

    /**
     * Iterate over list items errors and load it on allErrorsMessages map.
     * <p/>
     * Delegates on {@link #loadObjectError(FieldError, String, Map)}
     * 
     * @param fieldErrors
     * @param allErrorsMessages
     */
    @SuppressWarnings("unchecked")
    private void loadListErrors(List<FieldError> fieldErrors,
            Map<String, Object> allErrorsMessages) {

        // Get prefix to unwrapping list:
        // "list[0].employedSince"
        String fieldNamePath = fieldErrors.get(0).getField();
        // "list"
        String prefix = StringUtils.substringBefore(fieldNamePath, "[");

        String index;
        Map<String, Object> currentErrors;

        // Iterate over errors
        for (FieldError error : fieldErrors) {
            // get property path without list prefix
            // "[0].employedSince"
            fieldNamePath = StringUtils
                    .substringAfter(error.getField(), prefix);

            // Get item's index:
            // "[0].employedSince"
            index = StringUtils.substringBefore(
                    StringUtils.substringAfter(fieldNamePath, "["), "]");

            // Remove index definition from field path
            // "employedSince"
            fieldNamePath = StringUtils.substringAfter(fieldNamePath, ".");

            // Check if this item already has errors registered
            currentErrors = (Map<String, Object>) allErrorsMessages.get(index);
            if (currentErrors == null) {
                // No errors registered: create map to contain this error
                currentErrors = new HashMap<String, Object>();
                allErrorsMessages.put(index, currentErrors);
            }

            // Load error on item's map
            loadObjectError(error, fieldNamePath, currentErrors);
        }
    }

    /**
     * Loads an object field error in errors map.
     * <p/>
     * This method identifies if referred object property is an array, an object
     * or a simple property to decide how to store the error message.
     * 
     * @param error
     * @param fieldNamePath
     * @param objectErrors
     */
    @SuppressWarnings("unchecked")
    private void loadObjectError(FieldError error, String fieldNamePath,
            Map<String, Object> objectErrors) {

        String propertyName;
        boolean isObject = false;
        boolean isList = false;

        // Get this property name and if is a object property
        if (StringUtils.contains(fieldNamePath, ".")) {
            isObject = true;
            propertyName = StringUtils.substringBefore(fieldNamePath, ".");
        }
        else {
            isObject = false;
            propertyName = fieldNamePath;
        }

        // Check if property is an array or a list
        isList = StringUtils.contains(propertyName, "[");

        // Process a list item property
        if (isList) {
            // Get property name
            String listPropertyName = StringUtils.substringBefore(propertyName,
                    "[");

            // Get referred item index
            String index = StringUtils.substringBefore(
                    StringUtils.substringAfter(propertyName, "["), "]");

            // Get item path
            String itemPath = StringUtils.substringAfter(fieldNamePath, ".");

            // Get container of list property errors
            Map<String, Object> listErrors = (Map<String, Object>) objectErrors
                    .get(listPropertyName);

            if (listErrors == null) {
                // property has no errors yet: create a container for it
                listErrors = new HashMap<String, Object>();
                objectErrors.put(listPropertyName, listErrors);
            }

            // Get current item errors
            Map<String, Object> itemErrors = (Map<String, Object>) listErrors
                    .get(index);

            if (itemErrors == null) {
                // item has no errors yet: create a container for it
                itemErrors = new HashMap<String, Object>();
                listErrors.put(index, itemErrors);
            }

            // Load error in item property path
            loadObjectError(error, itemPath, itemErrors);

        }
        else if (isObject) {
            // It's not a list but it has properties in it value

            // Get current property errors
            Map<String, Object> propertyErrors = (Map<String, Object>) objectErrors
                    .get(propertyName);

            if (propertyErrors == null) {
                // item has no errors yet: create a container for it
                propertyErrors = new HashMap<String, Object>();
                objectErrors.put(propertyName, propertyErrors);
            }

            // Get error sub path
            String subFieldPath = StringUtils
                    .substringAfter(fieldNamePath, ".");

            // Load error in container
            loadObjectError(error, subFieldPath, propertyErrors);

        }
        else {
            // standard property with no children value

            // Store error message in container
            objectErrors.put(propertyName, error.getDefaultMessage());
        }
    }

}
