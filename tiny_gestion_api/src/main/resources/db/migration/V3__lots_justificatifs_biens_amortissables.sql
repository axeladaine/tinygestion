CREATE TABLE bien_amortissable (
    id BIGSERIAL PRIMARY KEY,
    logement_id BIGINT REFERENCES logement(id) ON DELETE CASCADE,
    nom VARCHAR(255) NOT NULL,
    categorie VARCHAR(100) NOT NULL, -- TINY_HOUSE, TERRASSE, RACCORDEMENT, MOBILIER, LITERIE, ELECTROMENAGER, CHAUFFAGE_CLIMATISATION, DECORATION, AUTRE
    date_achat DATE NOT NULL,
    date_mise_en_service DATE,
    montant_ttc NUMERIC(12, 2) NOT NULL,
    taux_affectation_location NUMERIC(5, 2) DEFAULT 100.00,
    base_amortissable NUMERIC(12, 2) NOT NULL,
    duree_amortissement_ans INTEGER NOT NULL,
    amortissement_annuel NUMERIC(12, 2) NOT NULL,
    amortissement_mensuel NUMERIC(12, 2) NOT NULL,
    amortissement_deduit_cumule NUMERIC(12, 2) DEFAULT 0.00,
    amortissement_non_utilise_cumule NUMERIC(12, 2) DEFAULT 0.00,
    possede_facture BOOLEAN DEFAULT FALSE,
    document_justificatif_id BIGINT REFERENCES document_justificatif(id) ON DELETE SET NULL,
    commentaire TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE impot_local (
    id BIGSERIAL PRIMARY KEY,
    logement_id BIGINT REFERENCES logement(id) ON DELETE CASCADE,
    type_impot_local VARCHAR(100) NOT NULL, -- CFE, TAXE_HABITATION_RESIDENCE_SECONDAIRE, TAXE_FONCIERE, TAXE_AMENAGEMENT, AUTRE
    annee INTEGER NOT NULL,
    montant NUMERIC(12, 2) NOT NULL,
    date_avis DATE,
    date_echeance DATE,
    date_paiement DATE,
    statut_paiement VARCHAR(50),
    statut_deductibilite VARCHAR(50),
    document_justificatif_id BIGINT REFERENCES document_justificatif(id) ON DELETE SET NULL,
    commentaire TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE journal_securite (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT REFERENCES utilisateur(id) ON DELETE SET NULL,
    email VARCHAR(255),
    type_evenement VARCHAR(100) NOT NULL, -- CONNEXION_REUSSIE, CONNEXION_ECHOUEE, DECONNEXION, MOT_DE_PASSE_MODIFIE, EXPORT_DONNEES
    adresse_ip VARCHAR(50),
    date_evenement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details TEXT
);
