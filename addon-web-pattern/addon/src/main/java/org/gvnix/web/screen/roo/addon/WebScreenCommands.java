/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2012 CIT - Generalitat
 * Valenciana
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.web.screen.roo.addon;

import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;

/**
 * Command Class for <code>web mvc screen *</code> commands
 * 
 * @author Jose Manuel Vivó (jmvivo at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @author Mario Martínez Sánchez (mmartinez at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.8
 */
@Component
@Service
public class WebScreenCommands implements CommandMarker {

    private Logger log = Logger.getLogger(getClass().getName());

    /** Get a reference to the operations from the underlying OSGi container */
    @Reference
    private WebScreenOperations operations;

    @Reference
    private SeleniumServices seleniumServices;

    /**
     * Informs if <code>web mvc pattern setup</code> command are available
     * 
     * @return true if commands are available
     */
    @CliAvailabilityIndicator({ "web mvc pattern setup" })
    public boolean isWebPatternAvaliable() {
        return operations.isPatternCommandAvailable()
                && !operations.isRelationPatternCommandAvailable();
    }

    /**
     * Adds a pattern to a web MVC controller
     * 
     * @param controllerClass The controller to apply the pattern to
     * @param name Identification to use for this pattern
     * @param type The pattern to apply
     */
    @CliCommand(value = "web mvc pattern master", help = "Add a screen pattern to a controller")
    public void webScreenAdd(
            @CliOption(key = "class", mandatory = true, help = "The controller to apply the pattern to (only active record entities supported)") JavaType controllerClass,
            @CliOption(key = "name", mandatory = true, help = "Identificication to use for this pattern") JavaSymbolName name,
            @CliOption(key = "type", mandatory = true, help = "The pattern to apply") WebPatternType type,
            @CliOption(key = "testAutomatically", mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false", help = "Create automatic Selenium test for this controller") boolean testAutomatically,
            @CliOption(key = "testName", mandatory = false, help = "Name of the test") String testName,
            @CliOption(key = "testServerUrl", mandatory = false, unspecifiedDefaultValue = "http://localhost:8080/", specifiedDefaultValue = "http://localhost:8080/", help = "URL of the server where the web application is available, including protocol, port and hostname") String url) {

        // Create pattern
        boolean added = operations.addPattern(controllerClass, name, type);

        // Generate optionally Selenium tests
        if (added && testAutomatically) {

            // Create test with defined name or with pattern name by default
            if (testName == null) {
                testName = name.getSymbolName();
            }

            if (type == WebPatternType.register) {

                seleniumServices.generateTestMasterRegister(controllerClass,
                        testName, url);
            }
            else if (type == WebPatternType.tabular) {

                seleniumServices.generateTestMasterTabular(controllerClass,
                        testName, url);
            }
            else if (type == WebPatternType.tabular_edit_register) {

                seleniumServices.generateTestMasterTabularEditRegister(
                        controllerClass, testName, url);
            }
            else {

                log.info("Test automatically not available for pattern type "
                        + type.name());
            }
        }
    }

    /**
     * Informs if <code>web mvc pattern *</code> command are available
     * 
     * @return true if commands are available
     */
    @CliAvailabilityIndicator({ "web mvc pattern detail",
            "web mvc pattern master", "web mvc pattern update tags" })
    public boolean isWebRelationPatternAvaliable() {
        return operations.isRelationPatternCommandAvailable();
    }

    /**
     * Adds a pattern to a web MVC controller filed
     * 
     * @param controllerClass The controller to apply the pattern to
     * @param name Identification to use for this pattern
     * @param field The one-to-may field
     * @param type The pattern to apply
     */
    @CliCommand(value = "web mvc pattern detail", help = "Add a detail screen pattern to a defined master pattern in a controller")
    public void webRelationPattern(
            @CliOption(key = "class", mandatory = true, help = "The controller to apply the pattern to (only active record entities supported)") JavaType controllerClass,
            @CliOption(key = "name", mandatory = true, help = "Identificication to use for this pattern") JavaSymbolName name,
            @CliOption(key = "field", mandatory = true, help = "The one-to-many field to apply the pattern to") JavaSymbolName field,
            @CliOption(key = "type", mandatory = true, help = "The pattern to apply") WebPatternType type,
            @CliOption(key = "testAutomatically", mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false", help = "Create automatic Selenium test for this controller") boolean testAutomatically,
            @CliOption(key = "testName", mandatory = false, help = "Name of the test") String testName,
            @CliOption(key = "testServerUrl", mandatory = false, unspecifiedDefaultValue = "http://localhost:8080/", specifiedDefaultValue = "http://localhost:8080/", help = "URL of the server where the web application is available, including protocol, port and hostname") String url) {

        boolean added = operations.addRelationPattern(controllerClass, name,
                field, type);

        // Generate optionally Selenium tests
        if (added && testAutomatically) {

            // Create test with defined name or with pattern name by default
            if (testName == null) {
                testName = name.getSymbolName();
            }

            seleniumServices.generateTestDetailTabular(controllerClass, type,
                    field, testName, url);
        }
    }

    /**
     * Installs the static resources (images, css, js) and TAGx
     */
    @CliCommand(value = "web mvc pattern setup", help = "Installs static resources (images, css, js) and TAGx used by patterns")
    public void webPatternSetup() {
        operations.setup();

    }

    /**
     * Forces update the static resources (images, css, js) and TAGx
     */
    @CliCommand(value = "web mvc pattern update tags", help = "Update static resources (images, css, js) and TAGx used by patterns forcing overwrite of them")
    public void webPatternUpdateTags() {
        operations.updatePattern();
    }

}
