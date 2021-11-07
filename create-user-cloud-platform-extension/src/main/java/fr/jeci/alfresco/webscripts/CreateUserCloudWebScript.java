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
	private static final int PASSWORD_LENGTH = 18;
	private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	private static final String ERROR = "error";
	private static final String SPLIT_EMAIL = "@";
	private static final String CODE = "code";
	private static final String EMAIL = "email";
	private static final String PASSWORD = "password";
	private static final String LASTNAME = "lastname";
	private static final String FIRSTNAME = "firstname";

	private static Log logger = LogFactory.getLog(CreateUserCloudWebScript.class);

	private static final Long AUTHORIZED_QUOTA = Long.parseLong("524288000"); // 500Mo
	private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
	private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

	private MutableAuthenticationService authenticationService;
	private PersonService personService;
	private NodeService nodeService;

	private List<String> domainBlack = new ArrayList<>(1);
	private List<String> validCodes = new ArrayList<>(1);

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
			case EMAIL:
				email = field.getValue();
				break;
			case CODE:
				code = field.getValue();
				break;
			default:
				break;
			}
		}

		if (StringUtils.isBlank(email)) {
			model.put(ERROR, "Le paramètre 'email' est manquant");
			return model;
		}

		String tlcEmail = email.trim().toLowerCase();

		if (StringUtils.isBlank(code)) {
			model.put(ERROR, "Le paramètre 'code' est manquant");
			return model;
		}

		String tucCode = code.trim().toUpperCase();

		if (!isValidEmail(tlcEmail)) {
			model.put(ERROR, "Le paramètre 'email' n'est pas valide");
			return model;
		}

		if (isForbidDomain(tlcEmail)) {
			model.put(ERROR, "Le domaine email n'est pas authorisé");
			return model;
		}

		final String[] data = tlcEmail.split(SPLIT_EMAIL);

		if (this.personService.personExists(data[0])) {
			// L'utilisateur existe déjà
			model.put(ERROR, "Un utilisateur existe déjà avec le meme login");
			return model;
		}

		// validation du code
		if (!isValidCode(tucCode)) {
			model.put(ERROR, "CreateUserCloud - Code non valide : " + code);
			return model;
		}

		// Génération du mot de passe
		final String password = generatePassword();
		// Création de l'utilisateur
		user = createUser(data[0], data[1], password, tlcEmail, tucCode);
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
		final String[] data = email.split(SPLIT_EMAIL);
		String domain = data[1];

		if (StringUtils.isBlank(domain)) {
			return true;
		}

		for (String dom : domainBlack) {
			if (dom.equalsIgnoreCase(domain)) {
				return true;
			}
		}
		return false;
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

		for (String vcode : this.validCodes) {
			if (vcode.equals(code)) {
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
		return RandomStringUtils.random(PASSWORD_LENGTH, PASSWORD_CHARS);
	}

	private NodeRef createUser(String username, String lastname, String password, String email, String code) {
		ParameterCheck.mandatory(FIRSTNAME, username);
		ParameterCheck.mandatory(LASTNAME, lastname);
		ParameterCheck.mandatory(PASSWORD, password);
		ParameterCheck.mandatory(EMAIL, email);
		ParameterCheck.mandatory(CODE, code);

		NodeRef person = null;

		PropertyMap properties = new PropertyMap(5);
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
			PropertyMap propsCloud = new PropertyMap(1);
			propsCloud.put(CloudJeciModel.PROP_SIGNIN_CODE, code.toUpperCase());
			this.nodeService.addAspect(person, CloudJeciModel.ASPECT_CLOUD_USER, propsCloud);

			// Create the user account
			// create account for person with the userName and password
			authenticationService.createAuthentication(username, password.toCharArray());
			authenticationService.setAuthenticationEnabled(username, true);

			// Notification with login and password
			personService.notifyPerson(username, password);
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
		this.validCodes = Arrays.asList(validCodes.split(","));
	}

}
