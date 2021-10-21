/**
 * 
 */
package fr.jeci.alfresco.webscripts;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.PropertyMap;
import org.apache.commons.lang3.RandomStringUtils;
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

	private static Long AUTHORIZED_QUOTA = Long.parseLong("524288000"); // 500Mo

	private MutableAuthenticationService authenticationService;
	private PersonService personService;
	private NodeService nodeService;

	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		Map<String, Object> model = new HashMap<String, Object>();
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

		if (email != null && code != null) {
			if (isValidEmail(email)) {
				String[] data = email.split("@");

				if (!this.personService.personExists(data[0])) {
					// validation du code
					if (isValidCode(code)) {
						// Génération du mot de passe
						String password = generatePassword();
						// Création de l'utilisateur
						user = createUser(data[0], data[1], password, email, code);
						model.put("user", user);
					} else {
						logger.error("CreateUserCloud - Code non valide : " + code);
					}
				} else {
					// L'utilisateur existe déjà
					logger.error("CreateUserCloud - Un utilisateur existe déjà avec le meme login : " + data[0]);
				}
			} else {
				logger.error("CreateUserCloud - Email non valide");
			}
		} else {
			logger.error("CreateUserCloud - Un paramètre est manquant");
		}

		return model;
	}

	/**
	 * Validation de l'email
	 * 
	 * @param mail
	 * @return <true> or <false>
	 */
	private boolean isValidEmail(String mail) {
		// TODO fonction de validation de l'email : format + SPAM
		return true;
	}

	/**
	 * Validation du code
	 * 
	 * @param code
	 * @return <true> or <false>
	 */
	private boolean isValidCode(String code) {
		// TODO Ajouter d'autres code
		return CloudJeciModel.CODE_OSXP.equalsIgnoreCase(code);
	}

	/**
	 * Génération d'un mot de passe basé sur 12 charactères
	 * 
	 * @return
	 */
	private String generatePassword() {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
		String pwd = RandomStringUtils.random(12, characters);
		return pwd;
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

}
