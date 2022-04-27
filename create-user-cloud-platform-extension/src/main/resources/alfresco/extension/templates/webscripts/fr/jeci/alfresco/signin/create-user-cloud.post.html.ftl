<html>

<#include "./create-user-head.ftl" />
<@head />

<body>
  <header class="navigation">
    <div class="container">
      <a class="navbar-brand" href="https://pristy.fr/">
        <img class="img-fluid" src="https://pristy.fr/images/logo.png" alt="Pristy - Gestion Documentaire" width="200px">
      </a>
    </div>
  </header>
  <section class="banner">
    <div class="container card" style:"text-align: center !important;">
    <#if user??>
      <div class="row">
        <h1>Création de compte effectuée</h1>
      </div>
      <div style=" margin:  1em;">
        <div style=" margin:  1em;">
          <span>Le compte pour 
          <b><#if user.properties.firstName??>${user.properties.firstName}</#if> <#if user.properties.lastName??>${user.properties.lastName}</#if></b>
          a bien été créé.</span>
        </div>
        <div style=" margin:  1em;">
          <span>Vos information de connexion ont été envoyées à l'adresse : </span>
          <span><#if user.properties.email??>${user.properties.email}</#if></span>
        </div>
        <#if site??>
        <div style=" margin:  1em;">
          <span>Vous pourrez avec ces derniers accéder à votre espace de démonstration </span>
          <a href="${serverPath}/pristy/#/libraries/${site.id}"><#if site.properties.title??>${site.properties.title}</#if></a>
        </div>
        </#if>
      </div>
    </#if>
    </div>
  </section>
</body>

</html>
