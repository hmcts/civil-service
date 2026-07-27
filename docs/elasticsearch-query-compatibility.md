# Elasticsearch query compatibility

Civil Service does not connect to Elasticsearch or manage indexes directly. It uses Elasticsearch query builders to
serialize Query DSL requests, which are sent to CCD Data Store through `CoreCaseDataApi`. CCD Data Store owns the
Elasticsearch connection, authentication, TLS, retries, timeouts, index mappings, templates and stored-data
compatibility.

## Version alignment

The query-builder dependency is aligned to Elasticsearch `9.2.3`, matching the version used by the CCD Data Store
preview chart and its Elasticsearch deployment. AAT, Demo, ITHC and Production use the shared CCD Data Store service;
their Elasticsearch deployment and upgrade are owned by CCD rather than Civil Service.

The version must remain aligned with CCD Data Store. It must not be upgraded independently to a newer major or minor
release without checking CCD's deployed version and running the Civil Service search-query tests.

## Compatibility verification

The unit tests under `service/search`, `ga/service/search` and `model/search` verify the serialized Query DSL used for:

- case searching and pagination;
- range, match, term, existence and boolean queries;
- source filtering and sorting;
- Civil and general-application searches.

Elasticsearch 9 serializes range bounds as `gt`, `gte`, `lt` and `lte`. This is semantically equivalent to the legacy
`from`, `to`, `include_lower` and `include_upper` representation and is supported by the Elasticsearch 9 Query DSL.

Deployment and rollback follow the normal Civil Service process. Rollback consists of redeploying the previous
application version; no data or index migration is performed by this service.
