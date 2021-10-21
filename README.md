# Create User Cloud

## Description

Ajout d'un formulaire pour saisir :
* une adresse mail
* un code

Si le code est accepté un compte utilisateur sera créé avec les éléments suivants :
* username : email sans le domaine
* mail : mail saisi dans le formulaire
* firstname: email sans le domaine
* lastname : nom de domaine de l'email
* mot de passe généré
* ajout du code sur le profil de l'utilisateur

Un mail est envoyé à l'adresse mail indiquée avec l'identifiant et le mot de passe.


## Test

```
mvn resources:resources
pip install docker-compose
docker-compose -f ./target/classes/docker-compose.yml up --build -d
```

Accès au formulaire : `http://localhost:8080/alfresco/s/fr/jeci/signin/create-user`
