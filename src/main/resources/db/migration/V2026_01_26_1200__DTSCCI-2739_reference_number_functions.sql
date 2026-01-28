CREATE TABLE claim_ref_prefix (
  series_key text NOT NULL,
  idx        int  NOT NULL,
  prefix     text NOT NULL,
  PRIMARY KEY (series_key, idx),
  UNIQUE (series_key, prefix)
);

CREATE TABLE claim_ref_state (
  series_key text PRIMARY KEY,
  prefix_idx int NOT NULL,
  counter    int NOT NULL CHECK (counter BETWEEN 0 AND 999999)
);

INSERT INTO claim_ref_prefix (series_key, idx, prefix) VALUES
  ('spec', 1, 'JE'),
  ('spec', 2, 'JF'),
  ('spec', 3, 'JG'),
  ('spec', 4, 'JH'),
  ('spec', 5, 'JJ'),
  ('spec', 6, 'JL');

INSERT INTO claim_ref_prefix (series_key, idx, prefix) VALUES
  ('unspec', 1, 'KA'),
  ('unspec', 2, 'KC'),
  ('unspec', 3, 'KD');

INSERT INTO claim_ref_state (series_key, prefix_idx, counter) VALUES
  ('spec', 1, 0),
  ('unspec', 1, 0)
ON CONFLICT (series_key) DO NOTHING;

CREATE OR REPLACE FUNCTION next_caseman_reference(p_series_key text)
RETURNS text
LANGUAGE plpgsql
AS $$
DECLARE
  v_prefix_idx int;
  v_counter    int;
  v_prefix     text;
BEGIN
  SELECT prefix_idx, counter
    INTO v_prefix_idx, v_counter
    FROM claim_ref_state
   WHERE series_key = p_series_key
   FOR UPDATE;

  IF NOT FOUND THEN
    RAISE EXCEPTION 'Unknown series_key: %', p_series_key;
  END IF;

  IF v_counter >= 999999 THEN
    v_counter := 1;
    v_prefix_idx := v_prefix_idx + 1;

    IF NOT EXISTS (
      SELECT 1 FROM claim_ref_prefix
      WHERE series_key = p_series_key AND idx = v_prefix_idx
    ) THEN
      RAISE EXCEPTION 'No more prefixes configured for series_key=% (wanted idx=%)',
        p_series_key, v_prefix_idx;
    END IF;
  ELSE
    v_counter := v_counter + 1;
  END IF;

  SELECT prefix
    INTO v_prefix
    FROM claim_ref_prefix
   WHERE series_key = p_series_key
     AND idx = v_prefix_idx;

  UPDATE claim_ref_state
     SET prefix_idx = v_prefix_idx,
         counter = v_counter
   WHERE series_key = p_series_key;

  RETURN regexp_replace(
    to_char(v_counter, 'FM000000'),
    '(\d{3})(\d{3})',
    '\1' || v_prefix || '\2'
  );
END;
$$;

CREATE OR REPLACE FUNCTION next_reference_number()
RETURNS text
LANGUAGE sql
AS $$
  SELECT next_caseman_reference('spec');
$$;

CREATE OR REPLACE FUNCTION next_damages_claims_reference_number()
RETURNS text
LANGUAGE sql
AS $$
  SELECT next_caseman_reference('unspec');
$$;

DROP SEQUENCE IF EXISTS claim_reference_number_seq;
DROP SEQUENCE IF EXISTS damages_claims_reference_number_seq;
