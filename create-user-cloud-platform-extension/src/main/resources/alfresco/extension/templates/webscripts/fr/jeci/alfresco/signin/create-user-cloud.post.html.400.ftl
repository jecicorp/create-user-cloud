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
          <h1>Erreur lors de la création du compte</h1>
        </div>
        <div style=" margin:  1em;">
          <div style=" margin:  1em;">
            <span>Message : </span>
            <span>${status.message}</span>
          </div>
        </div>
        <div style=" margin:  1em;">
          <div style=" margin:  1em;">
            <span>Besoin d'aide : envoyer un mail à </span>
            <a href="mailto:info@jeci.fr?subject=Erreur Démo Pristy">info@jeci.fr</a>
          </div>
        </div>
      </div>
    </section>
  </body>

</html>