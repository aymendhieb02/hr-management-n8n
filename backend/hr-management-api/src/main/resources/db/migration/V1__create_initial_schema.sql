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

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX idx_users_manager_id ON users(manager_id);
CREATE INDEX idx_users_department_id ON users(department_id);
CREATE INDEX idx_users_position_id ON users(position_id);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_leave_requests_requester_id ON leave_requests(requester_id);
CREATE INDEX idx_leave_requests_approver_id ON leave_requests(approver_id);
CREATE INDEX idx_leave_requests_leave_type_id ON leave_requests(leave_type_id);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);
CREATE INDEX idx_leave_requests_start_date ON leave_requests(start_date);
CREATE INDEX idx_leave_requests_end_date ON leave_requests(end_date);
CREATE INDEX idx_leave_balances_user_id ON leave_balances(user_id);
CREATE INDEX idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);

INSERT INTO leave_types (name, description, max_days, requires_medical_certificate)
VALUES
    ('Annual Leave', 'Default paid annual leave entitlement.', 30, FALSE),
    ('Sick Leave', 'Leave used for illness or medical recovery.', 15, TRUE),
    ('Unpaid Leave', 'Leave without pay.', NULL, FALSE),
    ('Exceptional Leave', 'Short leave for exceptional circumstances.', 5, FALSE)
ON CONFLICT (name) DO NOTHING;
