-- Create packages table for Box Sender application
CREATE TABLE IF NOT EXISTS packages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tracking_number VARCHAR(100) NOT NULL UNIQUE,
    carrier VARCHAR(50) NOT NULL,
    recipient_first_name VARCHAR(100),
    recipient_last_name VARCHAR(100),
    recipient_email VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    pickup_code VARCHAR(10) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    created_at DATETIME NOT NULL,
    picked_up_at DATETIME,
    logged_by_employee_id BIGINT NOT NULL,
    picked_up_by_employee_id BIGINT,

    INDEX idx_tracking_number (tracking_number),
    INDEX idx_pickup_code (pickup_code),
    INDEX idx_recipient_email (recipient_email),
    INDEX idx_status (status),
    INDEX idx_logged_by (logged_by_employee_id),

    CONSTRAINT fk_logged_by_employee
        FOREIGN KEY (logged_by_employee_id)
        REFERENCES employees(id),

    CONSTRAINT fk_picked_up_by_employee
        FOREIGN KEY (picked_up_by_employee_id)
        REFERENCES employees(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
