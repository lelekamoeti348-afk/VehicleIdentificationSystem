-- =====================================================
-- VEHICLE IDENTIFICATION SYSTEM - WINDOWS COMPATIBLE
-- =====================================================

-- Connect to the database
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
CREATE TABLE IF NOT EXISTS users (
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

CREATE TABLE IF NOT EXISTS customers (
    customer_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    id_number VARCHAR(20) UNIQUE,
    driver_license VARCHAR(20) UNIQUE,
    date_of_birth DATE
);

CREATE TABLE IF NOT EXISTS police_officers (
    officer_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    badge_number VARCHAR(20) UNIQUE NOT NULL,
    rank VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    station_name VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS insurance_agents (
    agent_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    agent_code VARCHAR(20) UNIQUE NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) UNIQUE NOT NULL,
    commission_rate DECIMAL(5,2) DEFAULT 0
);

CREATE TABLE IF NOT EXISTS workshop_staff (
    staff_id INTEGER PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    staff_code VARCHAR(20) UNIQUE NOT NULL,
    position VARCHAR(50) NOT NULL,
    specialization VARCHAR(100),
    hourly_rate DECIMAL(10,2)
);

-- =====================================================
-- VEHICLES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS vehicles (
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
CREATE TABLE IF NOT EXISTS service_records (
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
CREATE TABLE IF NOT EXISTS insurance_policies (
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
CREATE TABLE IF NOT EXISTS police_reports (
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
CREATE TABLE IF NOT EXISTS violations (
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
CREATE TABLE IF NOT EXISTS customer_queries (
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

CREATE OR REPLACE VIEW vw_vehicle_details AS
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

CREATE OR REPLACE VIEW vw_service_history AS
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

CREATE OR REPLACE VIEW vw_active_policies AS
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

-- Password is 'password' for all users
INSERT INTO users (user_id, username, password_hash, full_name, email, phone, address, role) VALUES
(1, 'moeti_leleka', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Moeti Leleka', 'moeti.leleka@vis.gov.ls', '+266 5888 1234', 'Maseru West, Maseru 100', 'ADMIN'),
(2, 'pofane_matlali', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Pofane Matlali', 'pofane.matlali@police.gov.ls', '+266 5888 2345', 'Police HQ, Maseru', 'POLICE'),
(3, 'retshedisitsoe_moeketsi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Retshedisitsoe Moeketsi', 'retshedisitsoe@email.com', '+266 5888 3456', 'Mazenod, Maseru', 'CUSTOMER'),
(4, 'lethako_khabele', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Lethako Khabele', 'lethako.khabele@insurance.co.ls', '+266 5888 4567', 'Khubetsoana, Maseru', 'INSURANCE'),
(5, 'reneilwe_moeketsi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs', 'Reneilwe Moeketsi', 'reneilwe@workshop.co.ls', '+266 5888 5678', 'Thetsane Industrial, Maseru', 'WORKSHOP')
ON CONFLICT (user_id) DO NOTHING;

-- Reset sequence
SELECT setval('users_user_id_seq', (SELECT COALESCE(MAX(user_id), 5) FROM users));

-- Insert Customers
INSERT INTO customers (customer_id, id_number, driver_license, date_of_birth) VALUES
(3, 'LS123456789', 'DL123456', '1985-03-15')
ON CONFLICT (customer_id) DO NOTHING;

-- Insert Police Officers
INSERT INTO police_officers (officer_id, badge_number, rank, department, station_name) VALUES
(2, 'BP12345', 'Inspector', 'Traffic Division', 'Maseru Central')
ON CONFLICT (officer_id) DO NOTHING;

-- Insert Insurance Agents
INSERT INTO insurance_agents (agent_id, agent_code, company_name, license_number, commission_rate) VALUES
(4, 'AG001', 'Lesotho Insurance Company', 'LIC123456', 15.00)
ON CONFLICT (agent_id) DO NOTHING;

-- Insert Workshop Staff
INSERT INTO workshop_staff (staff_id, staff_code, position, specialization, hourly_rate) VALUES
(5, 'WS001', 'Senior Mechanic', 'Engine & Transmission', 150.00)
ON CONFLICT (staff_id) DO NOTHING;

-- Insert Vehicles
INSERT INTO vehicles (registration_number, make, model, year, color, engine_number, chassis_number, fuel_type, transmission, owner_id, mileage) VALUES
('B 1234 LS', 'Toyota', 'Hilux', 2022, 'White', '2GD123456', 'MR0FZ29G001234567', 'DIESEL', 'MANUAL', 3, 45000),
('B 5678 LS', 'Hyundai', 'Grand i10', 2021, 'Silver', 'G4LC789012', 'KMHBT51GLMU123456', 'PETROL', 'MANUAL', 3, 28000)
ON CONFLICT (registration_number) DO NOTHING;

-- Insert Service Records
INSERT INTO service_records (vehicle_id, staff_id, service_date, service_type, description, cost, odometer_reading) VALUES
(1, 5, '2024-01-15', 'Oil Change', 'Regular oil and filter change', 850.00, 15000),
(1, 5, '2024-06-20', 'Major Service', 'Full service including brake pads', 2850.00, 32000)
ON CONFLICT DO NOTHING;

-- Insert Insurance Policies
INSERT INTO insurance_policies (vehicle_id, agent_id, policy_number, insurance_company, policy_type, start_date, end_date, premium_amount, coverage_amount) VALUES
(1, 4, 'POL00123456', 'Lesotho Insurance Company', 'COMPREHENSIVE', '2024-01-01', '2024-12-31', 4200.00, 350000.00)
ON CONFLICT (policy_number) DO NOTHING;

-- Insert Police Reports
INSERT INTO police_reports (vehicle_id, officer_id, report_number, report_type, description, accident_location, case_status) VALUES
(1, 2, 'RPT20240001', 'ACCIDENT', 'Minor fender bender at traffic light', 'Kingsway and Parliament Road', 'CLOSED')
ON CONFLICT (report_number) DO NOTHING;

-- Insert Violations
INSERT INTO violations (vehicle_id, officer_id, violation_code, violation_date, violation_type, violation_location, fine_amount, points, status) VALUES
(1, 2, 'SPEED01', '2024-02-20 14:30:00', 'Speeding (80 in 60 zone)', 'Main North 1 Road, Maseru', 500.00, 2, 'UNPAID')
ON CONFLICT DO NOTHING;

SELECT 'Database setup complete!' AS status;