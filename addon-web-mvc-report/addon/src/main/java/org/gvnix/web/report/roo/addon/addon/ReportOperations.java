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
package org.gvnix.web.report.roo.addon.addon;

import org.gvnix.web.report.roo.addon.annotations.GvNIXReports;
import org.springframework.roo.model.JavaType;

/**
 * Interface of operations this add-on offers. Typically used by a command type
 * or an external add-on.
 * 
 * @author Oscar Rovira (orovira at disid dot com) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 * @since 0.6
 */
public interface ReportOperations {

    /**
     * Indicate commands should be available
     * 
     * @return true if it should be available, otherwise false
     */
    boolean isCommandAvailable();

    /**
     * Indicate if project is already created
     * 
     * @return
     */
    boolean isProjectAvailable();

    /**
     * Annotate the provided Java type with {@link GvNIXReports}
     */
    void annotateType(JavaType type, String reportName, String format);

}
