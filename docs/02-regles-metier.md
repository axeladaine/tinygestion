# Règles métier

## Recette

Une recette correspond à une réservation encaissée.

Une recette doit conserver le détail suivant :

* montant brut ;
* frais de plateforme ;
* taxe de séjour ;
* montant net reçu.

Le montant brut permet de suivre ce qui a été généré par la location.

Le montant net reçu permet de rapprocher la recette avec le compte bancaire.

## Calcul du montant net reçu

Formule :

```text
montant_net_recu = montant_brut - frais_plateforme - montant_taxe_sejour
```

Cette formule peut être ajustée si la plateforme présente les montants différemment.

## Dépense

Une dépense correspond à un frais lié à l'activité de location.

Exemples :

* électricité ;
* eau ;
* assurance ;
* ménage ;
* consommables ;
* linge ;
* réparations ;
* frais Booking ;
* frais Airbnb ;
* CFE ;
* application ;
* comptabilité.

## Statut de déductibilité

Une dépense peut avoir trois statuts :

* `DEDUCTIBLE`
* `NON_DEDUCTIBLE`
* `A_VERIFIER`

Par défaut, si une dépense est ambiguë, elle doit être en `A_VERIFIER`.

## Taux d'affectation à la location

Certaines dépenses peuvent être partagées entre la maison principale et la tiny house.

Exemple :

* facture d'électricité totale : 120 euros ;
* part estimée de la tiny : 30 % ;
* montant retenu : 36 euros.

Formule :

```text
montant_retenu = montant_ttc * taux_affectation_location / 100
```

## Bien amortissable

Un bien amortissable est un achat durable utilisé plusieurs années pour la location.

Exemples :

* tiny house ;
* terrasse ;
* frigo ;
* lit ;
* climatisation ;
* raccordement électrique.

Un bien amortissable n'est pas considéré comme une simple dépense immédiate.

Il est suivi dans le temps avec un amortissement estimatif.

## Base amortissable

Formule :

```text
base_amortissable = montant_ttc * taux_affectation_location / 100
```

## Amortissement annuel

Formule :

```text
amortissement_annuel = base_amortissable / duree_amortissement_ans
```

## Amortissement mensuel

Formule :

```text
amortissement_mensuel = amortissement_annuel / 12
```

## Résultat avant amortissement

Formule :

```text
resultat_avant_amortissement = recettes_annuelles - depenses_retenues_annuelles
```

## Amortissement utilisable

L'amortissement ne doit pas rendre le résultat estimatif négatif.

Formule :

```text
amortissement_utilise = minimum(amortissement_disponible, resultat_avant_amortissement)
```

Si le résultat avant amortissement est négatif ou nul, l'amortissement utilisé est égal à zéro.

## Résultat réel estimatif

Formule :

```text
resultat_reel_estimatif = resultat_avant_amortissement - amortissement_utilise
```

Le résultat réel estimatif ne doit pas être inférieur à zéro à cause des amortissements.

## Amortissement non utilisé

Formule :

```text
amortissement_non_utilise = amortissement_disponible - amortissement_utilise
```

Ce montant doit être suivi pour information.

## Avertissement obligatoire

Afficher dans les écrans de calcul :

```text
Les calculs fournis sont des estimations destinées au suivi personnel. Ils ne remplacent pas une déclaration fiscale officielle ni l'avis d'un professionnel.
```
