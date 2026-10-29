const assert = require('node:assert/strict');
const test = require('node:test');
const select = require('./email-docs-artifact.cjs');

const TREE = 'd'.repeat(40);
function fixture({ manual = false } = {}) {
  const run = { id: 12, workflow_id: 34, head_sha: 'a'.repeat(40), head_branch: 'master',
    conclusion: 'success', event: 'push', head_repository: { full_name: 'hmcts/civil-service' } };
  const state = {
    runs: [run], path: '.github/workflows/email-notifications.yml',
    artefacts: [{ id: 56, name: `verified-email-docs-${TREE}`, expired: false }], calls: [],
  };
  const actions = {
    listWorkflowRuns: Symbol('runs'), listWorkflowRunArtifacts: Symbol('artefacts'),
    async getWorkflow(args) { state.calls.push(['workflow', args]); return { data: { path: state.path } }; },
  };
  const clients = {
    docsTree: TREE,
    context: { repo: { owner: 'hmcts', repo: 'civil-service' }, sha: 'b'.repeat(40),
      ref: 'refs/heads/master', eventName: manual ? 'workflow_dispatch' : 'workflow_run',
      payload: { workflow_run: run } },
    core: { info() {} },
    github: { rest: { actions }, async paginate(method, args) {
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
    const { clients, run } = fixture({ manual });
    assert.notEqual(clients.context.sha, run.head_sha);
    assert.deepEqual(await select(clients), { artefactId: 56, runId: 12, sha: run.head_sha });
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
  assert.equal(state.calls.length, 1);
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
    await assert.rejects(select(otherBranch), /only be published from master/);
  }
  const malformed = fixture().clients; malformed.docsTree = 'HEAD:docs';
  await assert.rejects(select(malformed), /tree SHA/);
});
