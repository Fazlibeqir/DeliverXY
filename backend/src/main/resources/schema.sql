-- DeliverXY Database Schema
-- This script creates all necessary tables for the crowdsourced delivery system

-- Enable UUID extension for PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- App Users Table
CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'CLIENT',

    -- KYC Fields
    kyc_status VARCHAR(20) DEFAULT 'PENDING',
    id_front_url VARCHAR(500),
    id_back_url VARCHAR(500),
    selfie_url VARCHAR(500),
    proof_of_address_url VARCHAR(500),
    kyc_submitted_at TIMESTAMP,
    kyc_verified_at TIMESTAMP,
    kyc_rejection_reason TEXT,

    -- Agent Specific Fields
    is_available BOOLEAN DEFAULT FALSE,
    current_latitude DECIMAL(10, 8),
    current_longitude DECIMAL(11, 8),
    last_location_update TIMESTAMP,
    rating DECIMAL(3, 2) DEFAULT 0.0,
    total_deliveries INTEGER DEFAULT 0,
    total_earnings DECIMAL(10, 2) DEFAULT 0.0,

    -- Account Status
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Wallets Table
CREATE TABLE IF NOT EXISTS wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES app_user(id),
    balance DECIMAL(10, 2) DEFAULT 0.0,
    currency VARCHAR(3) DEFAULT 'USD',
    is_active BOOLEAN DEFAULT TRUE,
    daily_limit DECIMAL(10, 2) DEFAULT 1000.00,
    monthly_limit DECIMAL(10, 2) DEFAULT 10000.00,
    daily_spent DECIMAL(10, 2) DEFAULT 0.0,
    monthly_spent DECIMAL(10, 2) DEFAULT 0.0,
    last_reset_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Deliveries Table
CREATE TABLE IF NOT EXISTS deliveries (
    id BIGSERIAL PRIMARY KEY,

    -- Basic Delivery Info
    title VARCHAR(255) NOT NULL,
    description TEXT,
    package_type VARCHAR(50),
    package_weight DECIMAL(8, 3),
    package_dimensions VARCHAR(100),
    is_fragile BOOLEAN DEFAULT FALSE,
    is_urgent BOOLEAN DEFAULT FALSE,

    -- Pickup Information
    pickup_address TEXT NOT NULL,
    pickup_latitude DECIMAL(10, 8),
    pickup_longitude DECIMAL(11, 8),
    pickup_contact_name VARCHAR(100),
    pickup_contact_phone VARCHAR(20),
    pickup_instructions TEXT,

    -- Dropoff Information
    dropoff_address TEXT NOT NULL,
    dropoff_latitude DECIMAL(10, 8),
    dropoff_longitude DECIMAL(11, 8),
    dropoff_contact_name VARCHAR(100),
    dropoff_contact_phone VARCHAR(20),
    dropoff_instructions TEXT,

    -- Timing
    requested_pickup_time TIMESTAMP,
    requested_delivery_time TIMESTAMP,
    actual_pickup_time TIMESTAMP,
    actual_delivery_time TIMESTAMP,
    expires_at TIMESTAMP,

    -- Status and Assignment
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    client_id BIGINT NOT NULL REFERENCES app_user(id),
    agent_id BIGINT REFERENCES app_user(id),
    assigned_at TIMESTAMP,

    -- Payment Information
    base_price DECIMAL(10, 2) NOT NULL,
    urgent_fee DECIMAL(10, 2) DEFAULT 0.0,
    fragile_fee DECIMAL(10, 2) DEFAULT 0.0,
    total_price DECIMAL(10, 2),
    tip_amount DECIMAL(10, 2) DEFAULT 0.0,
    final_amount DECIMAL(10, 2),
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    payment_method VARCHAR(20),
    paid_at TIMESTAMP,

    -- Tracking and Communication
    estimated_distance DECIMAL(8, 2),
    estimated_duration INTEGER,
    current_latitude DECIMAL(10, 8),
    current_longitude DECIMAL(11, 8),
    last_location_update TIMESTAMP,
    tracking_code VARCHAR(50) UNIQUE,

    -- Insurance and Safety
    is_insured BOOLEAN DEFAULT FALSE,
    insurance_amount DECIMAL(10, 2) DEFAULT 0.0,
    insurance_premium DECIMAL(10, 2) DEFAULT 0.0,

    -- Ratings and Reviews
    client_rating INTEGER CHECK (client_rating >= 1 AND client_rating <= 5),
    client_review TEXT,
    agent_rating INTEGER CHECK (agent_rating >= 1 AND agent_rating <= 5),
    agent_review TEXT,
    reviewed_by_client BOOLEAN DEFAULT FALSE,
    reviewed_by_agent BOOLEAN DEFAULT FALSE,

    -- Metadata
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,
    cancelled_by VARCHAR(20)
);

-- Payments Table
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(100) UNIQUE NOT NULL,
    delivery_id BIGINT REFERENCES deliveries(id),
    payer_id BIGINT NOT NULL REFERENCES app_user(id),
    recipient_id BIGINT REFERENCES app_user(id),
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    payment_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(20),
    description TEXT,
    stripe_payment_intent_id VARCHAR(100),
    stripe_charge_id VARCHAR(100),
    failure_reason TEXT,
    refund_amount DECIMAL(10, 2) DEFAULT 0.0,
    refund_reason TEXT,
    refunded_at TIMESTAMP,
    processing_fee DECIMAL(10, 2) DEFAULT 0.0,
    platform_fee DECIMAL(10, 2) DEFAULT 0.0,
    net_amount DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    expires_at TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    metadata TEXT
);

-- Ratings Table
CREATE TABLE IF NOT EXISTS ratings (
    id BIGSERIAL PRIMARY KEY,
    delivery_id BIGINT NOT NULL REFERENCES deliveries(id),
    reviewer_id BIGINT NOT NULL REFERENCES app_user(id),
    reviewed_id BIGINT NOT NULL REFERENCES app_user(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review TEXT,
    rating_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Chat Messages Table
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    delivery_id BIGINT NOT NULL REFERENCES deliveries(id),
    sender_id BIGINT NOT NULL REFERENCES app_user(id),
    message TEXT NOT NULL,
    message_type VARCHAR(20) DEFAULT 'TEXT',
    media_url VARCHAR(500),
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_app_user_email ON app_user(email);
CREATE INDEX IF NOT EXISTS idx_app_user_username ON app_user(username);
CREATE INDEX IF NOT EXISTS idx_app_user_role ON app_user(role);
CREATE INDEX IF NOT EXISTS idx_app_user_kyc_status ON app_user(kyc_status);

CREATE INDEX IF NOT EXISTS idx_deliveries_client_id ON deliveries(client_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_agent_id ON deliveries(agent_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_status ON deliveries(status);
CREATE INDEX IF NOT EXISTS idx_deliveries_tracking_code ON deliveries(tracking_code);

CREATE INDEX IF NOT EXISTS idx_payments_payer_id ON payments(payer_id);
CREATE INDEX IF NOT EXISTS idx_payments_delivery_id ON payments(delivery_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);

CREATE INDEX IF NOT EXISTS idx_ratings_delivery_id ON ratings(delivery_id);
CREATE INDEX IF NOT EXISTS idx_ratings_reviewer_id ON ratings(reviewer_id);
CREATE INDEX IF NOT EXISTS idx_ratings_reviewed_id ON ratings(reviewed_id);

CREATE INDEX IF NOT EXISTS idx_chat_messages_delivery_id ON chat_messages(delivery_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender_id ON chat_messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at ON chat_messages(created_at);

-- Triggers for updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_app_user_updated_at BEFORE UPDATE ON app_user
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_wallets_updated_at BEFORE UPDATE ON wallets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_deliveries_updated_at BEFORE UPDATE ON deliveries
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_ratings_updated_at BEFORE UPDATE ON ratings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_chat_messages_updated_at BEFORE UPDATE ON chat_messages
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();