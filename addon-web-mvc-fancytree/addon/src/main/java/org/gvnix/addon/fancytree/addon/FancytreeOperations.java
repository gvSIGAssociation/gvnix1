package org.gvnix.addon.fancytree.addon;

import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

/**
 * Interface of Fancytree operations
 * 
 * @author gvNIX Team
 * @since 1.5.0
 */
public interface FancytreeOperations extends Feature {

    public static String FANCY_TREE_FEATURE = "gvnix-fancytree";

    /**
     * Indicate if setup command should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isSetupAvailable();

    /**
     * Indicate if add or all commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isAddAvailable();

    /**
     * Setup all add-on artifacts (dependencies in this case)
     */
    void setup();

    /**
     * Generates template methods on controller. Creates new page if specified.
     * 
     * @param controller Controller class target to write
     * @param page The name of extra show page
     * @param mapping Custom URL to send requests
     * @param include edition method templates
     */
    void addFancyTree(JavaType controller, String page, String mapping,
            Boolean editable);

    /**
     * Update all addon resources
     */
    void updateTags();
}