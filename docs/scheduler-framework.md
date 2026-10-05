# Scheduler Framework

The service includes a common framework for implementing scheduled tasks. This framework provides standard logging, error handling, back-pressure, interceptor chains, circuit breaking, telemetry tracking, and case processing logic.

## Core Components

- **`CivilScheduler`**: The main interface for scheduler components. Implementations define a `getName()` and `runScheduledTask()`.
- **`ScheduledTask<T, I>`**: A generic interface for the logic to be executed for each item found. It extends `Consumer<T>` and requires `getItemId(T item)` to be implemented, with optional `maxCasesPerRun()` and `backPressureConfiguration()`.
- **`ScheduledTaskRunner<T, I>`**: A generic Spring component that coordinates task execution, including feature toggle checks, StopWatch timing, interceptor resolution, logging, and job outcome reporting.
- **`ScheduledTaskProcessor<T, I>`**: A Spring component that executes item-by-item stream processing, applying back-pressure delays, running the interceptor chain, evaluating the circuit breaker upon consecutive failures, and emitting item-level events.
- **`ScheduledTaskConfiguration<T, I>`**: A type-safe configuration builder used with `ScheduledTaskRunner.run(...)` to configure the scheduler name, search supplier, task, task-specific interceptors, and default interceptor inclusion.
- **`SchedulerRegistry`**: A Spring service managing all registered `CivilScheduler` instances, enabling programmatic/on-demand execution (`runScheduler(name)`) and listing enabled schedulers.
- **`TaskResult<T>`**: A generic interface for search results, providing `itemStream()`, `totalResults()`, and `isEmpty()`. It acts as a bridge between search logic and the runner.
- **`ListTaskResult<T>`**: A lightweight implementation of `TaskResult` wrapping a `List<T>` (e.g., from an API call or an in-memory filtered list).
- **`SetTaskResult<T>`**: A lightweight implementation of `TaskResult` wrapping a `Set<T>` for deduplicated collections.
- **`ElasticSearchService`**: A base class for services that search for cases to be processed by a scheduler via Elasticsearch.
- **`ElasticSearchPaginatedStreamProvider`**: A core component for performing efficient, memory-safe paginated searches using Elasticsearch's `search_after` mechanism.
- **`PaginatedQueryProvider`**: An interface for providing dynamic Elasticsearch queries for each page during a paginated search.
- **`ElasticSearchResult`**: An implementation of `TaskResult` that provides a lazy-loading stream of `CaseDetails`, fetching new pages only as the stream is consumed.
- **`SchedulerInterceptor<T>`**: An ordered interface for cross-cutting item interception before task execution (e.g., `OnGoingBusinessProcessCheck`).
- **`InterceptorChain<T>` & `InterceptorContext<T>`**: Thread-confined pipeline components managing ordered interceptor execution, attribute sharing via type-safe `AttributeKey<V>`, and StopWatch metric recording per interceptor.

## Creating a New Scheduler

1. **Implement a Search Service**: Define how to find the items to be processed.
    - **For CCD Case Searches**: Extend `ElasticSearchService` or implement `PaginatedQueryProvider` paired with `ElasticSearchPaginatedStreamProvider`.
    - **For Other Data Sources**: Create a service that returns a `TaskResult<T>` (typically using `ListTaskResult<T>` or `SetTaskResult<T>`) by fetching data from an API, database, or other source.
2. **Implement a Scheduled Task**:
    - Implement `ScheduledTask<T, I>` (typically `ScheduledTask<CaseDetails, Long>`).
    - Implement `accept(T item)` for the processing logic.
    - Implement `getItemId(T item)` (e.g., `return caseDetails.getId()`).
    - (Optional) Override `maxCasesPerRun()` or `backPressureConfiguration()` (or inject `DefaultBackPressureConfiguration`).
3. **Create the Scheduler Class**:
    - Implement `CivilScheduler`.
    - Use `@Scheduled` and `@SchedulerLock` on the `runScheduledTask` method.
    - Inject `ScheduledTaskRunner<T, I>` and invoke `run(...)`:

    ```java
    // Option A: Direct invocation
    scheduledTaskRunner.run(
        SCHEDULER_NAME,
        searchService::getElasticSearchResult,
        scheduledTask
    );

    // Option B: Configuration-driven (recommended when customizing interceptors)
    scheduledTaskRunner.run(
        ScheduledTaskConfiguration.<CaseDetails, Long>builder()
            .schedulerName(SCHEDULER_NAME)
            .searchResultSupplier(searchService::getElasticSearchResult)
            .scheduledTask(scheduledTask)
            .useDefaultInterceptors(true) // or false to disable default interceptors
            .build()
    );
    ```
4. **Add Configuration**:
    - Add a cron expression in `application.yaml` (e.g., `scheduler.<task-name>.cronExpression`).
    - Add the scheduler name to the `scheduler.active-schedulers` list.
    - (Optional) Configure pagination page size in `application.yaml` under `search.<task-name>.pageSize`.

## Best Practices & Patterns

### External Filtering Logic
For complex filtering (e.g., checking deadlines, case states, or specific business rules), extract the filtering logic into a separate `@Component` class. This improves testability and keeps the scheduler class focused on coordination.

#### Coding Style for Filters
When implementing these filters, prefer using **direct private methods** with `@RequiredArgsConstructor` constructor injection over class-level `Predicate` fields. This approach:
- **Avoids Initialization Issues**: Functional interfaces capturing injected services in field initializers can lead to `NullPointerException` or stale state.
- **Improves Debugging**: Stack traces will explicitly name the logic method (e.g., `isJudgeOrderStayDeadlineExpired`), making root cause analysis easier.
- **Standard Practice**: Align with standard Spring and Java idioms for logic encapsulation.

```java
@Component
@RequiredArgsConstructor
public class GAOrderMadeStayDeadlineFilter {

    private final Time time;
    private final CaseDetailsConverter caseDetailsConverter;

    public boolean hasExpiredStayDeadline(CaseDetails caseDetails) {
        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(caseDetails);
        return isJudgeOrderStayDeadlineExpired(caseData) || isConsentOrderStayDeadlineExpired(caseData);
    }

    private boolean isJudgeOrderStayDeadlineExpired(GeneralApplicationCaseData caseData) {
        GAJudicialMakeAnOrder judicialDecisionMakeOrder = caseData.getJudicialDecisionMakeOrder();
        return judicialDecisionMakeOrder != null
            && judicialDecisionMakeOrder.getJudgeApproveEditOptionDate() != null
            && !time.now().toLocalDate().isBefore(judicialDecisionMakeOrder.getJudgeApproveEditOptionDate());
    }

    private boolean isConsentOrderStayDeadlineExpired(GeneralApplicationCaseData caseData) {
        GAApproveConsentOrder approveConsentOrder = caseData.getApproveConsentOrder();
        return approveConsentOrder != null
            && approveConsentOrder.getConsentOrderDateToEnd() != null
            && !time.now().toLocalDate().isBefore(approveConsentOrder.getConsentOrderDateToEnd());
    }
}
```

Examples: `GAOrderMadeStayDeadlineFilter`, `GAUnlessOrderDeadlineFilter`.

### Choosing the Right Task Result Type
Selecting the appropriate `TaskResult` implementation is crucial for performance and memory management.

| Result Type | Use Case | Benefits |
|-------------|----------|----------|
| **`ListTaskResult<T>`** | Small, ordered materialized collections; items fetched from REST APIs; lists that require in-memory post-filtering. | Simple, low overhead, no stream state management. |
| **`SetTaskResult<T>`** | Deduplicated materialized collections where items must be unique. | Ensures distinct item processing with set semantics. |
| **`ElasticSearchResult`** | Large datasets from Elasticsearch; paginated case searches. | Memory efficient, lazy-loading (pages fetched only when needed). |

**Decision Rule:** If you are performing a search that might return thousands of cases, use `ElasticSearchResult` via `ElasticSearchPaginatedStreamProvider`. If you are filtering a small set of cases already in memory, use `ListTaskResult` or `SetTaskResult`.

### Using `CaseDetails`
When implementing `ScheduledTask`, prefer using `CaseDetails` as the item type (`T`). This avoids unnecessary early conversion to domain models and ensures seamless compatibility with the standard interceptor chain.

### Interceptor Framework

The framework supports an interceptor pipeline that executes before scheduled task processing for each item. Interceptors implement `SchedulerInterceptor<T>` and participate in an ordered chain.

#### Key Features:
- **`SchedulerInterceptorResolver`**: Automatically discovers and merges compatible default interceptors (matching generic type `T`) with task-specific interceptors.
- **`OnGoingBusinessProcessCheck`**: The standard interceptor (order: 1) for `CaseDetails`. It checks whether an ongoing business process is active (e.g., an unfinished Camunda task) for both standard Civil (`CaseData`) and General Application (`GeneralApplicationCaseData`) cases using `CaseTypeIdentifier`.
- **Attribute Caching & Context Sharing**: `InterceptorContext` allows sharing data between interceptors and tasks via type-safe `AttributeKey<V>` (e.g., `CaseInterceptorAttributes.CIVIL_CASE_DATA`, `GA_CASE_DATA`, `BASE_CASE_DATA`). If `CaseDetails` contains sufficient data, unnecessary CCD remote calls are avoided.
- **Task Abortion via `TaskAbortedException`**: If an interceptor throws `TaskAbortedException` (e.g. `Ongoing business process`), the item is skipped gracefully as aborted without triggering circuit breaker failures.
- **Execution Timing Metrics**: `InterceptorChain` tracks exclusive execution time per interceptor and the final task using `StopWatch`, recording them in `InterceptorContext.getMetrics()` (e.g. `metric_OnGoingBusinessProcessCheck`, `metric_FinalTask`).
- **Disabling Default Interceptors**: If a scheduled task handles cases with active business processes intentionally (e.g., `PollingEventEmitterScheduler`), default interceptors can be disabled via `ScheduledTaskConfiguration.builder().useDefaultInterceptors(false).build()`.

### Date, Time, Timezones, and Performance Handling

- **Business Logic Dates**: Always use the Spring-injected `Time` service for any date or time logic involving business rules (e.g., checking deadlines).
- **Timezone Conversion for Elasticsearch Queries**: When building Elasticsearch queries with timestamps against UTC fields, convert local time to UTC using `LOCAL_ZONE` (from `uk.gov.hmcts.reform.civil.helpers.LocalDateTimeHelper`) and `withZoneSameInstant` to properly handle Daylight Saving Time (BST) transitions:
  ```java
  String timeNow = time.now().atZone(LOCAL_ZONE).withZoneSameInstant(ZoneOffset.UTC).toString();
  ```
- **Execution Timing**: For performance monitoring and execution timing, the framework uses Spring's `StopWatch`. This provides:
  - **Consistency**: Unified timing mechanism across `ScheduledTaskRunner`, `ScheduledTaskProcessor`, and `InterceptorChain`.
  - **Granularity**: The ability to measure distinct phases (e.g., search duration vs. processing duration, individual interceptor durations).
  - **Accuracy**: Uses `System.nanoTime()` internally for precise measurement.

## Paginated Search Results

For large datasets, use `ElasticSearchPaginatedStreamProvider` to create a paginated search result. This approach is more efficient than standard pagination as it uses Elasticsearch's `search_after` mechanism and provides a lazy-loading stream.

1. **Implement `PaginatedQueryProvider`**: Define the query for each page.
    - Use `PageToken` to manage the `search_after` value.
    - Ensure the query includes a consistent `sort` field (e.g., `reference`).
2. **Create a Search Service**: Inject `ElasticSearchPaginatedStreamProvider` and call `getPaginatedSearchResult(queryProvider, pageSize)`.
3. **Lazy-loading Results**: The returned `ElasticSearchResult` provides a stream that fetches subsequent pages only as they are consumed, minimizing memory usage.

Example implementations: `JudgementBufferExpiredSearchService` and `JudgementBufferExpiredQueryProvider`, `DefendantResponseDeadlineCheckPaginatedSearchService` and `DefendantResponseDeadlineCheckQueryProvider`, `HearingFeeDuePaginatedSearchService` and `HearingFeeDueQueryProvider`.

## Features & Safety Mechanisms

- **Circuit Breaker**: The processor automatically aborts a job if it encounters consecutive failures (default threshold: 5, configured via `scheduler.circuitBreakerThreshold`). This protects downstream services when they are unhealthy while allowing aborted cases (`TaskAbortedException`) to bypass the failure counter.
- **Dynamic Back-pressure**: Tasks can apply back-pressure delays between processing items to manage load on external systems. Default settings are managed globally via `DefaultBackPressureConfiguration` and can be overridden per task via `ScheduledTask.backPressureConfiguration()`.
- **Event Tracking & Telemetry**: `ScheduledEventTracker` emits structured Application Insights events for job starts, completions, empty searches, and job abortions (with cumulative delays, search durations, and processing durations), as well as per-item processed, failed, or aborted events (with per-interceptor execution metrics and categorized errors via `ErrorCategorizer`).
- **Scheduler Registry**: `SchedulerRegistry` registers all `CivilScheduler` beans, supporting asynchronous triggering by name and dynamic discovery of enabled schedulers.
- **Thread Safety**: Interceptors and services are stateless Spring singletons; per-item execution state (`InterceptorChain`, `InterceptorContext`) is strictly thread-confined.
- **LaunchDarkly & Allowlist Controls**: The global `spring-scheduler-enabled` LaunchDarkly flag acts as a kill-switch, while `scheduler.active-schedulers` controls individual scheduler activation.

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
| `defaultLockAtMostFor` | Default maximum time a lock is held. | `PT30M` | `DEFAULT_LOCK_AT_MOST_FOR` |
| `default-back-pressure.initialDelay` | Initial delay before processing a case. | `PT0S` | `DEFAULT_BACK_PRESSURE_INITIAL_DELAY` |
| `default-back-pressure.maxDelay` | Maximum back-pressure delay between cases. | `PT10S` | `DEFAULT_BACK_PRESSURE_MAX_DELAY` |
| `default-back-pressure.delayIncreaseOnFailure` | Delay added after a failed case. | `PT0.5S` | `DEFAULT_BACK_PRESSURE_DELAY_INCREASE_ON_FAILURE` |
| `default-back-pressure.delayIncreaseOnSlowCase` | Delay added after a slow case. | `PT0.25S` | `DEFAULT_BACK_PRESSURE_DELAY_INCREASE_ON_SLOW_CASE` |
| `default-back-pressure.delayReductionOnSuccess` | Delay removed after a successful case. | `PT0.1S` | `DEFAULT_BACK_PRESSURE_DELAY_REDUCTION_ON_SUCCESS` |
| `default-back-pressure.slowCaseThreshold` | Case processing duration treated as slow. | `PT2S` | `DEFAULT_BACK_PRESSURE_SLOW_CASE_THRESHOLD` |
| `search.<scheduler-key>.pageSize` | Search page size for paginated queries. | `50` | `SEARCH_PAGE_SIZE_<KEY>` |
