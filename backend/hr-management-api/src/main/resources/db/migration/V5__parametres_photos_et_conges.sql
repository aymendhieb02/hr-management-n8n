-- Migration MySQL additive : aucune table ni donnee existante n'est supprimee.
ALTER TABLE employes
    ADD COLUMN IF NOT EXISTS photo_profil VARCHAR(500) NULL AFTER employe_sexe,
    ADD COLUMN IF NOT EXISTS code_activation_hash VARCHAR(255) NULL AFTER mot_de_passe_hash,
    ADD COLUMN IF NOT EXISTS changement_mot_de_passe_requis TINYINT(1) NOT NULL DEFAULT 0 AFTER code_activation_hash,
    ADD COLUMN IF NOT EXISTS code_activation_expire_le DATETIME(6) NULL AFTER changement_mot_de_passe_requis,
    ADD COLUMN IF NOT EXISTS derniere_acquisition_conge DATE NULL AFTER employe_date_embauche;

ALTER TABLE conge_demandes
    ADD COLUMN IF NOT EXISTS samedi_compte TINYINT(1) NOT NULL DEFAULT 0 AFTER conge_demande_nombre_jours,
    ADD COLUMN IF NOT EXISTS nombre_jours_consomme DECIMAL(5,2) NULL AFTER samedi_compte;

CREATE TABLE IF NOT EXISTS variables (
    variable_id BIGINT NOT NULL AUTO_INCREMENT,
    variable_nom VARCHAR(100) NOT NULL,
    variable_libelle VARCHAR(150) NOT NULL,
    variable_description VARCHAR(500) NULL,
    variable_type VARCHAR(30) NOT NULL,
    variable_valeur VARCHAR(100) NOT NULL,
    PRIMARY KEY (variable_id),
    UNIQUE KEY uk_variables_nom (variable_nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO variables (variable_nom, variable_libelle, variable_description, variable_type, variable_valeur)
VALUES
 ('solde_conge_par_mois', 'Solde de conge par mois', 'Nombre de jours de conge acquis a chaque date anniversaire mensuelle d embauche', 'DECIMAL', '2.16'),
 ('reinitialisation_solde', 'Periode de conservation du solde', 'Nombre d annees civiles pendant lesquelles les droits acquis restent utilisables', 'INT', '2'),
 ('solde_negatif', 'Solde negatif', 'Autorise ou non un employe a utiliser un solde negatif', 'BOOLEEN', 'OUI'),
 ('solde_negatif_max', 'Limite du solde negatif', 'Solde minimum autorise lorsque le solde negatif est active', 'DECIMAL', '-5')
ON DUPLICATE KEY UPDATE variable_nom = VALUES(variable_nom);
