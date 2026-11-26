#!/usr/bin/env node
// Seeds the cases a cutover is checked against and records how each looks before cutover. Run it before the
// migration's CUTOVER run. See scripts/cutover/README.md.
import { Auth } from './lib/auth.mjs';
import { captureViews } from './lib/capture.mjs';
import { CivilApi } from './lib/civil.mjs';
import { requireSecrets, resolveConfig } from './lib/env.mjs';
import { describeError, retry } from './lib/http.mjs';
import { writeJson } from './lib/manifest.mjs';
import { selectProfiles } from './profiles.mjs';

async function seedCase(api, profile, seeded) {
  switch (profile.profileId) {
    case 'claim-pending-issue':
      return { caseId: await api.createSpecClaim() };
    case 'claim-issued': {
      const caseId = await api.createSpecClaim();
      await api.issueSpecClaim(caseId);
      return { caseId };
    }
    case 'ga-awaiting-judge': {
      // Applies on the issued claim when it was seeded in this run, otherwise on a claim of its own.
      let parentCaseId = seeded.find(seed => seed.profileId === profile.parentProfileId && seed.status === 'seeded')?.caseId;
      if (!parentCaseId) {
        parentCaseId = await api.createSpecClaim();
        await api.issueSpecClaim(parentCaseId);
      }
      return { caseId: await api.createGeneralApplication(parentCaseId), parentCaseId };
    }
    default:
      throw new Error(`No seed journey for profile ${profile.profileId}`);
  }
}

/**
 * Records the case's work allocation tasks. Task creation is asynchronous, so a profile that expects a task waits
 * for it. Not seeing tasks doesn't fail the seed: it means the environment's WA isn't creating them before cutover,
 * and verify-tasks then has nothing to compare.
 */
async function recordTasks(api, config, entry, profile) {
  try {
    entry.tasks = profile.expectTask
      ? await retry(async () => {
        const tasks = await api.tasks(entry.caseId);
        if (!tasks.some(task => task.type === profile.expectTask)) {
          throw new Error(`No ${profile.expectTask} task yet`);
        }
        return tasks;
      }, config.poll)
      : await api.tasks(entry.caseId);
  } catch (error) {
    entry.tasks = [];
    entry.taskWarning = error.message;
    console.warn(`No tasks recorded for ${entry.profileId}: ${error.message}`);
  }
}

async function main() {
  const config = resolveConfig();
  requireSecrets(config, ['idamClientSecret', 'civilServiceS2s']);
  const api = new CivilApi(config, new Auth(config));
  const selected = selectProfiles(config.profileIds);
  const manifest = {
    manifestVersion: 1,
    environment: config.name,
    prId: config.prId,
    urls: config.urls,
    seededAt: new Date().toISOString(),
    cases: []
  };

  console.log(`Seeding ${selected.length} case(s) in ${config.name} through ${config.urls.dataStore}`);
  for (const profile of selected) {
    const entry = { profileId: profile.profileId, caseType: profile.caseType, expectedState: profile.expectedState };
    try {
      Object.assign(entry, await seedCase(api, profile, manifest.cases));
      entry.status = 'seeded';
      console.log(`Seeded ${profile.profileId}: ${profile.caseType} ${entry.caseId}`);
    } catch (error) {
      Object.assign(entry, { status: 'failed', error: describeError(error) });
      console.error(`Failed to seed ${profile.profileId}: ${error.message}`);
    }
    manifest.cases.push(entry);
    writeJson(config.manifestFile, manifest);
  }

  // Snapshot only once every journey has run: the GA journey changes its parent claim.
  for (const entry of manifest.cases.filter(seeded => seeded.status === 'seeded')) {
    try {
      entry.views = await captureViews(api, config, entry.caseType, entry.caseId);
      const observedState = Object.values(entry.views).find(view => view.access)?.state;
      if (observedState !== entry.expectedState) {
        throw new Error(`Seeded case is in ${observedState}, expected ${entry.expectedState}`);
      }
      if (config.urls.waTaskManagement && config.secrets.xuiS2s) {
        await recordTasks(api, config, entry, selected.find(profile => profile.profileId === entry.profileId));
      }
    } catch (error) {
      Object.assign(entry, { status: 'failed', error: describeError(error) });
      console.error(`Failed to snapshot ${entry.profileId}: ${error.message}`);
    }
  }
  manifest.status = manifest.cases.every(seeded => seeded.status === 'seeded') ? 'complete' : 'incomplete';
  writeJson(config.manifestFile, manifest);
  console.log(`Manifest (${manifest.status}): ${config.manifestFile}`);
  if (manifest.status !== 'complete') {
    process.exitCode = 1;
  }
}

main().catch(error => {
  console.error(error);
  process.exitCode = 1;
});
