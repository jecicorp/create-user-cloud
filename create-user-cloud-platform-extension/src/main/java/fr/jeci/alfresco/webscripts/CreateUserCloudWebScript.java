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
package fr.jeci.alfresco.webscripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;

import fr.jeci.alfresco.model.CloudJeciModel;

/**
 * @author PIASSALE Cindy Jeci
 *
 */
public class CreateUserCloudWebScript extends DeclarativeWebScript {
	private static Log logger = LogFactory.getLog(CreateUserCloudWebScript.class);

	private static final Long AUTHORIZED_QUOTA = Long.parseLong("524288000"); // 500Mo
	private static final String EMAIL_PATTERN = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\\\.[A-Za-z0-9-]+)*(\\\\.[A-Za-z]{2,})$";
	private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

	private MutableAuthenticationService authenticationService;
	private PersonService personService;
	private NodeService nodeService;

	private List<String> domainBlack = new ArrayList<>(1);
	private List<String> validCode = new ArrayList<>(1);

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		Map<String, Object> model = new HashMap<>();
		NodeRef user = null;

		// Récupération des données du formulaire
		String email = null;
		String code = null;

		final FormData form = (FormData) req.parseContent();
		for (FormData.FormField field : form.getFields()) {
			switch (field.getName()) {
			case "email":
				email = field.getValue();
				break;
			case "code":
				code = field.getValue();
				break;
			default:
				break;
			}
		}

		if (email == null || code == null) {
			logger.error("CreateUserCloud - Un paramètre est manquant");
			return model;
		}

		if (!isValidEmail(email)) {
			logger.error("CreateUserCloud - Email non valide");
			return model;
		}

		if (isForbidDomain(email)) {
			logger.error("CreateUserCloud - Domaine email non authorisé");
			return model;
		}

		final String[] data = email.split("@");

		if (this.personService.personExists(data[0])) {
			// L'utilisateur existe déjà
			logger.error("CreateUserCloud - Un utilisateur existe déjà avec le meme login : " + data[0]);
			return model;
		}

		// validation du code
		if (isValidCode(code)) {
			logger.error("CreateUserCloud - Code non valide : " + code);
			return model;
		}

		// Génération du mot de passe
		final String password = generatePassword();
		// Création de l'utilisateur
		user = createUser(data[0], data[1], password, email, code);
		model.put("user", user);
		return model;
	}

	/**
	 * Validation de l'email
	 * 
	 * @param mail
	 * @return <true> or <false>
	 */
	private boolean isValidEmail(String email) {
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

	private boolean isForbidDomain(String email) {
		final String[] data = email.split("@");
		String domain = data[1];

		if (StringUtils.isBlank(domain)) {
			return false;
		}

		for (String dom : domainBlack) {
			if (dom.equalsIgnoreCase(domain)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Validation du code
	 * 
	 * @param code
	 * @return <true> or <false>
	 */
	private boolean isValidCode(final String code) {
		if (StringUtils.isBlank(code)) {
			return false;
		}

		String tcode = code.trim().toUpperCase();

		for (String vcode : this.validCode) {
			if (vcode.equals(tcode)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Génération d'un mot de passe basé sur 12 charactères
	 * 
	 * @return
	 */
	private String generatePassword() {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
		return RandomStringUtils.random(12, characters);
	}

	private NodeRef createUser(String username, String lastname, String password, String email, String code) {
		ParameterCheck.mandatory("firstname", username);
		ParameterCheck.mandatory("lastname", lastname);
		ParameterCheck.mandatory("password", password);
		ParameterCheck.mandatory("email", email);
		ParameterCheck.mandatory("code", code);

		NodeRef person = null;

		PropertyMap properties = new PropertyMap();
		properties.put(ContentModel.PROP_USERNAME, username);
		properties.put(ContentModel.PROP_FIRSTNAME, username);
		properties.put(ContentModel.PROP_LASTNAME, lastname);
		properties.put(ContentModel.PROP_EMAIL, email);
		properties.put(ContentModel.PROP_SIZE_QUOTA, AUTHORIZED_QUOTA);

		if (logger.isDebugEnabled()) {
			logger.debug("CreateUserCloudWebScript - userName / firstName : " + username);
			logger.debug("CreateUserCloudWebScript - lastName : " + lastname);
			logger.debug("CreateUserCloudWebScript - email : " + email);
			logger.debug("CreateUserCloudWebScript - password : " + password);
			logger.debug("CreateUserCloudWebScript - code : " + code);
		}
		// Create the user profile
		if (!this.personService.personExists(username)) {
			person = personService.createPerson(properties);
		}

		if (person != null) {
			// Add aspect cloud:user
			PropertyMap propsCloud = new PropertyMap();
			propsCloud.put(CloudJeciModel.PROP_SIGNIN_CODE, code.toUpperCase());
			this.nodeService.addAspect(person, CloudJeciModel.ASPECT_CLOUD_USER, propsCloud);

			// Create the user account
			if (password != null) {
				// create account for person with the userName and password
				authenticationService.createAuthentication(username, password.toCharArray());
				authenticationService.setAuthenticationEnabled(username, true);

				// Notification with login and password
				personService.notifyPerson(username, password);
			}
		}

		return person;
	}

	/**
	 * @param authenticationService the authenticationService to set
	 */
	public void setAuthenticationService(MutableAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	/**
	 * @param personService the personService to set
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	/**
	 * @param nodeService the nodeService to set
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setDomainBlacklist(String domainBlacklist) {
		this.domainBlack = Arrays.asList(domainBlacklist.split(","));
	}

	public void setValidCodes(String validCodes) {
		this.validCode = Arrays.asList(validCodes.split(","));
	}

}
