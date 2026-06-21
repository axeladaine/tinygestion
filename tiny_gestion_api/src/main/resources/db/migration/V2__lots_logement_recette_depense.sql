CREATE TABLE proprietaire (
    id BIGSERIAL PRIMARY KEY,
    prenom VARCHAR(255) NOT NULL,
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    telephone VARCHAR(50),
    siret VARCHAR(14),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE logement (
    id BIGSERIAL PRIMARY KEY,
    proprietaire_id BIGINT REFERENCES proprietaire(id) ON DELETE CASCADE,
    nom VARCHAR(255) NOT NULL,
    adresse VARCHAR(255),
    code_postal VARCHAR(10),
    ville VARCHAR(255),
    qualification_logement VARCHAR(50) NOT NULL, -- RESIDENCE_SECONDAIRE, MEUBLE_DE_TOURISME, DEPENDANCE, A_VERIFIER
    est_deplacable BOOLEAN DEFAULT FALSE,
    est_sur_terrain_residence_principale BOOLEAN DEFAULT FALSE,
    est_loue_courte_duree BOOLEAN DEFAULT TRUE,
    est_meuble_tourisme BOOLEAN DEFAULT FALSE,
    date_debut_location DATE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE document_justificatif (
    id BIGSERIAL PRIMARY KEY,
    logement_id BIGINT REFERENCES logement(id) ON DELETE CASCADE,
    nom_fichier VARCHAR(255) NOT NULL,
    type_fichier VARCHAR(50),
    chemin_stockage VARCHAR(500) NOT NULL,
    type_document VARCHAR(50) NOT NULL, -- FACTURE, RELEVE_PLATEFORME, DOCUMENT_FISCAL, etc.
    entite_liee_type VARCHAR(50),
    entite_liee_id BIGINT,
    date_televersement TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE recette (
    id BIGSERIAL PRIMARY KEY,
    logement_id BIGINT REFERENCES logement(id) ON DELETE CASCADE,
    plateforme VARCHAR(50) NOT NULL, -- AIRBNB, BOOKING, DIRECT, AUTRE
    nom_client VARCHAR(255),
    date_debut_sejour DATE,
    date_fin_sejour DATE,
    date_encaissement DATE,
    nombre_nuits INTEGER,
    montant_brut NUMERIC(12, 2) NOT NULL,
    frais_plateforme NUMERIC(12, 2) DEFAULT 0.00,
    montant_taxe_sejour NUMERIC(12, 2) DEFAULT 0.00,
    montant_net_recu NUMERIC(12, 2) NOT NULL,
    commentaire TEXT,
    document_justificatif_id BIGINT REFERENCES document_justificatif(id) ON DELETE SET NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE depense (
    id BIGSERIAL PRIMARY KEY,
    logement_id BIGINT REFERENCES logement(id) ON DELETE CASCADE,
    date_depense DATE NOT NULL,
    fournisseur VARCHAR(255),
    categorie VARCHAR(100),
    montant_ttc NUMERIC(12, 2) NOT NULL,
    taux_affectation_location NUMERIC(5, 2) DEFAULT 100.00,
    montant_retenu NUMERIC(12, 2) NOT NULL,
    moyen_paiement VARCHAR(50),
    statut_deductibilite VARCHAR(50) NOT NULL, -- DEDUCTIBLE, NON_DEDUCTIBLE, A_VERIFIER
    commentaire TEXT,
    document_justificatif_id BIGINT REFERENCES document_justificatif(id) ON DELETE SET NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ajouter une colonne relationnelle facultative dans utilisateur pour lier un utilisateur à un propriétaire
ALTER TABLE utilisateur ADD COLUMN proprietaire_id BIGINT REFERENCES proprietaire(id) ON DELETE SET NULL;
