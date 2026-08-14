INSERT INTO conge_demande_statuts (
  conge_demande_statut_libelle,
  conge_demande_statut_description,
  conge_demande_statut_actif
)
SELECT 'BROUILLON', 'Demande enregistrée mais pas encore soumise', TRUE
WHERE NOT EXISTS (
  SELECT 1 FROM conge_demande_statuts
  WHERE conge_demande_statut_libelle = 'BROUILLON'
);

-- Le premier MANAGER actif devient DG. Les éventuels doublons redeviennent employés.
-- L'ancienne contrainte refuse DG/DT : elle doit disparaitre avant la migration des donnees.
SET @ancienne_contrainte_role = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'employes'
    AND CONSTRAINT_NAME = 'chk_employes_role' AND CONSTRAINT_TYPE = 'CHECK'
);
SET @sql_suppression_contrainte = IF(
  @ancienne_contrainte_role > 0,
  IF(LOCATE('MariaDB', VERSION()) > 0,
    'ALTER TABLE employes DROP CONSTRAINT chk_employes_role',
    'ALTER TABLE employes DROP CHECK chk_employes_role'),
  'SELECT 1'
);
PREPARE suppression_contrainte_role FROM @sql_suppression_contrainte;
EXECUTE suppression_contrainte_role;
DEALLOCATE PREPARE suppression_contrainte_role;

UPDATE employes e
JOIN (SELECT MIN(employe_id) AS dg_id FROM employes WHERE role = 'MANAGER') selection ON 1 = 1
SET e.role = CASE WHEN e.employe_id = selection.dg_id THEN 'DG' ELSE 'EMPLOYE' END
WHERE e.role = 'MANAGER';

ALTER TABLE employes ADD CONSTRAINT chk_employes_role
  CHECK (role IN ('EMPLOYE', 'DG', 'DT', 'RH', 'ADMIN'));

ALTER TABLE conge_demandes MODIFY conge_demande_date_soumission DATETIME(6) NULL;
