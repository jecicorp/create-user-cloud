/*  Copyright 2021 - Jeci SARL - https://jeci.fr
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
* 
* You should have received a copy of the GNU Lesser General Public License
* along with this program.  If not, see http://www.gnu.org/licenses/.
*/
package fr.jeci.alfresco.model;

import org.alfresco.service.namespace.QName;

public interface CloudJeciModel {

	static final String CLOUD_PREFIX = "cloud";
	static final String CLOUD_URI = "http://jeci.fr/model/cloud/1.0";
	
	static final QName ASPECT_CLOUD_USER = QName.createQName(CLOUD_URI, "user");
	static final QName PROP_SIGNIN_CODE = QName.createQName(CLOUD_URI, "signinCode");
}