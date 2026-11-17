# Preview dependencies and published charts

`charts/civil-service/Chart.yaml` declares the dependencies used by Jenkins preview
and AAT staging installs. Only `java` is needed in the chart published for Flux.
The optional CCD, AAC, XUI, Camunda, WireMock and document-service dependencies
are listed in the `hmcts.github.io/skip-publish` annotation.

The pinned `Infrastructure@2.13.6` library removes dependencies listed in this
annotation before publishing the chart, rebuilds the dependency archives, and
restores the source metadata afterwards. Preview and staging installs retain the
full dependency list. This preserves their existing release and service names.
Flux-managed AAT and production load the smaller published package.

The Jenkins PR size check applies the same annotation to a temporary chart copy
before building dependencies and packaging it. The 90 MiB limit therefore checks
the package that Flux will load, rather than the full preview stack. Entries that
do not match a dependency name (or alias) fail the check.

When adding an optional dependency, include its name or alias in the annotation
if it is needed only for Jenkins preview/staging. Keep production dependencies
out of that annotation. Bump the chart version when changing the published
dependency list: already-published versions are not overwritten.

## Deployment verification

- In a PR build, confirm the chart-size stage downloads only `java` and remains
  below 90 MiB. Preview installation must still download all declared dependencies.
- Run preview smoke and functional tests, including the CCD/AAC and WireMock paths.
- On master, confirm chart publication logs list all eight excluded dependencies.
- Inspect the published archive: its `Chart.yaml` and `charts/` must contain only
  the `java` dependency. Record its unpacked size.
- Confirm the AAT staging install retains its dependencies and passes its tests.
- Confirm Flux-managed AAT and prod reconcile the new chart successfully and
  retain the expected civil-service image, environment and application health.

Registry downloads, Jenkins execution and cluster reconciliation require checks
in the corresponding environments; local fixture checks do not establish those
results.
