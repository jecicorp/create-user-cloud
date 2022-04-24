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

	String CLOUD_PREFIX = "cloud";
	String CLOUD_URI = "http://jeci.fr/model/cloud/1.0";

	// Aspect for user
	QName ASPECT_CLOUD_USER = QName.createQName(CLOUD_URI, "user");
	QName PROP_SIGNIN_CODE = QName.createQName(CLOUD_URI, "signinCode");

	// Aspect for site
	QName ASPECT_CLOUD_SITE = QName.createQName(CLOUD_URI, "site");
	QName PROP_SIGNIN_CODE_SITE = QName.createQName(CLOUD_URI, "signinCodeSite");
	String GROUP_CREATE_USER_ADMIN = "GROUP_CREATE_USER_ADMINISTRATORS";

	// Email template
	String XPATH_TEMPLATE_CREATE_USER_EXISTING_SITE = "app:company_home/app:dictionary/app:email_templates/cm:create-user-email-templates/cm:create-user-existing-site.html.ftl";
	String XPATH_TEMPLATE_CREATE_USER_NEW_SITE = "app:company_home/app:dictionary/app:email_templates/cm:create-user-email-templates/cm:create-user-new-site.html.ftl";
}
