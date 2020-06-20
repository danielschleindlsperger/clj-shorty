-- Revert shorty:urls_table from pg

BEGIN;

DROP TABLE urls;

COMMIT;
