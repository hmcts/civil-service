const assert = require('node:assert/strict');
const test = require('node:test');
const update = require('./update-email-docs-pr.cjs');

const BASE = 'a'.repeat(40);
const PREVIOUS = 'b'.repeat(40);
const BRANCH = 'automation/email-notification-docs';
const FILES = ['docs/email-notifications.md', 'docs/email-notifications.html', '.github/email-docs-sources.json'];
const PR = { number: 42, html_url: 'https://example.invalid/pull/42', state: 'open', title: 'Old title', body: 'Old provenance' };

// All Git/API effects below are simulated. State persists across invocations to test recovery.
// Unexpected commands fail rather than silently succeeding. No child process or network client is used.
function fixture(options = {}) {
  const commands = [];
  const requests = [];
  const state = {
    base: BASE, head: options.previous ? PREVIOUS : '', parent: options.parent || BASE,
    sourceBase: options.parent || BASE, same: Boolean(options.same), noDrift: Boolean(options.noDrift),
    files: options.files || [...FILES], pr: options.existing ? { ...PR } : null,
    commits: 0, pushes: 0, failCreate: options.failCreate || 0, failUpdate: options.failUpdate || 0,
  };
  const summary = { addRaw() { return this; }, async write() {} };
  const clients = {
    context: { repo: { owner: 'hmcts', repo: 'civil-service' }, serverUrl: 'https://example.invalid', runId: 123 },
    core: { info() {}, setOutput() {}, summary },
    exec: {
      async getExecOutput(program, args) {
        assert.equal(program, 'git');
        commands.push(args);
        const success = stdout => ({ exitCode: 0, stdout: stdout || '' });
        if (args[0] === options.failCommand) return { exitCode: options.failCode || 2, stdout: '' };
        if (args[0] === 'rev-parse') {
          if (args[1] === 'HEAD') return success(state.base);
          assert.equal(args[1], `${state.head}^`);
          return success(state.parent);
        }
        if (args[0] === 'fetch') {
          assert.deepEqual(args, ['fetch', '--no-tags', 'origin', state.head]);
          return success();
        }
        if (args[0] === 'merge-base') {
          assert.deepEqual(args, ['merge-base', state.base, state.head]);
          return success(state.sourceBase);
        }
        if (args[0] === 'diff' && args[1] === '--no-renames') {
          assert.deepEqual(args, ['diff', '--no-renames', '--name-only', '-z', state.sourceBase, state.head, '--']);
          if (options.failScope) return { exitCode: 1, stdout: '' };
          return success(state.files.join('\0') + '\0');
        }
        if (args[0] === 'diff' && args[1] === '--quiet') {
          assert.deepEqual(args.slice(3), ['--', ...FILES]);
          if (args[2] === 'HEAD') return { exitCode: state.noDrift ? 0 : 1, stdout: '' };
          assert.equal(args[2], state.head);
          return { exitCode: state.same ? 0 : 1, stdout: '' };
        }
        if (args[0] === 'checkout') {
          assert.deepEqual(args, ['checkout', '-B', BRANCH]);
          return success();
        }
        if (args[0] === 'add') {
          assert.deepEqual(args, ['add', '--', ...FILES]);
          return success();
        }
        if (args[0] === '-c') {
          assert.deepEqual(args, ['-c', 'user.name=civil-service-bot', '-c',
            'user.email=civil-service-bot@users.noreply.github.com', 'commit', '--only',
            '-m', 'chore: regenerate email notification docs', '--', ...FILES]);
          state.commits += 1;
          state.localHead = String(state.commits).padStart(40, '0');
          return success();
        }
        if (args[0] === 'push') {
          assert.deepEqual(args, ['push', `--force-with-lease=refs/heads/${BRANCH}:${state.head}`,
            'origin', `HEAD:refs/heads/${BRANCH}`]);
          state.pushes += 1;
          state.head = state.localHead;
          state.parent = state.base;
          state.sourceBase = state.base;
          state.same = true;
          state.files = [...FILES];
          return success();
        }
        assert.fail(`Unexpected simulated Git command: ${JSON.stringify(args)}`);
      },
    },
    github: { rest: {
      pulls: {
        async list(args) {
          requests.push(['list', args]);
          assert.equal(args.state, 'open');
          assert.equal(args.base, 'master');
          assert.equal(args.head, `hmcts:${BRANCH}`);
          return { data: state.pr?.state === 'open' ? [{ ...state.pr }] : [] };
        },
        async create(args) {
          requests.push(['create', args]);
          if (state.failCreate > 0) {
            state.failCreate -= 1;
            throw new Error('simulated PR creation failure');
          }
          assert.notEqual(state.pr?.state, 'open');
          state.pr = { ...PR, title: args.title, body: args.body };
          return { data: { ...state.pr } };
        },
        async update(args) {
          requests.push(['update', args]);
          if (state.failUpdate > 0) {
            state.failUpdate -= 1;
            throw new Error('simulated PR update failure');
          }
          assert.equal(args.pull_number, state.pr.number);
          state.pr = { ...state.pr, ...args };
          return { data: { ...state.pr } };
        },
      },
      git: {
        async getRef() {
          if (options.refError) throw Object.assign(new Error('permission denied'), { status: 403 });
          if (!state.head) throw Object.assign(new Error('not found'), { status: 404 });
          return { data: { object: { sha: state.head } } };
        },
      },
    } },
  };
  return { clients, commands, requests, state };
}

test('no drift produces no commit, branch push, or PR', async () => {
  const { clients, commands, requests } = fixture({ noDrift: true });
  await update(clients);
  assert.deepEqual(commands.map(args => args[0]), ['rev-parse', 'diff']);
  assert.deepEqual(requests.map(([method]) => method), ['list']);
});

test('new drift commits only permitted files and pushes only the automation branch', async () => {
  const { clients, requests, state } = fixture();
  await update(clients);
  assert.equal(state.commits, 1);
  assert.equal(state.pushes, 1);
  const creation = requests.find(([method]) => method === 'create')[1];
  assert.equal(creation.base, 'master');
  assert.equal(creation.head, BRANCH);
  assert.match(creation.body, /Data rows: \d+/);
  assert.match(creation.body, /HTML SHA-256: `[0-9a-f]{64}`/);
  assert.ok(creation.body.includes(BASE));
});

test('identical generation and metadata produce no further commit, push, or PR update', async () => {
  const { clients, requests, state } = fixture();
  await update(clients);
  requests.length = 0;
  await update(clients);
  assert.equal(state.commits, 1);
  assert.equal(state.pushes, 1);
  assert.deepEqual(requests.map(([method]) => method), ['list']);
});

test('new changes update the same PR with a lease protecting its previous head', async () => {
  const { clients, commands, requests, state } = fixture({ existing: true, previous: true });
  await update(clients);
  assert.ok(commands.find(args => args[0] === 'push').includes(
    `--force-with-lease=refs/heads/${BRANCH}:${PREVIOUS}`));
  assert.ok(!requests.some(([method]) => method === 'create'));
  assert.equal(state.pr.number, PR.number);
  assert.ok(state.pr.body.includes(BASE));
});

test('master advancement refreshes the PR and scopes branch changes from their common ancestor', async () => {
  const { clients, commands, state } = fixture({ existing: true, previous: true, same: true, parent: 'c'.repeat(40) });
  await update(clients);
  const scope = commands.find(args => args[1] === '--no-renames');
  assert.equal(scope[4], 'c'.repeat(40));
  assert.equal(state.pushes, 1);
  assert.equal(state.parent, BASE);
});

test('drift resolved on master closes the obsolete PR without pushing', async () => {
  const { clients, state } = fixture({ existing: true, previous: true, noDrift: true });
  await update(clients);
  assert.equal(state.pushes, 0);
  assert.equal(state.commits, 0);
  assert.equal(state.pr.state, 'closed');
});

test('push rejection propagates without creating a misleading PR', async () => {
  const { clients, requests } = fixture({ failCommand: 'push', failCode: 1 });
  await assert.rejects(update(clients), /git push failed/);
  assert.ok(!requests.some(([method]) => method === 'create' || method === 'update'));
});

test('permission errors and Git failures are not mistaken for absent branches or normal differences', async () => {
  const denied = fixture({ refError: true });
  await assert.rejects(update(denied.clients), /permission denied/);
  for (const command of ['rev-parse', 'diff', 'fetch', 'merge-base', 'checkout', 'add', '-c']) {
    const failed = fixture({ previous: true, failCommand: command });
    await assert.rejects(update(failed.clients), /git .* failed/);
    assert.ok(!failed.requests.some(([method]) => ['create', 'update'].includes(method)));
  }
  const scopeFailure = fixture({ previous: true, failScope: true });
  await assert.rejects(update(scopeFailure.clients), /git diff failed with exit code 1/);
});

test('retry repairs PR metadata after a successful push and failed API update without another commit', async () => {
  const { clients, requests, state } = fixture({ existing: true, previous: true, failUpdate: 1 });
  await assert.rejects(update(clients), /simulated PR update failure/);
  assert.equal(state.pr.body, PR.body);
  assert.equal(state.pushes, 1);
  const pushed = state.head;
  clients.context.runId = 124;
  await update(clients);
  assert.equal(state.head, pushed);
  assert.equal(state.commits, 1);
  assert.equal(state.pushes, 1);
  assert.equal(requests.filter(([method]) => method === 'update').length, 2);
  assert.ok(state.pr.body.includes(BASE));
  assert.match(state.pr.body, /civil-ccd-definition: `[0-9a-f]{40}`/);
  assert.match(state.pr.body, /civil-general-apps-ccd-definition: `[0-9a-f]{40}`/);
  assert.match(state.pr.body, /HTML SHA-256: `[0-9a-f]{64}`/);
  assert.match(state.pr.body, /actions\/runs\/124/);
});

test('retry creates the missing PR after a successful push and failed API creation without pushing again', async () => {
  const { clients, requests, state } = fixture({ failCreate: 1 });
  await assert.rejects(update(clients), /simulated PR creation failure/);
  assert.equal(state.pr, null);
  assert.equal(state.pushes, 1);
  await update(clients);
  assert.equal(state.commits, 1);
  assert.equal(state.pushes, 1);
  assert.equal(state.pr.state, 'open');
  assert.equal(requests.filter(([method]) => method === 'create').length, 2);
});

test('unexpected branch files prevent reuse, replacement and closure, preserving the branch and PR', async () => {
  for (const options of [{ same: true }, { same: false }, { noDrift: true }]) {
    const { clients, commands, requests, state } = fixture({
      existing: true, previous: true, files: [...FILES, 'README.md'], ...options,
    });
    await assert.rejects(update(clients), /Unexpected files.*README\.md.*preserved/);
    assert.equal(state.head, PREVIOUS);
    assert.deepEqual(state.pr, PR);
    assert.ok(!commands.some(args => ['checkout', 'add', '-c', 'push'].includes(args[0])));
    assert.deepEqual(requests.map(([method]) => method), ['list']);
  }
});

test('closed or merged PRs allow creation with an already-correct branch or a deleted branch', async () => {
  for (const merged of [false, true]) {
    for (const previous of [false, true]) {
      const { clients, requests, state } = fixture({ existing: true, previous, same: true });
      state.pr.state = 'closed';
      state.pr.merged = merged;
      await update(clients);
      assert.equal(state.pr.state, 'open');
      assert.equal(state.commits, previous ? 0 : 1);
      assert.equal(state.pushes, previous ? 0 : 1);
      assert.equal(requests.filter(([method]) => method === 'create').length, 1);
    }
  }
});

test('an unchanged branch can refresh the generation-run link without committing or pushing', async () => {
  const { clients, state } = fixture();
  await update(clients);
  clients.context.runId = 125;
  await update(clients);
  assert.equal(state.commits, 1);
  assert.equal(state.pushes, 1);
  assert.match(state.pr.body, /actions\/runs\/125/);
});
