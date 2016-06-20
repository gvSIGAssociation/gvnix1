package org.gvnix.addon.gva.security.providers;

import org.springframework.roo.model.JavaPackage;

/**
 * 
 * Interface of Provider Security
 * 
 * @author gvNIX Team
 * @since 1.3.0
 */
public interface SecurityProvider {

    /**
     * Gets provider name
     * 
     * @return
     */
    String getName();

    /**
     * 
     */
    String getDescription();

    /**
     * This method installs the provider that implements the interface
     * 
     * @return true if the result is ok
     */
    void install(JavaPackage targetPackage);

    /**
     * Informs if this provider is already installed on project
     * 
     * @return true if is installed, false if not, or <code>null</code> if
     *         unknown
     */
    Boolean isInstalled();

}
