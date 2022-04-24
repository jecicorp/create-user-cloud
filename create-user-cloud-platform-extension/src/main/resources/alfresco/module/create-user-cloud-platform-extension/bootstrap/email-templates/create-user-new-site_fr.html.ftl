<html>
  <p style="margin-top:0pt;margin-bottom:1em;font-family:Arial;font-size:12pt">
    <span>
      Bonjour M. ${createdUser.properties["cm:firstName"]} ${createdUser.properties["cm:lastName"]},
    </span>
  </p>

  <p style="margin-top:1em;margin-bottom:1em;font-family:Arial;font-size:12pt">
    <span>Voici des accès pour tester Pristy.</span><br />
    <span>Accéder à notre plateforme démo à ce lien : </span>
    <a href="${serverPath}/pristy/">${serverPath}</a><br />
  </p>   

  <p style="margin-top:1em;margin-bottom:1em;font-family:Arial;font-size:12pt">
    <span>Vos informations de connexion sont les suivantes :</span>
    <ul>
      <li>
        <span style="font-family:Arial;font-size:12pt">Nom d'utilisateur : </span>
        <span style="font-family:Arial;font-size:12pt;font-weight:bold">${createdUser.properties["cm:userName"]}</span>
      </li>
      <li>
        <span style="font-family:Arial;font-size:12pt">Mot de passe : </span>
        <span style="font-family:Arial;font-size:12pt;font-weight:bold">${password}</span>
      </li>
    </ul>
    <span style="font-family:Arial;font-size:12pt">Votre compte est limité à 500Mo d'espace disque pour vous laisser la possibilité de découvrir</span>
    <span style="font-family:Arial;font-size:12pt"><a href="${serverPath}/pristy/">Pristy</a></span>
  </p>

  <p style="margin-top:1em;margin-bottom:1em;font-family:Arial;font-size:12pt">
    <span>Dans </span>
    <a href="${serverPath}/pristy/#/libraries/">vos espaces de travail</a>
    <span> se trouvent un espace démo </span
    <a href="${serverPath}/pristy/#/libraries/${siteRef.id}">${siteRef.properties["cm:title"]}</a>
    <span> qui vous permet d'avoir accès à des fichiers et médias tests et d'essayer nos différentes options.</span><br />
    <span>Vous pouvez évidemment ajouter les espaces, dossiers et documents de votre choix.</span>
  </p>

  <p style="margin-top:1em;margin-bottom:1em;font-family:Arial;font-size:12pt">
    <span>Si certains de vos collègues veulent essayer </span>
    <a href="${serverPath}/pristy/">Pristy</a>
    <span> ou si vous voulez tester la fonction de partage, n'hésitez pas à </span>
    <a href="${serverPath}/alfresco/s/fr/jeci/signin/create-user">créer d'autres comptes</a></br>
    <span>Utiliser le code </span>
    <span style="font-weight:bold">${siteRef.properties["cloud:signinCodeSite"]}</span>
    <span> pour l'ajouter dans votre espace de travail </span>
    <a href="${serverPath}/pristy/#/libraries/${siteRef.id}">${siteRef.properties["cm:title"]}</a><br />
    <span>Vous pouvez aussi nous transmettre adresse mail et nom à </span>
    <a href="mailto:info@jeci.fr?subject=Démo Pristy - Nouveaux comptes">info@jeci.fr</a>
    <span>pour que nous les créons pour vous.</span><br />
  </p>
  
  <p style="margin-top:1em;margin-bottom:1em;font-family:Arial;font-size:12pt">
    <span>Pour information la fonction de partage va connaître une mise à jour conséquente dans les semaines à venir.</span>
  </p>
  
  <p style="margin-top:1em;margin-bottom:1em;font-family:Arial;font-size:12pt">
    <span>Nous restons à votre disposition si vous avez des questions ou besoins de précisions (</span>
    <a href="mailto:info@jeci.fr?subject=Infos Démo Pristy">info@jeci.fr</a>
    <span>).</span>
  </p>

  <p style="margin-top:1em;margin-bottom:1em;font-family:Arial;font-size:12pt">
    <span>À toutes fins utiles vous pouvez consulter la </span>
    <a href="https://docs.pristy.fr/">documentation version bêta de Pristy</a>
    <span>.</span>
  </p>
    
  <p style="margin-top:1em;margin-bottom:1em;font-family:Arial;font-size:12pt">
    <span>Bien cordialement</span>
  </p>

  <p style="margin-top:2pt;margin-bottom:0pt;font-family:Arial;font-size:10pt;font-style:italic;color:#808080">
    <span>Ce mail est généré automatiquement, </span>
    <span style="font-weight:bold">ne pas répondre à ce mail</span>
    <span>. Pour toutes questions merci d'écrire à </span>
    <a href="mailto:info@jeci.fr?subject=Infos Démo Pristy">info@jeci.fr</a>
    <span>.</span>
  </p>
</html>