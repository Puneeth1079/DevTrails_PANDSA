-- GigShield Database Schema
-- Run this before starting the application (or let Hibernate auto-create with ddl-auto=update)

CREATE DATABASE IF NOT EXISTS gigshield_db;
USE gigshield_db;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  mobile VARCHAR(15) NOT NULL UNIQUE,
  email VARCHAR(150) UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('WORKER','ADMIN') NOT NULL DEFAULT 'WORKER',
  is_verified BOOLEAN DEFAULT FALSE,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS worker_profiles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE,
  platform VARCHAR(20) NOT NULL DEFAULT 'ZOMATO',
  platform_partner_id VARCHAR(100),
  city VARCHAR(100) NOT NULL,
  zone VARCHAR(100),
  pincode VARCHAR(10),
  avg_daily_earnings DECIMAL(10,2) DEFAULT 800.00,
  avg_daily_hours DECIMAL(4,2) DEFAULT 8.0,
  upi_id VARCHAR(100),
  bank_account VARCHAR(20),
  ifsc VARCHAR(12),
  aadhar_last4 VARCHAR(4),
  risk_score DECIMAL(5,2) DEFAULT 50.00,
  latitude DECIMAL(10,7),
  longitude DECIMAL(10,7),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS otp_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  mobile VARCHAR(15) NOT NULL,
  otp_code VARCHAR(6) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  is_used BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS policies (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  worker_id BIGINT NOT NULL,
  policy_number VARCHAR(30) UNIQUE NOT NULL,
  coverage_tier ENUM('BASIC','STANDARD','PREMIUM') NOT NULL,
  weekly_premium DECIMAL(8,2) NOT NULL,
  max_weekly_payout DECIMAL(8,2) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  status ENUM('ACTIVE','EXPIRED','CANCELLED','PENDING') DEFAULT 'PENDING',
  auto_renew BOOLEAN DEFAULT FALSE,
  triggers_covered JSON,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (worker_id) REFERENCES worker_profiles(id)
);

CREATE TABLE IF NOT EXISTS disruption_events (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  trigger_type ENUM('HEAVY_RAIN','EXTREME_HEAT','SEVERE_POLLUTION','CURFEW','FLOOD') NOT NULL,
  city VARCHAR(100) NOT NULL,
  zone VARCHAR(100),
  pincode VARCHAR(10),
  severity_value DECIMAL(8,2),
  severity_unit VARCHAR(20),
  threshold_breached DECIMAL(8,2),
  event_start TIMESTAMP NOT NULL,
  event_end TIMESTAMP,
  data_source VARCHAR(100),
  raw_api_response JSON,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS claims (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  claim_number VARCHAR(30) UNIQUE NOT NULL,
  policy_id BIGINT NOT NULL,
  worker_id BIGINT NOT NULL,
  disruption_event_id BIGINT,
  trigger_type ENUM('HEAVY_RAIN','EXTREME_HEAT','SEVERE_POLLUTION','CURFEW','FLOOD') NOT NULL,
  hours_lost DECIMAL(5,2) DEFAULT 0,
  payout_amount DECIMAL(8,2) NOT NULL,
  status ENUM('AUTO_APPROVED','PENDING_REVIEW','APPROVED','REJECTED','PAID') DEFAULT 'AUTO_APPROVED',
  fraud_score DECIMAL(5,2) DEFAULT 0.00,
  fraud_flags JSON,
  auto_triggered BOOLEAN DEFAULT TRUE,
  worker_location_lat DECIMAL(10,7),
  worker_location_lng DECIMAL(10,7),
  notes TEXT,
  claimed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  processed_at TIMESTAMP,
  FOREIGN KEY (policy_id) REFERENCES policies(id),
  FOREIGN KEY (worker_id) REFERENCES worker_profiles(id),
  FOREIGN KEY (disruption_event_id) REFERENCES disruption_events(id)
);

CREATE TABLE IF NOT EXISTS payouts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  claim_id BIGINT NOT NULL UNIQUE,
  worker_id BIGINT NOT NULL,
  amount DECIMAL(8,2) NOT NULL,
  payment_method ENUM('UPI','BANK_TRANSFER','WALLET') DEFAULT 'UPI',
  payment_reference VARCHAR(100),
  gateway_response JSON,
  status ENUM('PENDING','PROCESSING','SUCCESS','FAILED') DEFAULT 'PENDING',
  initiated_at TIMESTAMP,
  completed_at TIMESTAMP,
  FOREIGN KEY (claim_id) REFERENCES claims(id),
  FOREIGN KEY (worker_id) REFERENCES worker_profiles(id)
);
