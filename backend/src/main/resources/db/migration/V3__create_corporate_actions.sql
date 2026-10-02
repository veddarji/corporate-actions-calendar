CREATE TYPE event_type AS ENUM (
    'DIVIDEND', 'STOCK_SPLIT', 'BONUS_ISSUE', 'RIGHTS_ISSUE',
    'EARNINGS', 'AGM', 'EGM', 'BUYBACK', 'OTHER'
);

CREATE TYPE data_quality_status AS ENUM (
    'VERIFIED', 'PENDING_REVIEW', 'CONFLICT', 'INVALID'
);

CREATE TYPE corporate_action_status AS ENUM (
    'ANNOUNCED', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'PENDING'
);

CREATE TABLE corporate_actions (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES companies(id),
    event_type event_type NOT NULL,
    announcement_date DATE,
    ex_date DATE,
    record_date DATE,
    payment_date DATE,
    effective_date DATE,
    amount DECIMAL(20, 6),
    currency VARCHAR(10) DEFAULT 'INR',
    ratio_from DECIMAL(10, 4),
    ratio_to DECIMAL(10, 4),
    issue_price DECIMAL(20, 6),
    subscription_start_date DATE,
    subscription_end_date DATE,
    quarter VARCHAR(10),
    financial_year VARCHAR(10),
    meeting_type VARCHAR(20),
    buyback_price DECIMAL(20, 6),
    buyback_quantity BIGINT,
    period_start_date DATE,
    period_end_date DATE,
    dividend_type VARCHAR(50),
    description TEXT,
    status corporate_action_status NOT NULL DEFAULT 'ANNOUNCED',
    data_quality data_quality_status NOT NULL DEFAULT 'PENDING_REVIEW',
    source VARCHAR(50) NOT NULL,
    source_reference VARCHAR(255),
    source_record_id VARCHAR(255),
    source_updated_at TIMESTAMP WITH TIME ZONE,
    last_verified_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(source, source_record_id)
);

CREATE TABLE corporate_action_attributes (
    id BIGSERIAL PRIMARY KEY,
    corporate_action_id BIGINT NOT NULL REFERENCES corporate_actions(id) ON DELETE CASCADE,
    attribute_key VARCHAR(100) NOT NULL,
    attribute_value TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ca_company_id ON corporate_actions(company_id);
CREATE INDEX idx_ca_event_type ON corporate_actions(event_type);
CREATE INDEX idx_ca_ex_date ON corporate_actions(ex_date);
CREATE INDEX idx_ca_record_date ON corporate_actions(record_date);
CREATE INDEX idx_ca_payment_date ON corporate_actions(payment_date);
CREATE INDEX idx_ca_company_ex_date ON corporate_actions(company_id, ex_date);
CREATE INDEX idx_ca_announcement_date ON corporate_actions(announcement_date);
CREATE INDEX idx_ca_status ON corporate_actions(status);
CREATE INDEX idx_ca_data_quality ON corporate_actions(data_quality);
CREATE INDEX idx_ca_source ON corporate_actions(source);
