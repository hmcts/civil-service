'use strict';

const assert = require('node:assert/strict');
const test = require('node:test');

const { classifyBranch, isReleaseOrHotfixBranch, parseExceptions } = require('./cleanup-stale-branches.cjs');

const cutoff = new Date('2026-01-01T00:00:00Z');
const staleBranch = overrides => ({
  name: 'feature/old-work',
  protected: false,
  lastCommitAt: '2025-01-01T00:00:00Z',
  openPullRequests: 0,
  ...overrides
});

test('allows an unprotected stale branch without an open pull request', () => {
  assert.deepEqual(
    classifyBranch(staleBranch(), { defaultBranch: 'master', cutoff }),
    { eligible: true, reason: 'inactive beyond retention period' }
  );
});

test('excludes the default branch', () => {
  assert.equal(classifyBranch(staleBranch({ name: 'master' }), { defaultBranch: 'master', cutoff }).eligible, false);
});

test('excludes protected branches', () => {
  assert.equal(classifyBranch(staleBranch({ protected: true }), { defaultBranch: 'master', cutoff }).eligible, false);
});

test('excludes release and hotfix branch prefixes', () => {
  for (const name of ['release/2026.1', 'release-candidate', 'hotfix/urgent', 'HOTFIX_urgent']) {
    assert.equal(isReleaseOrHotfixBranch(name), true, name);
    assert.equal(classifyBranch(staleBranch({ name }), { defaultBranch: 'master', cutoff }).eligible, false, name);
  }
});

test('does not classify a feature branch containing release as a release branch', () => {
  assert.equal(isReleaseOrHotfixBranch('feature/release-cleanup'), false);
});

test('excludes branches with open pull requests', () => {
  assert.equal(classifyBranch(staleBranch({ openPullRequests: 1 }), { defaultBranch: 'master', cutoff }).eligible, false);
});

test('excludes branches with recent activity', () => {
  assert.equal(classifyBranch(staleBranch({ lastCommitAt: '2026-01-02T00:00:00Z' }), { defaultBranch: 'master', cutoff }).eligible, false);
});

test('parses exact branch names, comments, blank lines and Windows line endings', () => {
  assert.deepEqual([...parseExceptions('# Keep these\r\n  feature/old-work  \r\n\r\ndemo\r\ndemo\r\n')],
    ['feature/old-work', 'demo']);
});

test('excludes a stale branch listed as an exception', () => {
  assert.deepEqual(classifyBranch(staleBranch(), {
    defaultBranch: 'master', cutoff, exceptions: parseExceptions('feature/old-work')
  }), { eligible: false, reason: 'exception list' });
});

test('exceptions require an exact case-sensitive name and do not expand wildcards', () => {
  for (const name of ['feature/old', 'FEATURE/OLD-WORK', 'feature/*']) {
    assert.equal(classifyBranch(staleBranch(), {
      defaultBranch: 'master', cutoff, exceptions: parseExceptions(name)
    }).eligible, true, name);
  }
});

test('manual and scheduled modes load the exception file before deleting branches', async () => {
  const { mkdtempSync, mkdirSync, copyFileSync, writeFileSync, rmSync } = require('node:fs');
  const { tmpdir } = require('node:os');
  const { join } = require('node:path');
  const directory = mkdtempSync(join(tmpdir(), 'branch-exceptions-'));
  try {
    mkdirSync(join(directory, 'scripts'));
    copyFileSync(__filename.replace('.test.cjs', '.cjs'), join(directory, 'scripts/cleanup.cjs'));
    writeFileSync(join(directory, 'stale-branch-exceptions.txt'), 'feature/keep\n');
    const { run: runFixture } = require(join(directory, 'scripts/cleanup.cjs'));
    for (const dryRun of [true, false]) {
      const deleted = [];
      const lists = [];
      const summary = {
        addHeading() { return this; }, addTable() { return this; },
        addList(names) { lists.push(names); return this; }, addRaw() { return this; },
        async write() {}
      };
      const github = {
        rest: {
          repos: { get: async () => ({ data: { default_branch: 'master' } }), listBranches() {} },
          git: { deleteRef: async ({ ref }) => deleted.push(ref) }
        },
        paginate: async () => ['feature/keep', 'feature/remove'].map(name => ({ name, protected: false })),
        graphql: async () => ({ repository: { refs: {
          pageInfo: { hasNextPage: false, endCursor: null },
          nodes: ['feature/keep', 'feature/remove'].map(name => ({
            name, target: { committedDate: '2000-01-01T00:00:00Z' },
            associatedPullRequests: { totalCount: 0 }
          }))
        } } })
      };
      await runFixture({ github, context: { repo: { owner: 'test', repo: 'test' } },
        core: { summary, info() {} }, dryRun, daysBefore: 90 });
      assert.deepEqual(deleted, dryRun ? [] : ['heads/feature/remove']);
      assert.deepEqual(lists, [['feature/remove'], ['feature/keep']]);
    }
  } finally {
    rmSync(directory, { recursive: true, force: true });
  }
});
