# CCD definitions (mirror)

`civil/` and `generalapplication/` are a copy of
[hmcts/civil-ccd-definition](https://github.com/hmcts/civil-ccd-definition) `ccd-definition/` at the
commit in [`.source-sha`](.source-sha).

**Make definition changes in civil-ccd-definition, not here.** No build or import reads this copy yet.
`bin/pull-latest-civil-ccd-files.sh` deletes `ccd-definition/` and copies it from civil-ccd-definition
before every build, so edits made here are thrown away without any error. That changes at the cutover
to this repo.

## Definition specs

`test/` holds civil-ccd-definition's mocha specs from `e2e/tests/unit`. They check the JSON for
duplicates, missing references and invalid values, and need no running services.

```bash
npm ci
npm run test:definitions
```

`test/package.json` sets `"type": "commonjs"`, because the root `package.json` is an ES module
package and the specs use `require`.
