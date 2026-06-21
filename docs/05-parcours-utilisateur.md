# Parcours utilisateur

## Premier lancement

Au premier lancement, l'utilisateur doit renseigner :

1. Ses informations personnelles.
2. Les informations du logement.
3. Le régime fiscal suivi dans l'application.
4. Les plateformes utilisées.
5. Les premières informations de sécurité.

## Parcours simple mensuel

Chaque mois, l'utilisateur doit :

1. Ajouter les recettes Airbnb.
2. Ajouter les recettes Booking.
3. Vérifier les montants reçus.
4. Ajouter les dépenses.
5. Joindre les factures.
6. Vérifier les justificatifs manquants.
7. Valider la checklist mensuelle.

## Parcours recette

L'utilisateur clique sur :

```text
Ajouter une recette
```

Il renseigne :

* plateforme ;
* dates du séjour ;
* montant brut ;
* frais plateforme ;
* taxe de séjour ;
* montant net reçu.

L'application calcule ou vérifie le montant net reçu.

## Parcours dépense

L'utilisateur clique sur :

```text
Ajouter une dépense
```

Il renseigne :

* date ;
* fournisseur ;
* montant ;
* catégorie ;
* taux d'affectation ;
* facture.

Si le montant est important, l'application peut proposer :

```text
Cet achat semble durable. Voulez-vous l'enregistrer comme bien amortissable ?
```

## Parcours bien amortissable

L'utilisateur clique sur :

```text
Ajouter un bien amortissable
```

Il renseigne :

* nom ;
* catégorie ;
* date d'achat ;
* date de mise en service ;
* montant ;
* taux d'affectation ;
* durée d'amortissement ;
* facture.

L'application calcule :

* base amortissable ;
* amortissement annuel ;
* amortissement mensuel.

## Parcours annuel

En début d'année suivante, l'utilisateur va dans :

```text
Bilan annuel
```

Il vérifie :

* recettes annuelles ;
* dépenses annuelles ;
* justificatifs manquants ;
* biens amortissables ;
* impôts locaux ;
* résultat estimatif.

Puis il exporte :

* bilan annuel PDF ;
* recettes CSV ;
* dépenses CSV ;
* biens amortissables CSV ;
* dossier de contrôle.

## Parcours contrôle

En cas de contrôle ou de demande de justificatif, l'utilisateur va dans :

```text
Dossier de contrôle
```

Il sélectionne l'année.

L'application génère :

```text
recettes.csv
depenses.csv
biens-amortissables.csv
impots-locaux.csv
synthese-annuelle.pdf
justificatifs/
notes-de-calcul.pdf
```
