# Modèle de données

## Convention

Les noms doivent être en français, sans accents.

Les classes Java utilisent le PascalCase.

Les attributs Java utilisent le camelCase.

Les tables SQL utilisent le snake_case.

## Entité `Utilisateur`

Table : `utilisateur`

Champs :

```text
id
email
mot_de_passe_hash
prenom
nom
role
actif
date_derniere_connexion
date_creation
date_modification
```

Rôles possibles :

```text
PROPRIETAIRE
ASSISTANT
```

## Entité `Proprietaire`

Table : `proprietaire`

Champs :

```text
id
prenom
nom
email
telephone
siret
date_creation
date_modification
```

## Entité `Logement`

Table : `logement`

Champs :

```text
id
proprietaire_id
nom
adresse
code_postal
ville
qualification_logement
est_deplacable
est_sur_terrain_residence_principale
est_loue_courte_duree
est_meuble_tourisme
date_debut_location
date_creation
date_modification
```

Valeurs possibles pour `qualification_logement` :

```text
RESIDENCE_SECONDAIRE
MEUBLE_DE_TOURISME
DEPENDANCE
A_VERIFIER
```

## Entité `Recette`

Table : `recette`

Champs :

```text
id
logement_id
plateforme
nom_client
date_debut_sejour
date_fin_sejour
date_encaissement
nombre_nuits
montant_brut
frais_plateforme
montant_taxe_sejour
montant_net_recu
commentaire
document_justificatif_id
date_creation
date_modification
```

Valeurs possibles pour `plateforme` :

```text
AIRBNB
BOOKING
DIRECT
AUTRE
```

## Entité `Depense`

Table : `depense`

Champs :

```text
id
logement_id
date_depense
fournisseur
categorie
montant_ttc
taux_affectation_location
montant_retenu
moyen_paiement
statut_deductibilite
commentaire
document_justificatif_id
date_creation
date_modification
```

Valeurs possibles pour `statut_deductibilite` :

```text
DEDUCTIBLE
NON_DEDUCTIBLE
A_VERIFIER
```

## Entité `BienAmortissable`

Table : `bien_amortissable`

Champs :

```text
id
logement_id
nom
categorie
date_achat
date_mise_en_service
montant_ttc
taux_affectation_location
base_amortissable
duree_amortissement_ans
amortissement_annuel
amortissement_mensuel
amortissement_deduit_cumule
amortissement_non_utilise_cumule
possede_facture
document_justificatif_id
commentaire
date_creation
date_modification
```

Valeurs possibles pour `categorie` :

```text
TINY_HOUSE
TERRASSE
RACCORDEMENT
MOBILIER
LITERIE
ELECTROMENAGER
CHAUFFAGE_CLIMATISATION
DECORATION
AUTRE
```

## Entité `DocumentJustificatif`

Table : `document_justificatif`

Champs :

```text
id
logement_id
nom_fichier
type_fichier
chemin_stockage
type_document
entite_liee_type
entite_liee_id
date_televersement
```

Valeurs possibles pour `type_document` :

```text
FACTURE
RELEVE_PLATEFORME
DOCUMENT_FISCAL
DOCUMENT_MAIRIE
ASSURANCE
IMPOT_LOCAL
AUTRE
```

## Entité `ImpotLocal`

Table : `impot_local`

Champs :

```text
id
logement_id
type_impot_local
annee
montant
date_avis
date_echeance
date_paiement
statut_paiement
statut_deductibilite
document_justificatif_id
commentaire
date_creation
date_modification
```

Valeurs possibles pour `type_impot_local` :

```text
CFE
TAXE_HABITATION_RESIDENCE_SECONDAIRE
TAXE_FONCIERE
TAXE_AMENAGEMENT
AUTRE
```

## Entité `ChecklistMensuelle`

Table : `checklist_mensuelle`

Champs :

```text
id
logement_id
annee
mois
taches_json
taux_completion
date_creation
date_modification
```
