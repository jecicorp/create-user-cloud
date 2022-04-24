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
      <div class="row">
        <h1>Demande de création de compte</h1>
      </div>
      <div style=" margin:  1em;">
        <form action="${url.service}" method="post" enctype="multipart/form-data">
          <div style=" margin:  1em;">
            <label for="nom">*Nom : </label>
            <input type="text" id="nom" name="nom" />
          </div>
          <div style=" margin:  1em;">
            <label for="prenom">*Prénom : </label>
            <input type="text" id="prenom" name="prenom" />
          </div>
          <div style=" margin:  1em;">
            <label for="email">*Adresse mail : </label>
            <input type="text" id="email" name="email" />
          </div>
          <#if isMember>
          <div style=" margin:  1em;">
            <label for="entreprise">Entreprise : </label>
            <input type="text" id="entreprise" name="entreprise" />
          </div>
          </#if>
          <div style=" margin:  1em;">
            <label for="code" width="min"><#if !isMember>*</#if>Code : </label>
            <input type="text" id="code" name="code" />
          </div>
          <div class="button" style=" margin:  1em;">
            <button type="submit" class="btn btn-primary">S'inscrire</button>
          </div>
        </form>
      </div>
    </div>
  </section>
</body>

</html>
