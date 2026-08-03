# Camunda 7.24 client upgrade

Civil Service uses Camunda as a remote process engine. This upgrade changes the client-side libraries used by
Civil Service; it does not upgrade the Camunda engine deployed by the CCD platform.

## Versions

- Camunda external task client: `7.24.0`
- Camunda REST DTOs: `7.24.0`
- Camunda engine used by BPMN tests: `7.24.0`
- Holunda C7 REST client Spring Boot starter: `2026.04.2`

The former `org.camunda.community.rest` starter is archived and its final release redirects consumers to the
maintained `io.holunda.c7` project. The replacement preserves the existing Java package names while providing a
maintained Spring Boot client. Civil Service currently runs Spring Boot 3, so it uses the standard starter. The same
Holunda release publishes a `c7-rest-client-spring-boot-starter-4` variant for the planned Spring Boot 4 migration;
that migration must switch to the Boot 4-specific artifact.

## Engine compatibility

The production Camunda engine may run a different Camunda 7 minor version. Civil Service communicates with it only
through the REST API and external task protocol. The application must therefore avoid using REST fields or operations
that are unavailable on the deployed engine version.

Before production deployment, verify against the Camunda engine deployed in Preview or AAT that:

1. the Civil BPMN definitions deploy and a representative process starts;
2. an external task worker fetches and completes a task;
3. a failed external task records its message, details and retry count;
4. primitive, collection and object process variables round-trip correctly;
5. active and historic process definitions and instances remain queryable; and
6. smoke and functional tests pass.

## Database migration

No Civil Service database migration is required. The production application does not embed or own the Camunda
engine schema. The `camunda-engine` dependency is used only by in-memory BPMN tests.

Upgrading the remote Camunda engine remains a separate CCD platform change and must follow Camunda's sequential
minor-version schema migration instructions.

## Deployment and rollback

Deploy the Civil Service image through the normal environment pipeline. Monitor application logs for REST client
errors, external task fetch failures, variable deserialization failures and unexpected incidents before promoting the
image.

Rollback does not require a database change. Redeploy the previous Civil Service image, which restores the previous
client libraries. Process definitions and instances remain in the remote engine and are not changed by the rollback.
