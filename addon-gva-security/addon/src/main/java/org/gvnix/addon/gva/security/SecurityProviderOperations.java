package org.gvnix.addon.gva.security;

import java.util.List;

import org.gvnix.addon.gva.security.providers.SecurityProviderId;
import org.springframework.roo.model.JavaPackage;

/**
 * Interface of commands that are available via the Roo shell.
 * 
 * @since 1.1.1
 */
public interface SecurityProviderOperations {

    /**
     * Checks if the command security setup was executed
     * 
     * @return true if the command was executed or false if not
     */
    boolean checkSecuritySetup();

    /**
     * 
     * Get available providers on the system
     * 
     * @return A SecurityProviderId List
     */
    List<SecurityProviderId> getProvidersId();

    /**
     * Installs the selected provider
     * 
     * @param provider Provider SecurityProviderId
     */
    void installProvider(SecurityProviderId provider, JavaPackage targetPackage);

    /**
     * Gets the current provider by name
     * 
     * @param name Provider Name
     * @return SecurityProviderId
     */
    SecurityProviderId getProviderIdByName(String name);
}