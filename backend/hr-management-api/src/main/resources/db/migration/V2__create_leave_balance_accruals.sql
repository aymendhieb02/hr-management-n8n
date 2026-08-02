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

CREATE INDEX idx_leave_balance_accruals_user ON leave_balance_accruals(user_id);
CREATE INDEX idx_leave_balance_accruals_year_month ON leave_balance_accruals(accrual_year, accrual_month);
CREATE INDEX idx_leave_balance_accruals_leave_type ON leave_balance_accruals(leave_type_id);
