# Tiny Gestion

Tiny Gestion est une application simple destinée à aider un propriétaire de tiny house louée en courte durée à suivre ses recettes, ses dépenses, ses justificatifs et ses amortissements estimatifs.

L'application est pensée pour un utilisateur non comptable. Elle doit être claire, guidée et facile à utiliser sur mobile.

## Objectif

L'application permet de :

* saisir les recettes Airbnb, Booking ou directes ;
* saisir les dépenses liées à la location ;
* conserver les justificatifs ;
* suivre les biens amortissables ;
* estimer le résultat annuel ;
* préparer un dossier propre en cas de déclaration ou de contrôle ;
* guider l'utilisateur avec des checklists mensuelles et annuelles.

## Principe important

Tiny Gestion ne remplace pas un expert-comptable, une déclaration fiscale officielle ou une validation par l'administration fiscale.

Les calculs fournis par l'application sont des estimations destinées au suivi personnel de l'activité de location.

## Stack technique

Backend :

* Java 21
* Spring Boot 3
* Spring Security
* PostgreSQL
* Flyway
* JWT ou session sécurisée
* API REST

Frontend :

* Angular 20
* PWA
* Interface mobile-first
* Formulaires simples
* Libellés en français

## Convention de nommage

Tout le domaine métier doit être nommé en français, sans accents.

Exemples :

* `Proprietaire`
* `Logement`
* `Recette`
* `Depense`
* `BienAmortissable`
* `DocumentJustificatif`
* `calculerAmortissementAnnuel`
* `montantTtc`
* `dateMiseEnService`

Les tables et colonnes SQL doivent aussi être en français :

* `proprietaire`
* `logement`
* `recette`
* `depense`
* `bien_amortissable`
* `document_justificatif`
* `montant_ttc`
* `date_mise_en_service`

## Sécurité

L'application doit proposer une connexion simple et sécurisée :

* email ;
* mot de passe ;
* mot de passe chiffré côté backend ;
* token ou session sécurisée ;
* accès interdit sans connexion ;
* rôle `PROPRIETAIRE` ;
* rôle `ASSISTANT`.

## Documentation

Tout le code doit être documenté en français.

Les commentaires doivent expliquer les règles métier, les calculs, les limites et les choix importants.
