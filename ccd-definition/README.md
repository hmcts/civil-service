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

To re-sync, copy civil-ccd-definition's `ccd-definition/civil` and `ccd-definition/generalapplication`
at a newer master commit over these folders, and put that commit's full SHA in `.source-sha`.

Running `bin/pull-latest-civil-ccd-files.sh` locally replaces `civil/` and `generalapplication/` with the
branch it pulls. It leaves the rest of `ccd-definition/`, including `test/`, alone. Put the mirror back
with the command the script prints:

```bash
git checkout -- ccd-definition/civil ccd-definition/generalapplication && git clean -fdq -- ccd-definition/civil ccd-definition/generalapplication
```

`bin/dev-setup/start-devuser-preview-environment.sh` does that for you, including when the import fails.

## Definition specs

`test/` holds civil-ccd-definition's mocha specs from `e2e/tests/unit`. They check the JSON for
duplicates, missing references and invalid values, and need no running services.

```bash
npm ci
npm run test:definitions
```

`test/package.json` sets `"type": "commonjs"`, because the root `package.json` is an ES module
package and the specs use `require`.
