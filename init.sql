-- Docker entrypoint database initialization script
-- Database bot_db is created automatically by POSTGRES_DB environment variable.
-- Add custom extensions or initial DDL if required.
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
