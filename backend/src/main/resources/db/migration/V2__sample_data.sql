-- Sample data for DeliverXY platform (H2 compatible)
-- This script populates the database with test data

-- Insert sample users (separate INSERT statements for H2 compatibility)
INSERT INTO app_user (username, email, password, first_name, last_name, phone_number, role, kyc_status, is_verified, is_active, id_front_url, id_back_url, selfie_url, proof_of_address_url) VALUES ('admin', 'admin@deliverxy.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin', 'User', '+1234567890', 'ADMIN', 'VERIFIED', true, true, NULL, NULL, NULL, NULL);
INSERT INTO app_user (username, email, password, first_name, last_name, phone_number, role, kyc_status, is_verified, is_active, id_front_url, id_back_url, selfie_url, proof_of_address_url) VALUES ('john_doe', 'john@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John', 'Doe', '+1234567891', 'CLIENT', 'VERIFIED', true, true, 'john_id_front.jpg', 'john_id_back.jpg', 'john_selfie.jpg', 'john_address.jpg');
INSERT INTO app_user (username, email, password, first_name, last_name, phone_number, role, kyc_status, is_verified, is_active, id_front_url, id_back_url, selfie_url, proof_of_address_url) VALUES ('jane_smith', 'jane@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Jane', 'Smith', '+1234567892', 'AGENT', 'VERIFIED', true, true, 'jane_id_front.jpg', 'jane_id_back.jpg', 'jane_selfie.jpg', 'jane_address.jpg');
INSERT INTO app_user (username, email, password, first_name, last_name, phone_number, role, kyc_status, is_verified, is_active, id_front_url, id_back_url, selfie_url, proof_of_address_url) VALUES ('bob_wilson', 'bob@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Bob', 'Wilson', '+1234567893', 'CLIENT', 'PENDING', false, true, NULL, NULL, NULL, NULL);
INSERT INTO app_user (username, email, password, first_name, last_name, phone_number, role, kyc_status, is_verified, is_active, id_front_url, id_back_url, selfie_url, proof_of_address_url) VALUES ('alice_brown', 'alice@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Alice', 'Brown', '+1234567894', 'AGENT', 'SUBMITTED', false, true, 'alice_id_front.jpg', 'alice_id_back.jpg', 'alice_selfie.jpg', 'alice_address.jpg');

-- Insert sample wallets (separate INSERT statements for H2 compatibility)
INSERT INTO wallets (user_id, balance, currency) VALUES (1, 1000.00, 'USD');
INSERT INTO wallets (user_id, balance, currency) VALUES (2, 500.00, 'USD');
INSERT INTO wallets (user_id, balance, currency) VALUES (3, 750.00, 'USD');
INSERT INTO wallets (user_id, balance, currency) VALUES (4, 200.00, 'USD');
INSERT INTO wallets (user_id, balance, currency) VALUES (5, 300.00, 'USD');

-- Insert sample vehicles for agents
INSERT INTO vehicles (owner_id, vehicle_type, make, model, vehicle_year, license_plate, color, passenger_capacity, cargo_capacity_kg, cargo_volume_cubic_meters, insurance_provider, insurance_policy_number, insurance_expiry_date, registration_expiry_date, vehicle_condition) VALUES (3, 'CAR', 'Toyota', 'Camry', 2020, 'ABC123', 'Silver', 5, 200.0, 0.5, 'State Farm', 'SF123456', '2025-12-31 23:59:59', '2025-06-30 23:59:59', 'GOOD');
INSERT INTO vehicles (owner_id, vehicle_type, make, model, vehicle_year, license_plate, color, passenger_capacity, cargo_capacity_kg, cargo_volume_cubic_meters, insurance_provider, insurance_policy_number, insurance_expiry_date, registration_expiry_date, vehicle_condition) VALUES (5, 'VAN', 'Ford', 'Transit', 2019, 'XYZ789', 'White', 2, 1000.0, 3.0, 'Allstate', 'AS789012', '2025-11-30 23:59:59', '2025-05-31 23:59:59', 'EXCELLENT');


-- Insert sample deliveries (separate INSERT statements for H2 compatibility)
INSERT INTO deliveries (title, description, package_type, package_weight, pickup_address, dropoff_address, base_price, urgent_fee, fragile_fee, total_price, payment_status, status, client_id, agent_id) VALUES ('Documents Delivery', 'Important business documents', 'DOCUMENTS', 0.5, '123 Main St, City Center', '456 Business Ave, Downtown', 25.00, 0.00, 0.00, 25.00, 'PENDING', 'PENDING', 2, NULL);
INSERT INTO deliveries (title, description, package_type, package_weight, pickup_address, dropoff_address, base_price, urgent_fee, fragile_fee, total_price, payment_status, status, client_id, agent_id) VALUES ('Electronics Package', 'Fragile electronics item', 'ELECTRONICS', 2.0, '789 Tech Blvd, Tech Park', '321 Innovation Dr, Innovation District', 45.00, 0.00, 10.00, 55.00, 'PAID', 'ASSIGNED', 2, 3);
INSERT INTO deliveries (title, description, package_type, package_weight, pickup_address, dropoff_address, base_price, urgent_fee, fragile_fee, total_price, payment_status, status, client_id, agent_id) VALUES ('Food Delivery', 'Fresh groceries and food items', 'FOOD', 5.0, '555 Market St, Central Market', '777 Home St, Residential Area', 35.00, 5.00, 0.00, 40.00, 'PAID', 'IN_TRANSIT', 4, 3);
INSERT INTO deliveries (title, description, package_type, package_weight, pickup_address, dropoff_address, base_price, urgent_fee, fragile_fee, total_price, payment_status, status, client_id, agent_id) VALUES ('Clothing Package', 'Online shopping delivery', 'CLOTHING', 1.5, '888 Mall Rd, Shopping Center', '999 Fashion St, Fashion District', 30.00, 0.00, 0.00, 30.00, 'PAID', 'DELIVERED', 4, 5);

-- Insert sample payments (separate INSERT statements for H2 compatibility)
INSERT INTO payments (payment_id, delivery_id, payer_id, recipient_id, amount, currency, payment_type, status, description) VALUES ('PAY_001', 1, 2, NULL, 25.00, 'USD', 'DELIVERY_PAYMENT', 'PENDING', 'Payment for Documents Delivery');
INSERT INTO payments (payment_id, delivery_id, payer_id, recipient_id, amount, currency, payment_type, status, description) VALUES ('PAY_002', 2, 2, 3, 45.00, 'USD', 'DELIVERY_PAYMENT', 'COMPLETED', 'Payment for Electronics Package');
INSERT INTO payments (payment_id, delivery_id, payer_id, recipient_id, amount, currency, payment_type, status, description) VALUES ('PAY_003', 3, 4, 3, 35.00, 'USD', 'DELIVERY_PAYMENT', 'COMPLETED', 'Payment for Food Delivery');
INSERT INTO payments (payment_id, delivery_id, payer_id, recipient_id, amount, currency, payment_type, status, description) VALUES ('PAY_004', 4, 4, 5, 30.00, 'USD', 'DELIVERY_PAYMENT', 'COMPLETED', 'Payment for Clothing Package');

-- Insert sample ratings (separate INSERT statements for H2 compatibility)
INSERT INTO ratings (delivery_id, reviewer_id, reviewed_id, rating, review, rating_type) VALUES (2, 2, 3, 5, 'Excellent service, very professional!', 'CLIENT_TO_AGENT');
INSERT INTO ratings (delivery_id, reviewer_id, reviewed_id, rating, review, rating_type) VALUES (3, 4, 3, 4, 'Good delivery, on time', 'CLIENT_TO_AGENT');
INSERT INTO ratings (delivery_id, reviewer_id, reviewed_id, rating, review, rating_type) VALUES (4, 4, 5, 5, 'Perfect delivery experience', 'CLIENT_TO_AGENT');
INSERT INTO ratings (delivery_id, reviewer_id, reviewed_id, rating, review, rating_type) VALUES (2, 3, 2, 5, 'Great client, clear instructions', 'AGENT_TO_CLIENT');
INSERT INTO ratings (delivery_id, reviewer_id, reviewed_id, rating, review, rating_type) VALUES (3, 3, 4, 4, 'Good communication', 'AGENT_TO_CLIENT');
INSERT INTO ratings (delivery_id, reviewer_id, reviewed_id, rating, review, rating_type) VALUES (4, 5, 4, 5, 'Excellent client experience', 'AGENT_TO_CLIENT');

-- Insert sample chat messages (separate INSERT statements for H2 compatibility)
INSERT INTO chat_messages (delivery_id, sender_id, message, message_type) VALUES (1, 2, 'Hi, when can you pick up the documents?', 'TEXT');
INSERT INTO chat_messages (delivery_id, sender_id, message, message_type) VALUES (1, 1, 'I can pick up in 30 minutes', 'TEXT');
INSERT INTO chat_messages (delivery_id, sender_id, message, message_type) VALUES (2, 2, 'Please handle with care, it''s fragile', 'TEXT');
INSERT INTO chat_messages (delivery_id, sender_id, message, message_type) VALUES (2, 3, 'Understood, I''ll be extra careful', 'TEXT');
INSERT INTO chat_messages (delivery_id, sender_id, message, message_type) VALUES (3, 4, 'ETA for delivery?', 'TEXT');
INSERT INTO chat_messages (delivery_id, sender_id, message, message_type) VALUES (3, 3, 'About 20 minutes', 'TEXT');

-- Update agent availability and delivery statuses
UPDATE app_user SET is_available = true, current_latitude = 40.7128, current_longitude = -74.0060 WHERE id = 3;
UPDATE app_user SET is_available = true, current_latitude = 40.7589, current_longitude = -73.9851 WHERE id = 5;

-- Update delivery statuses
UPDATE deliveries SET assigned_at = CURRENT_TIMESTAMP WHERE id = 2;
UPDATE deliveries SET assigned_at = CURRENT_TIMESTAMP, actual_pickup_time = CURRENT_TIMESTAMP WHERE id = 3;
UPDATE deliveries SET assigned_at = CURRENT_TIMESTAMP, actual_pickup_time = CURRENT_TIMESTAMP, actual_delivery_time = CURRENT_TIMESTAMP WHERE id = 4; 