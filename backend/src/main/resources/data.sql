-- DeliverXY Sample Data
-- This script populates the database with sample data for development and testing

-- Insert sample users
INSERT INTO app_user (username, first_name, last_name, email, password, phone_number, role, is_verified) VALUES
('admin', 'Admin', 'User', 'admin@deliverxy.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+1234567890', 'ADMIN', true),
('john_client', 'John', 'Doe', 'john@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+1234567891', 'CLIENT', true),
('jane_agent', 'Jane', 'Smith', 'jane@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+1234567892', 'AGENT', true),
('mike_client', 'Mike', 'Johnson', 'mike@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+1234567893', 'CLIENT', true),
('sarah_agent', 'Sarah', 'Wilson', 'sarah@example.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '+1234567894', 'AGENT', false);

-- Insert sample wallets
INSERT INTO wallets (user_id, balance, currency) VALUES
(2, 500.00, 'USD'),  -- John's wallet
(3, 0.00, 'USD'),    -- Jane's wallet
(4, 200.00, 'USD'),  -- Mike's wallet
(5, 0.00, 'USD');    -- Sarah's wallet

-- Insert sample deliveries
INSERT INTO deliveries (title, description, pickup_address, dropoff_address, base_price, client_id, status, tracking_code) VALUES
('Package Delivery', 'Small package from downtown', '123 Main St, Downtown', '456 Oak Ave, Uptown', 25.00, 2, 'PENDING', 'DX1234567890'),
('Food Delivery', 'Lunch order from Italian restaurant', '789 Pizza St, Food District', '321 Home Blvd, Residential Area', 15.00, 2, 'ASSIGNED', 'DX1234567891'),
('Document Delivery', 'Important documents', '555 Business Center, Downtown', '777 Office Park, Business District', 30.00, 4, 'IN_TRANSIT', 'DX1234567892'),
('Grocery Delivery', 'Weekly groceries', '999 Supermarket, Shopping Center', '888 Home Street, Residential Area', 20.00, 4, 'PENDING', 'DX1234567893');

-- Assign some deliveries to agents
UPDATE deliveries SET agent_id = 3, assigned_at = CURRENT_TIMESTAMP WHERE id = 2;
UPDATE deliveries SET agent_id = 3, assigned_at = CURRENT_TIMESTAMP, status = 'IN_TRANSIT' WHERE id = 3;

-- Insert sample payments
INSERT INTO payments (payment_id, delivery_id, payer_id, amount, payment_type, status, description) VALUES
('PAY001', 1, 2, 25.00, 'DELIVERY_PAYMENT', 'PENDING', 'Payment for package delivery'),
('PAY002', 2, 2, 15.00, 'DELIVERY_PAYMENT', 'COMPLETED', 'Payment for food delivery'),
('PAY003', 3, 4, 30.00, 'DELIVERY_PAYMENT', 'COMPLETED', 'Payment for document delivery'),
('PAY004', 4, 4, 20.00, 'DELIVERY_PAYMENT', 'PENDING', 'Payment for grocery delivery');

-- Insert sample ratings
INSERT INTO ratings (delivery_id, reviewer_id, reviewed_id, rating, review, rating_type) VALUES
(2, 2, 3, 5, 'Excellent service, very fast delivery!', 'CLIENT_TO_AGENT'),
(2, 3, 2, 5, 'Great customer, clear instructions', 'AGENT_TO_CLIENT'),
(3, 4, 3, 4, 'Good service, on time delivery', 'CLIENT_TO_AGENT');

-- Insert sample chat messages
INSERT INTO chat_messages (delivery_id, sender_id, message, message_type) VALUES
(2, 2, 'Hi, I need this delivered by 2 PM', 'TEXT'),
(2, 3, 'No problem, I can deliver it by 1:30 PM', 'TEXT'),
(2, 2, 'Perfect, thank you!', 'TEXT'),
(3, 4, 'Please handle with care, these are important documents', 'TEXT'),
(3, 3, 'I will take extra care with your documents', 'TEXT');

-- Update user ratings based on received ratings
UPDATE app_user SET rating = 5.0, total_deliveries = 2 WHERE id = 3;  -- Jane
UPDATE app_user SET rating = 4.5, total_deliveries = 1 WHERE id = 5;  -- Sarah

-- Update delivery payment statuses
UPDATE deliveries SET payment_status = 'PAID', paid_at = CURRENT_TIMESTAMP WHERE id IN (2, 3);
UPDATE deliveries SET payment_status = 'PENDING' WHERE id IN (1, 4);

-- Set some users as available agents
UPDATE app_user SET is_available = true, current_latitude = 40.7128, current_longitude = -74.0060 WHERE id = 3;  -- Jane
UPDATE app_user SET is_available = true, current_latitude = 40.7589, current_longitude = -73.9851 WHERE id = 5;  -- Sarah 