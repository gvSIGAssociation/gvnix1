/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures i
 * Transport - Generalitat Valenciana Copyright (C) 2010 CIT - Generalitat
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
package org.gvnix.dynamic.configuration.roo.addon;

import org.gvnix.dynamic.configuration.roo.addon.entity.DynConfigurationList;

/**
 * Manage POM interface.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface PomManager {

    /**
     * Write a dynamic configurations list into POM profiles. <project> <build>
     * <resources> <resource> <directory>src/main/resources</directory>
     * <filtering>true</filtering> </resource> </resources> </build> <profiles>
     * <profile> <id>test</id> <activation>
     * <activeByDefault>true</activeByDefault> </activation> <properties>
     * </properties> </profile> </profiles> </project>
     * 
     * @param dynConfs List of dynamic configuration to export
     * @return Exported dynamic configuration list
     */
    public DynConfigurationList export(DynConfigurationList dynConfs);

}
