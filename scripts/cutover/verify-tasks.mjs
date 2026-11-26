#!/usr/bin/env node
// After routing: every work allocation task a seeded case had before cutover must still be there. See README.md.
import { Auth } from './lib/auth.mjs';
import { CivilApi } from './lib/civil.mjs';
import { requireSecrets, resolveConfig } from './lib/env.mjs';
import { describeError } from './lib/http.mjs';
import { finishReport, loadManifest, reportFile, selectCases } from './lib/manifest.mjs';
import { missingTasks } from './lib/snapshot.mjs';

async function main() {
  const config = resolveConfig();
  const cases = selectCases(loadManifest(config.manifestFile), config.profileIds);
  const results = [];
  const skipAll = !config.urls.waTaskManagement
    ? 'no task management URL (set CUTOVER_WA_TASK_MGMT_URL)'
    : !config.secrets.xuiS2s ? 'XUI_S2S_SECRET not set' : null;
  if (!skipAll) {
    requireSecrets(config, ['idamClientSecret', 'civilServiceS2s', 'xuiS2s']);
  }
  const api = new CivilApi(config, new Auth(config));

  for (const seeded of cases) {
    const result = { profileId: seeded.profileId, caseType: seeded.caseType, caseId: seeded.caseId };
    const before = seeded.tasks || [];
    if (skipAll || before.length === 0) {
      Object.assign(result, { status: 'skipped', reason: skipAll || seeded.taskWarning || 'no tasks before cutover' });
    } else {
      try {
        const after = await api.tasks(seeded.caseId);
        const missing = missingTasks(before, after);
        Object.assign(result, {
          status: missing.length === 0 ? 'passed' : 'failed',
          reason: missing.length === 0 ? undefined : `${missing.length} task(s) missing`,
          before,
          after
        });
      } catch (error) {
        Object.assign(result, { status: 'failed', reason: error.message, error: describeError(error) });
      }
    }
    results.push(result);
  }
  finishReport(reportFile(config, 'verify-tasks'), { check: 'verify-tasks', environment: config.name, results });
}

main().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
