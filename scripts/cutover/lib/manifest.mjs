import fs from 'node:fs';
import path from 'node:path';

export function writeJson(file, value) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, `${JSON.stringify(value, null, 2)}\n`);
}

export function loadManifest(file) {
  if (!fs.existsSync(file)) {
    throw new Error(`Seed manifest not found: ${file}. Run npm run cutover:seed first.`);
  }
  const manifest = JSON.parse(fs.readFileSync(file, 'utf8'));
  if (!Array.isArray(manifest.cases)) {
    throw new Error(`Seed manifest has no cases array: ${file}`);
  }
  return manifest;
}

/** The seeded cases to check: those that seeded, narrowed to profileIds when given. */
export function selectCases(manifest, profileIds = []) {
  const unknown = profileIds.filter(id => !manifest.cases.some(seeded => seeded.profileId === id));
  if (unknown.length > 0) {
    throw new Error(`CUTOVER_PROFILE_IDS not in the manifest: ${unknown.join(', ')}`);
  }
  return manifest.cases.filter(seeded => seeded.status === 'seeded'
    && (profileIds.length === 0 || profileIds.includes(seeded.profileId)));
}

export function reportFile(config, name) {
  return path.join(config.outputDir, `${name}-report.json`);
}

/**
 * Writes the report and sets a failing exit code if any result failed, so each script can run as a gate.
 */
export function finishReport(file, report) {
  const failed = report.results.filter(result => result.status === 'failed').length;
  const finished = {
    ...report,
    generatedAt: new Date().toISOString(),
    status: failed === 0 ? 'passed' : 'failed',
    passedCount: report.results.filter(result => result.status === 'passed').length,
    failedCount: failed,
    skippedCount: report.results.filter(result => result.status === 'skipped').length
  };
  writeJson(file, finished);
  for (const result of report.results) {
    const label = `${result.profileId} ${result.caseType} ${result.caseId}`;
    console.log(`${result.status.toUpperCase().padEnd(7)} ${label}${result.reason ? ` (${result.reason})` : ''}`);
  }
  console.log(`${finished.status.toUpperCase()}: ${finished.passedCount} passed, ${failed} failed, `
    + `${finished.skippedCount} skipped. Report: ${file}`);
  if (failed > 0) {
    process.exitCode = 1;
  }
  return finished;
}
