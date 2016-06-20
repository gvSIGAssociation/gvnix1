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
/**
 *
 */
package org.gvnix.service.roo.addon.addon.security;

import java.io.IOException;
import java.util.Map;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * <p>
 * Services to manage service layer Security.
 * </p>
 * 
 * @author Jose Manuel Vivó Arnal ( jmvivo at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface SecurityService {

    /**
     * Adds dependencies and initializes WSSJ4 library in project
     */
    void setupWSSJ4();

    /**
     * Adds or update a client service definition to client-config.wsdd file
     * 
     * @param serviceName
     * @param properties
     * @throws SAXException
     * @throws IOException
     */
    void addOrUpdateAxisClientService(String serviceName,
            Map<String, String> properties) throws SAXException, IOException;

    /**
     * Wrapper method of {@link #getWsdl(String, String)}
     * 
     * @param url
     * @return
     */
    Document getWsdl(String url);

}
