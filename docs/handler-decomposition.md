# Handler Decomposition

CCD callback handlers in this service have accumulated business logic over time. The worst of them are large, carry many injected dependencies, and are slow to change safely. [DTSCCI-365](https://hmcts.atlassian.net/browse/DTSCCI-365) measured the problem: the handler trees hold 32 per cent of the project's cyclomatic complexity and 40 per cent of its cognitive complexity in 37 per cent of its lines.

The problem is concentrated rather than systemic. The median handler is 30 non-comment lines with one injected dependency. About 20 classes carry the weight.

This document is the convention for decomposing them. It exists so that several developers refactoring in parallel produce the same shape, and so reviews are about correctness rather than structure.

## The pattern

A handler keeps its `callbacks()` map and delegates each callback to a task class. The task holds the logic and its own dependencies. The handler becomes orchestration.

The reference implementation is `RespondToClaimSpecCallbackHandler`. It is 229 non-comment lines and contains no business logic.

```java
@Override
protected Map<String, Callback> callbacks() {
    return new ImmutableMap.Builder<String, Callback>()
        .put(callbackKey(ABOUT_TO_START), this::populateRespondent1Copy)
        .put(callbackKey(MID, "confirm-details"), this::validateDateOfBirth)
        ...
        .build();
}

private CallbackResponse validateDateOfBirth(CallbackParams callbackParams) {
    return validateDateOfBirth.execute(callbackParams);
}
```

## Writing a task

A task is a Spring component implementing the `CaseTask` marker interface, exposing a single `execute` method.

```java
@Component
@RequiredArgsConstructor
public class SetUploadTimelineTypeFlag implements CaseTask {

    private final ObjectMapper objectMapper;

    public CallbackResponse execute(CallbackParams callbackParams) {
        ...
    }
}
```

`CaseTask` is an empty marker, so the `execute` signature is convention rather than compiler-enforced. Keep to it exactly. See [Known gaps](#known-gaps).

## Package layout

New task packages go in:

```
uk.gov.hmcts.reform.civil.handler.callback.user.<feature>.tasks
```

for example `handler/callback/user/sdo/tasks`. This matches `directionsorder/tasks`, `dj/tasks` and `sdo/tasks`.

Three layouts exist today, because the pattern grew without being written down:

| Layout | Used by |
| --- | --- |
| `user/<feature>/tasks` | `directionsorder`, `dj`, `sdo` |
| `user/<handlername>tasks` | `respondtoclaimspeccallbackhandlertasks` |
| `user/task/<handlername>task(s)` | `respondtoclaimcallbackhandlertasks`, `respondtodefencespeccallbackhandlertask` |

Use the first for anything new. Do not rename the existing packages solely to conform, the churn is not worth it. Move them only when you are already changing that handler.

General Application handlers live under `civil/ga/handler/callback/user` and have no task package yet. The first GA decomposition should create `ga/handler/callback/user/<feature>/tasks` on the same rule.

## What stays on the handler

- The `callbacks()` map.
- `handledEvents()` and `camundaActivityId()`.
- One-line private methods delegating to a task.
- Nothing else. No conditionals, no case data manipulation, no service calls.

## When the pattern does not apply

Decomposing by callback only works when a handler has several callbacks. Some targets do not:

- **One callback holding everything.** `UpdateCaseDetailsAfterNoCHandler` has a single `ABOUT_TO_SUBMIT` callback and 318 lines behind it. Extract cohesive groups of private methods into collaborator services instead.
- **Single entry point classes.** `UploadTranslatedDocumentDefaultStrategy` is a strategy, not a handler. Same approach.
- **Tasks that have themselves grown.** `SetGenericResponseTypeFlag` is already an extracted task at 494 lines and 102 cognitive complexity. Extracting a task does not by itself fix complexity. Split it into collaborators rather than fanning out to more tasks.

If the class has one entry point, extract by responsibility, not by callback.

## Testing

**The existing handler test must pass unedited.** A refactor that requires its tests to be rewritten is not a refactor, it is a behaviour change. Mechanical import updates are the only acceptable edit, and should be called out in the pull request.

This matters because the suites are the safety net and most are substantial. `CreateClaimCallbackHandlerTest` is 113 tests, `JudicialDecisionHandlerTest` is 130.

Where the net is thin, add characterisation tests covering current behaviour **before** refactoring. `AboutToSubmitRespondToDefenceTask` is the clearest case, 11 tests for 350 lines and 14 dependencies.

New task classes need their own unit tests. Per the repository guidelines, keep per-file coverage at or above the Sonar new-code target and watch duplication on near-identical task classes.

## Known trade-off

This pattern moves coupling from logic into wiring. The reference handler is 229 lines but injects 21 dependencies, one per task.

That is accepted. DTSCCI-365 measures the problem by cognitive complexity, which is what tracks how hard code is to understand, not by dependency count alone. A thin handler wiring 21 well-named tasks is easier to reason about than 600 lines of branching, even though the constructor is longer.

Do not raise this as a blocking review comment. If a handler's dependency count is itself the problem, consider a parameter object or a facade, and say so on the ticket.

## Known gaps

- `CaseTask` is an empty marker interface. Adding `CallbackResponse execute(CallbackParams)` to it would make the contract explicit and compiler-enforced. Not done yet because it touches every existing implementation. Worth a ticket.
- The three package layouts above should converge over time, opportunistically rather than as a dedicated exercise.
