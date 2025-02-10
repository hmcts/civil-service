-- 1728388325191581
UPDATE case_data
SET data = jsonb_set(data, '{claimFee}', '"{"code": "FEE0202", "version": "6", "calculatedAmountInPence": "3500"}"', true),
    data_classification = jsonb_set(data_classification,'{claimFee}', '"{
    "value": {
      "code": "PUBLIC",
      "version": "PUBLIC",
      "calculatedAmountInPence": "PUBLIC"
    },
    "classification": "PUBLIC"
  }"', true ),
    last_modified = now(), version=version+1
WHERE case_type_id = 'CIVIL' AND reference = 1728388325191581;

WITH latest_case_event AS (
  SELECT id,
         case_type_version,
         event_id,
         event_name,
         state_name,
         state_id
  FROM case_event
  WHERE case_data_id =
        (SELECT id
         FROM case_data
         WHERE case_type_id = 'CIVIL' AND reference = 1728388325191581)
  ORDER BY id DESC
  limit 1)
INSERT INTO case_event
(
  id,
  created_date,
  event_id,
  summary,
  description,
  user_id,
  case_data_id,
  case_type_id,
  case_type_version,
  state_id,
  data,
  user_first_name,
  user_last_name,
  event_name,
  state_name,
  data_classification,
  security_classification
)
SELECT
  nextval('case_event_id_seq'::regclass), -- case_event_id
  now(),  -- created_date
  'UPDATE_CASE_DATA', --event_id
  'Update case data', --summary
  'Update case data - replace with civil ticket', --description
  'd9238980-0503-4226-af6d-da809825d0e5', --user_id
  cd.id , -- case_data_id
  cd.case_type_id, -- case_type_id
  lce.case_type_version, --case_type_version
  lce.state_id, --state_id
  cd.data, -- Taken from case_data (jsonb column)
  'Civil', --user_first_name
  'system update', --user_last_name
  'Update case data', --event_name
  lce.state_name, --state_name
  cd.data_classification, --Taken from case_data (jsonb column)
  cd.security_classification
FROM case_data cd, latest_case_event lce
WHERE cd.case_type_id = 'CIVIL' AND cd.reference = 1728388325191581;

-- 1728485455474033
UPDATE case_data
SET data = jsonb_set(data, '{claimFee}', '"{"code": "FEE0207", "version": "6", "calculatedAmountInPence": "20500"}"', true),
    data_classification = jsonb_set(data_classification,'{claimFee}', '"{
    "value": {
      "code": "PUBLIC",
      "version": "PUBLIC",
      "calculatedAmountInPence": "PUBLIC"
    },
    "classification": "PUBLIC"
  }"', true ),
    last_modified = now(), version=version+1
WHERE case_type_id = 'CIVIL' AND reference = 1728485455474033;

WITH latest_case_event AS (
  SELECT id,
         case_type_version,
         event_id,
         event_name,
         state_name,
         state_id
  FROM case_event
  WHERE case_data_id =
        (SELECT id
         FROM case_data
         WHERE case_type_id = 'CIVIL' AND reference = 1728485455474033)
  ORDER BY id DESC
  limit 1)
INSERT INTO case_event
(
  id,
  created_date,
  event_id,
  summary,
  description,
  user_id,
  case_data_id,
  case_type_id,
  case_type_version,
  state_id,
  data,
  user_first_name,
  user_last_name,
  event_name,
  state_name,
  data_classification,
  security_classification
)
SELECT
  nextval('case_event_id_seq'::regclass), -- case_event_id
  now(),  -- created_date
  'UPDATE_CASE_DATA', --event_id
  'Update case data', --summary
  'Update case data - replace with civil ticket', --description
  'd9238980-0503-4226-af6d-da809825d0e5', --user_id
  cd.id , -- case_data_id
  cd.case_type_id, -- case_type_id
  lce.case_type_version, --case_type_version
  lce.state_id, --state_id
  cd.data, -- Taken from case_data (jsonb column)
  'Civil', --user_first_name
  'system update', --user_last_name
  'Update case data', --event_name
  lce.state_name, --state_name
  cd.data_classification, --Taken from case_data (jsonb column)
  cd.security_classification
FROM case_data cd, latest_case_event lce
WHERE cd.case_type_id = 'CIVIL' AND cd.reference = 1728485455474033;

-- 1728550236943182
UPDATE case_data
SET data = jsonb_set(data, '{claimFee}', '"{"code": "FEE0202", "version": "6", "calculatedAmountInPence": "3500"}"', true),
    data_classification = jsonb_set(data_classification,'{claimFee}', '"{
    "value": {
      "code": "PUBLIC",
      "version": "PUBLIC",
      "calculatedAmountInPence": "PUBLIC"
    },
    "classification": "PUBLIC"
  }"', true ),
    last_modified = now(), version=version+1
WHERE case_type_id = 'CIVIL' AND reference = 1728550236943182;

WITH latest_case_event AS (
  SELECT id,
         case_type_version,
         event_id,
         event_name,
         state_name,
         state_id
  FROM case_event
  WHERE case_data_id =
        (SELECT id
         FROM case_data
         WHERE case_type_id = 'CIVIL' AND reference = 1728550236943182)
  ORDER BY id DESC
  limit 1)
INSERT INTO case_event
(
  id,
  created_date,
  event_id,
  summary,
  description,
  user_id,
  case_data_id,
  case_type_id,
  case_type_version,
  state_id,
  data,
  user_first_name,
  user_last_name,
  event_name,
  state_name,
  data_classification,
  security_classification
)
SELECT
  nextval('case_event_id_seq'::regclass), -- case_event_id
  now(),  -- created_date
  'SupportUpdate', --event_id
  'Update case data', --summary
  'Set claimfee', --description
  '63517', --user_id
  cd.id , -- case_data_id
  cd.case_type_id, -- case_type_id
  lce.case_type_version, --case_type_version
  lce.state_id, --state_id
  cd.data, -- Taken from case_data (jsonb column)
  'cmc', --user_first_name
  'System Update', --user_last_name
  'Support update', --event_name
  lce.state_name, --state_name
  cd.data_classification, --Taken from case_data (jsonb column)
  cd.security_classification
FROM case_data cd, latest_case_event lce
WHERE cd.case_type_id = 'CIVIL' AND cd.reference = 1728550236943182;

-- 1728574290723611
UPDATE case_data
SET data = jsonb_set(data, '{claimFee}', '"{"code": "FEE0203", "version": "6", "calculatedAmountInPence": "5000"}"', true),
    data_classification = jsonb_set(data_classification,'{claimFee}', '"{
    "value": {
      "code": "PUBLIC",
      "version": "PUBLIC",
      "calculatedAmountInPence": "PUBLIC"
    },
    "classification": "PUBLIC"
  }"', true ),
    last_modified = now(), version=version+1
WHERE case_type_id = 'CIVIL' AND reference = 1728574290723611;

WITH latest_case_event AS (
  SELECT id,
         case_type_version,
         event_id,
         event_name,
         state_name,
         state_id
  FROM case_event
  WHERE case_data_id =
        (SELECT id
         FROM case_data
         WHERE case_type_id = 'CIVIL' AND reference = 1728574290723611)
  ORDER BY id DESC
  limit 1)
INSERT INTO case_event
(
  id,
  created_date,
  event_id,
  summary,
  description,
  user_id,
  case_data_id,
  case_type_id,
  case_type_version,
  state_id,
  data,
  user_first_name,
  user_last_name,
  event_name,
  state_name,
  data_classification,
  security_classification
)
SELECT
  nextval('case_event_id_seq'::regclass), -- case_event_id
  now(),  -- created_date
  'UPDATE_CASE_DATA', --event_id
  'Update case data', --summary
  'Update case data - replace with civil ticket', --description
  'd9238980-0503-4226-af6d-da809825d0e5', --user_id
  cd.id , -- case_data_id
  cd.case_type_id, -- case_type_id
  lce.case_type_version, --case_type_version
  lce.state_id, --state_id
  cd.data, -- Taken from case_data (jsonb column)
  'Civil', --user_first_name
  'system update', --user_last_name
  'Update case data', --event_name
  lce.state_name, --state_name
  cd.data_classification, --Taken from case_data (jsonb column)
  cd.security_classification
FROM case_data cd, latest_case_event lce
WHERE cd.case_type_id = 'CIVIL' AND cd.reference = 1728574290723611;

-- 1728574943192131
UPDATE case_data
SET data = jsonb_set(data, '{claimFee}', '"{"code": "FEE0209", "version": "3", "calculatedAmountInPence": "67945"}"', true),
    data_classification = jsonb_set(data_classification,'{claimFee}', '"{
    "value": {
      "code": "PUBLIC",
      "version": "PUBLIC",
      "calculatedAmountInPence": "PUBLIC"
    },
    "classification": "PUBLIC"
  }"', true ),
    last_modified = now(), version=version+1
WHERE case_type_id = 'CIVIL' AND reference = 1728574943192131;

WITH latest_case_event AS (
  SELECT id,
         case_type_version,
         event_id,
         event_name,
         state_name,
         state_id
  FROM case_event
  WHERE case_data_id =
        (SELECT id
         FROM case_data
         WHERE case_type_id = 'CIVIL' AND reference = 1728574943192131)
  ORDER BY id DESC
  limit 1)
INSERT INTO case_event
(
  id,
  created_date,
  event_id,
  summary,
  description,
  user_id,
  case_data_id,
  case_type_id,
  case_type_version,
  state_id,
  data,
  user_first_name,
  user_last_name,
  event_name,
  state_name,
  data_classification,
  security_classification
)
SELECT
  nextval('case_event_id_seq'::regclass), -- case_event_id
  now(),  -- created_date
  'UPDATE_CASE_DATA', --event_id
  'Update case data', --summary
  'Update case data - replace with civil ticket', --description
  'd9238980-0503-4226-af6d-da809825d0e5', --user_id
  cd.id , -- case_data_id
  cd.case_type_id, -- case_type_id
  lce.case_type_version, --case_type_version
  lce.state_id, --state_id
  cd.data, -- Taken from case_data (jsonb column)
  'Civil', --user_first_name
  'system update', --user_last_name
  'Update case data', --event_name
  lce.state_name, --state_name
  cd.data_classification, --Taken from case_data (jsonb column)
  cd.security_classification
FROM case_data cd, latest_case_event lce
WHERE cd.case_type_id = 'CIVIL' AND cd.reference = 1728574943192131;

-- 1728598820889055
UPDATE case_data
SET data = jsonb_set(data, '{claimFee}', '"{"code": "FEE0205", "version": "6", "calculatedAmountInPence": "8000"}"', true),
    data_classification = jsonb_set(data_classification,'{claimFee}', '"{
    "value": {
      "code": "PUBLIC",
      "version": "PUBLIC",
      "calculatedAmountInPence": "PUBLIC"
    },
    "classification": "PUBLIC"
  }"', true ),
    last_modified = now(), version=version+1
WHERE case_type_id = 'CIVIL' AND reference = 1728598820889055;

WITH latest_case_event AS (
  SELECT id,
         case_type_version,
         event_id,
         event_name,
         state_name,
         state_id
  FROM case_event
  WHERE case_data_id =
        (SELECT id
         FROM case_data
         WHERE case_type_id = 'CIVIL' AND reference = 1728598820889055)
  ORDER BY id DESC
  limit 1)
INSERT INTO case_event
(
  id,
  created_date,
  event_id,
  summary,
  description,
  user_id,
  case_data_id,
  case_type_id,
  case_type_version,
  state_id,
  data,
  user_first_name,
  user_last_name,
  event_name,
  state_name,
  data_classification,
  security_classification
)
SELECT
  nextval('case_event_id_seq'::regclass), -- case_event_id
  now(),  -- created_date
  'UPDATE_CASE_DATA', --event_id
  'Update case data', --summary
  'Update case data - replace with civil ticket', --description
  'd9238980-0503-4226-af6d-da809825d0e5', --user_id
  cd.id , -- case_data_id
  cd.case_type_id, -- case_type_id
  lce.case_type_version, --case_type_version
  lce.state_id, --state_id
  cd.data, -- Taken from case_data (jsonb column)
  'Civil', --user_first_name
  'system update', --user_last_name
  'Update case data', --event_name
  lce.state_name, --state_name
  cd.data_classification, --Taken from case_data (jsonb column)
  cd.security_classification
FROM case_data cd, latest_case_event lce
WHERE cd.case_type_id = 'CIVIL' AND cd.reference = 1728598820889055;

-- 1728640399072356
UPDATE case_data
SET data = jsonb_set(data, '{claimFee}', '"{"code": "FEE0204", "version": "6", "calculatedAmountInPence": "7000"}"', true),
    data_classification = jsonb_set(data_classification,'{claimFee}', '"{
    "value": {
      "code": "PUBLIC",
      "version": "PUBLIC",
      "calculatedAmountInPence": "PUBLIC"
    },
    "classification": "PUBLIC"
  }"', true ),
    last_modified = now(), version=version+1
WHERE case_type_id = 'CIVIL' AND reference = 1728640399072356;

WITH latest_case_event AS (
  SELECT id,
         case_type_version,
         event_id,
         event_name,
         state_name,
         state_id
  FROM case_event
  WHERE case_data_id =
        (SELECT id
         FROM case_data
         WHERE case_type_id = 'CIVIL' AND reference = 1728640399072356)
  ORDER BY id DESC
  limit 1)
INSERT INTO case_event
(
  id,
  created_date,
  event_id,
  summary,
  description,
  user_id,
  case_data_id,
  case_type_id,
  case_type_version,
  state_id,
  data,
  user_first_name,
  user_last_name,
  event_name,
  state_name,
  data_classification,
  security_classification
)
SELECT
  nextval('case_event_id_seq'::regclass), -- case_event_id
  now(),  -- created_date
  'UPDATE_CASE_DATA', --event_id
  'Update case data', --summary
  'Update case data - replace with civil ticket', --description
  'd9238980-0503-4226-af6d-da809825d0e5', --user_id
  cd.id , -- case_data_id
  cd.case_type_id, -- case_type_id
  lce.case_type_version, --case_type_version
  lce.state_id, --state_id
  cd.data, -- Taken from case_data (jsonb column)
  'Civil', --user_first_name
  'system update', --user_last_name
  'Update case data', --event_name
  lce.state_name, --state_name
  cd.data_classification, --Taken from case_data (jsonb column)
  cd.security_classification
FROM case_data cd, latest_case_event lce
WHERE cd.case_type_id = 'CIVIL' AND cd.reference = 1728640399072356;

-- 1728905924939340
UPDATE case_data
SET data = jsonb_set(data, '{claimFee}', '"{"code": "FEE0209", "version": "3", "calculatedAmountInPence": "59114"}"', true),
    data_classification = jsonb_set(data_classification,'{claimFee}', '"{
    "value": {
      "code": "PUBLIC",
      "version": "PUBLIC",
      "calculatedAmountInPence": "PUBLIC"
    },
    "classification": "PUBLIC"
  }"', true ),
    last_modified = now(), version=version+1
WHERE case_type_id = 'CIVIL' AND reference = 1728649592985181;

WITH latest_case_event AS (
  SELECT id,
         case_type_version,
         event_id,
         event_name,
         state_name,
         state_id
  FROM case_event
  WHERE case_data_id =
        (SELECT id
         FROM case_data
         WHERE case_type_id = 'CIVIL' AND reference = 1728905924939340)
  ORDER BY id DESC
  limit 1)
INSERT INTO case_event
(
  id,
  created_date,
  event_id,
  summary,
  description,
  user_id,
  case_data_id,
  case_type_id,
  case_type_version,
  state_id,
  data,
  user_first_name,
  user_last_name,
  event_name,
  state_name,
  data_classification,
  security_classification
)
SELECT
  nextval('case_event_id_seq'::regclass), -- case_event_id
  now(),  -- created_date
  'UPDATE_CASE_DATA', --event_id
  'Update case data', --summary
  'Update case data - replace with civil ticket', --description
  'd9238980-0503-4226-af6d-da809825d0e5', --user_id
  cd.id , -- case_data_id
  cd.case_type_id, -- case_type_id
  lce.case_type_version, --case_type_version
  lce.state_id, --state_id
  cd.data, -- Taken from case_data (jsonb column)
  'Civil', --user_first_name
  'system update', --user_last_name
  'Update case data', --event_name
  lce.state_name, --state_name
  cd.data_classification, --Taken from case_data (jsonb column)
  cd.security_classification
FROM case_data cd, latest_case_event lce
WHERE cd.case_type_id = 'CIVIL' AND cd.reference = 1728905924939340;

-- 1728910877048594
UPDATE case_data
SET data = jsonb_set(data, '{claimFee}', '"{"code": "FEE0204", "version": "6", "calculatedAmountInPence": "7000"}"', true),
    data_classification = jsonb_set(data_classification,'{claimFee}', '"{
    "value": {
      "code": "PUBLIC",
      "version": "PUBLIC",
      "calculatedAmountInPence": "PUBLIC"
    },
    "classification": "PUBLIC"
  }"', true ),
    last_modified = now(), version=version+1
WHERE case_type_id = 'CIVIL' AND reference = 1728910877048594;

WITH latest_case_event AS (
  SELECT id,
         case_type_version,
         event_id,
         event_name,
         state_name,
         state_id
  FROM case_event
  WHERE case_data_id =
        (SELECT id
         FROM case_data
         WHERE case_type_id = 'CIVIL' AND reference = 1728910877048594)
  ORDER BY id DESC
  limit 1)
INSERT INTO case_event
(
  id,
  created_date,
  event_id,
  summary,
  description,
  user_id,
  case_data_id,
  case_type_id,
  case_type_version,
  state_id,
  data,
  user_first_name,
  user_last_name,
  event_name,
  state_name,
  data_classification,
  security_classification
)
SELECT
  nextval('case_event_id_seq'::regclass), -- case_event_id
  now(),  -- created_date
  'UPDATE_CASE_DATA', --event_id
  'Update case data', --summary
  'Update case data - replace with civil ticket', --description
  'd9238980-0503-4226-af6d-da809825d0e5', --user_id
  cd.id , -- case_data_id
  cd.case_type_id, -- case_type_id
  lce.case_type_version, --case_type_version
  lce.state_id, --state_id
  cd.data, -- Taken from case_data (jsonb column)
  'Civil', --user_first_name
  'system update', --user_last_name
  'Update case data', --event_name
  lce.state_name, --state_name
  cd.data_classification, --Taken from case_data (jsonb column)
  cd.security_classification
FROM case_data cd, latest_case_event lce
WHERE cd.case_type_id = 'CIVIL' AND cd.reference = 1728910877048594;

-- 1728910880370118
UPDATE case_data
SET data = jsonb_set(data, '{claimFee}', '"{"code": "FEE0206", "version": "6", "calculatedAmountInPence": "11500"}"', true),
    data_classification = jsonb_set(data_classification,'{claimFee}', '"{
    "value": {
      "code": "PUBLIC",
      "version": "PUBLIC",
      "calculatedAmountInPence": "PUBLIC"
    },
    "classification": "PUBLIC"
  }"', true ),
    last_modified = now(), version=version+1
WHERE case_type_id = 'CIVIL' AND reference = 1728910880370118;

WITH latest_case_event AS (
  SELECT id,
         case_type_version,
         event_id,
         event_name,
         state_name,
         state_id
  FROM case_event
  WHERE case_data_id =
        (SELECT id
         FROM case_data
         WHERE case_type_id = 'CIVIL' AND reference = 1728910880370118)
  ORDER BY id DESC
  limit 1)
INSERT INTO case_event
(
  id,
  created_date,
  event_id,
  summary,
  description,
  user_id,
  case_data_id,
  case_type_id,
  case_type_version,
  state_id,
  data,
  user_first_name,
  user_last_name,
  event_name,
  state_name,
  data_classification,
  security_classification
)
SELECT
  nextval('case_event_id_seq'::regclass), -- case_event_id
  now(),  -- created_date
  'UPDATE_CASE_DATA', --event_id
  'Update case data', --summary
  'Update case data - replace with civil ticket', --description
  'd9238980-0503-4226-af6d-da809825d0e5', --user_id
  cd.id , -- case_data_id
  cd.case_type_id, -- case_type_id
  lce.case_type_version, --case_type_version
  lce.state_id, --state_id
  cd.data, -- Taken from case_data (jsonb column)
  'Civil', --user_first_name
  'system update', --user_last_name
  'Update case data', --event_name
  lce.state_name, --state_name
  cd.data_classification, --Taken from case_data (jsonb column)
  cd.security_classification
FROM case_data cd, latest_case_event lce
WHERE cd.case_type_id = 'CIVIL' AND cd.reference = 1728910880370118;

-- 1728925222440494
UPDATE case_data
SET data = jsonb_set(data, '{claimFee}', '"{"code": "FEE0207", "version": "6", "calculatedAmountInPence": "20500"}"', true),
    data_classification = jsonb_set(data_classification,'{claimFee}', '"{
    "value": {
      "code": "PUBLIC",
      "version": "PUBLIC",
      "calculatedAmountInPence": "PUBLIC"
    },
    "classification": "PUBLIC"
  }"', true ),
    last_modified = now(), version=version+1
WHERE case_type_id = 'CIVIL' AND reference = 1728925222440494;

WITH latest_case_event AS (
  SELECT id,
         case_type_version,
         event_id,
         event_name,
         state_name,
         state_id
  FROM case_event
  WHERE case_data_id =
        (SELECT id
         FROM case_data
         WHERE case_type_id = 'CIVIL' AND reference = 1728925222440494)
  ORDER BY id DESC
  limit 1)
INSERT INTO case_event
(
  id,
  created_date,
  event_id,
  summary,
  description,
  user_id,
  case_data_id,
  case_type_id,
  case_type_version,
  state_id,
  data,
  user_first_name,
  user_last_name,
  event_name,
  state_name,
  data_classification,
  security_classification
)
SELECT
  nextval('case_event_id_seq'::regclass), -- case_event_id
  now(),  -- created_date
  'UPDATE_CASE_DATA', --event_id
  'Update case data', --summary
  'Update case data - replace with civil ticket', --description
  'd9238980-0503-4226-af6d-da809825d0e5', --user_id
  cd.id , -- case_data_id
  cd.case_type_id, -- case_type_id
  lce.case_type_version, --case_type_version
  lce.state_id, --state_id
  cd.data, -- Taken from case_data (jsonb column)
  'Civil', --user_first_name
  'system update', --user_last_name
  'Update case data', --event_name
  lce.state_name, --state_name
  cd.data_classification, --Taken from case_data (jsonb column)
  cd.security_classification
FROM case_data cd, latest_case_event lce
WHERE cd.case_type_id = 'CIVIL' AND cd.reference = 1728925222440494;

-- 1729025313897808
UPDATE case_data
SET data = jsonb_set(data, '{claimFee}', '"{"code": "FEE0208", "version": "4", "calculatedAmountInPence": "45500"}"', true),
    data_classification = jsonb_set(data_classification,'{claimFee}', '"{
    "value": {
      "code": "PUBLIC",
      "version": "PUBLIC",
      "calculatedAmountInPence": "PUBLIC"
    },
    "classification": "PUBLIC"
  }"', true ),
    last_modified = now(), version=version+1
WHERE case_type_id = 'CIVIL' AND reference = 1729025313897808;

WITH latest_case_event AS (
  SELECT id,
         case_type_version,
         event_id,
         event_name,
         state_name,
         state_id
  FROM case_event
  WHERE case_data_id =
        (SELECT id
         FROM case_data
         WHERE case_type_id = 'CIVIL' AND reference = 1729025313897808)
  ORDER BY id DESC
  limit 1)
INSERT INTO case_event
(
  id,
  created_date,
  event_id,
  summary,
  description,
  user_id,
  case_data_id,
  case_type_id,
  case_type_version,
  state_id,
  data,
  user_first_name,
  user_last_name,
  event_name,
  state_name,
  data_classification,
  security_classification
)
SELECT
  nextval('case_event_id_seq'::regclass), -- case_event_id
  now(),  -- created_date
  'UPDATE_CASE_DATA', --event_id
  'Update case data', --summary
  'Update case data - replace with civil ticket', --description
  'd9238980-0503-4226-af6d-da809825d0e5', --user_id
  cd.id , -- case_data_id
  cd.case_type_id, -- case_type_id
  lce.case_type_version, --case_type_version
  lce.state_id, --state_id
  cd.data, -- Taken from case_data (jsonb column)
  'Civil', --user_first_name
  'system update', --user_last_name
  'Update case data', --event_name
  lce.state_name, --state_name
  cd.data_classification, --Taken from case_data (jsonb column)
  cd.security_classification
FROM case_data cd, latest_case_event lce
WHERE cd.case_type_id = 'CIVIL' AND cd.reference = 1729025313897808;

-- 1729072674103434
UPDATE case_data
SET data = jsonb_set(data, '{claimFee}', '"{"code": "FEE0208", "version": "4", "calculatedAmountInPence": "6000"}"', true),
    data_classification = jsonb_set(data_classification,'{claimFee}', '"{
    "value": {
      "code": "PUBLIC",
      "version": "PUBLIC",
      "calculatedAmountInPence": "PUBLIC"
    },
    "classification": "PUBLIC"
  }"', true ),
    last_modified = now(), version=version+1
WHERE case_type_id = 'CIVIL' AND reference = 1729072674103434;

WITH latest_case_event AS (
  SELECT id,
         case_type_version,
         event_id,
         event_name,
         state_name,
         state_id
  FROM case_event
  WHERE case_data_id =
        (SELECT id
         FROM case_data
         WHERE case_type_id = 'CIVIL' AND reference = 1729072674103434)
  ORDER BY id DESC
  limit 1)
INSERT INTO case_event
(
  id,
  created_date,
  event_id,
  summary,
  description,
  user_id,
  case_data_id,
  case_type_id,
  case_type_version,
  state_id,
  data,
  user_first_name,
  user_last_name,
  event_name,
  state_name,
  data_classification,
  security_classification
)
SELECT
  nextval('case_event_id_seq'::regclass), -- case_event_id
  now(),  -- created_date
  'UPDATE_CASE_DATA', --event_id
  'Update case data', --summary
  'Update case data - replace with civil ticket', --description
  'd9238980-0503-4226-af6d-da809825d0e5', --user_id
  cd.id , -- case_data_id
  cd.case_type_id, -- case_type_id
  lce.case_type_version, --case_type_version
  lce.state_id, --state_id
  cd.data, -- Taken from case_data (jsonb column)
  'Civil', --user_first_name
  'system update', --user_last_name
  'Update case data', --event_name
  lce.state_name, --state_name
  cd.data_classification, --Taken from case_data (jsonb column)
  cd.security_classification
FROM case_data cd, latest_case_event lce
WHERE cd.case_type_id = 'CIVIL' AND cd.reference = 1729072674103434;


-- 1729158382022584
UPDATE case_data
SET data = jsonb_set(data, '{claimFee}', '"{"code": "FEE0209", "version": "3", "calculatedAmountInPence": "51072"}"', true),
    data_classification = jsonb_set(data_classification,'{claimFee}', '"{
    "value": {
      "code": "PUBLIC",
      "version": "PUBLIC",
      "calculatedAmountInPence": "PUBLIC"
    },
    "classification": "PUBLIC"
  }"', true ),
    last_modified = now(), version=version+1
WHERE case_type_id = 'CIVIL' AND reference = 1729158382022584;

WITH latest_case_event AS (
  SELECT id,
         case_type_version,
         event_id,
         event_name,
         state_name,
         state_id
  FROM case_event
  WHERE case_data_id =
        (SELECT id
         FROM case_data
         WHERE case_type_id = 'CIVIL' AND reference = 1729158382022584)
  ORDER BY id DESC
  limit 1)
INSERT INTO case_event
(
  id,
  created_date,
  event_id,
  summary,
  description,
  user_id,
  case_data_id,
  case_type_id,
  case_type_version,
  state_id,
  data,
  user_first_name,
  user_last_name,
  event_name,
  state_name,
  data_classification,
  security_classification
)
SELECT
  nextval('case_event_id_seq'::regclass), -- case_event_id
  now(),  -- created_date
  'UPDATE_CASE_DATA', --event_id
  'Update case data', --summary
  'Update case data - replace with civil ticket', --description
  'd9238980-0503-4226-af6d-da809825d0e5', --user_id
  cd.id , -- case_data_id
  cd.case_type_id, -- case_type_id
  lce.case_type_version, --case_type_version
  lce.state_id, --state_id
  cd.data, -- Taken from case_data (jsonb column)
  'Civil', --user_first_name
  'system update', --user_last_name
  'Update case data', --event_name
  lce.state_name, --state_name
  cd.data_classification, --Taken from case_data (jsonb column)
  cd.security_classification
FROM case_data cd, latest_case_event lce
WHERE cd.case_type_id = 'CIVIL' AND cd.reference = 1729158382022584;
