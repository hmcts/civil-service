CREATE SEQUENCE bulk_claims_reference_number_seq MAXVALUE 999999 NO CYCLE;

/**
 * Returns a new reference number from a 000DC001...999DC999 range.
 */
CREATE FUNCTION next_bulk_claims_reference_number() RETURNS TEXT AS $$
SELECT
  regexp_replace(
    to_char(
      nextval('bulk_claims_reference_number_seq'),
      'FM000000'),
    '(\d{3})(\d{3})', '\1BC\2')
    $$ LANGUAGE SQL;
