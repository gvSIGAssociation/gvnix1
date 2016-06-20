package org.gvnix.addon.geo.addon;

import static org.springframework.roo.shell.OptionContexts.UPDATE_PROJECT;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Sample of a command class. The command class is registered by the Roo shell
 * following an automatic classpath scan. You can provide simple user
 * presentation-related logic in this class. You can return any objects from
 * each method, or use the logger directly if you'd like to emit messages of
 * different severity (and therefore different colours on non-Windows systems).
 * 
 * @since 1.1
 */
@Component
@Service
public class GeoCommands implements CommandMarker {

    /**
     * Get a reference to the GeoOperations from the underlying OSGi container
     */
    @Reference
    private GeoOperations operations;

    @Reference
    private TypeLocationService typeLocationService;

    private static final Logger LOGGER = HandlerUtils
            .getLogger(GeoCommands.class);

    /**
     * This method checks if the setup method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo setup")
    public boolean isSetupCommandAvailable() {
        return operations.isSetupCommandAvailable();
    }

    /**
     * This method checks if the map method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo controller")
    public boolean isMapCommandAvailable() {
        return operations.isMapCommandAvailable();
    }

    /**
     * This method checks if web mvc geo all method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo entity all")
    public boolean isAllCommandAvailable() {
        return operations.isAllCommandAvailable();
    }

    /**
     * This method checks if web mvc geo add method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo entity add")
    public boolean isAddCommandAvailable() {
        return operations.isAddCommandAvailable();
    }

    /**
     * This method checks if web mvc geo field method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo field")
    public boolean isFieldCommandAvailable() {
        return operations.isFieldCommandAvailable();
    }

    /**
     * This method checks if web mvc geo tilelayer or wmslayer method is
     * available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "web mvc geo base layer field",
            "web mvc geo tilelayer", "web mvc geo wmslayer" })
    public boolean isLayerCommandAvailable() {
        return operations.isLayerCommandAvailable();
    }

    /**
     * This method checks if web mvc geo group method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo group")
    public boolean isGroupCommandAvailable() {
        return operations.isGroupCommandAvailable();
    }

    /**
     * This method checks if web mvc geo tool method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator({ "web mvc geo tool measure",
            "web mvc geo tool custom" })
    public boolean isToolCommandAvailable() {
        return operations.isToolCommandAvailable();
    }

    /**
     * This method checks if web mvc geo update tags method is available
     * 
     * @return true (default) if the command should be visible at this stage,
     *         false otherwise
     */
    @CliAvailabilityIndicator("web mvc geo update tags")
    public boolean isUpdateCommandAvailable() {
        return operations.isUpdateCommandAvailable();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo setup",
            help = "Setup GEO components in your project.")
    public void setup() {
        operations.setup();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo update tags",
            help = "Updates geo components to new version.")
    public void updateTags() {
        operations.updateTags();
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo controller",
            help = "Add new Map view to your project")
    public void addMap(
            @CliOption(key = "class",
                    mandatory = true,
                    unspecifiedDefaultValue = "*",
                    optionContext = UPDATE_PROJECT,
                    help = "The name of the new Map Controller") final JavaType controller,
            @CliOption(key = "preferredMapping",
                    mandatory = true,
                    optionContext = UPDATE_PROJECT,
                    help = "Indicates a specific request mapping path for this map (eg /foo); no default value") final JavaSymbolName path,
            @CliOption(key = "projection",
                    mandatory = false,
                    help = "Indicates which CRS you want to use on current wms layer.  Don't change this if you're not sure what it means. If you change it, you must to specify URL param. DEFAULT: EPSG3857. ") final ProjectionCRSTypes crs) {
        operations.addMap(controller, path, crs);
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo entity all",
            help = "Run this method to include all GEO entities on specific map.")
    public void all(
            @CliOption(key = "class",
                    mandatory = false,
                    unspecifiedDefaultValue = "*",
                    optionContext = UPDATE_PROJECT,
                    help = "Indicates the controller that contains the map where you want to add current layer. If blank, adds current layer to focus controller") final JavaType mapController) {
        operations.all(mapController);
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo entity add",
            help = "Run this method to include specific GEO entity on specific map")
    public void add(
            @CliOption(key = "controller",
                    mandatory = true,
                    help = "Indicates which entity controller you want to add to map") final JavaType controller,
            @CliOption(key = "class",
                    mandatory = false,
                    unspecifiedDefaultValue = "*",
                    optionContext = UPDATE_PROJECT,
                    help = "Indicates the controller that contains the map where you want to add current layer. If blank, adds current layer to focus controller") final JavaType mapController) {
        operations.add(controller, mapController);
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo field",
            help = "Run this method to transform input to map control on entity CRU views.")
    public void field(
            @CliOption(key = "controller",
                    mandatory = true,
                    help = "Indicates which entity controller has field") final JavaType controller,
            @CliOption(key = "field",
                    mandatory = true,
                    help = "Indicates which field you want to implements as map control") final JavaSymbolName fieldName,
            @CliOption(key = "color",
                    mandatory = false,
                    help = "Indicates which color you want to use to draw element") final String color,
            @CliOption(key = "weight",
                    mandatory = false,
                    help = "Indicates which weight you want to use to draw element") final String weight,
            @CliOption(key = "center",
                    mandatory = false,
                    help = "Indicates map center to use as default. FORMAT: 'lat , lng'") final String center,
            @CliOption(key = "zoom",
                    mandatory = false,
                    help = "Indicates which zoom you want to use on map") final String zoom,
            @CliOption(key = "maxZoom",
                    mandatory = false,
                    help = "Indicates which maxZoom you want to use to on map") final String maxZoom) {

        LOGGER.warning(String
                .format("You should add a base layer for field %s in order to view it as geo field in entity views. You can use 'web mvc geo base layer field' command or do it manually.",
                        fieldName));

        operations.field(controller, fieldName, color, weight, center, zoom,
                maxZoom);
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo base layer field",
            help = "This command sets the base layers for a geo field views")
    public void fieldLayer(
            @CliOption(key = "controller",
                    mandatory = true,
                    help = "Indicates in which entity controller is the geo field to add a layer") final JavaType controller,
            @CliOption(key = "field",
                    mandatory = true,
                    help = "Indicates which geo field to add a layer") final JavaSymbolName field,
            @CliOption(key = "url",
                    mandatory = true,
                    help = "Indicates base layer URL") final String url,
            @CliOption(key = "type",
                    mandatory = true,
                    help = "Indicates new base layer type. VALUES: tile, wms, wmts") final String type,
            @CliOption(key = "labelCode",
                    mandatory = false,
                    help = "i18n code to identify new layer on properties for the language") final String labelCode,
            @CliOption(key = "label",
                    mandatory = false,
                    help = "Text to use as layer label. Overwrites --labelCode value") final String label) {

        operations.fieldBaseLayer(controller, field, url, type, labelCode,
                label);

    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo tilelayer",
            help = "Run this method to add new base tile layers on your map.")
    public void tileLayer(
            @CliOption(key = "name",
                    mandatory = true,
                    help = "Indicates which name has current base tile layer") final String name,
            @CliOption(key = "url",
                    mandatory = true,
                    help = "Indicates base tile layer URL") final String url,
            @CliOption(key = "opacity",
                    mandatory = false,
                    help = "Indicates which opacity has base layer. Number between 0 and 1. DEFAULT: 1") final String opacity,
            @CliOption(key = "labelCode",
                    mandatory = false,
                    help = "i18n code to identify new layer on properties for the language") final String labelCode,
            @CliOption(key = "label",
                    mandatory = false,
                    help = "Text to use as layer label. Overwrites --labelCode value") final String label,
            @CliOption(key = "group",
                    mandatory = false,
                    help = "Indicates which layer group this layer belongs. It represents group name") final String group,
            @CliOption(key = "class",
                    mandatory = false,
                    unspecifiedDefaultValue = "*",
                    optionContext = UPDATE_PROJECT,
                    help = "Indicates the controller that contains the map where you want to add current layer. If blank, adds current layer to focus controller") final JavaType controller) {

        operations.tileLayer(name, url, controller, opacity, label, labelCode,
                group, null);
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param name
     * @param url
     * @param path
     * @param addCheckBox
     * @param layer
     * @param opacity
     * @param labelCode
     * @param label
     */
    @CliCommand(value = "web mvc geo wmtslayer",
            help = "This command adds a new base wmts layer on your map")
    public void wmtsLayer(
            @CliOption(key = "name",
                    mandatory = true,
                    help = "Indicates which name has current base wms layer") final String name,
            @CliOption(key = "url",
                    mandatory = true,
                    help = "Indicates base wms layer URL") final String url,
            @CliOption(key = "layer",
                    mandatory = true,
                    help = "Layer to request") final String layer,
            @CliOption(key = "addCheckBox",
                    mandatory = false,
                    unspecifiedDefaultValue = "true",
                    help = "Show checkbox control. Default true") final boolean addCheckBox,
            @CliOption(key = "opacity",
                    mandatory = false,
                    help = "Indicates which opacity has base layer. Number between 0 and 1. DEFAULT: 0.5") final String opacity,
            @CliOption(key = "labelCode",
                    mandatory = false,
                    help = "i18n code to identify new layer on properties for the language") final String labelCode,
            @CliOption(key = "label",
                    mandatory = false,
                    help = "Text to use as layer label. Overwrites --labelCode value") final String label,
            @CliOption(key = "group",
                    mandatory = false,
                    help = "Indicates which layer group this layer belongs. It represents group name") final String group,
            @CliOption(key = "class",
                    mandatory = false,
                    unspecifiedDefaultValue = "*",
                    optionContext = UPDATE_PROJECT,
                    help = "Indicates the controller that contains the map where you want to add current layer. If blank, adds current layer to focus controller") final JavaType controller) {

        operations.wmtsLayer(name, url, controller, addCheckBox, layer,
                opacity, labelCode, label, group, null);
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo wmslayer",
            help = "Run this method to add new base wms layers on your map.")
    public void wmsLayer(
            @CliOption(key = "name",
                    mandatory = true,
                    help = "Indicates which name has current base wms layer") final String name,
            @CliOption(key = "url",
                    mandatory = true,
                    help = "Indicates base wms layer URL") final String url,
            @CliOption(key = "opacity",
                    mandatory = false,
                    help = "Indicates which opacity has base layer. Number between 0 and 1. DEFAULT: 0.5") final String opacity,
            @CliOption(key = "layers",
                    mandatory = true,
                    help = "Indicates which layers you want to load on this wms layer") final String layers,
            @CliOption(key = "format",
                    mandatory = false,
                    help = "Indicates which image format you want to load on this wms layer. EX: image/png") final String format,
            @CliOption(key = "transparent",
                    mandatory = false,
                    help = "Indicates if current layer is transparent or not",
                    unspecifiedDefaultValue = "false") final boolean transparent,
            @CliOption(key = "styles",
                    mandatory = false,
                    help = "Indicates which styles you want to use on current wms layer") final String styles,
            @CliOption(key = "version",
                    mandatory = false,
                    help = "Indicates which wms version you want to use on current wms layer") final String version,
            @CliOption(key = "crs",
                    mandatory = false,
                    help = "Indicates which CRS projection you want to use on current wms layer. DEFAULT: EPSG3857") final ProjectionCRSTypes crs,
            @CliOption(key = "labelCode",
                    mandatory = false,
                    help = "i18n code to identify new layer on properties for the language") final String labelCode,
            @CliOption(key = "label",
                    mandatory = false,
                    help = "Text to use as layer label. Overwrites --labelCode value") final String label,
            @CliOption(key = "group",
                    mandatory = false,
                    help = "Indicates which layer group this layer belongs. It represents group name") final String group,
            @CliOption(key = "class",
                    mandatory = false,
                    unspecifiedDefaultValue = "*",
                    optionContext = UPDATE_PROJECT,
                    help = "Indicates the controller that contains the map where you want to add current layer. If blank, adds current layer to focus controller") final JavaType controller) {

        // Checking if crs value has selected
        String crsToUse = null;
        if (crs != null) {
            crsToUse = crs.toString();
        }
        operations.wmsLayer(name, url, controller, opacity, layers, format,
                transparent, styles, version, crsToUse, label, labelCode,
                group, null);

    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo group",
            help = "Run this method to add new layer groups on your map.")
    public void group(
            @CliOption(key = "name",
                    mandatory = true,
                    help = "Indicates group name. It must be unique in the map view") final String name,
            @CliOption(key = "group",
                    mandatory = false,
                    help = "Indicates which layer group this group belongs. It represents parent group name") final String group,
            @CliOption(key = "labelCode",
                    mandatory = false,
                    help = "i18n code to identify new layer group on properties for the language") final String labelCode,
            @CliOption(key = "label",
                    mandatory = false,
                    help = "Text to use as layer group label. Overwrites --labelCode value") final String label,
            @CliOption(key = "class",
                    mandatory = false,
                    unspecifiedDefaultValue = "*",
                    optionContext = UPDATE_PROJECT,
                    help = "Indicates the controller that contains the map where you want to add current layer. If blank, adds current layer to focus controller") final JavaType controller) {

        operations.group(name, group, controller, labelCode, label);
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param controller
     * @param field
     * @param pk
     * @param path
     * @param labelCode
     * @param label
     */
    @CliCommand(value = "web mvc geo entity simple",
            help = " Generate a layer to show values from a single entity")
    public void entitySimple(
            @CliOption(key = "controller",
                    mandatory = true,
                    help = "Indicates the entity controller") final JavaType controller,
            @CliOption(key = "field",
                    mandatory = true,
                    help = "Indicates the field to apply entity simple layer") final String field,
            @CliOption(key = "pk",
                    mandatory = true,
                    help = "Primary key for selected entity") final String pk,
            @CliOption(key = "group",
                    mandatory = false,
                    help = "Indicates which layer group this group belongs. It represents parent group name") final String group,
            @CliOption(key = "labelCode",
                    mandatory = false,
                    help = "i18n code to identify new layer on properties for the language") final String labelCode,
            @CliOption(key = "label",
                    mandatory = false,
                    help = "Text to use as layer label. Overwrites --labelCode value") final String label,
            @CliOption(key = "class",
                    mandatory = false,
                    unspecifiedDefaultValue = "*",
                    optionContext = UPDATE_PROJECT,
                    help = "Indicates the controller that contains the map where you want to add current layer. If blank, adds current layer to focus controller") final JavaType mapController) {

        operations.entitySimpleLayer(controller, field, pk, mapController,
                labelCode, label, group);
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo tool measure",
            help = "Run this method to add new measure tool on your map.")
    public void measureTool(
            @CliOption(key = "name",
                    mandatory = true,
                    help = "Indicates which name has current tool") final String name,
            @CliOption(key = "preventExitMessageCode",
                    mandatory = false,
                    help = "Indicates which MessageCode you want to use to prevent exit. If blank, not prevent exit. DEFAULT: blank.") final String preventExitMessageCode,
            @CliOption(key = "class",
                    mandatory = false,
                    unspecifiedDefaultValue = "*",
                    optionContext = UPDATE_PROJECT,
                    help = "Indicates the controller that contains the map where you want to add current layer. If blank, adds current layer to focus controller") final JavaType mapController) {

        operations.addMeasureTool(name, mapController, preventExitMessageCode);
    }

    /**
     * This method registers a command with the Roo shell. It also offers a
     * mandatory command attribute.
     * 
     * @param type
     */
    @CliCommand(value = "web mvc geo tool custom",
            help = "Run this method to add new custom tool on your map.")
    public void customTool(
            @CliOption(key = "name",
                    mandatory = true,
                    help = "Indicates which name has current tool") final String name,
            @CliOption(key = "icon",
                    mandatory = true,
                    help = "Icon to show on ToolBar to identiy the tool element. Fontawesome icons or glyphicon icons") final String icon,
            @CliOption(key = "activateFunction",
                    mandatory = true,
                    help = "Function to invoke when the user press on tool button") final String activateFunction,
            @CliOption(key = "deactivateFunction",
                    mandatory = true,
                    help = "Function to invoke when the user press a different tool button") final String deactivateFunction,
            @CliOption(key = "iconLibrary",
                    mandatory = false,
                    help = "DESC: Select de icon library.| DEFAULT: 'glyphicon' | POSSIBLE VALUES: 'fa' for font-awesome or 'glyphicon' for bootstrap 3") final String iconLibrary,
            @CliOption(key = "actionTool",
                    mandatory = false,
                    help = "Indicates if this tool is a selectable tool (false) or and action tool (true)",
                    unspecifiedDefaultValue = "false") final boolean actionTool,
            @CliOption(key = "cursorIcon",
                    mandatory = false,
                    help = "Icon to show as cursor when enter on map element") final String cursorIcon,
            @CliOption(key = "preventExitMessageCode",
                    mandatory = false,
                    help = "Indicates which MessageCode you want to use to prevent exit. If blank, not prevent exit. DEFAULT: blank.") final String preventExitMessageCode,
            @CliOption(key = "class",
                    mandatory = false,
                    unspecifiedDefaultValue = "*",
                    optionContext = UPDATE_PROJECT,
                    help = "Indicates the controller that contains the map where you want to add current layer. If blank, adds current layer to focus controller") final JavaType mapController) {

        operations.addCustomTool(name, mapController, preventExitMessageCode,
                icon, iconLibrary, actionTool, activateFunction,
                deactivateFunction, cursorIcon);
    }

    /**
     * Add overview component to the map that represents the class specified
     * 
     * @param mapController Controller that contains the map where you want to
     *        add the overview component. If blank, adds the overview component
     *        to focus controller
     */
    @CliCommand(value = "web mvc geo component overview",
            help = "Run this method to add minimap control into your map.")
    public void overvierComponent(
            @CliOption(key = "class",
                    mandatory = false,
                    unspecifiedDefaultValue = "*",
                    optionContext = UPDATE_PROJECT,
                    help = "Indicates the controller that contains the map where you want to add the overview component. If blank, adds the overview component to focus controller") final JavaType mapController) {

        operations.addOverview(mapController);
    }

}