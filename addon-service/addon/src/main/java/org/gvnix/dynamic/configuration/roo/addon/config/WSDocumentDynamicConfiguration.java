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
package org.gvnix.dynamic.configuration.roo.addon.config;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * Dynamic configuration manager of document type web services.
 * 
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
@Component
@Service
public class WSDocumentDynamicConfiguration extends
        XpathElementsDynamicConfiguration implements
        DefaultDynamicConfiguration {

    /**
     * {@inheritDoc}
     */
    public String getName() {

        return "Document Type Web Services WSDL";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFilePath() {

        return "pom.xml";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getXpath() {

        return "/project/build/plugins/plugin"
                + "[groupId='org.apache.cxf' and artifactId='cxf-codegen-plugin']/"
                + "executions/execution[phase='generate-sources']";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {

        return "id";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue() {

        return "configuration/wsdlOptions/wsdlOption/wsdl";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPrefix() {

        return "wsdl.";
    }
}
