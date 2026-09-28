'use strict';

const DEFAULT_DAYS_BEFORE_CLEANUP = 90;

function isReleaseOrHotfixBranch(name) {
  return /^(release|hotfix)([/_.-]|$)/i.test(name);
}

function classifyBranch(branch, { defaultBranch, cutoff }) {
  if (branch.name === defaultBranch) {
    return { eligible: false, reason: 'default branch' };
  }

  if (branch.protected) {
    return { eligible: false, reason: 'protected branch' };
  }

  if (isReleaseOrHotfixBranch(branch.name)) {
    return { eligible: false, reason: 'release/hotfix branch' };
  }

  if (branch.openPullRequests > 0) {
    return { eligible: false, reason: 'open pull request' };
  }

  if (!branch.lastCommitAt || new Date(branch.lastCommitAt) > cutoff) {
    return { eligible: false, reason: 'recent activity' };
  }

  return { eligible: true, reason: 'inactive beyond retention period' };
}

async function getBranchData(github, owner, repo) {
  const protectedBranches = await github.paginate(github.rest.repos.listBranches, {
    owner,
    repo,
    per_page: 100
  });
  const protectedByName = new Map(
    protectedBranches.map(branch => [branch.name, branch.protected])
  );

  const branches = [];
  let endCursor = null;
  let hasNextPage = true;

  while (hasNextPage) {
    const result = await github.graphql(
      `query($owner: String!, $repo: String!, $endCursor: String) {
        repository(owner: $owner, name: $repo) {
          refs(refPrefix: "refs/heads/", first: 100, after: $endCursor) {
            pageInfo { hasNextPage endCursor }
            nodes {
              name
              target {
                ... on Commit { committedDate }
              }
              associatedPullRequests(first: 1, states: OPEN) {
                totalCount
              }
            }
          }
        }
      }`,
      { owner, repo, endCursor }
    );

    const refs = result.repository.refs;
    branches.push(...refs.nodes.map(branch => ({
      name: branch.name,
      protected: protectedByName.get(branch.name) === true,
      lastCommitAt: branch.target?.committedDate ?? null,
      openPullRequests: branch.associatedPullRequests.totalCount
    })));
    ({ hasNextPage, endCursor } = refs.pageInfo);
  }

  return branches;
}

function parseDays(value) {
  const days = Number(value || DEFAULT_DAYS_BEFORE_CLEANUP);
  if (!Number.isInteger(days) || days < 1) {
    throw new Error(`days_before must be a positive integer, got ${value}`);
  }
  return days;
}

async function run({ github, context, core, dryRun, daysBefore }) {
  const { owner, repo } = context.repo;
  const repository = await github.rest.repos.get({ owner, repo });
  const days = parseDays(daysBefore);
  const cutoff = new Date(Date.now() - days * 24 * 60 * 60 * 1000);
  const branches = await getBranchData(github, owner, repo);
  const classified = branches.map(branch => ({
    ...branch,
    classification: classifyBranch(branch, {
      defaultBranch: repository.data.default_branch,
      cutoff
    })
  }));
  const eligible = classified.filter(branch => branch.classification.eligible);
  const removed = [];

  for (const branch of eligible) {
    if (!dryRun) {
      await github.rest.git.deleteRef({
        owner,
        repo,
        ref: `heads/${branch.name}`
      });
      removed.push(branch.name);
    }
  }

  const action = dryRun ? 'would be removed' : 'removed';
  const summaryRows = [
    ['Repository', `${owner}/${repo}`],
    ['Mode', dryRun ? 'dry-run' : 'delete'],
    ['Inactivity threshold', `${days} days`],
    ['Cutoff', cutoff.toISOString()],
    ['Branches inspected', String(branches.length)],
    ['Eligible branches', String(eligible.length)],
    ['Branches removed', String(removed.length)]
  ];

  core.summary
    .addHeading(`Stale branch cleanup: ${dryRun ? 'dry-run' : 'delete'}`)
    .addTable([['Metric', 'Value'], ...summaryRows])
    .addHeading(`Branches ${action}`)
    .addList((dryRun ? eligible.map(branch => branch.name) : removed).slice(0, 500));

  if ((dryRun ? eligible.length : removed.length) > 500) {
    core.summary.addRaw(`Only the first 500 branch names are listed. Total: ${dryRun ? eligible.length : removed.length}.`);
  }

  await core.summary.write();
  core.info(`${dryRun ? 'Would remove' : 'Removed'} ${dryRun ? eligible.length : removed.length} branches from ${owner}/${repo}`);
}

module.exports = {
  DEFAULT_DAYS_BEFORE_CLEANUP,
  classifyBranch,
  isReleaseOrHotfixBranch,
  parseDays,
  run
};
