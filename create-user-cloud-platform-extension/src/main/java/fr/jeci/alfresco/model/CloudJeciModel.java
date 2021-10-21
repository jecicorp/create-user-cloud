package fr.jeci.alfresco.model;

import org.alfresco.service.namespace.QName;

public interface CloudJeciModel {

	static final String CLOUD_PREFIX = "cloud";
	static final String CLOUD_URI = "http://jeci.fr/model/cloud/1.0";
	
	static final String CODE_OSXP = "OSXP2021";

	static final QName ASPECT_CLOUD_USER = QName.createQName(CLOUD_URI, "user");
	static final QName PROP_SIGNIN_CODE = QName.createQName(CLOUD_URI, "signinCode");
}