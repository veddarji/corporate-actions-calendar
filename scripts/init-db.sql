#!/bin/bash
# Initialize database with required extensions
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Enable UUID extension
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    -- Enable pg_trgm for fuzzy text search
    CREATE EXTENSION IF NOT EXISTS pg_trgm;
    -- Enable btree_gin for composite indexes
    CREATE EXTENSION IF NOT EXISTS btree_gin;
    
    GRANT ALL PRIVILEGES ON DATABASE corporate_actions_db TO $POSTGRES_USER;
EOSQL
