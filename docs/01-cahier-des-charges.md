# Cahier des charges

## Nom de l'application

Tiny Gestion

## Objectif principal

Créer une application simple permettant de suivre une tiny house louée en courte durée.

## Fonctionnalités principales

L'application doit permettre de gérer :

1. Le propriétaire.
2. Le logement.
3. Les recettes.
4. Les dépenses.
5. Les justificatifs.
6. Les biens amortissables.
7. Les checklists.
8. Les exports.
9. Une petite connexion sécurisée.

## Fonctionnalités incluses dans le MVP

### Tableau de bord

Le tableau de bord affiche :

* recettes du mois ;
* dépenses du mois ;
* résultat estimatif du mois ;
* justificatifs manquants ;
* tâches à faire ;
* bouton "Ajouter une recette" ;
* bouton "Ajouter une dépense" ;
* bouton "Ajouter un justificatif".

### Recettes

L'utilisateur peut ajouter une recette avec :

* plateforme ;
* date du séjour ;
* date d'encaissement ;
* nombre de nuits ;
* montant brut ;
* frais de plateforme ;
* taxe de séjour ;
* montant net reçu ;
* justificatif ;
* commentaire.

### Dépenses

L'utilisateur peut ajouter une dépense avec :

* date ;
* fournisseur ;
* catégorie ;
* montant TTC ;
* taux d'affectation à la location ;
* montant retenu ;
* statut de déductibilité ;
* justificatif ;
* commentaire.

### Justificatifs

L'utilisateur peut ajouter des documents :

* PDF ;
* JPG ;
* PNG.

Chaque justificatif peut être lié à :

* une recette ;
* une dépense ;
* un bien amortissable ;
* un impôt local ;
* le logement.

### Biens amortissables

L'utilisateur peut suivre les achats durables :

* tiny house ;
* terrasse ;
* raccordement ;
* mobilier ;
* literie ;
* électroménager ;
* chauffage ;
* climatisation ;
* équipements durables.

L'application calcule un amortissement simple et estimatif.

### Checklists

L'application propose :

* une checklist mensuelle ;
* une checklist annuelle.

### Exports

L'application permet d'exporter :

* recettes en CSV ;
* dépenses en CSV ;
* biens amortissables en CSV ;
* bilan annuel en PDF ;
* dossier de contrôle annuel.

### Connexion

L'application doit être accessible uniquement après connexion.

## Fonctionnalités exclues du MVP

Ne pas développer au début :

* synchronisation bancaire automatique ;
* connexion directe Airbnb ou Booking ;
* génération de liasse fiscale officielle ;
* télétransmission fiscale ;
* multi-propriétaires complexe ;
* comptabilité complète ;
* TVA ;
* gestion de paie ;
* facturation professionnelle avancée.
