#!/usr/bin/env node
// After routing data store to civil-service: each seeded case must look to each user exactly as it did before
// cutover (state, data, history, the events offered and what starting each one returns). See README.md.
import { Auth } from './lib/auth.mjs';
import { captureViews } from './lib/capture.mjs';
import { CivilApi } from './lib/civil.mjs';
import { requireSecrets, resolveConfig } from './lib/env.mjs';
import { describeError } from './lib/http.mjs';
import { finishReport, loadManifest, reportFile, selectCases } from './lib/manifest.mjs';
import { diffViews } from './lib/snapshot.mjs';

async function main() {
  const config = resolveConfig();
  requireSecrets(config, ['idamClientSecret', 'civilServiceS2s']);
  const api = new CivilApi(config, new Auth(config));
  const startEvents = process.env.CUTOVER_SKIP_EVENT_STARTS !== 'true';
  const allowNewEvents = process.env.CUTOVER_ALLOW_NEW_EVENTS === 'true';
  const results = [];

  for (const seeded of selectCases(loadManifest(config.manifestFile), config.profileIds)) {
    const result = { profileId: seeded.profileId, caseType: seeded.caseType, caseId: seeded.caseId };
    try {
      const views = await captureViews(api, config, seeded.caseType, seeded.caseId, { startEvents });
      const differences = diffViews(seeded.views, views, { allowNewEvents });
      Object.assign(result, {
        status: differences.length === 0 ? 'passed' : 'failed',
        reason: [...new Set(differences.map(difference => `${difference.role}.${difference.check}`))].join(', ')
          || undefined,
        differences
      });
    } catch (error) {
      Object.assign(result, { status: 'failed', reason: error.message, error: describeError(error) });
    }
    results.push(result);
  }
  finishReport(reportFile(config, 'verify-cases'), { check: 'verify-cases', environment: config.name, results });
}

main().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
