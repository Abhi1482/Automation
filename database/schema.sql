CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE integrations_meta (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    account_name VARCHAR(255),
    ad_account_id VARCHAR(255),
    access_token TEXT, -- Encrypted
    currency VARCHAR(10),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE integrations_shopify (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    store_name VARCHAR(255),
    store_url VARCHAR(255),
    client_id VARCHAR(255),
    client_secret TEXT, -- Encrypted
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    shopify_product_id VARCHAR(255),
    cost DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cost_config (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    per_order_cost DECIMAL(10, 2) DEFAULT 65,
    payment_fee_percent DECIMAL(5, 4) DEFAULT 0.0295,
    affiliate_percent DECIMAL(5, 4) DEFAULT 0.28,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE hourly_metrics (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    meta_account_id INTEGER REFERENCES integrations_meta(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    hour INTEGER NOT NULL,
    source VARCHAR(255) NOT NULL,
    revenue DECIMAL(15, 2) DEFAULT 0,
    orders INTEGER DEFAULT 0,
    quantity INTEGER DEFAULT 0,
    product_cost DECIMAL(15, 2) DEFAULT 0,
    logistics_cost DECIMAL(15, 2) DEFAULT 0,
    payment_fee DECIMAL(15, 2) DEFAULT 0,
    ad_spend DECIMAL(15, 2) DEFAULT 0,
    profit DECIMAL(15, 2) DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_hourly_metrics_user_date_hour ON hourly_metrics(user_id, date, hour);
CREATE INDEX idx_hourly_metrics_user_meta ON hourly_metrics(user_id, meta_account_id);

-- Unique constraint for UPSERT (ON CONFLICT)
ALTER TABLE hourly_metrics ADD CONSTRAINT unique_user_meta_date_hour_source UNIQUE (user_id, meta_account_id, date, hour, source);
