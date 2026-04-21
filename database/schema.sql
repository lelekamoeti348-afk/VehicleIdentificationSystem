-- =====================================================
-- VEHICLE IDENTIFICATION SYSTEM - COMPLETE DATABASE
-- =====================================================

-- Drop existing database if exists (for clean setup)
DROP DATABASE IF EXISTS vehicle_identification_system;

-- Create new database
CREATE DATABASE vehicle_identification_system
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8';

-- Connect to database
\c vehicle_identification_system;

-- =====================================================
-- ENUM TYPES
-- =====================================================
CREATE TYPE user_role AS ENUM ('ADMIN', 'CUSTOMER', 'POLICE', 'INSURANCE', 'WORKSHOP');
CREATE TYPE claim_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'SETTLED');
CREATE TYPE violation_status AS ENUM ('PAID', 'UNPAID', 'DISPUTED');
CREATE TYPE report_status AS ENUM ('OPEN', 'INVESTIGATING', 'CLOSED', 'RESOLVED');
CREATE TYPE vehicle_status AS ENUM ('ACTIVE', 'STOLEN', 'IMPOUNDED', 'SCRAPPED', 'SOLD');

-- =====================================================
-- USERS TABLE (Base Table)
-- =====================================================
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT,
    role user_role NOT NULL,
    profile_image BYTEA,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    created_by INTEGER REFERENCES users(user_id),
    CONSTRAINT chk_username_format CHECK (username ~ '^[a-zA-Z0-9_]{3,50}$'),
    CONSTRAINT chk_email_format CHECK (email ~ '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- =====================================================
-- ROLE-SPECIFIC TABLES (Extending Users)
-- =====================================================

-- CUSTOMERS
CREATE TABLE customers (
    customer_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    id_number VARCHAR(20) UNIQUE,
    driver_license VARCHAR(20) UNIQUE,
    date_of_birth DATE,
    emergency_contact VARCHAR(20),
    preferred_language VARCHAR(20) DEFAULT 'English',
    notification_preference VARCHAR(20) DEFAULT 'EMAIL'
);

-- POLICE OFFICERS
CREATE TABLE police_officers (
    officer_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    badge_number VARCHAR(20) UNIQUE NOT NULL,
    rank VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    station_name VARCHAR(100),
    jurisdiction_area VARCHAR(200),
    years_of_service INTEGER DEFAULT 0
);

-- INSURANCE AGENTS
CREATE TABLE insurance_agents (
    agent_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    agent_code VARCHAR(20) UNIQUE NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) UNIQUE NOT NULL,
    commission_rate DECIMAL(5,2) DEFAULT 0,
    expiry_date DATE
);

-- WORKSHOP STAFF
CREATE TABLE workshop_staff (
    staff_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    staff_code VARCHAR(20) UNIQUE NOT NULL,
    position VARCHAR(50) NOT NULL,
    specialization VARCHAR(100),
    hourly_rate DECIMAL(10,2),
    qualification VARCHAR(100)
);

-- =====================================================
-- VEHICLES TABLE
-- =====================================================
CREATE TABLE vehicles (
    vehicle_id SERIAL PRIMARY KEY,
    registration_number VARCHAR(20) UNIQUE NOT NULL,
    make VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL,
    color VARCHAR(30),
    engine_number VARCHAR(50) UNIQUE,
    chassis_number VARCHAR(50) UNIQUE,
    fuel_type VARCHAR(20) CHECK (fuel_type IN ('PETROL', 'DIESEL', 'ELECTRIC', 'HYBRID')),
    transmission VARCHAR(20) CHECK (transmission IN ('MANUAL', 'AUTOMATIC')),
    seating_capacity INTEGER DEFAULT 5,
    mileage INTEGER DEFAULT 0,
    owner_id INTEGER REFERENCES customers(customer_id),
    status vehicle_status DEFAULT 'ACTIVE',
    registration_date DATE DEFAULT CURRENT_DATE,
    last_service_date DATE,
    next_service_due INTEGER DEFAULT 5000,
    image BYTEA,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_year CHECK (year BETWEEN 1900 AND EXTRACT(YEAR FROM CURRENT_DATE) + 1)
);

-- Create indexes for faster search
CREATE INDEX idx_vehicle_registration ON vehicles(registration_number);
CREATE INDEX idx_vehicle_owner ON vehicles(owner_id);
CREATE INDEX idx_vehicle_make_model ON vehicles(make, model);
CREATE INDEX idx_vehicle_status ON vehicles(status);

-- =====================================================
-- SERVICE RECORDS
-- =====================================================
CREATE TABLE service_records (
    service_id SERIAL PRIMARY KEY,
    vehicle_id INTEGER REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    staff_id INTEGER REFERENCES workshop_staff(staff_id),
    service_date DATE NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    service_category VARCHAR(50) CHECK (service_category IN ('MAINTENANCE', 'REPAIR', 'INSPECTION', 'EMERGENCY')),
    description TEXT,
    parts_replaced TEXT,
    cost DECIMAL(10,2) NOT NULL,
    odometer_reading INTEGER,
    next_service_due INTEGER,
    labor_hours DECIMAL(5,2),
    warranty_period INTEGER,
    invoice_number VARCHAR(50) UNIQUE,
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_vehicle ON service_records(vehicle_id);
CREATE INDEX idx_service_date ON service_records(service_date);
CREATE INDEX idx_service_type ON service_records(service_type);

-- =====================================================
-- INSURANCE POLICIES
-- =====================================================
CREATE TABLE insurance_policies (
    policy_id SERIAL PRIMARY KEY,
    vehicle_id INTEGER REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    agent_id INTEGER REFERENCES insurance_agents(agent_id),
    policy_number VARCHAR(50) UNIQUE NOT NULL,
    insurance_company VARCHAR(100) NOT NULL,
    policy_type VARCHAR(50) CHECK (policy_type IN ('COMPREHENSIVE', 'THIRD_PARTY', 'COLLISION', 'LIABILITY')),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    premium_amount DECIMAL(10,2) NOT NULL,
    coverage_amount DECIMAL(12,2),
    excess_amount DECIMAL(10,2),
    deductible DECIMAL(10,2),
    coverage_details TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    renewal_date DATE,
    payment_frequency VARCHAR(20) DEFAULT 'ANNUAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_dates CHECK (end_date > start_date)
);

CREATE INDEX idx_policy_vehicle ON insurance_policies(vehicle_id);
CREATE INDEX idx_policy_number ON insurance_policies(policy_number);
CREATE INDEX idx_policy_dates ON insurance_policies(start_date, end_date);

-- =====================================================
-- INSURANCE CLAIMS
-- =====================================================
CREATE TABLE insurance_claims (
    claim_id SERIAL PRIMARY KEY,
    policy_id INTEGER REFERENCES insurance_policies(policy_id) ON DELETE CASCADE,
    claim_number VARCHAR(50) UNIQUE NOT NULL,
    claim_date DATE NOT NULL,
    accident_date DATE,
    accident_location TEXT,
    claim_amount DECIMAL(12,2) NOT NULL,
    approved_amount DECIMAL(12,2),
    description TEXT,
    status claim_status DEFAULT 'PENDING',
    adjuster_notes TEXT,
    settlement_date DATE,
    police_report_number VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_claim_policy ON insurance_claims(policy_id);
CREATE INDEX idx_claim_status ON insurance_claims(status);

-- =====================================================
-- POLICE REPORTS
-- =====================================================
CREATE TABLE police_reports (
    report_id SERIAL PRIMARY KEY,
    vehicle_id INTEGER REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    officer_id INTEGER REFERENCES police_officers(officer_id),
    report_number VARCHAR(50) UNIQUE NOT NULL,
    report_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    report_type VARCHAR(50) CHECK (report_type IN ('ACCIDENT', 'THEFT', 'VANDALISM', 'IMPOUND', 'STOLEN_RECOVERY')),
    description TEXT NOT NULL,
    accident_location TEXT,
    accident_severity VARCHAR(20) CHECK (accident_severity IN ('MINOR', 'MODERATE', 'SEVERE', 'FATAL')),
    injuries_reported BOOLEAN DEFAULT FALSE,
    fatalities INTEGER DEFAULT 0,
    witness_statements TEXT,
    case_status report_status DEFAULT 'OPEN',
    assigned_detective VARCHAR(100),
    resolution_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_report_vehicle ON police_reports(vehicle_id);
CREATE INDEX idx_report_number ON police_reports(report_number);
CREATE INDEX idx_report_date ON police_reports(report_date);

-- =====================================================
-- VIOLATIONS
-- =====================================================
CREATE TABLE violations (
    violation_id SERIAL PRIMARY KEY,
    vehicle_id INTEGER REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    officer_id INTEGER REFERENCES police_officers(officer_id),
    violation_code VARCHAR(20) NOT NULL,
    violation_date TIMESTAMP NOT NULL,
    violation_type VARCHAR(100) NOT NULL,
    violation_location TEXT,
    fine_amount DECIMAL(10,2) NOT NULL,
    points INTEGER DEFAULT 0,
    status violation_status DEFAULT 'UNPAID',
    payment_date DATE,
    payment_receipt VARCHAR(50),
    court_date DATE,
    court_outcome TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_violation_vehicle ON violations(vehicle_id);
CREATE INDEX idx_violation_status ON violations(status);

-- =====================================================
-- CUSTOMER QUERIES
-- =====================================================
CREATE TABLE customer_queries (
    query_id SERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(customer_id) ON DELETE CASCADE,
    vehicle_id INTEGER REFERENCES vehicles(vehicle_id),
    query_number VARCHAR(50) UNIQUE NOT NULL,
    query_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    query_type VARCHAR(50) CHECK (query_type IN ('INFORMATION', 'COMPLAINT', 'SERVICE', 'INSURANCE', 'POLICE')),
    subject VARCHAR(200) NOT NULL,
    query_text TEXT NOT NULL,
    priority VARCHAR(20) DEFAULT 'NORMAL',
    response_text TEXT,
    responded_by INTEGER REFERENCES users(user_id),
    responded_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    resolution_date DATE,
    satisfaction_rating INTEGER CHECK (satisfaction_rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_query_customer ON customer_queries(customer_id);
CREATE INDEX idx_query_status ON customer_queries(status);

-- =====================================================
-- STOLEN VEHICLE ALERTS
-- =====================================================
CREATE TABLE stolen_vehicle_alerts (
    alert_id SERIAL PRIMARY KEY,
    vehicle_id INTEGER REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    report_id INTEGER REFERENCES police_reports(report_id),
    alert_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_seen_location TEXT,
    last_seen_date TIMESTAMP,
    reward_amount DECIMAL(10,2),
    alert_status VARCHAR(20) DEFAULT 'ACTIVE',
    recovery_date DATE,
    recovery_notes TEXT
);

-- =====================================================
-- APPOINTMENTS
-- =====================================================
CREATE TABLE appointments (
    appointment_id SERIAL PRIMARY KEY,
    vehicle_id INTEGER REFERENCES vehicles(vehicle_id),
    customer_id INTEGER REFERENCES customers(customer_id),
    staff_id INTEGER REFERENCES workshop_staff(staff_id),
    appointment_date TIMESTAMP NOT NULL,
    appointment_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- VIEWS
-- =====================================================

-- Complete Vehicle Information View
CREATE VIEW vw_vehicle_details AS
SELECT
    v.vehicle_id,
    v.registration_number,
    v.make,
    v.model,
    v.year,
    v.color,
    v.fuel_type,
    v.transmission,
    v.mileage,
    v.status,
    c.full_name AS owner_name,
    c.email AS owner_email,
    c.phone AS owner_phone,
    c.driver_license,
    c.id_number,
    u.address AS owner_address,
    (SELECT COUNT(*) FROM service_records s WHERE s.vehicle_id = v.vehicle_id) AS total_services,
    (SELECT COALESCE(SUM(cost), 0) FROM service_records s WHERE s.vehicle_id = v.vehicle_id) AS total_service_cost
FROM vehicles v
LEFT JOIN customers c ON v.owner_id = c.customer_id
LEFT JOIN users u ON c.customer_id = u.user_id;

-- Complete Service History View
CREATE VIEW vw_service_history AS
SELECT
    s.service_id,
    v.registration_number,
    v.make || ' ' || v.model AS vehicle_name,
    s.service_date,
    s.service_type,
    s.service_category,
    s.description,
    s.parts_replaced,
    s.cost,
    s.odometer_reading,
    s.labor_hours,
    s.payment_status,
    u.full_name AS serviced_by,
    ws.specialization,
    s.invoice_number
FROM service_records s
JOIN vehicles v ON s.vehicle_id = v.vehicle_id
LEFT JOIN workshop_staff ws ON s.staff_id = ws.staff_id
LEFT JOIN users u ON ws.staff_id = u.user_id;

-- Active Insurance Policies View
CREATE VIEW vw_active_policies AS
SELECT
    p.policy_id,
    p.policy_number,
    v.registration_number,
    v.make || ' ' || v.model AS vehicle_name,
    p.insurance_company,
    p.policy_type,
    p.start_date,
    p.end_date,
    p.premium_amount,
    p.coverage_amount,
    (p.end_date - CURRENT_DATE) AS days_remaining,
    CASE
        WHEN p.end_date < CURRENT_DATE THEN 'EXPIRED'
        WHEN p.end_date - CURRENT_DATE <= 30 THEN 'EXPIRING_SOON'
        ELSE 'ACTIVE'
    END AS policy_status,
    u.full_name AS agent_name,
    u.phone AS agent_phone
FROM insurance_policies p
JOIN vehicles v ON p.vehicle_id = v.vehicle_id
LEFT JOIN insurance_agents ia ON p.agent_id = ia.agent_id
LEFT JOIN users u ON ia.agent_id = u.user_id
WHERE p.status = 'ACTIVE';

-- Stolen Vehicles View
CREATE VIEW vw_stolen_vehicles AS
SELECT
    pr.report_number,
    v.registration_number,
    v.make,
    v.model,
    v.year,
    v.color,
    u.full_name AS owner_name,
    u.phone AS owner_phone,
    pr.report_date,
    pr.description AS theft_details,
    pr.accident_location AS last_known_location,
    pr.case_status,
    pr.assigned_detective,
    sa.reward_amount,
    sa.alert_status
FROM police_reports pr
JOIN vehicles v ON pr.vehicle_id = v.vehicle_id
LEFT JOIN customers c ON v.owner_id = c.customer_id
LEFT JOIN users u ON c.customer_id = u.user_id
LEFT JOIN stolen_vehicle_alerts sa ON pr.report_id = sa.report_id
WHERE pr.report_type = 'THEFT' AND pr.case_status != 'CLOSED';

-- Vehicle Violations View
CREATE VIEW vw_vehicle_violations AS
SELECT
    v.violation_id,
    v.violation_code,
    v.violation_date,
    v.violation_type,
    v.violation_location,
    v.fine_amount,
    v.points,
    v.status,
    ve.registration_number,
    ve.make || ' ' || ve.model AS vehicle_name,
    u.full_name AS owner_name,
    po.badge_number,
    po.rank,
    vu.full_name AS officer_name
FROM violations v
JOIN vehicles ve ON v.vehicle_id = ve.vehicle_id
LEFT JOIN customers c ON ve.owner_id = c.customer_id
LEFT JOIN users u ON c.customer_id = u.user_id
LEFT JOIN police_officers po ON v.officer_id = po.officer_id
LEFT JOIN users vu ON po.officer_id = vu.user_id;

-- =====================================================
-- STORED PROCEDURES
-- =====================================================

-- Procedure: Register New Vehicle
CREATE OR REPLACE PROCEDURE sp_register_vehicle(
    p_registration_number VARCHAR,
    p_make VARCHAR,
    p_model VARCHAR,
    p_year INTEGER,
    p_color VARCHAR,
    p_owner_id INTEGER,
    p_engine_number VARCHAR,
    p_chassis_number VARCHAR,
    p_fuel_type VARCHAR,
    p_transmission VARCHAR,
    p_seating_capacity INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO vehicles (
        registration_number, make, model, year, color, owner_id,
        engine_number, chassis_number, fuel_type, transmission, seating_capacity
    ) VALUES (
        p_registration_number, p_make, p_model, p_year, p_color, p_owner_id,
        p_engine_number, p_chassis_number, p_fuel_type, p_transmission, p_seating_capacity
    );

    RAISE NOTICE 'Vehicle % registered successfully', p_registration_number;
END;
$$;

-- Procedure: Add Service Record
CREATE OR REPLACE PROCEDURE sp_add_service_record(
    p_vehicle_id INTEGER,
    p_staff_id INTEGER,
    p_service_date DATE,
    p_service_type VARCHAR,
    p_service_category VARCHAR,
    p_description TEXT,
    p_cost DECIMAL,
    p_odometer_reading INTEGER,
    p_parts_replaced TEXT
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO service_records (
        vehicle_id, staff_id, service_date, service_type, service_category,
        description, cost, odometer_reading, parts_replaced, invoice_number
    ) VALUES (
        p_vehicle_id, p_staff_id, p_service_date, p_service_type, p_service_category,
        p_description, p_cost, p_odometer_reading, p_parts_replaced,
        'INV-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(CAST(NEXTVAL('service_records_service_id_seq') AS TEXT), 5, '0')
    );

    -- Update vehicle last service date
    UPDATE vehicles
    SET last_service_date = p_service_date,
        mileage = p_odometer_reading,
        updated_at = CURRENT_TIMESTAMP
    WHERE vehicle_id = p_vehicle_id;

    RAISE NOTICE 'Service record added for vehicle ID: %', p_vehicle_id;
END;
$$;

-- Procedure: File Police Report
CREATE OR REPLACE PROCEDURE sp_file_police_report(
    p_vehicle_id INTEGER,
    p_officer_id INTEGER,
    p_report_type VARCHAR,
    p_description TEXT,
    p_location TEXT,
    p_severity VARCHAR DEFAULT NULL
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_report_number VARCHAR;
BEGIN
    v_report_number := 'RPT-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || LPAD(CAST(NEXTVAL('police_reports_report_id_seq') AS TEXT), 4, '0');

    INSERT INTO police_reports (
        vehicle_id, officer_id, report_number, report_type, description,
        accident_location, accident_severity
    ) VALUES (
        p_vehicle_id, p_officer_id, v_report_number, p_report_type, p_description,
        p_location, p_severity
    );

    -- Update vehicle status if theft
    IF p_report_type = 'THEFT' THEN
        UPDATE vehicles SET status = 'STOLEN' WHERE vehicle_id = p_vehicle_id;

        -- Create stolen vehicle alert
        INSERT INTO stolen_vehicle_alerts (vehicle_id, report_id, last_seen_location)
        SELECT p_vehicle_id, report_id, p_location
        FROM police_reports
        WHERE report_number = v_report_number;
    END IF;

    RAISE NOTICE 'Police report % filed successfully', v_report_number;
END;
$$;

-- Procedure: Issue Violation
CREATE OR REPLACE PROCEDURE sp_issue_violation(
    p_vehicle_id INTEGER,
    p_officer_id INTEGER,
    p_violation_code VARCHAR,
    p_violation_type VARCHAR,
    p_location TEXT,
    p_fine_amount DECIMAL,
    p_points INTEGER
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO violations (
        vehicle_id, officer_id, violation_code, violation_type,
        violation_location, fine_amount, points, status
    ) VALUES (
        p_vehicle_id, p_officer_id, p_violation_code, p_violation_type,
        p_location, p_fine_amount, p_points, 'UNPAID'
    );

    RAISE NOTICE 'Violation issued to vehicle %', p_vehicle_id;
END;
$$;

-- =====================================================
-- FUNCTIONS
-- =====================================================

-- Function: Search Vehicle by Registration
CREATE OR REPLACE FUNCTION fn_search_vehicle(p_search_term VARCHAR)
RETURNS TABLE(
    registration_number VARCHAR,
    make VARCHAR,
    model VARCHAR,
    year INTEGER,
    owner_name VARCHAR,
    owner_phone VARCHAR,
    vehicle_status VARCHAR
) LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        v.registration_number,
        v.make,
        v.model,
        v.year,
        u.full_name,
        u.phone,
        CAST(v.status AS VARCHAR)
    FROM vehicles v
    LEFT JOIN customers c ON v.owner_id = c.customer_id
    LEFT JOIN users u ON c.customer_id = u.user_id
    WHERE v.registration_number ILIKE '%' || p_search_term || '%'
       OR v.make ILIKE '%' || p_search_term || '%'
       OR v.model ILIKE '%' || p_search_term || '%';
END;
$$;

-- Function: Get Vehicle Service Summary
CREATE OR REPLACE FUNCTION fn_get_service_summary(p_vehicle_id INTEGER)
RETURNS TABLE(
    total_services BIGINT,
    total_cost DECIMAL,
    average_cost DECIMAL,
    last_service_date DATE,
    most_common_service VARCHAR
) LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        COUNT(*)::BIGINT,
        COALESCE(SUM(cost), 0),
        COALESCE(AVG(cost), 0),
        MAX(service_date),
        MODE() WITHIN GROUP (ORDER BY service_type)
    FROM service_records
    WHERE vehicle_id = p_vehicle_id;
END;
$$;

-- Function: Calculate Insurance Risk Score
CREATE OR REPLACE FUNCTION fn_calculate_risk_score(p_vehicle_id INTEGER)
RETURNS DECIMAL
LANGUAGE plpgsql
AS $$
DECLARE
    v_violation_count INTEGER;
    v_accident_count INTEGER;
    v_vehicle_age INTEGER;
    v_risk_score DECIMAL;
BEGIN
    -- Count violations in last 3 years
    SELECT COUNT(*) INTO v_violation_count
    FROM violations
    WHERE vehicle_id = p_vehicle_id
      AND violation_date >= CURRENT_DATE - INTERVAL '3 years';

    -- Count accidents
    SELECT COUNT(*) INTO v_accident_count
    FROM police_reports
    WHERE vehicle_id = p_vehicle_id
      AND report_type = 'ACCIDENT'
      AND report_date >= CURRENT_DATE - INTERVAL '3 years';

    -- Calculate vehicle age
    SELECT EXTRACT(YEAR FROM CURRENT_DATE) - year INTO v_vehicle_age
    FROM vehicles
    WHERE vehicle_id = p_vehicle_id;

    -- Risk score calculation (lower is better)
    v_risk_score := 0;
    v_risk_score := v_risk_score + (v_violation_count * 5);
    v_risk_score := v_risk_score + (v_accident_count * 15);
    v_risk_score := v_risk_score + (GREATEST(0, v_vehicle_age - 5) * 2);

    RETURN LEAST(100, v_risk_score);
END;
$$;

-- =====================================================
-- TRIGGERS
-- =====================================================

-- Update timestamp trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_vehicles_updated_at
    BEFORE UPDATE ON vehicles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_insurance_claims_updated_at
    BEFORE UPDATE ON insurance_claims
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- SAMPLE DATA (SESOTHO NAMES)
-- =====================================================

-- Insert Users
INSERT INTO users (username, password_hash, full_name, email, phone, address, role) VALUES
('moeti_leleka', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Moeti Leleka', 'moeti.leleka@vis.gov.ls', '+266 5888 1234', 'Maseru West, Maseru 100', 'ADMIN'),
('pofane_matlali', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Pofane Matlali', 'pofane.matlali@police.gov.ls', '+266 5888 2345', 'Police HQ, Maseru', 'POLICE'),
('retshedisitsoe_moeketsi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Retshedisitsoe Moeketsi', 'retshedisitsoe@email.com', '+266 5888 3456', 'Mazenod, Maseru', 'CUSTOMER'),
('lethako_khabele', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Lethako Khabele', 'lethako.khabele@insurance.co.ls', '+266 5888 4567', 'Khubetsoana, Maseru', 'INSURANCE'),
('reneilwe_moeketsi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Reneilwe Moeketsi', 'reneilwe@workshop.co.ls', '+266 5888 5678', 'Thetsane Industrial, Maseru', 'WORKSHOP'),
('lethabo_radebe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Lethabo Radebe', 'lethabo.radebe@email.com', '+266 5888 6789', 'Mabote, Maseru', 'CUSTOMER'),
('nthuping_nthuping', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Nthuping Nthuping', 'nthuping@police.gov.ls', '+266 5888 7890', 'Maseru Central', 'POLICE'),
('thabo_molefe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Thabo Molefe', 'thabo@email.com', '+266 5888 8901', 'Ha Thetsane, Maseru', 'CUSTOMER'),
('mapaseka_mofolo', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Mapaseka Mofolo', 'mapaseka@workshop.co.ls', '+266 5888 9012', 'Letsie Road, Maseru', 'WORKSHOP'),
('tsepiso_ramokoena', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Tsepiso Ramokoena', 'tsepiso@insurance.co.ls', '+266 5888 0123', 'Kingsway, Maseru', 'INSURANCE');

-- Insert Customers (extending users)
INSERT INTO customers (customer_id, id_number, driver_license, date_of_birth, emergency_contact) VALUES
(3, 'LS123456789', 'DL123456', '1985-03-15', '+266 5888 1111'),
(6, 'LS987654321', 'DL789012', '1990-07-22', '+266 5888 2222'),
(8, 'LS456789123', 'DL345678', '1978-11-08', '+266 5888 3333');

-- Insert Police Officers
INSERT INTO police_officers (officer_id, badge_number, rank, department, station_name, years_of_service) VALUES
(2, 'BP12345', 'Inspector', 'Traffic Division', 'Maseru Central', 12),
(7, 'BP67890', 'Sergeant', 'Vehicle Theft Unit', 'Maseru West', 8);

-- Insert Insurance Agents
INSERT INTO insurance_agents (agent_id, agent_code, company_name, license_number, commission_rate) VALUES
(4, 'AG001', 'Lesotho Insurance Company', 'LIC123456', 15.00),
(10, 'AG002', 'Metropolitan Lesotho', 'MET789012', 12.50);

-- Insert Workshop Staff
INSERT INTO workshop_staff (staff_id, staff_code, position, specialization, hourly_rate) VALUES
(5, 'WS001', 'Senior Mechanic', 'Engine & Transmission', 150.00),
(9, 'WS002', 'Diagnostic Technician', 'Electrical Systems', 120.00);

-- Insert Vehicles
INSERT INTO vehicles (registration_number, make, model, year, color, engine_number, chassis_number, fuel_type, transmission, seating_capacity, owner_id, mileage, status) VALUES
('B 1234 LS', 'Toyota', 'Hilux', 2022, 'White', '2GD123456', 'MR0FZ29G001234567', 'DIESEL', 'MANUAL', 5, 3, 45000, 'ACTIVE'),
('B 5678 LS', 'Hyundai', 'Grand i10', 2021, 'Silver', 'G4LC789012', 'KMHBT51GLMU123456', 'PETROL', 'MANUAL', 5, 6, 28000, 'ACTIVE'),
('B 9012 LS', 'Ford', 'Ranger', 2023, 'Blue', 'P5AT345678', '1FTFW1E56PFA12345', 'DIESEL', 'AUTOMATIC', 5, 8, 15000, 'ACTIVE'),
('B 3456 LS', 'Kia', 'Sportage', 2020, 'Red', 'D4HB901234', 'KNDPBCACXG7123456', 'DIESEL', 'AUTOMATIC', 5, 3, 67000, 'ACTIVE'),
('B 7890 LS', 'Volkswagen', 'Polo', 2022, 'Black', 'CJZA567890', 'WVWZZZ6RZLY123456', 'PETROL', 'MANUAL', 5, 6, 32000, 'ACTIVE'),
('B 2345 LS', 'Nissan', 'NP200', 2021, 'White', 'K9K234567', 'VSKBWN1DNU1234567', 'PETROL', 'MANUAL', 2, 8, 52000, 'ACTIVE');

-- Insert Service Records
INSERT INTO service_records (vehicle_id, staff_id, service_date, service_type, service_category, description, parts_replaced, cost, odometer_reading, labor_hours, payment_status) VALUES
(1, 5, '2024-01-15', 'Oil Change', 'MAINTENANCE', 'Regular oil and filter change', 'Oil filter, Engine oil', 850.00, 15000, 1.5, 'PAID'),
(1, 5, '2024-06-20', 'Major Service', 'MAINTENANCE', 'Full service including brake pads', 'Brake pads, Air filter, Oil', 2850.00, 32000, 3.0, 'PAID'),
(2, 9, '2024-02-10', 'Tire Rotation', 'MAINTENANCE', 'Tire rotation and balancing', NULL, 450.00, 12000, 1.0, 'PAID'),
(3, 5, '2024-03-05', 'Engine Diagnostic', 'REPAIR', 'Check engine light diagnostic', 'Spark plugs', 1250.00, 8000, 2.0, 'PAID'),
(4, 9, '2024-04-12', 'Brake Service', 'REPAIR', 'Front brake pad replacement', 'Front brake pads', 1850.00, 45000, 2.5, 'PENDING'),
(5, 5, '2024-05-18', 'Air Conditioning', 'REPAIR', 'AC gas refill and leak check', 'AC gas', 950.00, 25000, 1.5, 'PAID');

-- Insert Insurance Policies
INSERT INTO insurance_policies (vehicle_id, agent_id, policy_number, insurance_company, policy_type, start_date, end_date, premium_amount, coverage_amount, excess_amount, status) VALUES
(1, 4, 'POL00123456', 'Lesotho Insurance Company', 'COMPREHENSIVE', '2024-01-01', '2024-12-31', 4200.00, 350000.00, 2000.00, 'ACTIVE'),
(2, 10, 'POL00789012', 'Metropolitan Lesotho', 'COMPREHENSIVE', '2024-02-01', '2025-01-31', 2800.00, 180000.00, 1500.00, 'ACTIVE'),
(3, 4, 'POL00345678', 'Lesotho Insurance Company', 'THIRD_PARTY', '2024-03-01', '2024-08-31', 1800.00, NULL, 1000.00, 'ACTIVE'),
(4, 10, 'POL00901234', 'Metropolitan Lesotho', 'COMPREHENSIVE', '2024-01-15', '2024-07-14', 3500.00, 250000.00, 2000.00, 'ACTIVE');

-- Insert Violations
INSERT INTO violations (vehicle_id, officer_id, violation_code, violation_date, violation_type, violation_location, fine_amount, points, status) VALUES
(1, 2, 'SPEED01', '2024-02-20 14:30:00', 'Speeding (80 in 60 zone)', 'Main North 1 Road, Maseru', 500.00, 2, 'UNPAID'),
(4, 2, 'PARK01', '2024-03-10 09:15:00', 'Illegal Parking', 'Kingsway, Maseru', 250.00, 0, 'PAID'),
(2, 7, 'SEAT01', '2024-04-05 16:45:00', 'Seatbelt Violation', 'Airport Road, Maseru', 300.00, 1, 'UNPAID');

-- Insert Police Reports
INSERT INTO police_reports (vehicle_id, officer_id, report_number, report_type, description, accident_location, accident_severity, case_status) VALUES
(1, 2, 'RPT20240001', 'ACCIDENT', 'Minor fender bender at traffic light', 'Kingsway and Parliament Road', 'MINOR', 'CLOSED'),
(5, 7, 'RPT20240002', 'THEFT', 'Vehicle stolen from parking lot', 'Pioneer Mall Parking', NULL, 'INVESTIGATING'),
(3, 2, 'RPT20240003', 'VANDALISM', 'Broken window and scratched paint', 'Maseru Mall Parking', NULL, 'OPEN');

-- Insert Stolen Vehicle Alert
INSERT INTO stolen_vehicle_alerts (vehicle_id, report_id, last_seen_location, reward_amount) VALUES
(5, (SELECT report_id FROM police_reports WHERE report_number = 'RPT20240002'), 'Near Ha Foso', 5000.00);

-- Insert Customer Queries
INSERT INTO customer_queries (customer_id, vehicle_id, query_number, subject, query_text, query_type, priority) VALUES
(3, 1, 'QRY20240001', 'Service History Request', 'I need my complete service history for warranty purposes', 'SERVICE', 'HIGH'),
(6, 2, 'QRY20240002', 'Insurance Claim Status', 'When will my claim be processed?', 'INSURANCE', 'NORMAL'),
(8, 3, 'QRY20240003', 'Report Stolen Vehicle', 'My vehicle was stolen, please assist', 'POLICE', 'URGENT');

-- Confirm setup
SELECT 'Database setup complete!' AS status;
SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS total_vehicles FROM vehicles;

-- =====================================================
-- VEHICLE IDENTIFICATION SYSTEM - COMPLETE DATABASE
-- =====================================================

-- Drop existing database if exists
DROP DATABASE IF EXISTS vehicle_identification_system;

-- Create new database
CREATE DATABASE vehicle_identification_system
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8';

-- Connect to database
\c vehicle_identification_system;

-- =====================================================
-- ENUM TYPES
-- =====================================================
CREATE TYPE user_role AS ENUM ('ADMIN', 'CUSTOMER', 'POLICE', 'INSURANCE', 'WORKSHOP');
CREATE TYPE claim_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'SETTLED');
CREATE TYPE violation_status AS ENUM ('PAID', 'UNPAID', 'DISPUTED');
CREATE TYPE report_status AS ENUM ('OPEN', 'INVESTIGATING', 'CLOSED', 'RESOLVED');
CREATE TYPE vehicle_status AS ENUM ('ACTIVE', 'STOLEN', 'IMPOUNDED', 'SCRAPPED', 'SOLD');

-- =====================================================
-- USERS TABLE (Base Table)
-- =====================================================
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT,
    role user_role NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- =====================================================
-- ROLE-SPECIFIC TABLES
-- =====================================================

CREATE TABLE customers (
    customer_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    id_number VARCHAR(20) UNIQUE,
    driver_license VARCHAR(20) UNIQUE,
    date_of_birth DATE
);

CREATE TABLE police_officers (
    officer_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    badge_number VARCHAR(20) UNIQUE NOT NULL,
    rank VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    station_name VARCHAR(100)
);

CREATE TABLE insurance_agents (
    agent_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    agent_code VARCHAR(20) UNIQUE NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) UNIQUE NOT NULL,
    commission_rate DECIMAL(5,2) DEFAULT 0
);

CREATE TABLE workshop_staff (
    staff_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    staff_code VARCHAR(20) UNIQUE NOT NULL,
    position VARCHAR(50) NOT NULL,
    specialization VARCHAR(100),
    hourly_rate DECIMAL(10,2)
);

-- =====================================================
-- VEHICLES TABLE
-- =====================================================
CREATE TABLE vehicles (
    vehicle_id SERIAL PRIMARY KEY,
    registration_number VARCHAR(20) UNIQUE NOT NULL,
    make VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL,
    color VARCHAR(30),
    engine_number VARCHAR(50) UNIQUE,
    chassis_number VARCHAR(50) UNIQUE,
    fuel_type VARCHAR(20),
    transmission VARCHAR(20),
    mileage INTEGER DEFAULT 0,
    owner_id INTEGER REFERENCES customers(customer_id),
    status vehicle_status DEFAULT 'ACTIVE',
    registration_date DATE DEFAULT CURRENT_DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- SERVICE RECORDS
-- =====================================================
CREATE TABLE service_records (
    service_id SERIAL PRIMARY KEY,
    vehicle_id INTEGER REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    staff_id INTEGER REFERENCES workshop_staff(staff_id),
    service_date DATE NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    description TEXT,
    cost DECIMAL(10,2) NOT NULL,
    odometer_reading INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- INSURANCE POLICIES
-- =====================================================
CREATE TABLE insurance_policies (
    policy_id SERIAL PRIMARY KEY,
    vehicle_id INTEGER REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    agent_id INTEGER REFERENCES insurance_agents(agent_id),
    policy_number VARCHAR(50) UNIQUE NOT NULL,
    insurance_company VARCHAR(100) NOT NULL,
    policy_type VARCHAR(50),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    premium_amount DECIMAL(10,2) NOT NULL,
    coverage_amount DECIMAL(12,2),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- POLICE REPORTS
-- =====================================================
CREATE TABLE police_reports (
    report_id SERIAL PRIMARY KEY,
    vehicle_id INTEGER REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    officer_id INTEGER REFERENCES police_officers(officer_id),
    report_number VARCHAR(50) UNIQUE NOT NULL,
    report_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    report_type VARCHAR(50),
    description TEXT NOT NULL,
    accident_location TEXT,
    case_status report_status DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- VIOLATIONS
-- =====================================================
CREATE TABLE violations (
    violation_id SERIAL PRIMARY KEY,
    vehicle_id INTEGER REFERENCES vehicles(vehicle_id) ON DELETE CASCADE,
    officer_id INTEGER REFERENCES police_officers(officer_id),
    violation_code VARCHAR(20) NOT NULL,
    violation_date TIMESTAMP NOT NULL,
    violation_type VARCHAR(100) NOT NULL,
    violation_location TEXT,
    fine_amount DECIMAL(10,2) NOT NULL,
    points INTEGER DEFAULT 0,
    status violation_status DEFAULT 'UNPAID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- CUSTOMER QUERIES
-- =====================================================
CREATE TABLE customer_queries (
    query_id SERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(customer_id) ON DELETE CASCADE,
    vehicle_id INTEGER REFERENCES vehicles(vehicle_id),
    query_number VARCHAR(50) UNIQUE NOT NULL,
    query_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subject VARCHAR(200) NOT NULL,
    query_text TEXT NOT NULL,
    response_text TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- VIEWS
-- =====================================================

CREATE VIEW vw_vehicle_details AS
SELECT
    v.vehicle_id,
    v.registration_number,
    v.make,
    v.model,
    v.year,
    v.color,
    v.status,
    u.full_name AS owner_name,
    u.email AS owner_email,
    u.phone AS owner_phone
FROM vehicles v
LEFT JOIN customers c ON v.owner_id = c.customer_id
LEFT JOIN users u ON c.customer_id = u.user_id;

CREATE VIEW vw_service_history AS
SELECT
    s.service_id,
    v.registration_number,
    s.service_date,
    s.service_type,
    s.description,
    s.cost,
    s.odometer_reading,
    u.full_name AS serviced_by
FROM service_records s
JOIN vehicles v ON s.vehicle_id = v.vehicle_id
LEFT JOIN workshop_staff ws ON s.staff_id = ws.staff_id
LEFT JOIN users u ON ws.staff_id = u.user_id;

CREATE VIEW vw_active_policies AS
SELECT
    p.policy_id,
    p.policy_number,
    v.registration_number,
    p.insurance_company,
    p.start_date,
    p.end_date,
    p.premium_amount,
    u.full_name AS agent_name
FROM insurance_policies p
JOIN vehicles v ON p.vehicle_id = v.vehicle_id
LEFT JOIN insurance_agents ia ON p.agent_id = ia.agent_id
LEFT JOIN users u ON ia.agent_id = u.user_id
WHERE p.status = 'ACTIVE';

-- =====================================================
-- SAMPLE DATA (SESOTHO NAMES)
-- =====================================================

-- Password is 'password' for all users (BCrypt hash: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs)
INSERT INTO users (username, password_hash, full_name, email, phone, address, role) VALUES
('moeti_leleka', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Moeti Leleka', 'moeti.leleka@vis.gov.ls', '+266 5888 1234', 'Maseru West, Maseru 100', 'ADMIN'),
('pofane_matlali', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Pofane Matlali', 'pofane.matlali@police.gov.ls', '+266 5888 2345', 'Police HQ, Maseru', 'POLICE'),
('retshedisitsoe_moeketsi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Retshedisitsoe Moeketsi', 'retshedisitsoe@email.com', '+266 5888 3456', 'Mazenod, Maseru', 'CUSTOMER'),
('lethako_khabele', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Lethako Khabele', 'lethako.khabele@insurance.co.ls', '+266 5888 4567', 'Khubetsoana, Maseru', 'INSURANCE'),
('reneilwe_moeketsi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Reneilwe Moeketsi', 'reneilwe@workshop.co.ls', '+266 5888 5678', 'Thetsane Industrial, Maseru', 'WORKSHOP');

-- Insert Customers
INSERT INTO customers (customer_id, id_number, driver_license, date_of_birth) VALUES
(3, 'LS123456789', 'DL123456', '1985-03-15');

-- Insert Police Officers
INSERT INTO police_officers (officer_id, badge_number, rank, department, station_name) VALUES
(2, 'BP12345', 'Inspector', 'Traffic Division', 'Maseru Central');

-- Insert Insurance Agents
INSERT INTO insurance_agents (agent_id, agent_code, company_name, license_number, commission_rate) VALUES
(4, 'AG001', 'Lesotho Insurance Company', 'LIC123456', 15.00);

-- Insert Workshop Staff
INSERT INTO workshop_staff (staff_id, staff_code, position, specialization, hourly_rate) VALUES
(5, 'WS001', 'Senior Mechanic', 'Engine & Transmission', 150.00);

-- Insert Vehicles
INSERT INTO vehicles (registration_number, make, model, year, color, engine_number, chassis_number, fuel_type, transmission, owner_id, mileage) VALUES
('B 1234 LS', 'Toyota', 'Hilux', 2022, 'White', '2GD123456', 'MR0FZ29G001234567', 'DIESEL', 'MANUAL', 3, 45000),
('B 5678 LS', 'Hyundai', 'Grand i10', 2021, 'Silver', 'G4LC789012', 'KMHBT51GLMU123456', 'PETROL', 'MANUAL', 3, 28000);

-- Insert Service Records
INSERT INTO service_records (vehicle_id, staff_id, service_date, service_type, description, cost, odometer_reading) VALUES
(1, 5, '2024-01-15', 'Oil Change', 'Regular oil and filter change', 850.00, 15000),
(1, 5, '2024-06-20', 'Major Service', 'Full service including brake pads', 2850.00, 32000);

-- Insert Insurance Policies
INSERT INTO insurance_policies (vehicle_id, agent_id, policy_number, insurance_company, policy_type, start_date, end_date, premium_amount, coverage_amount) VALUES
(1, 4, 'POL00123456', 'Lesotho Insurance Company', 'COMPREHENSIVE', '2024-01-01', '2024-12-31', 4200.00, 350000.00);

-- Insert Police Reports
INSERT INTO police_reports (vehicle_id, officer_id, report_number, report_type, description, accident_location, case_status) VALUES
(1, 2, 'RPT20240001', 'ACCIDENT', 'Minor fender bender at traffic light', 'Kingsway and Parliament Road', 'CLOSED');

-- Insert Violations
INSERT INTO violations (vehicle_id, officer_id, violation_code, violation_date, violation_type, violation_location, fine_amount, points, status) VALUES
(1, 2, 'SPEED01', '2024-02-20 14:30:00', 'Speeding (80 in 60 zone)', 'Main North 1 Road, Maseru', 500.00, 2, 'UNPAID');

SELECT 'Database setup complete!' AS status;