DROP TABLE IF EXISTS leave_balance_accruals CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS medical_documents CASCADE;
DROP TABLE IF EXISTS leave_requests CASCADE;
DROP TABLE IF EXISTS leave_balances CASCADE;
DROP TABLE IF EXISTS leave_types CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS positions CASCADE;
DROP TABLE IF EXISTS departments CASCADE;

CREATE TABLE postes (
    poste_id BIGSERIAL PRIMARY KEY,
    poste_intitule VARCHAR(100) NOT NULL UNIQUE,
    poste_description VARCHAR(255),
    poste_niveau_poste VARCHAR(100),
    poste_actif BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP
);

CREATE TABLE type_contrats (
    type_contrat_id BIGSERIAL PRIMARY KEY,
    type_contrat_libelle VARCHAR(100) NOT NULL UNIQUE,
    type_contrat_description VARCHAR(255),
    type_contrat_actif BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP
);

CREATE TABLE employes (
    employe_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    mot_de_passe_hash VARCHAR(255) NOT NULL,
    employe_nom VARCHAR(100) NOT NULL,
    employe_prenom VARCHAR(100) NOT NULL,
    employe_email VARCHAR(150) NOT NULL UNIQUE,
    employe_telephone VARCHAR(30),
    employe_adresse VARCHAR(255),
    employe_date_naissance DATE,
    employe_date_embauche DATE,
    employe_sexe VARCHAR(30),
    role VARCHAR(30) NOT NULL,
    statut VARCHAR(30) NOT NULL DEFAULT 'ACTIF',
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    manager_id BIGINT,
    poste_id BIGINT,
    type_contrat_id BIGINT,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP,
    CONSTRAINT chk_employes_role CHECK (role IN ('EMPLOYE', 'MANAGER', 'RH', 'ADMIN')),
    CONSTRAINT chk_employes_statut CHECK (statut IN ('ACTIF', 'INACTIF')),
    CONSTRAINT chk_employes_manager_not_self CHECK (manager_id IS NULL OR manager_id <> employe_id),
    CONSTRAINT fk_employes_manager FOREIGN KEY (manager_id) REFERENCES employes(employe_id) ON DELETE SET NULL,
    CONSTRAINT fk_employes_poste FOREIGN KEY (poste_id) REFERENCES postes(poste_id) ON DELETE RESTRICT,
    CONSTRAINT fk_employes_type_contrat FOREIGN KEY (type_contrat_id) REFERENCES type_contrats(type_contrat_id) ON DELETE RESTRICT
);

CREATE TABLE conge_types (
    conge_type_id BIGSERIAL PRIMARY KEY,
    conge_type_nom VARCHAR(100) NOT NULL UNIQUE,
    conge_type_description VARCHAR(255),
    conge_type_jours_maximum NUMERIC(5,2),
    conge_type_certificat_obligatoire BOOLEAN NOT NULL DEFAULT FALSE,
    conge_type_remunere BOOLEAN NOT NULL DEFAULT TRUE,
    conge_type_actif BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP,
    CONSTRAINT chk_conge_types_jours_maximum CHECK (conge_type_jours_maximum IS NULL OR conge_type_jours_maximum > 0)
);

CREATE TABLE conge_demande_statuts (
    conge_demande_statut_id BIGSERIAL PRIMARY KEY,
    conge_demande_statut_libelle VARCHAR(100) NOT NULL UNIQUE,
    conge_demande_statut_description VARCHAR(255),
    conge_demande_statut_actif BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE raisons (
    raison_id BIGSERIAL PRIMARY KEY,
    raison_commentaire VARCHAR(500) NOT NULL,
    raison_date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE conge_demandes (
    conge_demande_id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    decideur_id BIGINT,
    conge_type_id BIGINT NOT NULL,
    conge_demande_statut_id BIGINT NOT NULL,
    raison_id BIGINT,
    conge_demande_date_debut DATE NOT NULL,
    conge_demande_heure_debut TIME,
    conge_demande_date_fin DATE NOT NULL,
    conge_demande_heure_fin TIME,
    conge_demande_date_soumission TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    conge_demande_nombre_jours NUMERIC(5,2) NOT NULL,
    conge_demande_commentaire_employe VARCHAR(1000),
    conge_demande_commentaire_decision VARCHAR(1000),
    conge_demande_date_decision TIMESTAMP,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP,
    CONSTRAINT fk_conge_demandes_employe FOREIGN KEY (employe_id) REFERENCES employes(employe_id) ON DELETE RESTRICT,
    CONSTRAINT fk_conge_demandes_decideur FOREIGN KEY (decideur_id) REFERENCES employes(employe_id) ON DELETE SET NULL,
    CONSTRAINT fk_conge_demandes_type FOREIGN KEY (conge_type_id) REFERENCES conge_types(conge_type_id) ON DELETE RESTRICT,
    CONSTRAINT fk_conge_demandes_statut FOREIGN KEY (conge_demande_statut_id) REFERENCES conge_demande_statuts(conge_demande_statut_id) ON DELETE RESTRICT,
    CONSTRAINT fk_conge_demandes_raison FOREIGN KEY (raison_id) REFERENCES raisons(raison_id) ON DELETE SET NULL,
    CONSTRAINT chk_conge_demandes_dates CHECK (conge_demande_date_fin >= conge_demande_date_debut),
    CONSTRAINT chk_conge_demandes_nombre_jours CHECK (conge_demande_nombre_jours > 0),
    CONSTRAINT chk_conge_demandes_decideur_not_employe CHECK (decideur_id IS NULL OR decideur_id <> employe_id)
);

CREATE TABLE employe_certificat_medicals (
    employe_certificat_medical_id BIGSERIAL PRIMARY KEY,
    conge_demande_id BIGINT NOT NULL UNIQUE,
    employe_certificat_medical_nom_fichier VARCHAR(255) NOT NULL,
    employe_certificat_medical_lien_fichier VARCHAR(500) NOT NULL,
    employe_certificat_medical_type_mime VARCHAR(100) NOT NULL,
    employe_certificat_medical_taille_fichier BIGINT NOT NULL,
    employe_certificat_medical_date_soumission TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_certificats_conge_demande FOREIGN KEY (conge_demande_id) REFERENCES conge_demandes(conge_demande_id) ON DELETE CASCADE,
    CONSTRAINT chk_certificats_taille CHECK (employe_certificat_medical_taille_fichier > 0)
);

CREATE TABLE conge_soldes (
    conge_solde_id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    conge_type_id BIGINT NOT NULL,
    conge_solde_jours_utilises NUMERIC(5,2) NOT NULL DEFAULT 0,
    conge_solde_restants NUMERIC(5,2) NOT NULL,
    conge_solde_annee INTEGER NOT NULL,
    conge_solde_droit_acquis NUMERIC(5,2) NOT NULL,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP,
    CONSTRAINT fk_conge_soldes_employe FOREIGN KEY (employe_id) REFERENCES employes(employe_id) ON DELETE RESTRICT,
    CONSTRAINT fk_conge_soldes_type FOREIGN KEY (conge_type_id) REFERENCES conge_types(conge_type_id) ON DELETE RESTRICT,
    CONSTRAINT uq_conge_soldes_employe_type_annee UNIQUE (employe_id, conge_type_id, conge_solde_annee),
    CONSTRAINT chk_conge_soldes_non_negative CHECK (conge_solde_jours_utilises >= 0 AND conge_solde_restants >= 0 AND conge_solde_droit_acquis >= 0),
    CONSTRAINT chk_conge_soldes_annee CHECK (conge_solde_annee BETWEEN 2000 AND 2100)
);

CREATE TABLE conge_solde_historiques (
    conge_solde_historique_id BIGSERIAL PRIMARY KEY,
    conge_solde_id BIGINT NOT NULL,
    conge_solde_historique_solde_avant NUMERIC(5,2) NOT NULL,
    conge_solde_historique_solde_apres NUMERIC(5,2) NOT NULL,
    conge_solde_historique_date_execution TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    conge_solde_historique_statut_acquisition VARCHAR(50) NOT NULL,
    conge_solde_historique_erreur VARCHAR(500),
    CONSTRAINT fk_conge_solde_historiques_solde FOREIGN KEY (conge_solde_id) REFERENCES conge_soldes(conge_solde_id) ON DELETE CASCADE
);

CREATE TABLE conge_demande_historiques (
    conge_demande_historique_id BIGSERIAL PRIMARY KEY,
    conge_demande_id BIGINT NOT NULL,
    conge_demande_historique_date_action TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    conge_demande_historique_commentaire VARCHAR(1000),
    conge_demande_historique_action VARCHAR(100) NOT NULL,
    conge_demande_historique_ancien_statut VARCHAR(100),
    conge_demande_historique_nouveau_statut VARCHAR(100),
    CONSTRAINT fk_conge_demande_historiques_demande FOREIGN KEY (conge_demande_id) REFERENCES conge_demandes(conge_demande_id) ON DELETE CASCADE
);

CREATE TABLE notification_types (
    notification_type_id BIGSERIAL PRIMARY KEY,
    notification_type_libelle VARCHAR(100) NOT NULL UNIQUE,
    notification_type_description VARCHAR(255),
    notification_type_actif BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE notifications (
    notification_id BIGSERIAL PRIMARY KEY,
    employe_id BIGINT NOT NULL,
    notification_type_id BIGINT NOT NULL,
    notification_titre VARCHAR(200) NOT NULL,
    notification_contenu TEXT NOT NULL,
    notification_lu BOOLEAN NOT NULL DEFAULT FALSE,
    notification_date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notification_date_lecture TIMESTAMP,
    notification_priorite_notification VARCHAR(50),
    CONSTRAINT fk_notifications_employe FOREIGN KEY (employe_id) REFERENCES employes(employe_id) ON DELETE RESTRICT,
    CONSTRAINT fk_notifications_type FOREIGN KEY (notification_type_id) REFERENCES notification_types(notification_type_id) ON DELETE RESTRICT
);

CREATE INDEX idx_employes_manager_id ON employes(manager_id);
CREATE INDEX idx_employes_poste_id ON employes(poste_id);
CREATE INDEX idx_employes_type_contrat_id ON employes(type_contrat_id);
CREATE INDEX idx_employes_role ON employes(role);
CREATE INDEX idx_conge_demandes_employe_id ON conge_demandes(employe_id);
CREATE INDEX idx_conge_demandes_decideur_id ON conge_demandes(decideur_id);
CREATE INDEX idx_conge_demandes_type_id ON conge_demandes(conge_type_id);
CREATE INDEX idx_conge_demandes_statut_id ON conge_demandes(conge_demande_statut_id);
CREATE INDEX idx_conge_demandes_dates ON conge_demandes(conge_demande_date_debut, conge_demande_date_fin);
CREATE INDEX idx_conge_soldes_employe_id ON conge_soldes(employe_id);
CREATE INDEX idx_notifications_employe_id ON notifications(employe_id);
CREATE INDEX idx_notifications_lu ON notifications(notification_lu);

INSERT INTO postes (poste_intitule, poste_description, poste_niveau_poste)
VALUES ('Collaborateur', 'Poste par defaut', 'Standard')
ON CONFLICT (poste_intitule) DO NOTHING;

INSERT INTO type_contrats (type_contrat_libelle, type_contrat_description)
VALUES
    ('CDI', 'Contrat a duree indeterminee'),
    ('CDD', 'Contrat a duree determinee'),
    ('Stage', 'Convention de stage')
ON CONFLICT (type_contrat_libelle) DO NOTHING;

INSERT INTO conge_types (conge_type_nom, conge_type_description, conge_type_jours_maximum, conge_type_certificat_obligatoire, conge_type_remunere)
VALUES
    ('Conge annuel', 'Droit annuel remunere.', 30, FALSE, TRUE),
    ('Conge maladie', 'Conge pour maladie ou recuperation medicale.', 15, TRUE, TRUE),
    ('Conge sans solde', 'Conge non remunere.', NULL, FALSE, FALSE),
    ('Conge exceptionnel', 'Absence courte pour evenement exceptionnel.', 5, FALSE, TRUE)
ON CONFLICT (conge_type_nom) DO NOTHING;

INSERT INTO conge_demande_statuts (conge_demande_statut_libelle, conge_demande_statut_description)
VALUES
    ('EN_ATTENTE', 'Demande en attente de decision'),
    ('APPROUVEE', 'Demande approuvee'),
    ('REFUSEE', 'Demande refusee'),
    ('ANNULEE', 'Demande annulee')
ON CONFLICT (conge_demande_statut_libelle) DO NOTHING;

INSERT INTO notification_types (notification_type_libelle, notification_type_description)
VALUES
    ('INFO', 'Notification informative'),
    ('DEMANDE_CONGE', 'Notification liee aux demandes de conge'),
    ('DECISION_CONGE', 'Notification liee aux decisions de conge')
ON CONFLICT (notification_type_libelle) DO NOTHING;

-- Compatibilite temporaire backend pendant la refonte radicale du code Java.
-- Ces tables seront supprimees quand tous les modules auront ete renommes en francais.
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE positions (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    hire_date DATE,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    manager_id BIGINT,
    department_id BIGINT,
    position_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_users_role CHECK (role IN ('EMPLOYEE', 'MANAGER', 'HR', 'ADMIN')),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT fk_users_manager FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_users_position FOREIGN KEY (position_id) REFERENCES positions(id) ON DELETE RESTRICT,
    CONSTRAINT chk_users_manager_not_self CHECK (manager_id IS NULL OR manager_id <> id)
);

CREATE TABLE leave_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    max_days INTEGER,
    requires_medical_certificate BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT chk_leave_types_max_days CHECK (max_days IS NULL OR max_days > 0)
);

CREATE TABLE leave_balances (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    year INTEGER NOT NULL,
    total_days NUMERIC(5,2) NOT NULL,
    used_days NUMERIC(5,2) NOT NULL DEFAULT 0,
    remaining_days NUMERIC(5,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_leave_balances_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_leave_balances_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types(id) ON DELETE RESTRICT,
    CONSTRAINT uq_leave_balances_user_type_year UNIQUE (user_id, leave_type_id, year),
    CONSTRAINT chk_leave_balances_days_non_negative CHECK (total_days >= 0 AND used_days >= 0 AND remaining_days >= 0),
    CONSTRAINT chk_leave_balances_year CHECK (year BETWEEN 2000 AND 2100)
);

CREATE TABLE leave_requests (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    approver_id BIGINT,
    leave_type_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    requested_days NUMERIC(5,2) NOT NULL,
    reason TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    decision_comment TEXT,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decision_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_leave_requests_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_leave_requests_approver FOREIGN KEY (approver_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_leave_requests_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types(id) ON DELETE RESTRICT,
    CONSTRAINT chk_leave_requests_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    CONSTRAINT chk_leave_requests_date_range CHECK (end_date >= start_date),
    CONSTRAINT chk_leave_requests_requested_days CHECK (requested_days > 0),
    CONSTRAINT chk_leave_requests_approver_not_requester CHECK (approver_id IS NULL OR approver_id <> requester_id)
);

CREATE TABLE medical_documents (
    id BIGSERIAL PRIMARY KEY,
    leave_request_id BIGINT NOT NULL UNIQUE,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL UNIQUE,
    storage_path VARCHAR(500) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_medical_documents_leave_request FOREIGN KEY (leave_request_id) REFERENCES leave_requests(id) ON DELETE CASCADE,
    CONSTRAINT chk_medical_documents_file_size CHECK (file_size > 0)
);

CREATE TABLE leave_balance_accruals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    accrual_year INTEGER NOT NULL,
    accrual_month INTEGER NOT NULL,
    credited_days NUMERIC(5,2) NOT NULL,
    balance_before NUMERIC(5,2) NOT NULL,
    balance_after NUMERIC(5,2) NOT NULL,
    executed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL,
    error_message VARCHAR(500),
    CONSTRAINT fk_leave_balance_accruals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_leave_balance_accruals_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave_types(id) ON DELETE RESTRICT,
    CONSTRAINT chk_leave_balance_accruals_month CHECK (accrual_month BETWEEN 1 AND 12),
    CONSTRAINT chk_leave_balance_accruals_credited_days CHECK (credited_days > 0),
    CONSTRAINT uq_leave_balance_accruals_user_type_year_month UNIQUE (user_id, leave_type_id, accrual_year, accrual_month)
);

CREATE TABLE legacy_notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    CONSTRAINT fk_legacy_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE RESTRICT
);

INSERT INTO leave_types (name, description, max_days, requires_medical_certificate)
VALUES
    ('Annual Leave', 'Default paid annual leave entitlement.', 30, FALSE),
    ('Sick Leave', 'Leave used for illness or medical recovery.', 15, TRUE),
    ('Unpaid Leave', 'Leave without pay.', NULL, FALSE),
    ('Exceptional Leave', 'Short leave for exceptional circumstances.', 5, FALSE)
ON CONFLICT (name) DO NOTHING;
