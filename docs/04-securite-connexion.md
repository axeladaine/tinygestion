# Sécurité et connexion

## Objectif

L'application doit avoir une connexion simple et sécurisée.

Elle est destinée à un usage familial, mais elle contient des données sensibles :

* recettes ;
* dépenses ;
* justificatifs ;
* documents fiscaux ;
* informations personnelles ;
* SIRET ;
* documents liés au logement.

L'accès doit donc être protégé.

## Utilisateurs

Deux rôles sont nécessaires :

```text
PROPRIETAIRE
ASSISTANT
```

## Rôle `PROPRIETAIRE`

Le propriétaire peut :

* consulter toutes les données ;
* créer des recettes ;
* créer des dépenses ;
* ajouter des justificatifs ;
* modifier les informations du logement ;
* exporter les données ;
* gérer l'assistant.

## Rôle `ASSISTANT`

L'assistant peut :

* consulter les données ;
* ajouter ou modifier des recettes ;
* ajouter ou modifier des dépenses ;
* ajouter des justificatifs ;
* exporter les données.

L'assistant ne peut pas :

* supprimer le compte propriétaire ;
* modifier le mot de passe du propriétaire ;
* changer les paramètres critiques de sécurité.

## Connexion

L'utilisateur se connecte avec :

```text
email
mot de passe
```

## Stockage du mot de passe

Le mot de passe ne doit jamais être stocké en clair.

Le backend doit stocker uniquement un hash sécurisé du mot de passe.

Exemple recommandé :

```text
BCrypt
```

## Politique de mot de passe

Règles minimales :

```text
8 caractères minimum
au moins 1 lettre
au moins 1 chiffre
```

Le but est de rester simple pour le propriétaire.

## Session

L'application peut utiliser une authentification par token JWT ou par session sécurisée.

Pour une application familiale simple, la solution recommandée est :

```text
Access token court
Refresh token stocké en cookie HttpOnly Secure SameSite
```

Si l'application est uniquement hébergée sur le même domaine que le backend, une session serveur sécurisée peut aussi être utilisée.

## Durée de connexion

Règles recommandées :

```text
access token : 15 minutes
refresh token : 7 jours
```

L'utilisateur doit pouvoir se déconnecter.

## Déconnexion

Lors de la déconnexion :

* supprimer le token côté frontend ;
* invalider le refresh token si utilisé ;
* rediriger vers l'écran de connexion.

## Protection des routes frontend

Les pages suivantes doivent être interdites sans connexion :

* tableau de bord ;
* recettes ;
* dépenses ;
* justificatifs ;
* biens amortissables ;
* paramètres ;
* exports.

Créer un guard Angular :

```text
gardeAuthentification
```

## Protection des routes backend

Toutes les routes `/api/**` doivent être protégées sauf :

```text
POST /api/authentification/connexion
POST /api/authentification/rafraichir
```

Routes à protéger :

```text
/api/tableau-de-bord/**
/api/logements/**
/api/recettes/**
/api/depenses/**
/api/biens-amortissables/**
/api/documents-justificatifs/**
/api/exports/**
```

## Anti-bruteforce simple

L'application doit bloquer temporairement les tentatives de connexion répétées.

Règle simple :

```text
5 tentatives échouées en 15 minutes
blocage temporaire de 15 minutes
```

## Journal de sécurité

Créer une table simple :

```text
journal_securite
```

Champs :

```text
id
utilisateur_id
email
type_evenement
adresse_ip
date_evenement
details
```

Types d'événements :

```text
CONNEXION_REUSSIE
CONNEXION_ECHOUEE
DECONNEXION
MOT_DE_PASSE_MODIFIE
EXPORT_DONNEES
```

## Changement de mot de passe

L'utilisateur connecté doit pouvoir modifier son mot de passe.

Champs :

```text
mot de passe actuel
nouveau mot de passe
confirmation du nouveau mot de passe
```

## Réinitialisation de mot de passe

Pour le MVP, la réinitialisation automatique par email peut être exclue.

Solution simple au début :

* un compte administrateur technique peut réinitialiser le mot de passe ;
* ou une procédure manuelle documentée.

## Données sensibles

Les justificatifs peuvent contenir des informations personnelles.

L'application doit :

* limiter l'accès aux utilisateurs connectés ;
* ne jamais exposer les fichiers publiquement ;
* servir les fichiers via un endpoint sécurisé ;
* vérifier les droits avant téléchargement.

## Stockage des fichiers

Les fichiers doivent être stockés dans un dossier privé du backend.

Exemple :

```text
/storage/tiny-gestion/documents
```

Ne pas stocker les documents dans le dossier public du frontend.

## Message utilisateur

Sur la page de connexion, afficher :

```text
Connectez-vous pour accéder au suivi de la tiny house.
```

En cas d'erreur :

```text
Email ou mot de passe incorrect.
```

Ne pas préciser si l'email existe ou non.
