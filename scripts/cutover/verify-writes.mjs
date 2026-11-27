#!/usr/bin/env node
// After routing, and after verify-cases: changes the seeded cases through the decentralised runtime, then creates
// and issues a new claim. This changes the cases, so run verify-cases first. See README.md.
import { Auth } from './lib/auth.mjs';
import { CIVIL, CivilApi } from './lib/civil.mjs';
import { requireSecrets, resolveConfig } from './lib/env.mjs';
import { describeError } from './lib/http.mjs';
import { finishReport, loadManifest, reportFile, selectCases } from './lib/manifest.mjs';
import { profiles } from './profiles.mjs';

const WRITES = {
  'initiate-ga': async (api, caseId) => ({ generalApplicationCaseId: await api.createGeneralApplication(caseId) }),
  'notice-of-change': (api, caseId) => api.noticeOfChange(caseId)
};

async function run(result, fn) {
  try {
    Object.assign(result, await fn(), { status: 'passed' });
  } catch (error) {
    Object.assign(result, { status: 'failed', reason: error.message, error: describeError(error) });
  }
  return result;
}

async function main() {
  const config = resolveConfig();
  requireSecrets(config, ['idamClientSecret', 'civilServiceS2s']);
  const api = new CivilApi(config, new Auth(config));
  const results = [];

  for (const seeded of selectCases(loadManifest(config.manifestFile), config.profileIds)) {
    const { writes = [] } = profiles.find(profile => profile.profileId === seeded.profileId) || {};
    for (const write of writes) {
      results.push(await run(
        { profileId: `${seeded.profileId}:${write}`, caseType: seeded.caseType, caseId: seeded.caseId },
        () => WRITES[write](api, seeded.caseId)
      ));
    }
  }

  if (process.env.CUTOVER_SKIP_NEW_CLAIM !== 'true') {
    const result = { profileId: 'new-claim', caseType: CIVIL, caseId: null };
    results.push(await run(result, async () => {
      // Recorded before issuing, so a failed issue still names the case.
      result.caseId = await api.createSpecClaim();
      await api.issueSpecClaim(result.caseId);
      return {};
    }));
  }
  finishReport(reportFile(config, 'verify-writes'), { check: 'verify-writes', environment: config.name, results });
}

main().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
