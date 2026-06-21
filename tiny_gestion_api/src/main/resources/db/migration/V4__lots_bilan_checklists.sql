CREATE TABLE checklist_mensuelle (
    id BIGSERIAL PRIMARY KEY,
    logement_id BIGINT NOT NULL REFERENCES logement(id) ON DELETE CASCADE,
    annee INT NOT NULL,
    mois INT NOT NULL,
    taches_json TEXT NOT NULL,
    taux_completion DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_checklist_logement_annee_mois ON checklist_mensuelle(logement_id, annee, mois);
