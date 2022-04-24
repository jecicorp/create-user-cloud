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

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.rest.api.impl.SiteImportPackageHandler;
import org.alfresco.rest.api.impl.SiteSurfConfig;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.invitation.InvitationException;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.view.ImportPackageHandler;
import org.alfresco.service.cmr.view.ImporterBinding;
import org.alfresco.service.cmr.view.ImporterContentCache;
import org.alfresco.service.cmr.view.ImporterProgress;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.jeci.alfresco.model.CloudJeciModel;

/**
 * @author PIASSALE Cindy Jeci
 *
 */
public class CreateUserCloudWebScript extends DeclarativeWebScript {

	// Req parameters
	private static final String PARAM_NOM = "nom";
	private static final String PARAM_PRENOM = "prenom";
	private static final String PARAM_EMAIL = "email";
	private static final String PARAM_ENTREPRISE = "entreprise";
	private static final String PARAM_CODE = "code";

	private static final int PASSWORD_LENGTH = 18;
	private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final int CODE_LENGTH = 6;

	private static final String PATTERN_USERNAME = "{0}.{1}";
	private static final String SPLIT_EMAIL = "@";

	private static Log logger = LogFactory.getLog(CreateUserCloudWebScript.class);

	private static final Long AUTHORIZED_QUOTA = Long.parseLong("524288000"); // 500Mo
	private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
	private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

	// Services
	private MutableAuthenticationService authenticationService;
	private AuthorityService authorityService;
	private PersonService personService;
	private NodeService nodeService;
	private SearchService searchService;
	private SiteService siteService;
	private FileFolderService fileFolderService;
	private NamespaceService namespaceService;
	private SiteSurfConfig siteSurfConfig;
	private ImporterService importerService;
	private ActionService actionService;

	private List<String> domainBlack = new ArrayList<>(1);
	private String siteModeleShortName;
	private String inboxMailCopy;

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		if (logger.isDebugEnabled()) {
			logger.debug("CreateUserCloudWebscript START");
		}

		Map<String, Object> model = new HashMap<>();
		String currentUserName = authenticationService.getCurrentUserName();
		boolean isCreateUserAdmin = isMemberOf(CloudJeciModel.GROUP_CREATE_USER_ADMIN, currentUserName);
		// Check parameters
		checkParameters(req, isCreateUserAdmin);

		// Get parameters
		String lastName = req.getParameter(PARAM_NOM);
		String firstName = req.getParameter(PARAM_PRENOM);
		String mail = req.getParameter(PARAM_EMAIL);
		String enterprise = req.getParameter(PARAM_ENTREPRISE);
		String code = req.getParameter(PARAM_CODE);

		// Check validation mail
		String tlcEmail = mail.trim().toLowerCase();
		isValidEmail(tlcEmail);
		isForbidDomain(tlcEmail);

		// Check valid code
		NodeRef siteRef = null;
		if (StringUtils.isNotBlank(code)) {
			siteRef = getSiteFromCode(code);
		}

		// Création de l'utilisateur
		String userName = MessageFormat.format(PATTERN_USERNAME, withoutSpaceAndAccent(firstName),
				withoutSpaceAndAccent(lastName));
		final String password = generateRandomString(PASSWORD_LENGTH, PASSWORD_CHARS);
		NodeRef user = createUser(userName, password, tlcEmail, firstName, lastName, enterprise);

		// Add user in site correponding at the code
		NodeRef templateMail = null;
		if (siteRef != null) {
			// Template mail - Add user in site
			templateMail = getNotifyEmailTemplateNodeRef(CloudJeciModel.XPATH_TEMPLATE_CREATE_USER_EXISTING_SITE);
		} else if (isCreateUserAdmin && StringUtils.isNotBlank(enterprise)) {
			// Create a site for the enterprise
			String shortName = withoutSpaceAndAccent(enterprise);
			SiteInfo newSite = siteService
					.getSite(MessageFormat.format(CloudJeciModel.PATTERN_SITE_DEMO_SHORTNAME, shortName));
			if (newSite == null) {
				newSite = initializeSite(MessageFormat.format(CloudJeciModel.PATTERN_SITE_DEMO_SHORTNAME, shortName),
						enterprise);
			}
			siteRef = newSite.getNodeRef();
			// Template mail - New site with code
			templateMail = getNotifyEmailTemplateNodeRef(CloudJeciModel.XPATH_TEMPLATE_CREATE_USER_NEW_SITE);
		}

		// Add user manager
		siteService.setMembership((String) nodeService.getProperty(siteRef, ContentModel.PROP_NAME), userName,
				SiteModel.SITE_MANAGER);
		// Notification with login and password
		notifyPerson(personService.getPerson(currentUserName), user, password, siteRef, templateMail, req.getServerPath());

		model.put("user", user);
		model.put("site", siteRef);

		return model;
	}

	/**
	 * Test if the current user is member of the group <code>authorityName</code>
	 * 
	 * @param authorityName : group's authorityName
	 * @param userName
	 * @return <code>true</code> or <code>false</code>
	 */
	private boolean isMemberOf(String authorityName, String userName) {
		AuthenticationUtil.pushAuthentication();
		try {
			AuthenticationUtil.setRunAsUser(CloudJeciModel.USER_ADMIN);
			Set<String> authorities = authorityService.getAuthoritiesForUser(userName);
			for (String authority : authorities) {
				if (authorityName.equals(authority)) {
					return true;
				}
			}
		} finally {
			AuthenticationUtil.popAuthentication();
		}

		return false;
	}

	/**
	 * Check mandatory parameters
	 * 
	 * @param request
	 * @param isCreateUserAdmin
	 */
	private void checkParameters(WebScriptRequest request, boolean isCreateUserAdmin) {
		if (StringUtils.isBlank(request.getParameter(PARAM_NOM))) {
			throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Le paramètre 'Nom' est manquant");
		}
		if (StringUtils.isBlank(request.getParameter(PARAM_PRENOM))) {
			throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Le paramètre 'Prénom' est manquant");
		}
		if (StringUtils.isBlank(request.getParameter(PARAM_EMAIL))) {
			throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Le paramètre 'Email' est manquant");
		}
		if (!isCreateUserAdmin && StringUtils.isBlank(request.getParameter(PARAM_CODE))) {
			throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Le paramètre 'Code' est manquant");
		}
		if (isCreateUserAdmin && StringUtils.isBlank(request.getParameter(PARAM_ENTREPRISE))
				&& StringUtils.isBlank(request.getParameter(PARAM_CODE))) {
			throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Le paramètre 'Entreprise' ou 'Code' est manquant");
		}
	}

	/**
	 * Validation de l'email
	 *
	 * @param mail
	 */
	private void isValidEmail(String email) {
		Matcher matcher = pattern.matcher(email);
		if (!matcher.matches()) {
			throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Le paramètre 'email' n'est pas valide");
		}
	}

	/**
	 * Check if the domain of email is forbidden
	 * 
	 * @param email
	 */
	private void isForbidDomain(String email) {
		final String[] data = email.split(SPLIT_EMAIL);
		String domain = data[1];

		if (StringUtils.isBlank(domain)) {
			throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Le domaine email n'est pas authorisé");
		}

		for (String dom : domainBlack) {
			if (dom.equalsIgnoreCase(domain)) {
				throw new WebScriptException(HttpStatus.SC_BAD_REQUEST, "Le domaine email n'est pas authorisé");
			}
		}
	}

	/**
	 * Format a string without accent and replace space by '-'
	 * 
	 * @param stringToFormat
	 * @return the string formatted
	 */
	private String withoutSpaceAndAccent(String stringToFormat) {
		String stringLowerAndStrip = stringToFormat.strip().replace(' ', '-').toLowerCase();
		String stringNormalized = Normalizer.normalize(stringLowerAndStrip, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(stringNormalized).replaceAll("");
	}

	/**
	 * Search the corresponding site
	 *
	 * @param code : meta-data cloud:signinCodeSite
	 * @return <code>null</code> or the <code>NodeRef</code> of the site
	 */
	private NodeRef getSiteFromCode(String code) {
		StringBuilder query = new StringBuilder();
		query.append("TYPE:\"").append(SiteModel.TYPE_SITE.toString()).append("\"");
		query.append(" AND =").append(CloudJeciModel.PROP_SIGNIN_CODE_SITE.toString());
		query.append(":\"").append(code).append("\"");

		if (logger.isDebugEnabled()) {
			logger.debug("----- CreateUserCloudWebscript - getSiteFromCode - query : " + query.toString());
		}

		ResultSet result = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,
				SearchService.LANGUAGE_FTS_ALFRESCO, query.toString());
		try {
			List<NodeRef> nodeRefs = result.getNodeRefs();
			if (nodeRefs == null || nodeRefs.isEmpty()) {
				throw new WebScriptException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
						"Aucun 'Espace de travail' ne correspond au code : " + code);
			}
			return nodeRefs.get(0);
		} finally {
			if (result != null) {
				result.close();
			}
		}
	}

	/**
	 * Génération d'une chaîne de caractères aléatoires
	 *
	 * @param length               : nombre de caractères dans la chaine
	 * @param authorizedCharacters : caractères utilisés pour générer la chaine
	 * @return la chaine de caractère aléatoire
	 */
	private String generateRandomString(int length, String authorizedCharacters) {
		return RandomStringUtils.random(length, authorizedCharacters);
	}

	/**
	 * Création d'un compte utilisateur
	 * 
	 * @param userName
	 * @param password
	 * @param mail
	 * @param code
	 * @param prenom
	 * @param nom
	 * @param entreprise
	 * @return
	 */
	private NodeRef createUser(String userName, String password, String mail, String firstName, String lastName,
			String enterprise) {
		AuthenticationUtil.pushAuthentication();
		NodeRef person = null;
		try {
			AuthenticationUtil.setRunAsUser(CloudJeciModel.USER_ADMIN);

			// Check user exist
			if (this.personService.personExists(userName)) {
				throw new WebScriptException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
						"Un utilisateur existe déjà avec cet identifiant : " + userName);
			}

			PropertyMap properties = new PropertyMap(5);
			properties.put(ContentModel.PROP_USERNAME, userName);
			properties.put(ContentModel.PROP_FIRSTNAME, firstName);
			properties.put(ContentModel.PROP_LASTNAME, lastName);
			properties.put(ContentModel.PROP_EMAIL, mail);
			properties.put(ContentModel.PROP_SIZE_QUOTA, AUTHORIZED_QUOTA);
			if (StringUtils.isNotBlank(enterprise)) {
				properties.put(ContentModel.PROP_ORGANIZATION, enterprise);
			}

			if (logger.isInfoEnabled()) {
				logger.info(String.format("CreateUser %s;%s;%s;%s;%s", firstName, lastName, enterprise, password, mail));
			}

			person = personService.createPerson(properties);
			if (person == null) {
				logger.error("createPerson return null");
				throw new WebScriptException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
						"Echec de la création du profil utilisateur");
			}

			// Create the user account
			// create account for person with the userName and password
			authenticationService.createAuthentication(userName, password.toCharArray());
			authenticationService.setAuthenticationEnabled(userName, true);
		} catch (AuthenticationException e) {
			throw new WebScriptException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
					"Echec de la création du compte utilisateur");
		} finally {
			AuthenticationUtil.popAuthentication();
		}

		return person;
	}

	/**
	 * Send notification with account information
	 * 
	 * @param currentUser
	 * @param createdUser
	 * @param password
	 * @param siteRef
	 * @param templateMail
	 * @paramm serverPath
	 */
	private void notifyPerson(NodeRef currentUser, NodeRef createdUser, String password, NodeRef siteRef,
			NodeRef templateMail, String serverPath) {
		Map<String, Serializable> model = new HashMap<>(4);
		model.put("currentUser", currentUser);
		model.put("createdUser", createdUser);
		model.put("siteRef", siteRef);
		model.put("password", password);
		model.put("serverPath", serverPath);

		// Set the details for the action
		Map<String, Serializable> actionParams = new HashMap<String, Serializable>();
		actionParams.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) model);
		actionParams.put(MailActionExecuter.PARAM_TO, nodeService.getProperty(createdUser, ContentModel.PROP_EMAIL));
		actionParams.put(MailActionExecuter.PARAM_SUBJECT, "Pristy - Accès à la plateforme de démo");
		actionParams.put(MailActionExecuter.PARAM_BCC, inboxMailCopy);

		// Pick the appropriate localised template
		actionParams.put(MailActionExecuter.PARAM_TEMPLATE, templateMail);

		// Ask for the email to be sent asynchronously
		Action mailAction = actionService.createAction(MailActionExecuter.NAME, actionParams);
		actionService.executeAction(mailAction, siteRef, false, false);
	}

	/**
	 * Finds the email template and then attempts to find a localized version
	 * 
	 * @param xpath
	 * @return <code>NodeRef</code> of the email template
	 */
	private NodeRef getNotifyEmailTemplateNodeRef(String xpath) {
		try {
			NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
			List<NodeRef> nodeRefs = searchService.selectNodes(rootNodeRef, xpath, null, namespaceService, false);
			if (nodeRefs.size() > 1) {
				logger.error("Found too many email templates using: " + xpath);
				nodeRefs = Collections.singletonList(nodeRefs.get(0));
			} else if (nodeRefs.size() == 0) {
				throw new InvitationException("Cannot find the email template using " + xpath);
			}
			// Now localise this
			NodeRef base = nodeRefs.get(0);
			NodeRef local = fileFolderService.getLocalizedSibling(base);
			return local;
		} catch (SearcherException e) {
			throw new InvitationException("Cannot find the email template!", e);
		}
	}

	/**
	 * Création du site et recopie des données d'exemple
	 * 
	 * @param shortName
	 * @param enterprise
	 * @return
	 */
	private SiteInfo initializeSite(String shortName, String enterprise) {
		SiteInfo newSite = siteService.createSite(CloudJeciModel.SITE_PRESET_DASHBOARD, shortName,
				MessageFormat.format(CloudJeciModel.PATTERN_SITE_DEMO_TITLE, enterprise),
				CloudJeciModel.SITE_DEMO_DESCRIPTION, SiteVisibility.MODERATED);

		// import default/fixed preset Share surf config
		importSite(newSite.getShortName(), newSite.getNodeRef());

		// Initialize documentLibrary
		NodeRef docLibNewSite = siteService.createContainer(newSite.getShortName(), SiteService.DOCUMENT_LIBRARY,
				ContentModel.TYPE_FOLDER, null);

		// Add code d'inscription
		String code = generateRandomString(CODE_LENGTH, PASSWORD_CHARS);
		Map<QName, Serializable> props = new HashMap<>(1);
		props.put(CloudJeciModel.PROP_SIGNIN_CODE_SITE, code);
		nodeService.addAspect(newSite.getNodeRef(), CloudJeciModel.ASPECT_CLOUD_SITE, props);

		if (logger.isDebugEnabled()) {
			logger.debug("CreateUserCloudWebScript - New site : " + shortName);
			logger.debug("CreateUserCloudWebScript - Code d'inscription : " + code);
		}

		// Add folder and file examples
		SiteInfo siteTemplate = siteService.getSite(this.siteModeleShortName);
		if (siteTemplate == null) {
			throw new WebScriptException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Aucun dossier modèle n'a été trouvé");
		}
		NodeRef docLibSiteTemplate = siteService.getContainer(siteModeleShortName, SiteService.DOCUMENT_LIBRARY);
		List<FileInfo> filesAndFoldersTemplates = fileFolderService.list(docLibSiteTemplate);
		for (FileInfo template : filesAndFoldersTemplates) {
			try {
				fileFolderService.copy(template.getNodeRef(), docLibNewSite, null);
			} catch (FileExistsException | FileNotFoundException e) {
				throw new WebScriptException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
						"Echec de la copie des dossiers et fichiers modèles");
			}
		}
		return newSite;
	}

	private void importSite(final String siteId, final NodeRef siteNodeRef) {
		ImportPackageHandler acpHandler = new SiteImportPackageHandler(siteSurfConfig, siteId);
		Location location = new Location(siteNodeRef);
		ImporterBinding binding = new ImporterBinding() {
			@Override
			public String getValue(String key) {
				if (key.equals("siteId")) {
					return siteId;
				}
				return null;
			}

			@Override
			public UUID_BINDING getUUIDBinding() {
				return UUID_BINDING.CREATE_NEW;
			}

			@Override
			public QName[] getExcludedClasses() {
				return null;
			}

			@Override
			public boolean allowReferenceWithinTransaction() {
				return false;
			}

			@Override
			public ImporterContentCache getImportConentCache() {
				return null;
			}
		};
		importerService.importView(acpHandler, location, binding, (ImporterProgress) null);
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

	/**
	 * @param authorityService the authorityService to set
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	/**
	 * @param searchService the searchService to set
	 */
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	/**
	 * @param siteService the siteService to set
	 */
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	/**
	 * @param fileFolderService the fileFolderService to set
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	/**
	 * @param namespaceService the namespaceService to set
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * @param siteModeleShortName the siteModeleShortName to set
	 */
	public void setSiteModeleShortName(String siteModeleShortName) {
		this.siteModeleShortName = siteModeleShortName;
	}

	/**
	 * @param siteSurfConfig the siteSurfConfig to set
	 */
	public void setSiteSurfConfig(SiteSurfConfig siteSurfConfig) {
		this.siteSurfConfig = siteSurfConfig;
	}

	/**
	 * @param importerService the importerService to set
	 */
	public void setImporterService(ImporterService importerService) {
		this.importerService = importerService;
	}

	/**
	 * @param actionService the actionService to set
	 */
	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	/**
	 * @param inboxMailCopy the inboxMailCopy to set
	 */
	public void setInboxMailCopy(String inboxMailCopy) {
		this.inboxMailCopy = inboxMailCopy;
	}

}
