package org.gvnix.addon.fancytree.addon;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Fancytree addon commands
 * 
 * @author gvNIX Team
 * @since 1.5.0
 */
@Component
@Service
public class FancytreeCommands implements CommandMarker {

    /**
     * Get a reference to the FancytreeOperations from the underlying OSGi
     * container
     */
    @Reference
    private FancytreeOperations operations;

    /**
     * Returns if setup command is available on project.
     * 
     * FancyTree setup will be available if jQuery was installed on current
     * project and fancytree was not installed before.
     * 
     * @return true if command is available
     */
    @CliAvailabilityIndicator("web mvc fancytree setup")
    public boolean isSetupAvailable() {
        return operations.isSetupAvailable();
    }

    /**
     * Returns if fancytree management commands are available on project.
     * 
     * This command will be available if fancytree was installed on current
     * project
     * 
     */
    @CliAvailabilityIndicator({ "web mvc fancytree add show",
            "web mvc fancytree add edit", "web mvc fancytree update tags" })
    public boolean isAddAvailable() {
        return operations.isAddAvailable();
    }

    /**
     * This command adds controller methods to manage and show a fancytree
     * representation.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc fancytree add show",
            help = "Add a controller class with a method to create and show a tree component through ajax request")
    public void addShow(
            @CliOption(key = "controller",
                    mandatory = false,
                    unspecifiedDefaultValue = "*",
                    optionContext = UPDATE_PROJECT,
                    help = "Indicates the controller to add method template. If blank, adds current layer to focus controller") JavaType controller,
            @CliOption(key = "page",
                    mandatory = false,
                    help = "New web page to show tree component tied with request") String page,
            @CliOption(key = "mapping",
                    mandatory = false,
                    help = "Custom method path (URL) to send requests") String mapping) {

        operations.addFancyTree(controller, page, mapping, false);
    }

    /**
     * TThis command adds controller methods to manage, show, and edit a
     * fancytree representation, including methods to update, delete and create
     * data.
     * 
     */
    @CliCommand(value = "web mvc fancytree add edit",
            help = "Add a controller class with methods to edit a tree component through ajax requests")
    public void addEdit(
            @CliOption(key = "controller",
                    mandatory = false,
                    unspecifiedDefaultValue = "*",
                    optionContext = UPDATE_PROJECT,
                    help = "Indicates the controller to add method templates. If blank, adds current layer to focus controller") JavaType controller,
            @CliOption(key = "page",
                    mandatory = false,
                    help = "New web page to show tree component tied with request") String page,
            @CliOption(key = "mapping",
                    mandatory = false,
                    help = "Custom method path (URL) to send requests") String mapping) {

        operations.addFancyTree(controller, page, mapping, true);
    }

    /**
     * Install all necessary components on project
     * 
     */
    @CliCommand(value = "web mvc fancytree setup",
            help = "Install all necessary Fancytree resources on current project")
    public void setup() {
        operations.setup();
    }

    /**
     * Reinstall addon components
     * 
     */
    @CliCommand(value = "web mvc fancytree update tags",
            help = "Update all fancytree addon resources")
    public void updateTags() {
        operations.updateTags();
    }
}