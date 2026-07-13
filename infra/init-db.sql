-- PostgreSQL Initialization Script for TicketWave
-- ============================================================================

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "unaccent";

-- Set timezone
SET timezone = 'America/Sao_Paulo';

-- Create schema
CREATE SCHEMA IF NOT EXISTS ticketwave;

-- Grant privileges
GRANT USAGE ON SCHEMA ticketwave TO postgres;
GRANT CREATE ON SCHEMA ticketwave TO postgres;

-- Set search path
ALTER DATABASE ticketwave SET search_path TO ticketwave,public;

-- Log initialization
SELECT 'PostgreSQL database initialized for TicketWave' AS initialization_status;
