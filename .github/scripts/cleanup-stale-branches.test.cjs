'use strict';

const assert = require('node:assert/strict');
const test = require('node:test');

const { classifyBranch, isReleaseOrHotfixBranch } = require('./cleanup-stale-branches.cjs');

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
