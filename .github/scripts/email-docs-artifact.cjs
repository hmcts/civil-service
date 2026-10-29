// Select only artefacts produced by successful verification of master's exact docs tree.
// Clients are injected so selection and failure handling can be tested offline.
module.exports = async function select({ github, context, core, docsTree }) {
  if (!/^[0-9a-f]{40}$/.test(docsTree)) throw new Error('Expected the committed docs tree SHA.');
  if (context.ref !== 'refs/heads/master') {
    throw new Error('Email documentation can only be published from master verification.');
  }
  const repo = context.repo;
  const repository = `${repo.owner}/${repo.repo}`;
  const workflow = 'email-notifications.yml';
  const trusted = run => run.conclusion === 'success' && run.head_branch === 'master'
    && run.head_repository?.full_name === repository
    && ['push', 'workflow_dispatch'].includes(run.event);
  let runs;
  if (context.eventName === 'workflow_run') {
    const run = context.payload.workflow_run;
    if (!trusted(run)) {
      core.info('Verification does not describe a successful master run; nothing to publish.');
      return null;
    }
    runs = [run];
  } else if (context.eventName === 'workflow_dispatch') {
    // Identical docs can be republished after unrelated commits on master, without
    // requiring the CCD inputs again. Artefacts are retained for up to 90 days.
    runs = await github.paginate(github.rest.actions.listWorkflowRuns, {
      ...repo, workflow_id: workflow, branch: 'master', status: 'success', per_page: 100,
      created: `>=${new Date(Date.now() - 90 * 24 * 60 * 60 * 1000).toISOString()}`,
    });
  } else {
    throw new Error('Email documentation can only be published from master verification.');
  }
  for (const run of runs.filter(trusted)) {
    // Verify the workflow identity as well as the run's branch/repository.
    const definition = await github.rest.actions.getWorkflow({ ...repo, workflow_id: run.workflow_id });
    if (definition.data.path !== `.github/workflows/${workflow}`) continue;
    const artefacts = await github.paginate(github.rest.actions.listWorkflowRunArtifacts, {
      ...repo, run_id: run.id, per_page: 100,
    });
    const artefact = artefacts.find(item => !item.expired && item.name === `verified-email-docs-${docsTree}`);
    if (artefact) return { artefactId: artefact.id, runId: run.id, sha: run.head_sha };
  }
  if (context.eventName === 'workflow_run') {
    core.info('This verification run produced no strictly validated documentation artefact; nothing to publish.');
    return null;
  }
  throw new Error('No retained, strictly verified artefact matches the complete docs directory on master. '
    + 'Run Verify email notification documentation on master successfully, then retry publication.');
};
