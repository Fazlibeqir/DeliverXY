-- Disable referential integrity checks for initial bulk inserts (if needed for complex ordering)
SET REFERENTIAL_INTEGRITY FALSE;

-- ----------------------------------------
-- 1. APP_USER (Admin, Client, Agent)
-- Password is 'password' (must be BCrypt encoded in a real scenario)
-- NOTE: Replace 'ENCODED_PASSWORD' with a BCrypt hash of 'password'
-- Example Hash (DO NOT USE IN PRODUCTION): $2a$10$v0vM3H/D5J7E7T3L8W7S.yK4U4X5Z6T8I1J2K3L4M5N6O7P8Q9R0
-- I will use a placeholder:
-- ----------------------------------------

INSERT INTO app_user (id, username, first_name, last_name, email, password, phone_number, role, is_active, is_verified, created_at, updated_at) VALUES
                                                                                                                                                    (1, 'admin_user', 'Super', 'Admin', 'admin@deliverxy.com', '$2a$10$v0vM3H/D5J7E7T3L8W7S.yK4U4X5Z6T8I1J2K3L4M5N6O7P8Q9R0', '0000000000', 'ADMIN', TRUE, TRUE, NOW(), NOW()),
                                                                                                                                                    (2, 'client1', 'Alice', 'Client', 'client1@test.com', '$2a$10$v0vM3H/D5J7E7T3L8W7S.yK4U4X5Z6T8I1J2K3L4M5N6O7P8Q9R0', '555111222', 'CLIENT', TRUE, TRUE, NOW(), NOW()),
                                                                                                                                                    (3, 'driver1', 'Bob', 'Driver', 'driver1@test.com', '$2a$10$v0vM3H/D5J7E7T3L8W7S.yK4U4X5Z6T8I1J2K3L4M5N6O7P8Q9R0', '555333444', 'AGENT', TRUE, TRUE, NOW(), NOW());

-- ----------------------------------------
-- 2. PRICING_CONFIGS
-- Essential for fare estimation (Step 3.1)
-- ----------------------------------------

INSERT INTO pricing_configs (id, name, city, currency, base_fare, per_km_rate, per_minute_rate, minimum_fare, surge_multiplier, city_center_multiplier, airport_surcharge, night_multiplier, weekend_multiplier, peak_hour_multiplier, is_active, description) VALUES
    (1, 'Standard Skopje', 'Skopje', 'MKD', 50.0, 30.0, 2.0, 80.0, 1.0, 1.1, 100.0, 1.25, 1.15, 1.3, TRUE, 'Standard delivery rates for Skopje');

-- ----------------------------------------
-- 3. DRIVER_SPECIFIC ENTITIES (Agent User ID: 3)
-- ----------------------------------------

-- Vehicle for Driver1 (ID: 3)
INSERT INTO vehicles (id, owner_id, vehicle_type, make, model, license_plate, is_available, created_at, updated_at) VALUES
    (1, 3, 'MOTORCYCLE', 'Yamaha', 'YZF-R3', 'SK-0001-A', TRUE, NOW(), NOW());

-- Agent Profile for Driver1 (ID: 3)
INSERT INTO app_user_agent_profile (user_id, is_available) VALUES
    (3, TRUE); -- Driver is available to receive requests

-- ----------------------------------------
-- 4. WALLETS (Client ID: 2)
-- Client must have a wallet for future payment steps
-- ----------------------------------------

-- Wallet for Client1 (ID: 2)
INSERT INTO wallets (id, user_id, balance, currency, is_active, created_at, updated_at) VALUES
    (100, 2, 150.00, 'MKD', TRUE, NOW(), NOW());

-- Wallet for Driver1 (ID: 3) - Starts empty
INSERT INTO wallets (id, user_id, balance, currency, is_active, created_at, updated_at) VALUES
    (101, 3, 0.00, 'MKD', TRUE, NOW(), NOW());

-- ----------------------------------------
-- 5. KYC (for Driver)
-- Assuming a test driver is pre-approved
-- ----------------------------------------

INSERT INTO app_user_kyc (user_id, kyc_status, submitted_at, verified_at) VALUES
    (3, 'APPROVED', NOW(), NOW());

-- ----------------------------------------
SET REFERENTIAL_INTEGRITY TRUE;