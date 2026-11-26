#!/usr/bin/env node
// After routing: each seeded case must be found through data store's Elasticsearch search, and the indexed copy
// must match the case as data store reads it (state and last modified). Run it again after verify-writes to
// check the decentralised indexer picks up post-cutover changes. See README.md.
import { Auth } from './lib/auth.mjs';
import { CivilApi } from './lib/civil.mjs';
import { requireSecrets, resolveConfig } from './lib/env.mjs';
import { describeError, retry } from './lib/http.mjs';
import { finishReport, loadManifest, reportFile, selectCases } from './lib/manifest.mjs';

async function checkCaseSearch(api, user, seeded) {
  const current = await api.getCase(user, seeded.caseType, seeded.caseId);
  const response = await api.searchCases(user, seeded.caseType, {
    query: { bool: { filter: [{ match: { reference: seeded.caseId } }] } },
    size: 5
  });
  const hit = (response.cases || []).find(found => String(found.id) === String(seeded.caseId));
  if (!hit) {
    throw new Error(`Case ${seeded.caseId} not found by reference search`);
  }
  if (hit.state !== current.state || hit.last_modified !== current.last_modified) {
    throw new Error(`Indexed copy is stale: state ${hit.state} last_modified ${hit.last_modified}; `
      + `data store reads state ${current.state} last_modified ${current.last_modified}`);
  }
  return { state: hit.state, lastModified: hit.last_modified };
}

async function checkGlobalSearch(api, user, seeded) {
  const response = await api.globalSearch(user, seeded.caseId);
  const hit = (response.results || []).find(found => String(found.caseReference) === String(seeded.caseId));
  if (!hit) {
    throw new Error(`Case ${seeded.caseId} not found by global search`);
  }
  return { stateId: hit.stateId };
}

async function main() {
  const config = resolveConfig();
  requireSecrets(config, ['idamClientSecret', 'civilServiceS2s']);
  const api = new CivilApi(config, new Auth(config));
  const user = config.users.applicantSolicitor;
  const global = process.env.CUTOVER_SEARCH_GLOBAL === 'true';
  const results = [];

  for (const seeded of selectCases(loadManifest(config.manifestFile), config.profileIds)) {
    const result = { profileId: seeded.profileId, caseType: seeded.caseType, caseId: seeded.caseId };
    try {
      // Indexing is asynchronous, so wait for the index to catch up with the case.
      result.search = await retry(() => checkCaseSearch(api, user, seeded), config.poll);
      if (global) {
        result.globalSearch = await retry(() => checkGlobalSearch(api, user, seeded), config.poll);
      }
      result.status = 'passed';
    } catch (error) {
      Object.assign(result, { status: 'failed', reason: error.message, error: describeError(error) });
    }
    results.push(result);
  }
  finishReport(reportFile(config, 'verify-search'), { check: 'verify-search', environment: config.name, results });
}

main().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
