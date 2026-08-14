-- Repare les installations anterieures et garantit un seul DG et un seul DT.
SET @contrainte_role_presente = (
  SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'employes'
    AND CONSTRAINT_NAME = 'chk_employes_role' AND CONSTRAINT_TYPE = 'CHECK'
);
SET @sql_drop_role = IF(
  @contrainte_role_presente > 0,
  IF(LOCATE('MariaDB', VERSION()) > 0,
    'ALTER TABLE employes DROP CONSTRAINT chk_employes_role',
    'ALTER TABLE employes DROP CHECK chk_employes_role'),
  'SELECT 1'
);
PREPARE drop_role FROM @sql_drop_role;
EXECUTE drop_role;
DEALLOCATE PREPARE drop_role;

UPDATE employes e
JOIN (SELECT MIN(employe_id) AS titulaire FROM employes WHERE role IN ('MANAGER', 'DG')) choix ON 1 = 1
SET e.role = CASE WHEN e.employe_id = choix.titulaire THEN 'DG' ELSE 'EMPLOYE' END
WHERE e.role IN ('MANAGER', 'DG');

UPDATE employes e
JOIN (SELECT MIN(employe_id) AS titulaire FROM employes WHERE role = 'DT') choix ON 1 = 1
SET e.role = CASE WHEN e.employe_id = choix.titulaire THEN 'DT' ELSE 'EMPLOYE' END
WHERE e.role = 'DT';

ALTER TABLE employes ADD CONSTRAINT chk_employes_role
  CHECK (role IN ('EMPLOYE', 'DG', 'DT', 'RH', 'ADMIN'));

SET @colonne_direction_presente = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'employes'
    AND COLUMN_NAME = 'role_direction_unique'
);
SET @sql_add_colonne = IF(@colonne_direction_presente = 0,
  'ALTER TABLE employes ADD COLUMN role_direction_unique VARCHAR(2) GENERATED ALWAYS AS (CASE WHEN role IN (''DG'', ''DT'') THEN role ELSE NULL END) STORED',
  'SELECT 1');
PREPARE add_colonne_direction FROM @sql_add_colonne;
EXECUTE add_colonne_direction;
DEALLOCATE PREPARE add_colonne_direction;

SET @index_direction_present = (
  SELECT COUNT(*) FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'employes'
    AND INDEX_NAME = 'uk_employes_role_direction'
);
SET @sql_add_index = IF(@index_direction_present = 0,
  'CREATE UNIQUE INDEX uk_employes_role_direction ON employes (role_direction_unique)',
  'SELECT 1');
PREPARE add_index_direction FROM @sql_add_index;
EXECUTE add_index_direction;
DEALLOCATE PREPARE add_index_direction;
