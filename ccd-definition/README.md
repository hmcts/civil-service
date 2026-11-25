# CCD definitions (mirror)

`civil/` and `generalapplication/` are a copy of
[hmcts/civil-ccd-definition](https://github.com/hmcts/civil-ccd-definition) `ccd-definition/` at the
commit in [`.source-sha`](.source-sha).

**Make definition changes in civil-ccd-definition, not here.** No build or import reads this copy yet.
`bin/pull-latest-civil-ccd-files.sh` replaces `civil/` and `generalapplication/` with a copy from
civil-ccd-definition before every build, so edits made there would be thrown away without any error.
The [CCD definition mirror](../.github/workflows/ccd-definition-mirror.yml) workflow fails any PR in
which `civil/` or `generalapplication/` stop matching civil-ccd-definition at `.source-sha`, or in which
`.source-sha` is not a commit on civil-ccd-definition master. This changes at the cutover to this repo.

## The source-of-truth toggle

The cutover is a file: `SOURCE_OF_TRUTH` in this folder. It is not there yet.

- **Absent:** civil-ccd-definition is the source of truth, and everything below applies.
- **Present:** this repo is. `bin/pull-latest-civil-ccd-files.sh` and
  `bin/dev-setup/start-devuser-preview-environment.sh` use this folder as it is, and only copy the test
  code from civil-ccd-definition. A civil-ccd-definition branch passed to them, or set by a PR's CCD
  definition branch label, then changes only the test code. The pull script prints a warning when that
  happens.

Other repos decide the same way with
[`bin/shared/pull-ccd-definitions.sh`](../bin/shared/pull-ccd-definitions.sh)
`<civil-service ref> [civil-ccd-definition ref] [target dir]`. It checks for the file at the civil-service
ref they would pull from (master, or `pr-N`/`prN`), then copies `civil/` and `generalapplication/` from
civil-service if it is there, or from civil-ccd-definition if not, and prints which. It fails on any
fetch error instead of falling back to the other repo. A civil-service PR that adds the file therefore
rehearses the switch: its own preview, and any consumer build pointed at `pr-N`, take the new path while
nothing else changes.

`pr-N` is the head of the pull request, not the result of merging it into master, as in the other
scripts that pull from civil-service. A civil-service PR's own Jenkins build uses the merge result, so
the two can disagree. For example, a PR branched before the file reached master builds from this repo
itself, but a consumer pointed at its `pr-N` still copies from civil-ccd-definition until the PR is
rebased.

To re-sync, copy civil-ccd-definition's `ccd-definition/civil` and `ccd-definition/generalapplication`
at a newer master commit over these folders, and put that commit's full SHA in `.source-sha`.

Running `bin/pull-latest-civil-ccd-files.sh` locally replaces `civil/` and `generalapplication/` with the
branch it pulls. It leaves the rest of `ccd-definition/`, including `test/`, alone. Put the mirror back
with the command the script prints:

```bash
git checkout -- ccd-definition/civil ccd-definition/generalapplication && git clean -fdq -- ccd-definition/civil ccd-definition/generalapplication
```

`bin/dev-setup/start-devuser-preview-environment.sh` does that for you, including when the import fails.

## Building the spreadsheets

`bin/build-civil-xlsx-definitions.sh <environment> [true|false]` builds
`build/ccd-release-config/civil-ccd-<environment>.xlsx` and `civil-ga-ccd-<environment>.xlsx`. `true` builds
the shuttered variant. Which files each environment leaves out is set in [`exclusions.json`](exclusions.json),
which the specs below also read, so the specs check exactly what each environment imports. The patterns are
passed to `json2xlsx -e`: `*` matches any characters, matching ignores case, and a pattern that matches any
folder or file name in a path leaves that file out.

Until the cutover, civil-ccd-definition's `bin/build-release-ccd-definition.sh` is still the build that
imports. `bin/compare-ccd-definition-builds.sh` builds every environment, and the shuttered prod variant,
with both scripts from the definitions at `.source-sha`, and fails unless the spreadsheets are identical.
The mirror workflow runs it on every PR that touches this folder or the build scripts. Change an exclusion
rule in civil-ccd-definition first, then here when the mirror is re-synced past that change.

## Definition specs

`test/` holds civil-ccd-definition's mocha specs from `e2e/tests/unit`. They check the JSON for
duplicates, missing references and invalid values, and need no running services.

```bash
npm ci --ignore-scripts
npm run test:definitions
```

`--ignore-scripts` stops puppeteer, which the BPMN rendering tooling uses, from downloading Chrome.
The specs don't need it.

`bin/run-ccd-definition-specs.sh` does the same without touching `./node_modules`: it installs into a
temporary folder. Jenkins runs it in a `CCD definition specs` stage at the start of the Build stage, so a
broken definition fails in about a minute. It runs there because the preview import later replaces
`civil/` and `generalapplication/` with civil-ccd-definition's copy (see above). When the specs run, the folder
is still the checkout: this mirror until the switch, the live definitions after it. The
[CCD definition mirror](../.github/workflows/ccd-definition-mirror.yml) workflow also runs them, on PRs
that touch this folder.

Use Node 20.19+ or 22.12+ (`.nvmrc` has the version CI uses). chai 6 is published only as an ES
module, and older Node versions can't `require` it.

`test/package.json` sets `"type": "commonjs"`, because the root `package.json` is an ES module
package and the specs use `require`.
