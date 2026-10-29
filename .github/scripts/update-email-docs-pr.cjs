// Runs only in the trusted master updater, using the repository's github-script action.
// The injected clients allow lifecycle tests to run without GitHub or network access.
const fs = require('node:fs');
const crypto = require('node:crypto');

const BRANCH = 'automation/email-notification-docs';
const FILES = [
  'docs/email-notifications.md',
  'docs/email-notifications.html',
  '.github/email-docs-sources.json',
];

async function update({ github, context, exec, core }) {
  const repo = context.repo;
  async function git(args, allowDifference = false) {
    const result = await exec.getExecOutput('git', args, { ignoreReturnCode: true });
    if (result.exitCode !== 0 && !(allowDifference && result.exitCode === 1)) {
      throw new Error(`git ${args[0]} failed with exit code ${result.exitCode}`);
    }
    return result;
  }
  async function link(pr) {
    core.setOutput('pull-request-url', pr.html_url);
    await core.summary.addRaw(`Email documentation PR: ${pr.html_url}\n`).write();
  }
  const prs = await github.rest.pulls.list({
    ...repo, state: 'open', base: 'master', head: `${repo.owner}:${BRANCH}`,
  });
  const existing = prs.data[0];
  const base = (await git(['rev-parse', 'HEAD'])).stdout.trim();
  let previous = '';
  try {
    previous = (await github.rest.git.getRef({ ...repo, ref: `heads/${BRANCH}` })).data.object.sha;
  } catch (error) {
    if (error.status !== 404) throw error;
  }
  if (previous) {
    await git(['fetch', '--no-tags', 'origin', previous]);
    // Compare the branch's own changes, excluding subsequent changes on master.
    // Check all paths before accepting, replacing, or closing an existing update.
    const sourceBase = (await git(['merge-base', base, previous])).stdout.trim();
    const changes = await git(['diff', '--no-renames', '--name-only', '-z', sourceBase, previous, '--']);
    const unexpected = changes.stdout.split('\0').filter(path => path && !FILES.includes(path));
    if (unexpected.length) {
      throw new Error(`Unexpected files on ${BRANCH}: ${JSON.stringify(unexpected)}. `
        + 'The branch and PR have been preserved for investigation.');
    }
  }

  const difference = await git(['diff', '--quiet', 'HEAD', '--', ...FILES], true);
  if (difference.exitCode === 0) {
    if (existing) {
      await github.rest.pulls.update({ ...repo, pull_number: existing.number, state: 'closed' });
    }
    core.info('Documentation and CCD revisions match master; no update is needed.');
    return;
  }

  let branchCurrent = false;
  if (previous) {
    const parent = (await git(['rev-parse', `${previous}^`])).stdout.trim();
    const unchanged = await git(['diff', '--quiet', previous, '--', ...FILES], true);
    branchCurrent = parent === base && unchanged.exitCode === 0;
  }

  if (!branchCurrent) {
    await git(['checkout', '-B', BRANCH]);
    await git(['add', '--', ...FILES]);
    await git(['-c', 'user.name=civil-service-bot', '-c',
      'user.email=civil-service-bot@users.noreply.github.com', 'commit', '--only',
      '-m', 'chore: regenerate email notification docs', '--', ...FILES]);
    // An explicit lease also protects against updates made outside this workflow.
    await git(['push', `--force-with-lease=refs/heads/${BRANCH}:${previous}`,
      'origin', `HEAD:refs/heads/${BRANCH}`]);
  }

  const sources = JSON.parse(fs.readFileSync(FILES[2], 'utf8'));
  const rows = fs.readFileSync(FILES[0], 'utf8').split('\n').filter(line => line.startsWith('|')).length - 2;
  const checksum = crypto.createHash('sha256').update(fs.readFileSync(FILES[1])).digest('hex');
  const body = [
    'Refresh the notification matrix from the current application and CCD definitions.', '',
    `civil-service source: \`${base}\``,
    ...Object.entries(sources).map(([name, sha]) => `${name}: \`${sha}\``), '',
    `Data rows: ${rows}`, `HTML SHA-256: \`${checksum}\``, '',
    `Generation and tests: ${context.serverUrl}/${repo.owner}/${repo.repo}/actions/runs/${context.runId}`, '',
    'Both generated files must pass the documentation check before normal review and merge.',
  ].join('\n');
  const title = 'chore: refresh email notification documentation';
  if (existing && existing.title === title && existing.body === body) {
    await link(existing);
    return;
  }
  // A previous run may have pushed successfully before its API request failed.
  // Reconcile the PR even when its branch already has the required content.
  const result = existing
    ? await github.rest.pulls.update({ ...repo, pull_number: existing.number, title, body })
    : await github.rest.pulls.create({ ...repo, base: 'master', head: BRANCH, title, body });
  await link(result.data);
}

module.exports = update;
