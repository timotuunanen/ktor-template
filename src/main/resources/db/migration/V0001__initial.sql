
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE FUNCTION trigger_set_updated_at_timestamp() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  NEW.updated_at = NOW();
RETURN NEW;
END;
$$;

CREATE TABLE company (
    id uuid DEFAULT uuid_generate_v4() NOT NULL PRIMARY KEY,
    name TEXT,
    business_id TEXT UNIQUE,
    status TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
    updated_at TIMESTAMPTZ DEFAULT now() NOT NULL
);
CREATE TRIGGER set_timestamp BEFORE UPDATE ON company FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at_timestamp();