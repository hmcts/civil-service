#!/usr/bin/env node
// Compares the seeded cases in data store's database with civil-service's ccd schema. Run it after the
// migration's CUTOVER run completes and before routing data store to civil-service. See README.md.
import { caseRowsQuery, compareCaseRows, queryRows } from './lib/db.mjs';
import { resolveConfig } from './lib/env.mjs';
import { finishReport, loadManifest, reportFile, selectCases } from './lib/manifest.mjs';

function database(name, prefix, defaultSchema) {
  const url = process.env[`${prefix}_URL`];
  const password = process.env[`${prefix}_PASSWORD`];
  if (!url || !password) {
    throw new Error(`Set ${prefix}_URL and ${prefix}_PASSWORD for the ${name} database. See README.md.`);
  }
  return { name, url, password, schema: process.env[`${prefix}_SCHEMA`] || defaultSchema };
}

function main() {
  const config = resolveConfig();
  const cases = selectCases(loadManifest(config.manifestFile), config.profileIds);
  const ccd = database('data store', 'CUTOVER_CCD_DB', 'public');
  const civil = database('civil-service', 'CUTOVER_CIVIL_DB', 'ccd');
  const revisionOffset = Number(process.env.CUTOVER_CASE_REVISION_OFFSET || 1_000_000_000);
  const references = cases.map(seeded => seeded.caseId);
  if (references.length === 0) {
    throw new Error('The manifest has no seeded cases to check');
  }

  const index = rows => new Map(rows.map(row => [row.requested_reference, row]));
  const ccdRows = index(queryRows(ccd, caseRowsQuery(ccd.schema, references)));
  const civilRows = index(queryRows(civil, caseRowsQuery(civil.schema, references)));

  const results = cases.map(seeded => {
    const mismatches = compareCaseRows(ccdRows.get(seeded.caseId), civilRows.get(seeded.caseId), { revisionOffset });
    return {
      profileId: seeded.profileId,
      caseType: seeded.caseType,
      caseId: seeded.caseId,
      status: mismatches.length === 0 ? 'passed' : 'failed',
      reason: mismatches.map(mismatch => mismatch.field).join(', ') || undefined,
      mismatches,
      ccd: ccdRows.get(seeded.caseId) || null,
      civil: civilRows.get(seeded.caseId) || null
    };
  });
  finishReport(reportFile(config, 'check-db'), { check: 'check-db', environment: config.name, results });
}

try {
  main();
} catch (error) {
  console.error(error.message);
  process.exitCode = 1;
}
