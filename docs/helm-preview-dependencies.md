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
