import { spawnSync } from 'node:child_process';

/*
 * Compares the seeded cases' rows in data store's database with civil-service's ccd schema after the migration's
 * CUTOVER run, before routing or any new event. The SDK copies case_data and case_event columns as they are,
 * except: case_event.version and case_revision are renumbered 1..N per case, idempotency_key is new, and
 * case_data.case_revision becomes the case's highest event revision plus the revision offset (1,000,000,000 by
 * default), so migrated Elasticsearch documents replace the older central ones.
 */
export const COMPARED_FIELDS = [
  'jurisdiction',
  'case_type_id',
  'state',
  'security_classification',
  'version',
  'created_date',
  'last_modified',
  'last_state_modified_date',
  'resolved_ttl',
  'data_hash',
  'supplementary_data_hash',
  'event_count',
  'events_hash',
  'latest_event_id'
];

export function caseRowsQuery(schema, references) {
  if (!/^[a-z_]+$/.test(schema)) {
    throw new Error(`Invalid schema name: ${schema}`);
  }
  references.forEach(reference => {
    if (!/^\d{16}$/.test(String(reference))) {
      throw new Error(`Invalid case reference: ${reference}`);
    }
  });
  const values = references.map(reference => `(${reference}::bigint)`).join(',');
  return `
with requested(reference) as (values ${values}),
rows as (
  select
    requested.reference::text as requested_reference,
    cd.reference::text as reference,
    cd.jurisdiction,
    cd.case_type_id,
    cd.state,
    cd.security_classification::text as security_classification,
    cd.version,
    cd.created_date::text as created_date,
    cd.last_modified::text as last_modified,
    cd.last_state_modified_date::text as last_state_modified_date,
    cd.resolved_ttl::text as resolved_ttl,
    md5(cd.data::text) as data_hash,
    md5(coalesce(cd.supplementary_data, '{}'::jsonb)::text) as supplementary_data_hash,
    to_jsonb(cd) ->> 'case_revision' as case_revision,
    (select count(*) from ${schema}.case_event ce where ce.case_data_id = cd.id) as event_count,
    (select md5(string_agg(ce.id || ':' || ce.event_id || ':' || ce.state_id || ':' || md5(ce.data::text), ',' order by ce.id))
       from ${schema}.case_event ce where ce.case_data_id = cd.id) as events_hash,
    (select ce.event_id from ${schema}.case_event ce where ce.case_data_id = cd.id order by ce.id desc limit 1)
      as latest_event_id,
    (select max((to_jsonb(ce) ->> 'case_revision')::bigint) from ${schema}.case_event ce where ce.case_data_id = cd.id)
      as max_event_revision,
    (select bool_and((to_jsonb(ce) ->> 'case_revision')::bigint = ranked.position)
       from ${schema}.case_event ce
       join (select id, row_number() over (order by id) as position
               from ${schema}.case_event where case_data_id = cd.id) ranked on ranked.id = ce.id
      where ce.case_data_id = cd.id) as event_revisions_sequential
  from requested
  left join ${schema}.case_data cd on cd.reference = requested.reference
)
select coalesce(jsonb_agg(to_jsonb(rows) order by requested_reference), '[]'::jsonb)::text from rows;
`;
}

export function queryRows({ name, url, password }, query) {
  const result = spawnSync('psql', ['--quiet', '--tuples-only', '--no-align', '--set=ON_ERROR_STOP=1',
    `--dbname=${url}`, `--command=${query}`], {
    encoding: 'utf8',
    env: { ...process.env, PGPASSWORD: password },
    maxBuffer: 20 * 1024 * 1024
  });
  if (result.error) {
    throw new Error(`Could not run psql for the ${name} database: ${result.error.message}`);
  }
  if (result.status !== 0) {
    throw new Error(`Querying the ${name} database failed: ${result.stderr || result.stdout}`);
  }
  return JSON.parse(result.stdout.trim() || '[]');
}

/** Lists how civil's copy of one case differs from data store's, including the SDK's own renumbering. */
export function compareCaseRows(ccdRow, civilRow, { revisionOffset = 1_000_000_000 } = {}) {
  const mismatches = [];
  if (!ccdRow?.reference) {
    mismatches.push({ field: 'reference', problem: 'missing from data store' });
  }
  if (!civilRow?.reference) {
    mismatches.push({ field: 'reference', problem: 'missing from civil-service' });
  }
  if (mismatches.length > 0) {
    return mismatches;
  }
  for (const field of COMPARED_FIELDS) {
    if (String(ccdRow[field]) !== String(civilRow[field])) {
      mismatches.push({ field, ccd: ccdRow[field], civil: civilRow[field] });
    }
  }
  if (civilRow.event_revisions_sequential !== true) {
    mismatches.push({ field: 'case_event.case_revision', problem: 'not numbered 1..N in event order' });
  }
  const expectedRevision = Number(civilRow.max_event_revision) + revisionOffset;
  if (Number(civilRow.case_revision) !== expectedRevision) {
    mismatches.push({ field: 'case_revision', civil: civilRow.case_revision, expected: expectedRevision });
  }
  return mismatches;
}
