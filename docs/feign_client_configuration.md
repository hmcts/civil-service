### Feign Client Configuration

This project uses a custom `HttpClientFeignConfiguration` which provides an `InstrumentedFeignClient` for all Feign clients. This client adds telemetry for monitoring performance and connection pool issues.

While there are global defaults for HTTP connections, you can customise individual Feign clients using `src/main/resources/application.yaml`.

#### Global Defaults
The following properties in `application.yaml` define the global defaults for the underlying Apache HttpClient:

```yaml
http:
  client:
    connectTimeout: ${HTTP_CONNECT_TIMEOUT:5000}
    requestTimeout: ${HTTP_REQUEST_TIMEOUT:10000}
    readTimeout: ${HTTP_READ_TIMEOUT:30000}
    maxPerRoute: ${HTTP_MAX_PER_ROUTE:5}
    maxTotal: ${HTTP_MAX_TOTAL:25}
    threshold: ${HTTP_CLIENT_THRESHOLD:15000}
    error-classifier:
      enabled: ${HTTP_ERROR_CLASSIFIER_ENABLED:true}
      logHeaders: ${HTTP_ERROR_CLASSIFIER_LOG_HEADERS:false}
```

- `connectTimeout`: Time to establish the connection with the remote host.
- `requestTimeout`: Time to wait for a connection from the connection pool.
- `readTimeout`: Time to wait for data (socket timeout).
- `maxPerRoute`: Maximum concurrent connections per route/host.
- `maxTotal`: Total maximum concurrent connections across all routes.
- `threshold`: Threshold in milliseconds after which a request is logged as a 'slow request' in Application Insights.
- `error-classifier.enabled`: Enables logging-only Feign error classification while preserving default Feign exception behaviour.
- `error-classifier.logHeaders`: Includes selected headers (for now `Retry-After`) in classifier logs.

#### Error Classification (Logging-only)
`HttpClientFeignConfiguration` now also registers a global Feign `ErrorDecoder` (`FeignErrorClassificationDecoder`) that:

1. Calls the default Feign decoder and returns the same exception type (no retry behaviour change).
2. Logs whether an error would be considered retryable/non-retryable based on status + method idempotency.
3. Parses `Retry-After` when present and logs the parsed value.

This allows you to observe real production error patterns and refine idempotent endpoint policy before introducing an actual `Retryer`.

#### Customising Specific Clients
You can override these settings (except pool sizes) for specific Feign clients under `feign.client.config`. The configuration key must match the `name` or `value` attribute of the `@FeignClient` annotation.

Example customisation:

```yaml
feign:
  client:
    config:
      # Camunda API clients
      processInstance:
        readTimeout: ${CAMUNDA_API_READ_TIMEOUT:10000}
      historicProcessInstance:
        readTimeout: ${CAMUNDA_API_READ_TIMEOUT:10000}
      processDefinition:
        readTimeout: ${CAMUNDA_API_READ_TIMEOUT:10000}
      message:
        readTimeout: ${CAMUNDA_API_READ_TIMEOUT:10000}
      incident:
        readTimeout: ${CAMUNDA_API_READ_TIMEOUT:10000}
      externalTask:
        readTimeout: ${CAMUNDA_API_READ_TIMEOUT:10000}

      # IDAM API - Customised timeouts
      idam-api:
        connectTimeout: 5000
        readTimeout: ${IDAM_API_READ_TIMEOUT:45000}
        loggerLevel: basic

      # HMC API - Increase read timeout and add logging
      hmc-api:
        readTimeout: ${HMC_API_READ_TIMEOUT:60000}
        loggerLevel: headers

      # CCD API - Enable full logging and increase connect & read timeout
      core-case-data-api:
        connectTimeout: 10000
        readTimeout: ${CCD_API_READ_TIMEOUT:60000}
        loggerLevel: full

      # Document Management - Enable full logging and increase read timeout - 3mins
      document-management-metadata-download-api:
        readTimeout: ${DOC_API_READ_TIMEOUT:180000}
        loggerLevel: full
```

#### How it works
1. **Timeouts**: When a Feign client is invoked, the `readTimeout` and `connectTimeout` specified in `feign.client.config.<name>` are passed as `Request.Options`. The `InstrumentedFeignClient` passes these to the underlying `ApacheHttpClient`, which overrides the global defaults for that specific request.
   - Note: `requestTimeout` (time to wait for a connection from the pool) is a global setting and cannot be overridden per-client.
2. **Connection Pool**: The `maxPerRoute` and `maxTotal` settings are **global** to the `PoolingHttpClientConnectionManager` and cannot be overridden per-client.
3. **Logging**: The `loggerLevel` setting controls how much detail is logged for the Feign client (options: `none`, `basic`, `headers`, `full`). Note that you also need to set the logging level for the client's package to `DEBUG` in the logging configuration to see the output.
4. **URL**: The `url` property allows you to dynamically set or override the target endpoint for the Feign client, which is particularly useful for external services like Camunda.
5. **Additional Settings**: You can also configure `defaultRequestHeaders`, `defaultQueryParameters`, `retryer`, `errorDecoder`, and `decode404` per client in the same way.

#### Requirements for Customisation
For these customisations to be picked up, the `@FeignClient` should preferably use the standard Feign configuration or be explicitly configured to use `FeignClientProperties.FeignClientConfiguration.class`:

```java
@FeignClient(
    name = "hmc-api",
    url = "${hmc.api.url}",
    configuration = FeignClientProperties.FeignClientConfiguration.class
)
public interface HearingsApi {
    // ...
}
```
