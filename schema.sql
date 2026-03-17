-- ============================================================
-- GigShield - AI-Powered Parametric Insurance Platform
-- MySQL Database Schema
-- Guidewire DEVTrails 2026
-- ============================================================

CREATE DATABASE IF NOT EXISTS gigshield;
USE gigshield;

-- ------------------------------------------------------------
-- ZONES TABLE
-- Represents geographic delivery zones within a city
-- ------------------------------------------------------------
CREATE TABLE zones (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    zone_name     VARCHAR(100) NOT NULL,
    city          VARCHAR(100) NOT NULL,
    pincode       VARCHAR(10)  NOT NULL,
    state         VARCHAR(100) NOT NULL,
    risk_level    ENUM('LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH') DEFAULT 'MEDIUM',
    flood_prone   BOOLEAN DEFAULT FALSE,
    heat_prone    BOOLEAN DEFAULT FALSE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- WORKERS TABLE
-- Food delivery partners (Zomato / Swiggy)
-- ------------------------------------------------------------
CREATE TABLE workers (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    full_name           VARCHAR(150) NOT NULL,
    phone               VARCHAR(15)  NOT NULL UNIQUE,
    email               VARCHAR(150) UNIQUE,
    city                VARCHAR(100) NOT NULL,
    pincode             VARCHAR(10)  NOT NULL,
    zone_id             INT,
    platform            ENUM('ZOMATO', 'SWIGGY', 'BOTH') NOT NULL,
    avg_weekly_income   DECIMAL(10,2) DEFAULT 0.00,
    upi_id              VARCHAR(100),
    kyc_verified        BOOLEAN DEFAULT FALSE,
    risk_score          DECIMAL(5,2) DEFAULT 50.00,   -- 0 to 100 (AI calculated)
    account_status      ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (zone_id) REFERENCES zones(id)
);

-- ------------------------------------------------------------
-- POLICIES TABLE
-- Weekly insurance policies issued to workers
-- ------------------------------------------------------------
CREATE TABLE policies (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    worker_id           INT NOT NULL,
    plan_type           ENUM('BASIC', 'STANDARD', 'PRO') NOT NULL,
    weekly_premium      DECIMAL(8,2) NOT NULL,          -- AI calculated weekly amount
    base_premium        DECIMAL(8,2) NOT NULL,          -- Before multipliers (29/49/79)
    coverage_amount     DECIMAL(10,2) NOT NULL,         -- Max payout this week
    start_date          DATE NOT NULL,
    end_date            DATE NOT NULL,                  -- Always start_date + 7 days
    status              ENUM('ACTIVE', 'PAUSED', 'EXPIRED', 'CANCELLED') DEFAULT 'ACTIVE',
    auto_renew          BOOLEAN DEFAULT TRUE,
    zone_multiplier     DECIMAL(4,2) DEFAULT 1.00,
    season_multiplier   DECIMAL(4,2) DEFAULT 1.00,
    history_multiplier  DECIMAL(4,2) DEFAULT 1.00,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (worker_id) REFERENCES workers(id)
);

-- ------------------------------------------------------------
-- DISRUPTION EVENTS TABLE
-- External disruption events detected by the system
-- ------------------------------------------------------------
CREATE TABLE disruption_events (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    event_type      ENUM('HEAVY_RAIN', 'EXTREME_HEAT', 'SEVERE_AQI', 'FLOOD', 'BANDH_CURFEW') NOT NULL,
    zone_id         INT,
    city            VARCHAR(100) NOT NULL,
    severity_level  ENUM('MODERATE', 'SEVERE', 'EXTREME') NOT NULL,
    trigger_value   VARCHAR(100),                       -- e.g., "55mm rainfall", "44.2 degrees C"
    threshold_used  VARCHAR(100),                       -- e.g., "35mm in 3hrs"
    started_at      TIMESTAMP NOT NULL,
    ended_at        TIMESTAMP,
    data_source     VARCHAR(200),                       -- API name/URL
    api_verified    BOOLEAN DEFAULT FALSE,
    raw_data_json   JSON,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (zone_id) REFERENCES zones(id)
);

-- ------------------------------------------------------------
-- CLAIMS TABLE
-- Auto-generated claims triggered by disruption events
-- ------------------------------------------------------------
CREATE TABLE claims (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    worker_id           INT NOT NULL,
    policy_id           INT NOT NULL,
    event_id            INT NOT NULL,
    hours_lost          DECIMAL(4,2),                   -- Estimated hours lost
    amount_claimed      DECIMAL(10,2) NOT NULL,         -- System-calculated claim amount
    amount_approved     DECIMAL(10,2),                  -- Final approved amount
    status              ENUM('PENDING', 'AUTO_APPROVED', 'MANUAL_REVIEW', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    fraud_score         DECIMAL(5,2) DEFAULT 0.00,      -- 0-100 (>70 = review, >90 = reject)
    fraud_flags         JSON,                           -- Array of specific fraud flags
    auto_approved       BOOLEAN DEFAULT FALSE,
    reviewed_by         VARCHAR(100),
    review_notes        TEXT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (worker_id) REFERENCES workers(id),
    FOREIGN KEY (policy_id) REFERENCES policies(id),
    FOREIGN KEY (event_id) REFERENCES disruption_events(id)
);

-- ------------------------------------------------------------
-- PAYOUTS TABLE
-- Payment processing records
-- ------------------------------------------------------------
CREATE TABLE payouts (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    claim_id            INT NOT NULL,
    worker_id           INT NOT NULL,
    amount              DECIMAL(10,2) NOT NULL,
    upi_id              VARCHAR(100),
    gateway             ENUM('RAZORPAY', 'STRIPE', 'UPI_SIMULATOR') DEFAULT 'RAZORPAY',
    gateway_ref         VARCHAR(200),                   -- Payment gateway transaction ID
    gateway_response    JSON,
    status              ENUM('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED') DEFAULT 'PENDING',
    initiated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid_at             TIMESTAMP,
    failure_reason      TEXT,
    FOREIGN KEY (claim_id) REFERENCES claims(id),
    FOREIGN KEY (worker_id) REFERENCES workers(id)
);

-- ------------------------------------------------------------
-- PREMIUM HISTORY TABLE
-- Track weekly premium changes for each worker over time
-- ------------------------------------------------------------
CREATE TABLE premium_history (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    worker_id       INT NOT NULL,
    policy_id       INT NOT NULL,
    week_start      DATE NOT NULL,
    week_end        DATE NOT NULL,
    premium_paid    DECIMAL(8,2) NOT NULL,
    risk_score      DECIMAL(5,2),
    calculation_json JSON,                              -- Full breakdown of multipliers
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (worker_id) REFERENCES workers(id),
    FOREIGN KEY (policy_id) REFERENCES policies(id)
);

-- ------------------------------------------------------------
-- FRAUD LOGS TABLE
-- Detailed fraud detection audit trail
-- ------------------------------------------------------------
CREATE TABLE fraud_logs (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    claim_id        INT NOT NULL,
    worker_id       INT NOT NULL,
    check_type      ENUM('GPS_VALIDATION', 'TIMING_ANOMALY', 'DUPLICATE_CHECK', 'ACTIVITY_CROSS_REF') NOT NULL,
    result          ENUM('PASS', 'FLAG', 'FAIL') NOT NULL,
    score_impact    DECIMAL(5,2),
    details         TEXT,
    checked_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (claim_id) REFERENCES claims(id),
    FOREIGN KEY (worker_id) REFERENCES workers(id)
);

-- ------------------------------------------------------------
-- ADMIN USERS TABLE
-- Insurer / Admin portal users
-- ------------------------------------------------------------
CREATE TABLE admin_users (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            ENUM('SUPER_ADMIN', 'CLAIMS_MANAGER', 'ANALYST') DEFAULT 'ANALYST',
    is_active       BOOLEAN DEFAULT TRUE,
    last_login      TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- SAMPLE SEED DATA
-- ------------------------------------------------------------

-- Zones
INSERT INTO zones (zone_name, city, pincode, state, risk_level, flood_prone, heat_prone) VALUES
('Zone 1 - Koramangala',    'Bengaluru', '560034', 'Karnataka', 'MEDIUM',    FALSE, FALSE),
('Zone 2 - HSR Layout',     'Bengaluru', '560102', 'Karnataka', 'MEDIUM',    FALSE, FALSE),
('Zone 3 - Bellandur',      'Bengaluru', '560103', 'Karnataka', 'HIGH',      TRUE,  FALSE),
('Zone 4 - Whitefield',     'Bengaluru', '560066', 'Karnataka', 'HIGH',      TRUE,  FALSE),
('Zone 5 - Banjara Hills',  'Hyderabad', '500034', 'Telangana', 'MEDIUM',    FALSE, TRUE),
('Zone 6 - Hitech City',    'Hyderabad', '500081', 'Telangana', 'LOW',       FALSE, TRUE),
('Zone 7 - LB Nagar',       'Hyderabad', '500074', 'Telangana', 'VERY_HIGH', TRUE,  TRUE);

-- Sample Workers
INSERT INTO workers (full_name, phone, city, pincode, zone_id, platform, avg_weekly_income, upi_id, kyc_verified, risk_score) VALUES
('Ravi Kumar',      '9876543210', 'Bengaluru', '560103', 3, 'SWIGGY', 4200.00, 'ravi@upi',    TRUE,  62.5),
('Suresh Babu',     '9876543211', 'Hyderabad', '500074', 7, 'ZOMATO', 3800.00, 'suresh@upi',  TRUE,  74.0),
('Mahesh Rao',      '9876543212', 'Bengaluru', '560066', 4, 'BOTH',   4800.00, 'mahesh@upi',  TRUE,  58.0),
('Priya Devi',      '9876543213', 'Hyderabad', '500034', 5, 'SWIGGY', 3200.00, 'priya@upi',   FALSE, 45.0);

-- Sample Admin
INSERT INTO admin_users (username, email, password_hash, role) VALUES
('admin', 'admin@gigshield.in', '$2a$10$placeholder_bcrypt_hash', 'SUPER_ADMIN');
