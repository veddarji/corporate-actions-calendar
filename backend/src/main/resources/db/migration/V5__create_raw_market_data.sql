CREATE TABLE raw_market_data (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(50) NOT NULL,
    source_record_id VARCHAR(255),
    company_symbol VARCHAR(50),
    payload_json JSONB NOT NULL,
    retrieved_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    checksum VARCHAR(64) NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    processed_at TIMESTAMP WITH TIME ZONE,
    processing_error TEXT,
    UNIQUE(source, checksum)
);

CREATE INDEX idx_raw_market_data_source ON raw_market_data(source);
CREATE INDEX idx_raw_market_data_company_symbol ON raw_market_data(company_symbol);
CREATE INDEX idx_raw_market_data_processed ON raw_market_data(processed);
CREATE INDEX idx_raw_market_data_retrieved_at ON raw_market_data(retrieved_at);
