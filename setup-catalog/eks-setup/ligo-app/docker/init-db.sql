-- Database initialization script
-- This script runs automatically when the PostgreSQL container starts for the first time

-- Create database if not exists handled by POSTGRES_DB env var

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE ligochat TO ligouser;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
