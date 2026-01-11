-- H2 Database Schema for DeliverXY
-- This script is compatible with H2 database for development

-- Note: H2 has built-in UUID support, no need for custom alias

-- Create app_user table
CREATE TABLE app_user (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          username VARCHAR(100) NOT NULL UNIQUE,
                          first_name VARCHAR(100),
                          last_name VARCHAR(100),
                          email VARCHAR(255) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          phone_number VARCHAR(50),
                          role VARCHAR(50) NOT NULL,
                          kyc_status VARCHAR(50) DEFAULT 'PENDING',
                          id_front_url VARCHAR(255),
                          id_back_url VARCHAR(255),
                          selfie_url VARCHAR(255),
                          proof_of_address_url VARCHAR(255),
                          kyc_submitted_at TIMESTAMP,
                          kyc_verified_at TIMESTAMP,
                          kyc_rejection_reason VARCHAR(255),
                          drivers_license_number VARCHAR(100),
                          drivers_license_expiry TIMESTAMP,
                          drivers_license_front_url VARCHAR(255),
                          drivers_license_back_url VARCHAR(255),
                          is_available BOOLEAN DEFAULT FALSE,
                          current_latitude DOUBLE,
                          current_longitude DOUBLE,
                          last_location_update TIMESTAMP,
                          rating DOUBLE DEFAULT 0.0,
                          total_deliveries INT DEFAULT 0,
                          total_earnings DOUBLE DEFAULT 0.0,
                          is_active BOOLEAN DEFAULT TRUE,
                          is_verified BOOLEAN DEFAULT FALSE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create deliveries table
CREATE TABLE deliveries (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            title VARCHAR(255) NOT NULL,
                            description TEXT,
                            package_type VARCHAR(50),
                            package_weight DOUBLE,
                            package_dimensions VARCHAR(50),
                            is_fragile BOOLEAN DEFAULT FALSE,
                            is_urgent BOOLEAN DEFAULT FALSE,
                            pickup_address VARCHAR(255) NOT NULL,
                            pickup_latitude DOUBLE,
                            pickup_longitude DOUBLE,
                            pickup_contact_name VARCHAR(100),
                            pickup_contact_phone VARCHAR(50),
                            pickup_instructions VARCHAR(255),
                            dropoff_address VARCHAR(255) NOT NULL,
                            dropoff_latitude DOUBLE,
                            dropoff_longitude DOUBLE,
                            dropoff_contact_name VARCHAR(100),
                            dropoff_contact_phone VARCHAR(50),
                            dropoff_instructions VARCHAR(255),
                            requested_pickup_time TIMESTAMP,
                            requested_delivery_time TIMESTAMP,
                            actual_pickup_time TIMESTAMP,
                            actual_delivery_time TIMESTAMP,
                            expires_at TIMESTAMP,
                            status VARCHAR(50) DEFAULT 'REQUESTED',
                            client_id BIGINT NOT NULL,
                            agent_id BIGINT,
                            assigned_at TIMESTAMP,
                            base_price DECIMAL(15,2) NOT NULL,
                            urgent_fee DECIMAL(15,2) DEFAULT 0.00,
                            fragile_fee DECIMAL(15,2) DEFAULT 0.00,
                            total_price DECIMAL(15,2),
                            tip_amount DECIMAL(15,2) DEFAULT 0.00,
                            final_amount DECIMAL(15,2),
                            payment_status VARCHAR(50) DEFAULT 'PENDING',
                            payment_method VARCHAR(50),
                            paid_at TIMESTAMP,
                            tip DOUBLE,
                            driver_earnings DOUBLE,
                            driver_rating DOUBLE,
                            estimated_distance DOUBLE,
                            estimated_duration INT,
                            current_latitude DOUBLE,
                            current_longitude DOUBLE,
                            last_location_update TIMESTAMP,
                            tracking_code VARCHAR(255) UNIQUE,
                            is_insured BOOLEAN DEFAULT FALSE,
                            insurance_amount DECIMAL(15,2) DEFAULT 0.00,
                            insurance_premium DECIMAL(15,2) DEFAULT 0.00,
                            client_rating INT,
                            client_review TEXT,
                            agent_rating INT,
                            agent_review TEXT,
                            reviewed_by_client BOOLEAN DEFAULT FALSE,
                            reviewed_by_agent BOOLEAN DEFAULT FALSE,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            cancelled_at TIMESTAMP,
                            cancellation_reason VARCHAR(255),
                            cancelled_by VARCHAR(50),
                            CONSTRAINT fk_delivery_client FOREIGN KEY (client_id) REFERENCES app_user(id),
                            CONSTRAINT fk_delivery_agent FOREIGN KEY (agent_id) REFERENCES app_user(id)
);
ALTER TABLE deliveries ADD COLUMN IF NOT EXISTS driver_earnings DECIMAL(10,2) DEFAULT 0.0;


-- Create payments table
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_id VARCHAR(100) UNIQUE NOT NULL,
    delivery_id BIGINT,
    payer_id BIGINT NOT NULL,
    recipient_id BIGINT,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    payment_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(20),
    description TEXT,
    stripe_payment_intent_id VARCHAR(100),
    stripe_charge_id VARCHAR(100),
    failure_reason TEXT,
    refund_amount DECIMAL(10,2) DEFAULT 0.00,
    refund_reason TEXT,
    refunded_at TIMESTAMP,
    processing_fee DECIMAL(10,2) DEFAULT 0.00,
    platform_fee DECIMAL(10,2) DEFAULT 0.00,
    net_amount DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    expires_at TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,
    metadata TEXT,
    FOREIGN KEY (delivery_id) REFERENCES deliveries(id) ON DELETE SET NULL,
    FOREIGN KEY (payer_id) REFERENCES app_user(id) ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES app_user(id) ON DELETE SET NULL
);

-- Create ratings table
CREATE TABLE IF NOT EXISTS ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    reviewed_id BIGINT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review TEXT,
    rating_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (delivery_id) REFERENCES deliveries(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewer_id) REFERENCES app_user(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- Create chat_messages table
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    message_type VARCHAR(20) DEFAULT 'TEXT',
    media_url VARCHAR(500),
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (delivery_id) REFERENCES deliveries(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- Create wallets table
CREATE TABLE wallets (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         user_id BIGINT NOT NULL UNIQUE,
                         balance DECIMAL(15,2) DEFAULT 0.00,
                         currency VARCHAR(10) DEFAULT 'USD',
                         is_active BOOLEAN DEFAULT TRUE,
                         daily_limit DECIMAL(15,2) DEFAULT 1000.00,
                         monthly_limit DECIMAL(15,2) DEFAULT 10000.00,
                         daily_spent DECIMAL(15,2) DEFAULT 0.00,
                         monthly_spent DECIMAL(15,2) DEFAULT 0.00,
                         last_reset_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- Create vehicles table
CREATE TABLE vehicles (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          owner_id BIGINT NOT NULL,
                          vehicle_type VARCHAR(50) NOT NULL,
                          make VARCHAR(100) NOT NULL,
                          model VARCHAR(100) NOT NULL,
                          vehicle_year INT,
                          license_plate VARCHAR(50) NOT NULL UNIQUE,
                          color VARCHAR(50),
                          passenger_capacity INT,
                          cargo_capacity_kg DOUBLE,
                          cargo_volume_cubic_meters DOUBLE,
                          is_available BOOLEAN DEFAULT TRUE,
                          insurance_provider VARCHAR(100),
                          insurance_policy_number VARCHAR(100),
                          insurance_expiry_date TIMESTAMP,
                          registration_expiry_date TIMESTAMP,
                          vehicle_condition VARCHAR(50) DEFAULT 'GOOD',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_vehicle_owner FOREIGN KEY (owner_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- Add new KYC fields to app_user table
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS drivers_license_number VARCHAR(50);
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS drivers_license_expiry TIMESTAMP;
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS drivers_license_front_url VARCHAR(500);
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS drivers_license_back_url VARCHAR(500);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_app_user_email ON app_user(email);
CREATE INDEX IF NOT EXISTS idx_app_user_role ON app_user(role);
CREATE INDEX IF NOT EXISTS idx_app_user_kyc_status ON app_user(kyc_status);
CREATE INDEX IF NOT EXISTS idx_deliveries_status ON deliveries(status);
CREATE INDEX IF NOT EXISTS idx_deliveries_client_id ON deliveries(client_id);
CREATE INDEX IF NOT EXISTS idx_deliveries_agent_id ON deliveries(agent_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_ratings_delivery_id ON ratings(delivery_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_delivery_id ON chat_messages(delivery_id);

-- Note: Triggers are not needed for H2 as JPA will handle updated_at automatically
-- The @PreUpdate annotation in the entity classes will handle this