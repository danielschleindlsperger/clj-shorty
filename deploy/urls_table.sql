-- Deploy shorty:urls_table to pg

BEGIN;

CREATE TABLE urls(
  id VARCHAR(8) PRIMARY KEY,
  target_url TEXT NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now()
);

COMMIT;
