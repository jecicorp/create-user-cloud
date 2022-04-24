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
    <#if error??>
      <div class="row">
        <h1>Erreur lors de la création du compte</h1>
      </div>
      <div style=" margin:  1em;">
          <div style=" margin:  1em;">
            <span>Message : </span>
            <span>${error}</span>
          </div>
      </div>
    </#if>
    <#if user??>
        <div class="row">
          <h1>Création de compte effectuée</h1>
        </div>
        <div style=" margin:  1em;">
            <div style=" margin:  1em;">
              <span>Identifiant : </span>
              <span><#if user.properties.userName??>${user.properties.userName}</#if></span>
            </div>
            <div style=" margin:  1em;">
              <span>Votre mot de passe a été envoyé à l'adresse : </span>
              <span><#if user.properties.email??>${user.properties.email}</#if></span>
            </div>
        </div>
      </#if>
    </div>
  </section>
</body>

</html>
