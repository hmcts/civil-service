# Scheduler Framework

The service includes a common framework for implementing scheduled tasks. This framework provides standard logging, error handling, back-pressure, and case processing logic.

## Core Components

- **`CivilScheduler`**: The main interface for scheduler components. Implementations define a `getName()` and `runScheduledTask()`.
- **`ScheduledTask<T, I>`**: A generic interface for the logic to be executed for each item found. It extends `Consumer<T>` and requires `getItemId(T item)` to be implemented.
- **`ScheduledTaskRunner<T, I>`**: A generic Spring component that coordinates task execution, including feature toggle checks, logging, and item processing.
- **`TaskResult<T>`**: An interface for search results, providing an `itemStream()` and `totalResults()`.
- **`ListTaskResult<T>`**: A standard implementation of `TaskResult` used for wrapping a simple list of items (e.g., from an API call).
- **`ElasticSearchService`**: A base class for services that search for cases to be processed by a scheduler via Elasticsearch.
- **`ElasticSearchPaginatedStreamProvider`**: A component for performing efficient paginated searches using Elasticsearch's `search_after`.
- **`PaginatedQueryProvider`**: An interface for providing Elasticsearch queries for paginated searches.
- **`ElasticSearchResult`**: An implementation of `TaskResult` that provides a lazy-loading stream of `CaseDetails` for paginated results.

## Creating a New Scheduler

1. **Implement a Search Service**: Define how to find the items to be processed.
    - **For CCD Case Searches**: Extend `ElasticSearchService` to define the query for finding cases.
    - **For Other Data Sources**: Create a service that returns a `TaskResult<T>` (typically using `ListTaskResult<T>`) by fetching data from an API, database, or other source.
2. **Implement a Scheduled Task**:
    - Implement `ScheduledTask<T, I>` (typically `ScheduledTask<CaseDetails, Long>`).
    - Implement `accept(T item)` for the processing logic.
    - Implement `getItemId(T item)` (e.g., `return caseDetails.getId()`).
    - (Optional) Override `maxCasesPerRun()` or `backPressureConfiguration()`.
3. **Create the Scheduler Class**:
    - Implement `CivilScheduler`.
    - Use `@Scheduled` and `@SchedulerLock` on the `runScheduledTask` method.
    - Inject and call `ScheduledTaskRunner.run()`.
4. **Add Configuration**:
    - Add a cron expression in `application.yaml`.
    - Add the scheduler name to the `active-schedulers` list.

## Best Practices & New Features

### External Filtering Logic
For complex filtering (e.g., checking deadlines, case states, or specific business rules), it is recommended to extract the filtering logic into a separate `@Component` class. This improves testability and keeps the scheduler class focused on coordination.

Example: `GAOrderMadeStayDeadlineFilter` or `GAUnlessOrderDeadlineFilter`.

### Using `CaseDetails`
When implementing `ScheduledTask`, prefer using `CaseDetails` as the item type (`T`). This avoids unnecessary early conversion to domain models if only a few fields are needed or if the conversion is handled better inside the task itself.

### Date and Time Handling
Always use the Spring-injected `Time` service for any date or time calculations. This ensures consistency across the application and allows for easy mocking in unit tests.

```java
private final Time time;
// ...
LocalDate today = time.now().toLocalDate();
```

### Interceptor Framework
The framework supports interceptors that can be applied to all or specific schedulers. Interceptors implement `SchedulerInterceptor` and can be used for cross-cutting concerns.

- **`OnGoingBusinessProcessCheck`**: A standard interceptor that ensures a case is not processed if there is an ongoing business process (e.g., an unfinished Camunda task).

Interceptors are resolved and applied by the `ScheduledTaskRunner` using the `InterceptorChainFactory`.

## Paginated Search Results

For large datasets, use `ElasticSearchPaginatedStreamProvider` to create a paginated search result. This approach is more efficient than standard pagination as it uses Elasticsearch's `search_after` mechanism and provides a lazy-loading stream.

1. **Implement `PaginatedQueryProvider`**: Define the query for each page.
    - Use `PageToken` to manage the `search_after` value.
    - Ensure the query includes a consistent `sort` field (e.g., `reference`).
2. **Create a Search Service**: Inject `ElasticSearchPaginatedStreamProvider` and call `getPaginatedSearchResult(queryProvider, pageSize)`.
3. **Lazy-loading Results**: The returned `ElasticSearchResult` provides a stream that fetches subsequent pages only as they are consumed, minimizing memory usage.

Example implementation: `JudgementBufferExpiredSearchService` and `JudgementBufferExpiredQueryProvider`.

## Features & Safety Mechanisms

- **Circuit Breaker**: The runner automatically aborts a job if it encounters consecutive failures (default: 5). This prevents overwhelming downstream services when they are unhealthy.
- **Back-pressure**: Tasks can apply back-pressure delays between processing items to manage load on external systems. Default settings are managed globally but can be overridden per-task.
- **Event Tracking**: Standardized events are emitted for job start, completion, failure, and early abortion, providing detailed observability.
- **LaunchDarkly Control**: The `spring-scheduler-enabled` flag acts as a global kill-switch for all Spring-managed schedulers.

## Spring Scheduler Configuration

Spring-based schedulers are controlled by both a LaunchDarkly feature flag and an allowlist of active schedulers.

### Feature Flag
The `spring-scheduler-enabled` flag in LaunchDarkly acts as a global kill-switch for all Spring-managed scheduled tasks. If this flag is disabled, no Spring schedulers will execute, regardless of their individual configuration.

### Active Schedulers List
To enable specific Spring schedulers, they must be added to the `active-schedulers` list in `application.yaml` or via the `SCHEDULER_ACTIVE_SCHEDULERS` environment variable.

**Configuration in `application.yaml`:**
```yaml
scheduler:
  active-schedulers: ${SCHEDULER_ACTIVE_SCHEDULERS:JudgementBuffer}
```

**Using Environment Variables:**
To enable multiple schedulers, provide a comma-separated list:
```bash
export SCHEDULER_ACTIVE_SCHEDULERS="JudgementBuffer,DefendantResponseDeadline"
```

To disable all Spring schedulers (even if the feature flag is on), set the list to be empty:
```bash
export SCHEDULER_ACTIVE_SCHEDULERS=""
```

### Global Scheduler Settings

| Setting | Description | Default | Environment Variable |
|---------|-------------|---------|----------------------|
| `circuitBreakerThreshold` | Number of consecutive failures before aborting. | `5` | `CIRCUIT_BREAKER_THRESHOLD` |
| `lockAtLeastFor` | Minimum time a task lock is held. | `PT1M` | `LOCK_AT_LEAST_FOR` |
| `lockAtMostFor` | Maximum time a task lock is held. | `PT30M` | `LOCK_AT_MOST_FOR` |
| `default-back-pressure.initialDelay` | Initial delay before processing a case. | `PT0S` | `DEFAULT_BACK_PRESSURE_INITIAL_DELAY` |
| `default-back-pressure.maxDelay` | Maximum back-pressure delay between cases. | `PT10S` | `DEFAULT_BACK_PRESSURE_MAX_DELAY` |
| `default-back-pressure.delayIncreaseOnFailure` | Delay added after a failed case. | `PT0.5S` | `DEFAULT_BACK_PRESSURE_DELAY_INCREASE_ON_FAILURE` |
| `default-back-pressure.delayIncreaseOnSlowCase` | Delay added after a slow case. | `PT0.25S` | `DEFAULT_BACK_PRESSURE_DELAY_INCREASE_ON_SLOW_CASE` |
| `default-back-pressure.delayReductionOnSuccess` | Delay removed after a successful case. | `PT0.1S` | `DEFAULT_BACK_PRESSURE_DELAY_REDUCTION_ON_SUCCESS` |
| `default-back-pressure.slowCaseThreshold` | Case processing duration treated as slow. | `PT2S` | `DEFAULT_BACK_PRESSURE_SLOW_CASE_THRESHOLD` |
