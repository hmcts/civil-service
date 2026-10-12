# Stale branch cleanup exceptions

Each repository maintains its own exception list in
[stale-branch-exceptions.txt](stale-branch-exceptions.txt).
Listed branches are excluded from both scheduled cleanup and manual runs,
including the initial 365-day cleanup.

## Keep a branch

1. Open `.github/stale-branch-exceptions.txt` in the repository containing the branch.
2. Add the exact branch name on a new line. Names are case-sensitive; omit
   `refs/heads/`. Wildcards and patterns are not supported.
3. Add a comment on the preceding line explaining why it is needed, ideally
   with an owner and review date.
4. Raise a pull request and have it merged into `master` before cleanup runs.
   Editing the file only on the branch being retained does not protect it.
5. Run **Actions → Clean up stale branches → Run workflow** on `master` with
   `dry_run=true` and the intended `days_before` threshold (`365` for the
   initial cleanup, otherwise `90`). Check that the job summary lists the name
   under **Configured branch exceptions** and omits it from **Branches would
   be removed**.

Example entries (replace these with your actual branch names):

```text
# Retained regression baseline; owner: Civil QA; review: 2026-12-01
feature/regression-baseline

# Ongoing investigation; owner: Civil development
DTSCCI-1234
```

Blank lines and lines starting with `#` are ignored. Put comments on separate
lines; an inline comment would become part of the branch name.

The default branch, protected branches, release/hotfix branches and branches
with open pull requests are already excluded automatically.

## Remove an exception

Remove its branch-name line and associated comment through a pull request.
After merge, the branch is eligible again if it meets the inactivity threshold
and none of the other exclusions apply. An exception has no automatic expiry;
a review date in a comment is a reminder only.

## Which version of the list is used?

Scheduled runs use the default branch. Manual runs use the branch selected in
**Run workflow**, so select `master` to use the merged list. Exceptions are local
to each repository: add a name in both repositories if both need it retained.

The cleanup stops if the exception file cannot be read. No branch names are
pre-populated; the initial file contains guidance comments only.
