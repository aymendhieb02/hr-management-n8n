CREATE TABLE IF NOT EXISTS pipelines_validation (
  pipeline_validation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  pipeline_validation_nom VARCHAR(150) NOT NULL,
  employe_id BIGINT NOT NULL,
  pipeline_validation_actif BOOLEAN NOT NULL DEFAULT TRUE,
  date_creation DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  date_modification DATETIME(6) NULL,
  CONSTRAINT fk_pipeline_employe FOREIGN KEY (employe_id) REFERENCES employes(employe_id),
  KEY idx_pipeline_employe_actif (employe_id, pipeline_validation_actif)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pipeline_validation_etapes (
  pipeline_validation_etape_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  pipeline_validation_id BIGINT NOT NULL,
  decideur_id BIGINT NOT NULL,
  priorite INT NOT NULL,
  actif BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_pipeline_etape_pipeline FOREIGN KEY (pipeline_validation_id) REFERENCES pipelines_validation(pipeline_validation_id) ON DELETE CASCADE,
  CONSTRAINT fk_pipeline_etape_decideur FOREIGN KEY (decideur_id) REFERENCES employes(employe_id),
  CONSTRAINT uq_pipeline_priorite UNIQUE (pipeline_validation_id, priorite),
  CONSTRAINT uq_pipeline_decideur UNIQUE (pipeline_validation_id, decideur_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS conge_demande_workflows (
  conge_demande_workflow_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  conge_demande_id BIGINT NOT NULL,
  pipeline_source_id BIGINT NULL,
  statut VARCHAR(30) NOT NULL DEFAULT 'EN_COURS',
  etape_courante INT NULL,
  date_creation DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  date_fin DATETIME(6) NULL,
  CONSTRAINT uq_workflow_demande UNIQUE (conge_demande_id),
  CONSTRAINT fk_workflow_demande FOREIGN KEY (conge_demande_id) REFERENCES conge_demandes(conge_demande_id) ON DELETE CASCADE,
  CONSTRAINT fk_workflow_pipeline FOREIGN KEY (pipeline_source_id) REFERENCES pipelines_validation(pipeline_validation_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS conge_demande_workflow_etapes (
  conge_demande_workflow_etape_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  conge_demande_workflow_id BIGINT NOT NULL,
  decideur_id BIGINT NOT NULL,
  decideur_initial_id BIGINT NOT NULL,
  priorite INT NOT NULL,
  statut VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE',
  commentaire VARCHAR(1000) NULL,
  date_action DATETIME(6) NULL,
  date_reaffectation DATETIME(6) NULL,
  reaffecte_par_id BIGINT NULL,
  CONSTRAINT fk_workflow_etape_workflow FOREIGN KEY (conge_demande_workflow_id) REFERENCES conge_demande_workflows(conge_demande_workflow_id) ON DELETE CASCADE,
  CONSTRAINT fk_workflow_etape_decideur FOREIGN KEY (decideur_id) REFERENCES employes(employe_id),
  CONSTRAINT fk_workflow_etape_initial FOREIGN KEY (decideur_initial_id) REFERENCES employes(employe_id),
  CONSTRAINT fk_workflow_etape_reaffecteur FOREIGN KEY (reaffecte_par_id) REFERENCES employes(employe_id),
  CONSTRAINT uq_workflow_priorite UNIQUE (conge_demande_workflow_id, priorite),
  KEY idx_workflow_decideur_statut (decideur_id, statut)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS conge_demande_id BIGINT NULL;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS workflow_etape_id BIGINT NULL;

CREATE TABLE IF NOT EXISTS email_outbox (
  email_outbox_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  destinataire VARCHAR(150) NOT NULL,
  sujet VARCHAR(250) NOT NULL,
  contenu_html LONGTEXT NOT NULL,
  statut VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE',
  nombre_tentatives INT NOT NULL DEFAULT 0,
  prochaine_tentative DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  derniere_erreur VARCHAR(1000) NULL,
  date_creation DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  date_envoi DATETIME(6) NULL,
  KEY idx_outbox_a_envoyer (statut, prochaine_tentative)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO notification_types(notification_type_libelle, notification_type_description, notification_type_actif)
SELECT 'WORKFLOW_ACTION', 'Action requise dans un circuit de validation', TRUE
WHERE NOT EXISTS (SELECT 1 FROM notification_types WHERE notification_type_libelle='WORKFLOW_ACTION');
INSERT INTO notification_types(notification_type_libelle, notification_type_description, notification_type_actif)
SELECT 'WORKFLOW_ANOMALIE', 'Anomalie ou réaffectation du circuit de validation', TRUE
WHERE NOT EXISTS (SELECT 1 FROM notification_types WHERE notification_type_libelle='WORKFLOW_ANOMALIE');

INSERT INTO conge_demande_workflows(conge_demande_id, statut, etape_courante, date_creation)
SELECT d.conge_demande_id, 'EN_COURS', 1, COALESCE(d.date_creation, CURRENT_TIMESTAMP(6))
FROM conge_demandes d
WHERE d.conge_demande_statut_id=(SELECT conge_demande_statut_id FROM conge_demande_statuts WHERE conge_demande_statut_libelle='EN_ATTENTE' LIMIT 1)
  AND d.decideur_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM conge_demande_workflows w WHERE w.conge_demande_id=d.conge_demande_id);

INSERT INTO conge_demande_workflow_etapes(conge_demande_workflow_id, decideur_id, decideur_initial_id, priorite, statut)
SELECT w.conge_demande_workflow_id, d.decideur_id, d.decideur_id, 1, 'EN_ATTENTE'
FROM conge_demande_workflows w JOIN conge_demandes d ON d.conge_demande_id=w.conge_demande_id
WHERE NOT EXISTS (SELECT 1 FROM conge_demande_workflow_etapes e WHERE e.conge_demande_workflow_id=w.conge_demande_workflow_id);
