const assert = require('node:assert/strict');
const test = require('node:test');
const fs = require('node:fs');
const path = require('node:path');

// Execute the actual inline workflow script with simulated API and filesystem
// clients. No checked-out helper, child process or real network client is used.
const workflow = fs.readFileSync(path.join(__dirname, '../workflows/deploy-email-docs.yml'), 'utf8');
assert.ok(!workflow.includes('actions/checkout@'), 'Publication must not check out repository code');
const scriptMarker = '          script: |\n';
assert.equal(workflow.split(scriptMarker).length, 2, 'Expected one inline publication script');
const scriptLines = [];
for (const line of workflow.split(scriptMarker)[1].split('\n')) {
  if (line.trim() && !line.startsWith('            ')) break;
  scriptLines.push(line.slice(12));
}
const AsyncFunction = Object.getPrototypeOf(async function () {}).constructor;
const execute = new AsyncFunction('github', 'context', 'core', 'require', scriptLines.join('\n'));
const select = clients => execute(clients.github, clients.context, clients.core, clients.require);

const TREE = 'd'.repeat(40);
function fixture({ manual = false } = {}) {
  const run = { id: 12, workflow_id: 34, head_sha: 'a'.repeat(40), head_branch: 'master',
    conclusion: 'success', event: 'push', head_repository: { full_name: 'hmcts/civil-service' } };
  const state = {
    runs: [run], path: '.github/workflows/email-notifications.yml',
    artefacts: [{ id: 56, name: `verified-email-docs-${TREE}`, expired: false }], calls: [],
    written: new Map(), outputs: new Map(), rootTruncated: false, docsTruncated: false,
    root: [{ path: 'docs', type: 'tree', sha: TREE }], entries: [],
  };
  const actions = {
    listWorkflowRuns: Symbol('runs'), listWorkflowRunArtifacts: Symbol('artefacts'),
    async getWorkflow(args) { state.calls.push(['workflow', args]); return { data: { path: state.path } }; },
    async downloadArtifact(args) {
      state.calls.push(['download', args]);
      assert.equal(args.artifact_id, 56); assert.equal(args.archive_format, 'zip');
      return { data: Buffer.from('simulated ZIP') };
    },
  };
  const summary = { addRaw() { return this; }, async write() {} };
  const clients = {
    context: { repo: { owner: 'hmcts', repo: 'civil-service' }, sha: 'b'.repeat(40),
      ref: 'refs/heads/master', eventName: manual ? 'workflow_dispatch' : 'workflow_run',
      payload: { workflow_run: run } },
    core: { info() {}, setOutput(name, value) { state.outputs.set(name, value); }, summary },
    require(name) {
      assert.equal(name, 'node:fs');
      return { writeFileSync(file, content) { state.written.set(path.basename(file), content); } };
    },
    github: { rest: { actions,
      repos: { async getBranch(args) {
        state.calls.push(['branch', args]);
        assert.deepEqual(args, { owner: 'hmcts', repo: 'civil-service', branch: 'master' });
        return { data: { commit: { sha: 'b'.repeat(40) } } };
      } },
      git: {
        async getCommit(args) {
          state.calls.push(['commit', args]);
          assert.equal(args.commit_sha, 'b'.repeat(40));
          return { data: { tree: { sha: 'c'.repeat(40) } } };
        },
        async getTree(args) {
          state.calls.push(['tree', args]);
          if (args.tree_sha === TREE) {
            assert.equal(args.recursive, 'true');
            return { data: { tree: state.entries, truncated: state.docsTruncated } };
          }
          assert.equal(args.tree_sha, 'c'.repeat(40));
          return { data: { tree: state.root, truncated: state.rootTruncated } };
        },
      },
    }, async paginate(method, args) {
      state.calls.push([method, args]);
      assert.equal(args.owner, 'hmcts'); assert.equal(args.repo, 'civil-service');
      if (method === actions.listWorkflowRuns) {
        assert.equal(args.workflow_id, 'email-notifications.yml');
        assert.equal(args.branch, 'master'); assert.equal(args.status, 'success');
        assert.match(args.created, /^>=/);
        return state.runs;
      }
      assert.equal(method, actions.listWorkflowRunArtifacts);
      assert.ok(state.runs.some(item => item.id === args.run_id));
      return state.artefacts;
    } },
  };
  return { clients, state, run };
}

test('publication selects the verified complete docs tree despite unrelated master advancement', async () => {
  for (const manual of [false, true]) {
    const { clients, state, run } = fixture({ manual });
    assert.notEqual(clients.context.sha, run.head_sha);
    assert.deepEqual(await select(clients), { artefactId: 56, runId: 12, sha: run.head_sha });
    assert.equal(state.outputs.get('found'), 'true');
    assert.deepEqual(JSON.parse(state.written.get('email-docs-tree.json')), state.entries);
    assert.equal(state.written.get('email-docs.zip').toString(), 'simulated ZIP');
  }
});

test('automatic publication skips source-only runs, expired artefacts and different docs trees', async () => {
  for (const artefacts of [[], [{ id: 56, name: `verified-email-docs-${TREE}`, expired: true }],
    [{ id: 56, name: `verified-email-docs-${'e'.repeat(40)}`, expired: false }]]) {
    const { clients, state } = fixture(); state.artefacts = artefacts;
    assert.equal(await select(clients), null);
  }
});

test('manual publication refuses missing or expired verification instead of uploading unchecked docs', async () => {
  for (const expired of [false, true]) {
    const { clients, state } = fixture({ manual: true });
    state.artefacts = expired ? [{ ...state.artefacts[0], expired: true }] : [];
    await assert.rejects(select(clients), /No retained.*Verify email notification documentation/);
  }
});

test('PR, fork, non-master and failed runs can never supply publication artefacts', async () => {
  for (const change of [{ event: 'pull_request' }, { head_branch: 'feature' }, { conclusion: 'failure' },
    { head_repository: { full_name: 'fork/civil-service' } }]) {
    for (const manual of [false, true]) {
      const { clients, state, run } = fixture({ manual }); Object.assign(run, change);
      if (manual) await assert.rejects(select(clients), /No retained/);
      else assert.equal(await select(clients), null);
      assert.ok(!state.calls.some(([method]) => method === 'workflow'));
    }
  }
});

test('a different workflow cannot supply the artefact even with a matching name', async () => {
  const { clients, state } = fixture(); state.path = '.github/workflows/ci.yml';
  assert.equal(await select(clients), null);
  assert.ok(!state.calls.some(([method]) => method === 'download'));
});

test('manual recovery searches earlier successful runs when a newer run has no retained artefact', async () => {
  const { clients, state, run } = fixture({ manual: true });
  state.runs.unshift({ ...run, id: 99 });
  const paginate = clients.github.paginate;
  clients.github.paginate = async (method, args) => args.run_id === 99 ? [] : paginate(method, args);
  assert.equal((await select(clients)).runId, 12);
});

test('API failures propagate and unsupported publication requests fail', async () => {
  const { clients } = fixture();
  clients.github.rest.actions.getWorkflow = async () => { throw new Error('permission denied'); };
  await assert.rejects(select(clients), /permission denied/);
  for (const manual of [false, true]) {
    const otherBranch = fixture({ manual }).clients;
    otherBranch.context.ref = 'refs/heads/feature';
    await assert.rejects(select(otherBranch), /only be published from hmcts\/civil-service master/);
  }
  const fork = fixture({ manual: true }).clients; fork.context.repo.owner = 'fork';
  await assert.rejects(select(fork), /only be published from hmcts\/civil-service master/);
});

test('missing or truncated Git trees stop publication before any archive download', async () => {
  for (const changes of [{ root: [] }, { rootTruncated: true }, { docsTruncated: true }]) {
    const { clients, state } = fixture(); Object.assign(state, changes);
    await assert.rejects(select(clients), /no docs directory|response is incomplete/);
    assert.ok(!state.calls.some(([method]) => method === 'download'));
    assert.equal(state.outputs.size, 0);
  }
});
