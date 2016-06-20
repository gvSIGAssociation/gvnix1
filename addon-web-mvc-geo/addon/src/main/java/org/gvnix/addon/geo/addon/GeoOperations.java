package org.gvnix.addon.geo.addon;

import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Feature;

/**
 * Interface of operations this add-on offers. Typically used by a command type
 * or an external add-on.
 * 
 * @since 1.4.0
 */
public interface GeoOperations extends Feature {

    /**
     * Feature name. Use to know if gvNIX GEO component has been setup in this
     * project
     */
    static final String FEATURE_NAME_GVNIX_GEO_WEB_MVC = "gvnix-geo-web-mvc";

    static final String FT_DESC_GVNIX_GEO = "Geo Component";

    /**
     * This method checks if setup command is available
     * 
     * @return true if setup command is available
     */
    boolean isSetupCommandAvailable();

    /**
     * This method checks if add map command is available
     * 
     * @return true if map command is available
     */
    boolean isMapCommandAvailable();

    /**
     * This method checks if web mvc geo all command is available
     * 
     * @return true if web mvc geo all command is available
     */
    boolean isAllCommandAvailable();

    /**
     * This method checks if web mvc geo add command is available
     * 
     * @return true if web mvc geo add command is available
     */
    boolean isAddCommandAvailable();

    /**
     * This method checks if web mvc geo field command is available
     * 
     * @return true if web mvc geo field command is available
     */
    boolean isFieldCommandAvailable();

    /**
     * This method checks if web mvc geo layers command is available
     * 
     * @return true if web mvc geo layers command is available
     */
    boolean isLayerCommandAvailable();

    /**
     * This method checks if web mvc group command is available
     * 
     * @return true if web mvc group command is available
     */
    boolean isGroupCommandAvailable();

    /**
     * This method checks if web mvc geo tool command is available
     * 
     * @return true if web mvc geo tool command is available
     */
    boolean isToolCommandAvailable();

    /**
     * This method checks if web mvc geo update tags command is available
     * 
     * @return true if web mvc geo update tags command is available
     */
    boolean isUpdateCommandAvailable();

    /**
     * This method imports all necessary element to build a gvNIX GEO
     * application
     */
    void setup();

    /**
     * This method updates all necessary components of a gvNIX GEO application
     * to the current gvNIX version
     */
    void updateTags();

    /**
     * This method creates new components to visualize a Map component
     * 
     * @param controller
     * @param path
     */
    void addMap(JavaType controller, JavaSymbolName path, ProjectionCRSTypes crs);

    /**
     * This method includes all GEO entities on specific map controller
     * 
     * @param controller
     */
    void all(JavaType controller);

    /**
     * This method includes specific GEO entity on specific map controller
     * 
     * @param controller
     * @param mapController
     */
    void add(JavaType controller, JavaType mapController);

    /**
     * This method transforms an input element to map controller on CRU views
     * 
     * @param controller
     * @param fieldName
     * @param color
     * @param weight
     * @param center
     * @param zoom
     * @param maxZoom
     */
    void field(JavaType controller, JavaSymbolName fieldName, String color,
            String weight, String center, String zoom, String maxZoom);

    /**
     * This method add set base layer on selected geo field
     * 
     * @param controller
     * @param fieldName
     * @param url
     * @param type
     * @param labelCode
     * @param label
     * @param group
     * @param group2
     */
    void fieldBaseLayer(JavaType controller, JavaSymbolName fieldName,
            String url, String type, String labelCode, String label);

    /**
     * This method add new base tile layers on selected map or controller
     * 
     * @param name
     * @param url
     * @param controller
     * @param path
     * @param opacity
     * @param group
     */
    void tileLayer(String name, String url, JavaType controller,
            String opacity, String label, String labelCode, String group,
            JavaSymbolName path);

    /**
     * This method add a new base wmts layer on selected map
     * 
     * @param name
     * @param url
     * @param controller
     * @param checkbox
     * @param layer
     * @param opacity
     * @param label
     * @param labelCode
     * @param group
     * @param path
     */
    void wmtsLayer(String name, String url, JavaType controller,
            Boolean checkbox, String layer, String opacity, String label,
            String labelCode, String group, JavaSymbolName path);

    /**
     * This method add a new entity-simple layer on selected map
     * 
     * @param field
     * @param pk
     * @param mapController
     * @param label
     * @param labelCode
     * @param group
     */
    void entitySimpleLayer(JavaType controller, String field, String pk,
            JavaType mapController, String label, String labelCode, String group);

    /**
     * 
     * This method add new base wms layers on selected map
     * 
     * @param name
     * @param url
     * @param controller
     * @param opacity
     * @param layers
     * @param format
     * @param transparent
     * @param styles
     * @param version
     * @param crs
     * @param group
     * @param path
     */
    void wmsLayer(String name, String url, JavaType controller, String opacity,
            String layers, String format, boolean transparent, String styles,
            String version, String crs, String label, String labelCode,
            String group, JavaSymbolName path);

    /**
     * This method adds new layer group on selected map
     * 
     * @param name
     * @param group
     * @param controller
     * @param label
     * @param labelCode
     */
    void group(String name, String group, JavaType controller,
            String labelCode, String label);

    /**
     * 
     * This method adds new measure tool on selected map controller
     * 
     * @param name
     * @param mapController
     * @param preventExitMessageCode
     */
    void addMeasureTool(String name, JavaType mapController,
            String preventExitMessageCode);

    /**
     * 
     * This method adds new measure tool on selected map controller
     * 
     * @param name
     * @param mapController
     * @param preventExitMessageCode
     */
    void addCustomTool(String name, JavaType mapController,
            String preventExitMessageCode, String icon, String iconLibrary,
            boolean actionTool, String activateFunction,
            String deactivateFunction, String cursorIcon);

    /**
     * 
     * This method adds mini map component on selected map controller
     * 
     * @param mapController Controller that contains the map where you want to
     *        add the overview component. If blank, adds the overview component
     *        to focus controller
     */
    void addOverview(JavaType mapController);

    /**
     * This method updates geo addon to use Bootstrap components
     */
    void updateGeoAddonToBootstrap();

}