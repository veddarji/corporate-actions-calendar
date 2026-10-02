CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(50) NOT NULL,
    exchange VARCHAR(20) NOT NULL DEFAULT 'NSE',
    isin VARCHAR(12),
    name VARCHAR(255) NOT NULL,
    sector VARCHAR(100),
    industry VARCHAR(100),
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    country VARCHAR(50) NOT NULL DEFAULT 'India',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_synced_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(exchange, symbol)
);

CREATE UNIQUE INDEX idx_companies_isin ON companies(isin) WHERE isin IS NOT NULL;
CREATE INDEX idx_companies_symbol ON companies(symbol);
CREATE INDEX idx_companies_name ON companies(name);
CREATE INDEX idx_companies_active ON companies(active);
