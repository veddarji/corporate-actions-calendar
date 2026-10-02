CREATE TYPE job_status AS ENUM ('RUNNING', 'COMPLETED', 'FAILED', 'SKIPPED');

CREATE TABLE ingestion_job_logs (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    source VARCHAR(50) NOT NULL,
    status job_status NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    records_fetched INTEGER,
    records_processed INTEGER,
    records_inserted INTEGER,
    records_updated INTEGER,
    records_skipped INTEGER,
    error_message TEXT,
    additional_info JSONB
);

CREATE INDEX idx_job_logs_job_name ON ingestion_job_logs(job_name);
CREATE INDEX idx_job_logs_status ON ingestion_job_logs(status);
CREATE INDEX idx_job_logs_started_at ON ingestion_job_logs(started_at);
