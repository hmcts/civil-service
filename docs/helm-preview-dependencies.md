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

The pipeline also publishes `civil-service-preview` with the complete dependency
stack, generated from the same source and chart version. It removes the
`skip-publish` annotation from that companion chart so the publisher retains every
dependency. Both packages are published by the same master build; the companion
is published after the slim chart succeeds. Retries skip versions already present
in the registry, as with the standard publisher.

Downstream preview/staging charts must depend on `civil-service-preview`, with
`alias: civil-service` to retain their existing values and enablement conditions:

```yaml
- name: civil-service-preview
  alias: civil-service
  version: '>=0.0.134 <0.1.0'
  repository: 'oci://hmctsprod.azurecr.io/helm'
  condition: civil-service.enabled
```

Publish the companion chart before merging consumers onto it. The full chart is
for downstream preview/staging installs and must not replace the slim
`civil-service` chart in Flux-managed environments. The full stack may exceed
Flux's size limit. Updating a dependency only changes the generated companion
when the source chart version is bumped.

The Jenkins PR size check applies the same annotation to a temporary chart copy
before building dependencies and packaging it. The 90 MiB limit therefore checks
the package that Flux will load, rather than the full preview stack. Entries that
do not match a dependency name (or alias) fail the check.

The same PR stage also builds and packages the full companion, checking its name,
version and bundled dependency set. The 90 MiB guard applies only to the slim
chart; the full chart is validated for completeness instead.

When adding an optional dependency, include its name or alias in the annotation
if it is needed only for Jenkins preview/staging. Keep production dependencies
out of that annotation. Bump the chart version when changing the published
dependency list: already-published versions are not overwritten.
