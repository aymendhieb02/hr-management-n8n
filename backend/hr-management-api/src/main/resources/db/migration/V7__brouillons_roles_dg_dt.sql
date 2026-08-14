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
UPDATE employes e
JOIN (SELECT MIN(employe_id) AS dg_id FROM employes WHERE role = 'MANAGER') selection ON 1 = 1
SET e.role = CASE WHEN e.employe_id = selection.dg_id THEN 'DG' ELSE 'EMPLOYE' END
WHERE e.role = 'MANAGER';

ALTER TABLE conge_demandes MODIFY conge_demande_date_soumission DATETIME(6) NULL;
