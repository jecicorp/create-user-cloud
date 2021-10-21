<html>
  <body>
    <h1>Demande de création de compte</h1>
    <form action="${url.service}" method="post" enctype="multipart/form-data">
      <div>
        <label for="email">Adresse mail : </label>
        <input type="text" id="email" name="email" />
      </div>
      <div>
        <label for="code">Code : </label>
        <input type="text" id="code" name="code" />
      </div>
      <div class="button">
        <button type="submit">S'inscrire</button>
      </div>
    </form>
  </body>
</html>
