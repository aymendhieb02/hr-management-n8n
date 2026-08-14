ALTER TABLE conge_demandes
  ADD COLUMN IF NOT EXISTS date_fin_reelle DATE NULL AFTER nombre_jours_consomme,
  ADD COLUMN IF NOT EXISTS date_regularisation DATETIME(6) NULL AFTER date_fin_reelle,
  ADD COLUMN IF NOT EXISTS regularise_par_id BIGINT NULL AFTER date_regularisation,
  ADD COLUMN IF NOT EXISTS commentaire_regularisation VARCHAR(1000) NULL AFTER regularise_par_id;

SET @fk_regularisateur_presente = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'conge_demandes'
    AND CONSTRAINT_NAME = 'fk_conge_demande_regularisateur'
);
SET @sql_fk_regularisateur = IF(@fk_regularisateur_presente = 0,
  'ALTER TABLE conge_demandes ADD CONSTRAINT fk_conge_demande_regularisateur FOREIGN KEY (regularise_par_id) REFERENCES employes(employe_id) ON DELETE SET NULL',
  'SELECT 1');
PREPARE ajout_fk_regularisateur FROM @sql_fk_regularisateur;
EXECUTE ajout_fk_regularisateur;
DEALLOCATE PREPARE ajout_fk_regularisateur;

SET @index_regularisateur_present = (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'conge_demandes'
    AND INDEX_NAME = 'idx_conge_demande_regularisateur'
);
SET @sql_index_regularisateur = IF(@index_regularisateur_present = 0,
  'CREATE INDEX idx_conge_demande_regularisateur ON conge_demandes (regularise_par_id)',
  'SELECT 1');
PREPARE ajout_index_regularisateur FROM @sql_index_regularisateur;
EXECUTE ajout_index_regularisateur;
DEALLOCATE PREPARE ajout_index_regularisateur;
