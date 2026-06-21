# Definition of Done

Une fonctionnalité est terminée uniquement si toutes les conditions suivantes sont respectées.

## Fonctionnel

* La fonctionnalité répond au besoin utilisateur.
* L'écran est utilisable sur mobile.
* Les libellés sont en français.
* Les messages d'erreur sont compréhensibles.
* Les calculs sont testés.
* Les cas d'erreur sont gérés.

## Technique

* Le code compile.
* Les tests passent.
* Les migrations Flyway sont présentes.
* Les endpoints sont protégés si nécessaire.
* Les données sensibles ne sont pas exposées publiquement.
* Les fichiers justificatifs ne sont pas stockés dans un dossier public.

## Nommage

* Les classes métier sont en français.
* Les méthodes métier sont en français.
* Les tables sont en français.
* Les colonnes sont en français.
* Les routes API métier sont en français.
* Aucun nom métier anglais ne doit être utilisé.

Exemples interdits :

```text
Asset
Expense
Revenue
Property
Owner
```

Exemples attendus :

```text
BienAmortissable
Depense
Recette
Logement
Proprietaire
```

## Documentation

* Les classes importantes ont une JavaDoc en français.
* Les méthodes métier ont une documentation en français.
* Les endpoints sont documentés en français.
* Les fichiers `/docs` sont mis à jour.
* Le README est à jour.

## Sécurité

* Les mots de passe ne sont jamais stockés en clair.
* Les routes privées nécessitent une authentification.
* La déconnexion fonctionne.
* Les documents justificatifs ne sont accessibles qu'à un utilisateur connecté.
* Les tentatives de connexion échouées sont limitées.
* Les événements de sécurité importants sont journalisés.

## Fiscalité

* Les calculs fiscaux sont présentés comme estimatifs.
* Les avertissements sont visibles.
* Les durées d'amortissement sont indiquées comme modifiables et à valider.
* Les dépenses ambiguës sont en `A_VERIFIER` par défaut.

## Expérience utilisateur

* L'utilisateur comprend ce qu'il doit faire.
* Les boutons principaux sont visibles.
* Les écrans ne sont pas surchargés.
* Les informations importantes sont affichées en priorité.
* Les actions mensuelles sont guidées par une checklist.
