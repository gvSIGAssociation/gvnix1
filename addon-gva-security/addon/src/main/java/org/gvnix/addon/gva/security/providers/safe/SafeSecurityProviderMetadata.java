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
package org.gvnix.addon.gva.security.providers.safe;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.gvnix.support.ItdBuilderHelper;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * ITD generator for {@link GvNIXPasswordHandlerSAFE} annotation.
 * 
 * @author gvNIX Team
 * @since 1.1.0
 */
public class SafeSecurityProviderMetadata extends
        AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String GRANTED_AUTHORITY = "org.springframework.security.core.GrantedAuthority";
    private static final String HANDLER_CONSTANTS = "org.apache.ws.security.handler.WSHandlerConstants";
    private static final String BAD_CREDENTIALS = "org.springframework.security.authentication.BadCredentialsException";
    private static final String PERMISO = "es.gva.dgm.ayf.war.definitions.v2u00.Permiso";
    private static final String PERMISO_APP = "es.gva.dgm.ayf.war.definitions.v2u00.Permisoapp";
    private static final String JAVA_UTIL_LIST = "java.util.List";
    private static final String JAVA_UTIL_PROPERTIES = "java.util.Properties";
    private static final JavaSymbolName APPLICATION_ROL_METHOD = new JavaSymbolName(
            "convertToApplicationRol");
    private static final JavaSymbolName CONVERT_WS_METHOD = new JavaSymbolName(
            "convertWSInfoToUser");
    private static final JavaSymbolName CONVERT_WS_TODAS_METHOD = new JavaSymbolName(
            "convertWSInfoToUserTodasAplicaciones");
    private static final JavaSymbolName RETRIEVE_USER_METHOD = new JavaSymbolName(
            "retrieveUser");
    private static final JavaSymbolName ADDITIONAL_CHECKS_METHOD = new JavaSymbolName(
            "additionalAuthenticationChecks");
    private static final JavaType JAVA_TYPE_STRING = new JavaType(
            "java.lang.String");
    // Constants
    private static final String PROVIDES_TYPE_STRING = SafeSecurityProviderMetadata.class
            .getName();
    private static final String PROVIDES_TYPE = MetadataIdentificationUtils
            .create(PROVIDES_TYPE_STRING);

    /**
     * Itd builder herlper
     */
    private ItdBuilderHelper helper;
    private JavaPackage governorsPackage;

    public SafeSecurityProviderMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);

        // Helper itd generation
        this.helper = new ItdBuilderHelper(this, governorPhysicalTypeMetadata,
                builder.getImportRegistrationResolver());

        this.governorsPackage = governorPhysicalTypeMetadata.getType()
                .getPackage();
        // Imports
        helper.getFinalTypeName(new JavaType(
                "org.springframework.security.authentication.encoding.PlaintextPasswordEncoder"));

        // Adding Fields
        builder.addField(getField("logger",
                "Logger.getLogger(SafeProvider.class.getName())", new JavaType(
                        "org.apache.log4j.Logger"), Modifier.PRIVATE
                        + Modifier.STATIC, null));

        builder.addField(getField(
                "passwordEncoder",
                "new PlaintextPasswordEncoder()",
                new JavaType(
                        "org.springframework.security.authentication.encoding.PasswordEncoder"),
                Modifier.PRIVATE, null));

        builder.addField(getField("saltSource", null, new JavaType(
                "org.springframework.security.authentication.dao.SaltSource"),
                Modifier.PRIVATE, null));

        builder.addField(getField("applicationId", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE, null));

        builder.addField(getField("environment", null, JAVA_TYPE_STRING,
                Modifier.PRIVATE, null));

        builder.addField(getField("prop", null, new JavaType(
                JAVA_UTIL_PROPERTIES), Modifier.PUBLIC, null));

        builder.addField(getField("loader", null, new JavaType(
                "java.lang.ClassLoader"), Modifier.PUBLIC, null));

        builder.addField(getField("stream", null, new JavaType(
                "java.io.InputStream"), Modifier.PUBLIC, null));

        builder.addField(getField("mapRoles", "true",
                JavaType.BOOLEAN_PRIMITIVE, Modifier.PRIVATE, null));

        builder.addField(getField("active", null, JavaType.BOOLEAN_PRIMITIVE,
                Modifier.PRIVATE, null));

        builder.addField(getField("filtrarPorAplicacion", null,
                JavaType.BOOLEAN_PRIMITIVE, Modifier.PRIVATE, null));

        // Adding Field annotations
        List<AnnotationMetadataBuilder> autenticationAnnotations = new ArrayList<AnnotationMetadataBuilder>();

        // Add @Autowired annotation
        AnnotationMetadataBuilder autowiredAnnotation = new AnnotationMetadataBuilder(
                new JavaType(
                        "org.springframework.beans.factory.annotation.Autowired"));
        autenticationAnnotations.add(autowiredAnnotation);

        builder.addField(getField(
                "safeAutenticacionClient",
                null,
                new JavaType(
                        "es.gva.dgm.ayf.war.definitions.v2u00.AutenticacionArangiPortType"),
                Modifier.PRIVATE, autenticationAnnotations));

        builder.addField(getField("safeAutorizacionClient", null, new JavaType(
                "es.gva.dgm.ayf.war.definitions.v2u00.AutorizacionPortType"),
                Modifier.PRIVATE, autenticationAnnotations));

        // Creating getters and setters
        builder.addMethod(getGetterMethod("saltSource", new JavaType(
                "org.springframework.security.authentication.dao.SaltSource")));
        builder.addMethod(getSetterMethod("saltSource", new JavaType(
                "org.springframework.security.authentication.dao.SaltSource")));
        builder.addMethod(getGetterMethod(
                "passwordEncoder",
                new JavaType(
                        "org.springframework.security.authentication.encoding.PasswordEncoder")));
        builder.addMethod(getSetterMethod(
                "passwordEncoder",
                new JavaType(
                        "org.springframework.security.authentication.encoding.PasswordEncoder")));
        builder.addMethod(getGetterMethod("applicationId", JAVA_TYPE_STRING));
        builder.addMethod(getSetterMethod("applicationId", JAVA_TYPE_STRING));
        builder.addMethod(getGetterMethod("environment", JAVA_TYPE_STRING));
        builder.addMethod(getSetterMethod("environment", JAVA_TYPE_STRING));
        builder.addMethod(getGetterMethod("mapRoles",
                JavaType.BOOLEAN_PRIMITIVE));
        builder.addMethod(getSetterMethod("mapRoles",
                JavaType.BOOLEAN_PRIMITIVE));
        builder.addMethod(getGetterMethod("active", JavaType.BOOLEAN_PRIMITIVE));
        builder.addMethod(getSetterMethod("active", JavaType.BOOLEAN_PRIMITIVE));
        builder.addMethod(getGetterMethod("filtrarPorAplicacion",
                JavaType.BOOLEAN_PRIMITIVE));
        builder.addMethod(getSetterMethod("filtrarPorAplicacion",
                JavaType.BOOLEAN_PRIMITIVE));

        // Creating methods
        builder.addMethod(getAdditionalAuthenticationChecksMethod());
        builder.addMethod(getRetrieveUserMethod());
        builder.addMethod(getConvertWSInfoToUserMethod());
        builder.addMethod(getConvertWSInfoToUserTodasAplicacionesMethod());
        builder.addMethod(getConvertToApplicationRolMethod());

        // Create a representation of the desired output ITD
        itdTypeDetails = builder.build();
    }

    /**
     * Gets <code>additionalAuthenticationChecks</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getAdditionalAuthenticationChecksMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(
                        new JavaType(
                                "org.springframework.security.core.userdetails.UserDetails"),
                        new JavaType(
                                "org.springframework.security.authentication.UsernamePasswordAuthenticationToken"));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(ADDITIONAL_CHECKS_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();
        throwsTypes.add(new JavaType(
                "org.springframework.security.core.AuthenticationException"));

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("userDetails"));
        parameterNames.add(new JavaSymbolName("authentication"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildAdditionalAuthenticationChecksMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, ADDITIONAL_CHECKS_METHOD,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>retrieveUser</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getRetrieveUserMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(
                        JavaType.STRING,
                        new JavaType(
                                "org.springframework.security.authentication.UsernamePasswordAuthenticationToken"));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(RETRIEVE_USER_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("userTokenized"));
        parameterNames.add(new JavaSymbolName("authentication"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildRetrieveUserMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(),
                Modifier.PUBLIC,
                RETRIEVE_USER_METHOD,
                new JavaType(
                        "org.springframework.security.core.userdetails.UserDetails"),
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>convertWSInfoToUser</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getConvertWSInfoToUserMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(
                        new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.GetInformacionWSResponse"),
                        JavaType.STRING,
                        new JavaType(JAVA_UTIL_LIST, 0, DataType.TYPE, null,
                                Arrays.asList(new JavaType(PERMISO_APP))));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(CONVERT_WS_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("userFromWS"));
        parameterNames.add(new JavaSymbolName("username"));
        parameterNames.add(new JavaSymbolName("listPermisos"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildConvertWSInfoToUserMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PRIVATE, CONVERT_WS_METHOD, new JavaType(
                        "SafeUser"), parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>convertWSInfoToUserTodasAplicaciones</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getConvertWSInfoToUserTodasAplicacionesMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(
                        new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.GetInformacionWSResponse"),
                        JavaType.STRING,
                        new JavaType(JAVA_UTIL_LIST, 0, DataType.TYPE, null,
                                Arrays.asList(new JavaType(PERMISO))));

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(CONVERT_WS_TODAS_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("userFromWS"));
        parameterNames.add(new JavaSymbolName("username"));
        parameterNames.add(new JavaSymbolName("listPermisos"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildConvertWSInfoToUserTodasAplicacionesMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PRIVATE, CONVERT_WS_TODAS_METHOD,
                new JavaType("SafeUser"), parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets <code>additionalAuthenticationChecks</code> method. <br>
     * 
     * @return
     */
    private MethodMetadata getConvertToApplicationRolMethod() {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(JAVA_TYPE_STRING);

        // Check if a method with the same signature already exists in the
        // target type
        final MethodMetadata method = methodExists(APPLICATION_ROL_METHOD,
                parameterTypes);
        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("idgrupo"));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildConvertToApplicationRolMethodBody(bodyBuilder);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PRIVATE, APPLICATION_ROL_METHOD,
                JavaType.STRING, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for <code>buildAdditionalAuthenticationChecks</code>
     * method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildAdditionalAuthenticationChecksMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        // Object salt = null;
        bodyBuilder.appendFormalLine(String.format("%s salt = null;",
                helper.getFinalTypeName(new JavaType("java.lang.Object"))));
        bodyBuilder.appendFormalLine("if (this.saltSource != null) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("salt = this.saltSource.getSalt(userDetails);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder
                .appendFormalLine("if (authentication.getCredentials() == null) {");
        bodyBuilder.indent();
        // throw new BadCredentialsException("Bad credentials:
        bodyBuilder.appendFormalLine(String.format(
                "throw new %s(\"Bad credentials: \"",
                helper.getFinalTypeName(new JavaType(BAD_CREDENTIALS))));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("+ userDetails.getUsername());");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder
                .appendFormalLine("String presentedPassword = authentication.getCredentials().toString();");
        bodyBuilder
                .appendFormalLine("if (!passwordEncoder.isPasswordValid(userDetails.getPassword(),presentedPassword, salt)) {");
        bodyBuilder.indent();
        // throw new BadCredentialsException("Bad credentials:
        bodyBuilder.appendFormalLine(String.format(
                "throw new %s(\"Bad credentials: \"",
                helper.getFinalTypeName(new JavaType(BAD_CREDENTIALS))));
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("+ userDetails.getUsername() + \" password check\");");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

    }

    /**
     * Builds body method for <code>retrieveUser</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildRetrieveUserMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine("// Getting username or token");
        // String[] requestParts = userTokenized.split("::");
        bodyBuilder
                .appendFormalLine("String[] requestParts = userTokenized.split(\"::\");");
        // String username = "";
        bodyBuilder.appendFormalLine("String username = \"\";");
        // String token = "";
        bodyBuilder.appendFormalLine("String token = \"\";");
        // if(requestParts.length > 0){
        bodyBuilder.appendFormalLine("if(requestParts.length > 0){");
        bodyBuilder.indent();
        // username = requestParts[0];
        bodyBuilder.appendFormalLine("username = requestParts[0];");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        // if(requestParts.length > 1){
        bodyBuilder.appendFormalLine("if(requestParts.length > 1){");
        bodyBuilder.indent();
        // token = requestParts[1];
        bodyBuilder.appendFormalLine("token = requestParts[1];");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder
                .appendFormalLine("String presentedPassword = authentication.getCredentials().toString();");
        bodyBuilder.appendFormalLine("if(getActive()){");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.appendFormalLine("");
        bodyBuilder.indent();

        // If token is not defined, get token using username and password
        bodyBuilder
                .appendFormalLine("// If token is not defined, get token using username and password");
        // if(StringUtils.isBlank(token)){
        bodyBuilder.appendFormalLine(String.format("if(%s.isBlank(token)){",
                helper.getFinalTypeName(new JavaType(
                        "org.apache.commons.lang3.StringUtils"))));
        bodyBuilder.appendFormalLine("");
        bodyBuilder.indent();

        // AutenticaUsuarioLDAPWSRequest aut = new
        // AutenticaUsuarioLDAPWSRequest();
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s aut = new %s();",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.AutenticaUsuarioLDAPWSRequest")),
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.AutenticaUsuarioLDAPWSRequest"))));
        bodyBuilder.appendFormalLine("aut.setUsuarioLDAP(username);");
        bodyBuilder.appendFormalLine("aut.setPwdLDAP(presentedPassword);");
        bodyBuilder.appendFormalLine("");

        // AutenticaUsuarioLDAPWSResponse response1 = port
        bodyBuilder
                .appendFormalLine(String
                        .format("%s response1 = safeAutenticacionClient.autenticaUsuarioLDAPWS(aut);",
                                helper.getFinalTypeName(new JavaType(
                                        "es.gva.dgm.ayf.war.definitions.v2u00.AutenticaUsuarioLDAPWSResponse"))));

        // token = response1.getToken();
        bodyBuilder.appendFormalLine("token = response1.getToken();");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("");

        // GetInformacionWSRequest getInformacionWSRequest = new
        // GetInformacionWSRequest();
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s getInformacionWSRequest = new %s();",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.GetInformacionWSRequest")),
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.GetInformacionWSRequest"))));
        bodyBuilder
                .appendFormalLine("getInformacionWSRequest.setToken(token);");

        // GetInformacionWSResponse getInformacionWSResponse =
        // safeAutenticacionClient
        bodyBuilder
                .appendFormalLine(String
                        .format("%s getInformacionWSResponse = safeAutenticacionClient",
                                helper.getFinalTypeName(new JavaType(
                                        "es.gva.dgm.ayf.war.definitions.v2u00.GetInformacionWSResponse"))));
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine(".getInformacionWS(getInformacionWSRequest);");
        bodyBuilder.indentRemove();

        bodyBuilder
                .appendFormalLine("// Checking if is necessary filter by applicationId");
        bodyBuilder.appendFormalLine("");

        // RetornaAutorizacionWSRequest retornaUsuApliAut = null;
        // RetornaTodasAutorizacionesDNIWSRequest retornaTodasAut = null;
        // String usuarioHDFI = getInformacionWSResponse.getIdHDFI();
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s retornaUsuApliAut = null;",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.RetornaAutorizacionWSRequest"))));
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s retornaTodasAut = null;",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.RetornaTodasAutorizacionesDNIWSRequest"))));

        bodyBuilder
                .appendFormalLine("String usuarioHDFI = getInformacionWSResponse.getIdHDFI();");

        // if(getFiltrarPorAplicacion() && StringUtils.isNotBlank(usuarioHDFI)){
        bodyBuilder
                .appendFormalLine("if(getFiltrarPorAplicacion() && StringUtils.isNotBlank(usuarioHDFI)){");
        bodyBuilder.indent();

        // retornaUsuApliAut = new RetornaAutorizacionWSRequest();
        // String applicationId = getApplicationId();
        // retornaUsuApliAut.setIdAplicacion(applicationId);
        // retornaUsuApliAut.setUsuarioHDFI(usuarioHDFI)
        bodyBuilder
                .appendFormalLine("retornaUsuApliAut = new RetornaAutorizacionWSRequest();");
        bodyBuilder
                .appendFormalLine("String applicationId = getApplicationId();");
        bodyBuilder
                .appendFormalLine("retornaUsuApliAut.setIdAplicacion(applicationId);");
        bodyBuilder
                .appendFormalLine("retornaUsuApliAut.setUsuarioHDFI(usuarioHDFI);");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}else{");
        bodyBuilder.indent();

        // retornaTodasAut = new RetornaTodasAutorizacionesDNIWSRequest();
        // retornaTodasAut.setTipoBusqueda("ambas");
        // retornaTodasAut.setUsuarioDNI(getInformacionWSResponse.getNif());

        bodyBuilder
                .appendFormalLine("retornaTodasAut = new RetornaTodasAutorizacionesDNIWSRequest();");
        bodyBuilder
                .appendFormalLine("retornaTodasAut.setTipoBusqueda(\"ambas\");");
        bodyBuilder
                .appendFormalLine("retornaTodasAut.setUsuarioDNI(getInformacionWSResponse.getNif());");

        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("");
        bodyBuilder
                .appendFormalLine("// Checking if is necessary filter by applicationId");

        // SafeUser user = null;
        bodyBuilder.appendFormalLine("SafeUser user = null;");

        // if(getFiltrarPorAplicacion() && StringUtils.isNotBlank(usuarioHDFI)){
        bodyBuilder
                .appendFormalLine("if(getFiltrarPorAplicacion() && StringUtils.isNotBlank(usuarioHDFI)){");
        bodyBuilder.indent();
        // RetornaAutorizacionWSResponse autorizaResponse =
        // safeAutorizacionClient
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s autorizaResponse = safeAutorizacionClient",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.RetornaAutorizacionWSResponse"))));
        bodyBuilder.indent();

        bodyBuilder
                .appendFormalLine(".retornaAutorizacionWS(retornaUsuApliAut);");
        bodyBuilder.indentRemove();

        // List<Permisoapp> listaPermisos = autorizaResponse.getPermisoapp();
        bodyBuilder.appendFormalLine(String.format(
                "%s listaPermisos = autorizaResponse.getPermisoapp();", helper
                        .getFinalTypeName(new JavaType(JAVA_UTIL_LIST, 0,
                                DataType.TYPE, null, Arrays
                                        .asList(new JavaType(PERMISO_APP))))));
        bodyBuilder
                .appendFormalLine("user = convertWSInfoToUser(getInformacionWSResponse,");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("username, listaPermisos);");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}else{");
        bodyBuilder.indent();
        // RetornaTodasAutorizacionesDNIWSResponse autorizaResponse =
        // autorizaPort
        bodyBuilder
                .appendFormalLine(String.format(
                        "%s autorizaResponse = safeAutorizacionClient",
                        helper.getFinalTypeName(new JavaType(
                                "es.gva.dgm.ayf.war.definitions.v2u00.RetornaTodasAutorizacionesDNIWSResponse"))));
        bodyBuilder.indent();

        bodyBuilder
                .appendFormalLine(".retornaTodasAutorizacionesDNIWS(retornaTodasAut);");
        bodyBuilder.indentRemove();

        // List<Permiso> listaPermisos = autorizaResponse.getLista();
        bodyBuilder.appendFormalLine(String.format(
                "%s listaPermisos = autorizaResponse.getLista();", helper
                        .getFinalTypeName(new JavaType(JAVA_UTIL_LIST, 0,
                                DataType.TYPE, null, Arrays
                                        .asList(new JavaType(PERMISO))))));
        bodyBuilder
                .appendFormalLine("user = convertWSInfoToUserTodasAplicaciones(getInformacionWSResponse,");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("username, listaPermisos);");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Object salt = null;
        bodyBuilder.appendFormalLine(String.format("%s salt = null;",
                helper.getFinalTypeName(new JavaType("java.lang.Object"))));
        bodyBuilder.appendFormalLine("if (this.saltSource != null) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("salt = this.saltSource.getSalt(user);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder
                .appendFormalLine("user.setPassword(passwordEncoder.encodePassword(presentedPassword,salt));");
        bodyBuilder.appendFormalLine("return user;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("catch (Exception e) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("logger.warn(\"Solicitud de login denegada (usuario='\" + username");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("+ \"'): \" + e.getLocalizedMessage());");
        bodyBuilder.indentRemove();

        // if (e instanceof SOAPFaultException) {
        bodyBuilder.appendFormalLine(String.format("if (e instanceof %s) {",
                helper.getFinalTypeName(new JavaType(
                        "javax.xml.ws.soap.SOAPFaultException"))));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("String message = e.getMessage();");
        bodyBuilder
                .appendFormalLine("if (message.indexOf(\"autenticaUsuarioLDAPWS\") > 0) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("message = \"Por favor, compruebe que el usuario y password son correctos\";");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // throw new AuthenticationServiceException(\"Acceso denegado.
        bodyBuilder
                .appendFormalLine(String.format(
                        "throw new %s(\"Acceso denegado. \"",
                        helper.getFinalTypeName(new JavaType(
                                "org.springframework.security.authentication.AuthenticationServiceException"))));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("+ message, e);");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("else {");
        bodyBuilder.indent();

        // throw new AuthenticationServiceException(
        bodyBuilder
                .appendFormalLine(String.format(
                        "throw new %s(",
                        helper.getFinalTypeName(new JavaType(
                                "org.springframework.security.authentication.AuthenticationServiceException"))));
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("\"Error en servicio web de login al validar al usuario.\"");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("+ e.getMessage(), e);");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}else{");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("SafeUser user = new SafeUser();");
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("user.setUsername(username);");
        bodyBuilder.appendFormalLine("user.setPassword(presentedPassword);");
        bodyBuilder.appendFormalLine("user.setAccountNonExpired(true);");
        bodyBuilder.appendFormalLine("user.setAccountNonLocked(true);");
        bodyBuilder.appendFormalLine("user.setCredentialsNonExpired(true);");
        bodyBuilder.appendFormalLine("user.setEnabled(true);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("} catch (Exception e) {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("String message = e.getLocalizedMessage();");
        bodyBuilder
                .appendFormalLine("message = message.substring(message.lastIndexOf(\":\") + 1, message.length());");
        bodyBuilder
                .appendFormalLine("throw new AuthenticationServiceException(");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("\"Error en servicio web de login al validar al usuario.\"");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("+ message, e);");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("return user;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
    }

    /**
     * Builds body method for <code>convertWsInfoUser</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildConvertWSInfoToUserMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine("SafeUser user = new SafeUser();");
        bodyBuilder.appendFormalLine("user.setNombre(userFromWS.getNombre());");
        bodyBuilder.appendFormalLine("user.setEmail(userFromWS.getEmail());");
        bodyBuilder
                .appendFormalLine("user.setApellido1(userFromWS.getApellido1());");
        bodyBuilder
                .appendFormalLine("user.setApellido2(userFromWS.getApellido2());");
        bodyBuilder.appendFormalLine("user.setCif(userFromWS.getCif());");
        bodyBuilder
                .appendFormalLine("user.setHabilitado(userFromWS.getHabilitado());");
        bodyBuilder.appendFormalLine("user.setIdHDFI(userFromWS.getIdHDFI());");
        bodyBuilder
                .appendFormalLine("user.setIusserDN(userFromWS.getIssuerDN());");
        bodyBuilder.appendFormalLine("user.setNif(userFromWS.getNif());");
        bodyBuilder.appendFormalLine("user.setOid(userFromWS.getOid());");
        bodyBuilder
                .appendFormalLine("user.setRazonSocial(userFromWS.getRazonSocial());");
        bodyBuilder
                .appendFormalLine("user.setRepresentante(userFromWS.getRepresentante());");
        bodyBuilder
                .appendFormalLine("user.setSerialNumber(userFromWS.getSerialNumber());");
        bodyBuilder
                .appendFormalLine("user.setSubjectDN(userFromWS.getSubjectDN());");
        bodyBuilder
                .appendFormalLine("user.setTipoAut(userFromWS.getTipoAut());");
        bodyBuilder
                .appendFormalLine("user.setTipoCertificado(userFromWS.getTipoCertificado());");
        bodyBuilder.appendFormalLine("// Spring Security User info");
        bodyBuilder.appendFormalLine("user.setUsername(username);");
        bodyBuilder
                .appendFormalLine("user.setAccountNonExpired(true); // Status info");
        bodyBuilder
                .appendFormalLine("user.setAccountNonLocked(true);// Status info");
        bodyBuilder
                .appendFormalLine("user.setCredentialsNonExpired(true); // Status info");
        bodyBuilder.appendFormalLine("user.setEnabled(true);// Status info");
        bodyBuilder.appendFormalLine("// Roles");
        bodyBuilder.appendFormalLine("if (listPermisos == null) {");
        bodyBuilder.indent();

        // throw new BadCredentialsException(
        bodyBuilder.appendFormalLine(String.format("throw new %s(",
                helper.getFinalTypeName(new JavaType(BAD_CREDENTIALS))));
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("\" El usuario proporcionado no tiene módulos asignados\");");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        bodyBuilder.appendFormalLine(String.format(
                "%s<%s> authorities = new %s<%s>();",
                helper.getFinalTypeName(new JavaType("java.util.Set")),
                helper.getFinalTypeName(new JavaType(GRANTED_AUTHORITY)),
                helper.getFinalTypeName(new JavaType("java.util.HashSet")),
                helper.getFinalTypeName(new JavaType(GRANTED_AUTHORITY))));

        // Iterator<Permisoapp> iter = listPermisos.iterator();
        bodyBuilder.appendFormalLine(String.format(
                "%s<%s> iter = listPermisos.iterator();",
                helper.getFinalTypeName(new JavaType("java.util.Iterator")),
                helper.getFinalTypeName(new JavaType(PERMISO_APP))));
        bodyBuilder.appendFormalLine("while (iter.hasNext()) {");
        bodyBuilder.indent();

        // Permisoapp permisoApp = iter.next();
        bodyBuilder.appendFormalLine(String.format(
                "%s permisoApp = iter.next();",
                helper.getFinalTypeName(new JavaType(PERMISO_APP))));
        bodyBuilder
                .appendFormalLine("String rolUsu = convertToApplicationRol(permisoApp.getIdgrupo());");
        bodyBuilder.appendFormalLine("if (rolUsu != null) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("String[] roles = rolUsu.split(\",\");");
        bodyBuilder.appendFormalLine("for(int i = 0;i < roles.length;i++){");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("SafeUserAuthority usAuth = new SafeUserAuthority();");
        bodyBuilder.appendFormalLine("usAuth.setAuthority(roles[i]);");
        bodyBuilder
                .appendFormalLine("usAuth.setIdgrupo(permisoApp.getIdgrupo());");
        bodyBuilder
                .appendFormalLine("usAuth.setIdaplicacion(permisoApp.getIdaplicacion());");
        bodyBuilder.appendFormalLine("usAuth.setNif(permisoApp.getNif());");
        bodyBuilder
                .appendFormalLine("usAuth.setUsrtipo(permisoApp.getUsrtipo());");
        bodyBuilder.appendFormalLine("usAuth.setIdrol(permisoApp.getIdrol());");
        bodyBuilder.appendFormalLine("authorities.add(usAuth);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("if (authorities.isEmpty()) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("throw new BadCredentialsException(");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("\"El usuario proporcionado no tiene módulos válidos \"");
        bodyBuilder.appendFormalLine("+ \"para acceder a la aplicación\");");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("user.setAuthorities(authorities);");
        bodyBuilder.appendFormalLine("return user;");

    }

    /**
     * Builds body method for <code>convertWSInfoToUserTodasAplicaciones</code>
     * method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildConvertWSInfoToUserTodasAplicacionesMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine("SafeUser user = new SafeUser();");
        bodyBuilder.appendFormalLine("user.setNombre(userFromWS.getNombre());");
        bodyBuilder.appendFormalLine("user.setEmail(userFromWS.getEmail());");
        bodyBuilder
                .appendFormalLine("user.setApellido1(userFromWS.getApellido1());");
        bodyBuilder
                .appendFormalLine("user.setApellido2(userFromWS.getApellido2());");
        bodyBuilder.appendFormalLine("user.setCif(userFromWS.getCif());");
        bodyBuilder
                .appendFormalLine("user.setHabilitado(userFromWS.getHabilitado());");
        bodyBuilder.appendFormalLine("user.setIdHDFI(userFromWS.getIdHDFI());");
        bodyBuilder
                .appendFormalLine("user.setIusserDN(userFromWS.getIssuerDN());");
        bodyBuilder.appendFormalLine("user.setNif(userFromWS.getNif());");
        bodyBuilder.appendFormalLine("user.setOid(userFromWS.getOid());");
        bodyBuilder
                .appendFormalLine("user.setRazonSocial(userFromWS.getRazonSocial());");
        bodyBuilder
                .appendFormalLine("user.setRepresentante(userFromWS.getRepresentante());");
        bodyBuilder
                .appendFormalLine("user.setSerialNumber(userFromWS.getSerialNumber());");
        bodyBuilder
                .appendFormalLine("user.setSubjectDN(userFromWS.getSubjectDN());");
        bodyBuilder
                .appendFormalLine("user.setTipoAut(userFromWS.getTipoAut());");
        bodyBuilder
                .appendFormalLine("user.setTipoCertificado(userFromWS.getTipoCertificado());");
        bodyBuilder.appendFormalLine("// Spring Security User info");
        bodyBuilder.appendFormalLine("user.setUsername(username);");
        bodyBuilder
                .appendFormalLine("user.setAccountNonExpired(true); // Status info");
        bodyBuilder
                .appendFormalLine("user.setAccountNonLocked(true);// Status info");
        bodyBuilder
                .appendFormalLine("user.setCredentialsNonExpired(true); // Status info");
        bodyBuilder.appendFormalLine("user.setEnabled(true);// Status info");
        bodyBuilder.appendFormalLine("// Roles");
        bodyBuilder.appendFormalLine("if (listPermisos == null) {");
        bodyBuilder.indent();

        // throw new BadCredentialsException(
        bodyBuilder.appendFormalLine(String.format("throw new %s(",
                helper.getFinalTypeName(new JavaType(BAD_CREDENTIALS))));
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("\" El usuario proporcionado no tiene módulos asignados\");");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        bodyBuilder.appendFormalLine(String.format(
                "%s<%s> authorities = new %s<%s>();",
                helper.getFinalTypeName(new JavaType("java.util.Set")),
                helper.getFinalTypeName(new JavaType(GRANTED_AUTHORITY)),
                helper.getFinalTypeName(new JavaType("java.util.HashSet")),
                helper.getFinalTypeName(new JavaType(GRANTED_AUTHORITY))));

        // Iterator<Permiso> iter = listPermisos.iterator();
        bodyBuilder.appendFormalLine(String.format(
                "%s<%s> iter = listPermisos.iterator();",
                helper.getFinalTypeName(new JavaType("java.util.Iterator")),
                helper.getFinalTypeName(new JavaType(PERMISO))));
        bodyBuilder.appendFormalLine("while (iter.hasNext()) {");
        bodyBuilder.indent();

        // Permisoapp permisoApp = iter.next();
        bodyBuilder.appendFormalLine(String.format("%s permiso = iter.next();",
                helper.getFinalTypeName(new JavaType(PERMISO))));
        bodyBuilder
                .appendFormalLine("String rolUsu = convertToApplicationRol(permiso.getIdgrupo());");
        bodyBuilder.appendFormalLine("if (rolUsu != null) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("String[] roles = rolUsu.split(\",\");");
        bodyBuilder.appendFormalLine("for(int i = 0;i < roles.length;i++){");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("SafeUserAuthority usAuth = new SafeUserAuthority();");
        bodyBuilder.appendFormalLine("usAuth.setAuthority(roles[i]);");
        bodyBuilder
                .appendFormalLine("usAuth.setIdgrupo(permiso.getIdgrupo());");
        bodyBuilder
                .appendFormalLine("usAuth.setIdaplicacion(permiso.getIdaplicacion());");
        bodyBuilder.appendFormalLine("usAuth.setNif(userFromWS.getNif());");
        bodyBuilder
                .appendFormalLine("usAuth.setUsrtipo(permiso.getUsrtipo());");
        bodyBuilder.appendFormalLine("usAuth.setIdrol(permiso.getIdrol());");
        bodyBuilder.appendFormalLine("authorities.add(usAuth);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("if (authorities.isEmpty()) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("throw new BadCredentialsException(");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("\"El usuario proporcionado no tiene módulos válidos \"");
        bodyBuilder.appendFormalLine("+ \"para acceder a la aplicación\");");
        bodyBuilder.indentRemove();
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        bodyBuilder.appendFormalLine("user.setAuthorities(authorities);");
        bodyBuilder.appendFormalLine("return user;");

    }

    /**
     * Builds body method for <code>convertToApplicationRol</code> method. <br>
     * 
     * @param bodyBuilder
     */
    private void buildConvertToApplicationRolMethodBody(
            InvocableMemberBodyBuilder bodyBuilder) {
        bodyBuilder.appendFormalLine("if (!mapRoles) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return idgrupo;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("if (prop == null){");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("prop = new Properties();");

        // loader = Thread.currentThread().getContextClassLoader();
        bodyBuilder.appendFormalLine(String.format(
                "loader = %s.currentThread().getContextClassLoader();",
                helper.getFinalTypeName(new JavaType("java.lang.Thread"))));
        bodyBuilder
                .appendFormalLine("stream = loader.getResourceAsStream(\"safe_client_roles.properties\");");
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("prop.load(stream);");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // catch (IOException e) {
        bodyBuilder.appendFormalLine(String.format("catch (%s e) {",
                helper.getFinalTypeName(new JavaType("java.io.IOException"))));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("e.printStackTrace();");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // StringBuilder sbuilder = new StringBuilder(\"SAFE.\");
        bodyBuilder.appendFormalLine(String.format(
                "%s sbuilder = new %s(\"SAFE.\");", helper
                        .getFinalTypeName(new JavaType(
                                "java.lang.StringBuilder")), helper
                        .getFinalTypeName(new JavaType(
                                "java.lang.StringBuilder"))));
        bodyBuilder
                .appendFormalLine("if (getEnvironment() != null && !getEnvironment().isEmpty()) {");
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("sbuilder.append(getEnvironment());");
        bodyBuilder.appendFormalLine("sbuilder.append('.');");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("sbuilder.append(\"role.\");");
        bodyBuilder.appendFormalLine("sbuilder.append(idgrupo);");
        bodyBuilder.appendFormalLine("try {");
        bodyBuilder.indent();
        bodyBuilder
                .appendFormalLine("return prop.getProperty(sbuilder.toString());");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");

        // catch (MissingResourceException e) {
        bodyBuilder.appendFormalLine(String.format("catch (%s e) {", helper
                .getFinalTypeName(new JavaType(
                        "java.util.MissingResourceException"))));
        bodyBuilder.indent();
        bodyBuilder.appendFormalLine("return idgrupo;");
        bodyBuilder.indentRemove();
        bodyBuilder.appendFormalLine("}");
    }

    /**
     * Gets all getters methods. <br>
     * 
     * @return
     */
    private MethodMetadata getGetterMethod(String propertyName,
            JavaType returnType) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();

        // Check if a method with the same signature already exists in the
        // target type
        JavaSymbolName propertyMethodName = new JavaSymbolName(
                "get".concat(Character.toUpperCase(propertyName.charAt(0))
                        + propertyName.substring(1)));
        final MethodMetadata method = methodExists(propertyMethodName,
                parameterTypes);

        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildGetterMethodBody(bodyBuilder, propertyName);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, propertyMethodName, returnType,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Gets all setters methods. <br>
     * 
     * @return
     */
    private MethodMetadata getSetterMethod(String propertyName,
            JavaType parameterType) {
        // Define method parameter types
        List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(parameterType);

        // Check if a method with the same signature already exists in the
        // target type
        JavaSymbolName propertyMethodName = new JavaSymbolName(
                "set".concat(Character.toUpperCase(propertyName.charAt(0))
                        + propertyName.substring(1)));
        final MethodMetadata method = methodExists(propertyMethodName,
                parameterTypes);

        if (method != null) {
            // If it already exists, just return the method and omit its
            // generation via the ITD
            return method;
        }

        // Define method annotations
        List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();

        // Define method throws types
        List<JavaType> throwsTypes = new ArrayList<JavaType>();

        // Define method parameter names
        List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName(propertyName));

        // Create the method body
        InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        buildSetterMethodBody(bodyBuilder, propertyName);

        // Use the MethodMetadataBuilder for easy creation of MethodMetadata
        MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, propertyMethodName,
                JavaType.VOID_PRIMITIVE, parameterTypes, parameterNames,
                bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

        return methodBuilder.build(); // Build and return a MethodMetadata
        // instance
    }

    /**
     * Builds body method for all getters methods. <br>
     * 
     * @param bodyBuilder
     */
    private void buildGetterMethodBody(InvocableMemberBodyBuilder bodyBuilder,
            String propertyName) {
        bodyBuilder.appendFormalLine("return this.".concat(propertyName)
                .concat(";"));

    }

    /**
     * Builds body method for all setters methods. <br>
     * 
     * @param bodyBuilder
     */
    private void buildSetterMethodBody(InvocableMemberBodyBuilder bodyBuilder,
            String propertyName) {
        bodyBuilder.appendFormalLine("this.".concat(propertyName).concat(" = ")
                .concat(propertyName).concat(";"));

    }

    /**
     * Create metadata for a field definition.
     * 
     * @return a FieldMetadata object
     */
    private FieldMetadata getField(String name, String value,
            JavaType javaType, int modifier,
            List<AnnotationMetadataBuilder> annotations) {
        JavaSymbolName curName = new JavaSymbolName(name);
        String initializer = value;
        FieldMetadata field = getOrCreateField(curName, javaType, initializer,
                modifier, annotations);
        return field;
    }

    /**
     * Gets or creates a field based on parameters.<br>
     * First try to get a suitable field (by name and type). If not found create
     * a new one (adding a counter to name if it's needed)
     * 
     * @param fielName
     * @param fieldType
     * @param initializer (String representation)
     * @param modifier See {@link Modifier}
     * @param annotations optional (can be null)
     * @return
     */
    private FieldMetadata getOrCreateField(JavaSymbolName fielName,
            JavaType fieldType, String initializer, int modifier,
            List<AnnotationMetadataBuilder> annotations) {
        JavaSymbolName curName = fielName;

        // Check if field exist
        FieldMetadata currentField = governorTypeDetails
                .getDeclaredField(curName);
        if (currentField != null) {
            if (!currentField.getFieldType().equals(fieldType)) {
                // No compatible field: look for new name
                currentField = null;
                JavaSymbolName newName = curName;
                int i = 1;
                while (governorTypeDetails.getDeclaredField(newName) != null) {
                    newName = new JavaSymbolName(curName.getSymbolName()
                            .concat(StringUtils.repeat('_', i)));
                    i++;
                }
                curName = newName;
            }
        }
        if (currentField == null) {
            // create field
            if (annotations == null) {
                annotations = new ArrayList<AnnotationMetadataBuilder>(0);
            }
            // Using the FieldMetadataBuilder to create the field
            // definition.
            final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                    getId(), modifier, annotations, curName, // Field
                    fieldType); // Field type
            fieldBuilder.setFieldInitializer(initializer);
            currentField = fieldBuilder.build(); // Build and return a
            // FieldMetadata
            // instance
        }
        return currentField;

    }

    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identifier", getId());
        builder.append("valid", valid);
        builder.append("aspectName", aspectName);
        builder.append("destinationType", destination);
        builder.append("governor", governorPhysicalTypeMetadata.getId());
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }

    /**
     * Gets final names to use of a type in method body after import resolver.
     * 
     * @param type
     * @return name to use in method body
     */
    private String getFinalTypeName(JavaType type) {
        return type.getNameIncludingTypeParameters(false,
                builder.getImportRegistrationResolver());
    }

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType,
            LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(
                PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(
                PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING,
                metadataIdentificationString);
    }

    private MethodMetadata methodExists(JavaSymbolName methodName,
            List<AnnotatedJavaType> paramTypes) {
        return MemberFindingUtils.getDeclaredMethod(governorTypeDetails,
                methodName,
                AnnotatedJavaType.convertFromAnnotatedJavaTypes(paramTypes));
    }
}
