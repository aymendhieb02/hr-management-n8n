-- Un seul solde de congé par employé et traçabilité de chaque mouvement.
-- Compatible MySQL 8. Exécuter une seule fois sur rh_xtensus.

START TRANSACTION;

-- Conserver le solde le plus ancien de chaque employé avant de créer l'unicité.
DELETE h FROM conge_solde_historiques h
JOIN conge_soldes s ON s.conge_solde_id = h.conge_solde_id
JOIN conge_soldes garde ON garde.employe_id = s.employe_id AND garde.conge_solde_id < s.conge_solde_id;

DELETE s FROM conge_soldes s
JOIN conge_soldes garde ON garde.employe_id = s.employe_id AND garde.conge_solde_id < s.conge_solde_id;

ALTER TABLE conge_soldes DROP INDEX uq_conge_soldes_employe_type_annee;
ALTER TABLE conge_soldes ADD CONSTRAINT uq_conge_soldes_employe UNIQUE (employe_id);
ALTER TABLE conge_soldes DROP CONSTRAINT chk_conge_soldes_non_negative;
ALTER TABLE conge_soldes ADD CONSTRAINT chk_conge_soldes_limites
  CHECK (conge_solde_jours_utilises >= 0 AND conge_solde_restants >= -5.00 AND conge_solde_droit_acquis >= 0);

ALTER TABLE conge_solde_historiques
  ADD COLUMN conge_solde_historique_type_transaction VARCHAR(50) NOT NULL DEFAULT 'AJUSTEMENT' AFTER conge_solde_historique_statut_acquisition,
  ADD COLUMN conge_solde_historique_montant DECIMAL(5,2) NOT NULL DEFAULT 0 AFTER conge_solde_historique_type_transaction,
  ADD COLUMN conge_solde_historique_reference VARCHAR(100) NULL AFTER conge_solde_historique_montant,
  ADD COLUMN conge_demande_id BIGINT NULL AFTER conge_solde_id;

UPDATE conge_solde_historiques
SET conge_solde_historique_reference = CONCAT('LEGACY:', conge_solde_historique_id)
WHERE conge_solde_historique_reference IS NULL;

ALTER TABLE conge_solde_historiques
  MODIFY conge_solde_historique_reference VARCHAR(100) NOT NULL,
  ADD CONSTRAINT uq_conge_solde_historique_reference UNIQUE (conge_solde_historique_reference),
  ADD CONSTRAINT fk_conge_solde_historique_demande FOREIGN KEY (conge_demande_id)
    REFERENCES conge_demandes(conge_demande_id) ON DELETE SET NULL;

COMMIT;
