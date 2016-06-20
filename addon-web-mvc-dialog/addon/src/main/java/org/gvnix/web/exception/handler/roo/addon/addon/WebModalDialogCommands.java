/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010, 2011 CIT - Generalitat
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
package org.gvnix.web.exception.handler.roo.addon.addon;

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
 * Commands supporting Modal Dialogs
 * 
 * @author Óscar Rovira ( orovira at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class WebModalDialogCommands implements CommandMarker {

    @Reference
    private WebModalDialogOperations modalDialogOperations;
    @Reference
    private WebExceptionHandlerOperations exceptionOperations;

    @CliAvailabilityIndicator("web mvc dialog setup")
    public boolean isSetupGvNIXModalDialogAvailable() {
        return modalDialogOperations.isProjectAvailable()
                && exceptionOperations.isExceptionMappingAvailable();
    }

    @CliCommand(value = "web mvc dialog setup",
            help = "Setup support for Modal Dialogs in current project.")
    public void setupGvNIXModalDialog() {
        modalDialogOperations.setupModalDialogsSupport();
    }

    @CliAvailabilityIndicator("web mvc dialog add")
    public boolean isAddGvNIXModalDialogAvailable() {
        return modalDialogOperations.isProjectAvailable()
                && modalDialogOperations.isMessageBoxOfTypeModal();
    }

    @CliCommand(value = "web mvc dialog add",
            help = "Defines gvNIX customizable Dialog.")
    public void addGvNIXModalDialog(
            @CliOption(key = "class",
                    mandatory = true,
                    help = "The controller to apply the pattern to") JavaType controllerClass,
            @CliOption(key = "name",
                    mandatory = true,
                    help = "Identificication to use for this modal dialog") JavaSymbolName name) {
        modalDialogOperations.addModalDialogAnnotation(controllerClass, name);
    }
}
