# Workflow Integration Test Pattern

This folder contains the workflow-style integration test pattern for CCD callbacks in `civil-service`.

The point of this pattern is simple:

- hit the real callback controller with `MockMvc`
- run real service-owned callback logic
- keep external boundaries mocked
- make multi-step callback flows easy to test in Java
- avoid large, repetitive setup blocks in each test

This README is written for developers adding or migrating tests in this area.

## What This Pattern Is For

Use this pattern when you are testing a CCD callback flow that goes through:

- `/cases/callbacks/{callback-type}`
- `/cases/callbacks/{callback-type}/{page-id}`

Typical examples:

- `ABOUT_TO_START`
- `MID`
- `ABOUT_TO_SUBMIT`
- `SUBMITTED`

Use it when you want to test one or more of:

- callback response errors
- callback response data
- case-data mutation across steps
- state changes
- side effects caused by the callback

Do not use this pattern for:

- unit tests
- plain controller tests that are not workflow-style CCD callbacks
- tests whose value is mainly full end-to-end wiring across systems

General application callbacks are in scope, but they use a small sibling base because they run through the same
callback controller with `GENERALAPPLICATION` case data instead of civil `CaseData`.

## Current Structure

```text
workflow/
├── WorkflowIntegrationTest.java
├── README.md
├── ccd/
│   ├── ...WorkflowTest.java
│   └── fixture/
├── dashboard/
│   ├── DashboardWorkflowIntegrationTest.java
│   ├── ...WorkflowTest.java
│   └── fixture/
├── ga/
│   ├── GAWorkflowIntegrationTest.java
│   ├── ...WorkflowTest.java
│   └── fixture/
└── helper/
    ├── CaseDataTemplates.java
    └── WorkflowBuilder.java
```

Resources live here:

```text
src/integrationTest/resources/templates/case-data/
src/integrationTest/resources/templates/case-data/ga/
```

## The Main Pieces

### `WorkflowIntegrationTest`

This is the base class for normal CCD workflow tests.

It provides:

- the real callback entry point through `MockMvc`
- common system-user setup
- `startWorkflow(CaseData)` to begin a flow
- conversion of callback response data back into `CaseData`

If your test is about a CCD callback workflow, start here.

### `GAWorkflowIntegrationTest`

This is the GA sibling base for callbacks whose request payload contains `GeneralApplicationCaseData`.

It provides the same controller-level workflow execution pattern as `WorkflowIntegrationTest`, but with:

- `GENERALAPPLICATION` case type routing
- GA case-data rehydration between callback steps
- `startWorkflow(GeneralApplicationCaseData)` for GA flows

Use this when the event is a real GA callback and the normal civil workflow base cannot be used because the handler
reads `callbackParams.getGeneralApplicationCaseData()`.

### `WorkflowBuilder`

This is the fluent helper that executes callback steps for both CCD `CaseData` and
GA `GeneralApplicationCaseData`.

Available methods:

- `eventId(CaseEvent event)`
- `eventId(String eventId)`
- `caseDataBefore(CaseData caseDataBefore)`
- `aboutToStart()`
- `mid(String pageId)`
- `aboutToSubmit()`
- `submitted()`
- `then(...)`

Important behaviour:

- after `ABOUT_TO_START`, `MID`, and `ABOUT_TO_SUBMIT`, returned callback `data` becomes the input `CaseData`
  for the next step
- `SUBMITTED` responses are exposed through `submittedResponse()` and do not update the builder's current `CaseData`
- `caseDataBefore` is updated automatically to the previous non-submitted step's input
- if you forget to set `eventId(...)`, the builder fails fast
- `caseDataBefore(...)` is there for real callback flows that depend on CCD `caseDetailsBefore`, such as
  `TakeCaseOfflineWorkflowTest`

### `CaseDataTemplates`

This loads minimal JSON start states for both `CaseData` and `GeneralApplicationCaseData` from:

```text
src/integrationTest/resources/templates/case-data/
```

Use templates for the stable starting state.
Use fixture helpers for callback-specific shaping.

### `DashboardWorkflowIntegrationTest`

Use this only when the callback creates dashboard records and you need repository cleanup between tests.

It extends `WorkflowIntegrationTest`.

If your assertions are mainly on callback response and returned `CaseData`, do not use the dashboard base.

## How a Workflow Test Works

The pattern is:

1. Start from a minimal valid `CaseData`
2. Add only the fields needed for the scenario
3. Execute one or more callback steps
4. Assert on the callback result
5. Assert on side effects only if they are part of the scenario

Example:

```java
startWorkflow(CreateClaimFixtures.caseData())
    .eventId(CaseEvent.CREATE_CLAIM)
    .aboutToStart()
    .mid("start-claim")
    .then(result -> {
        assertThat(result.response().getErrors()).isNullOrEmpty();
        assertThat(result.caseData().getClaimStarted()).isEqualTo(YES);
    });
```

## How To Add a New Test

### Step 1: Decide the right package

Put the test in:

- `workflow/ccd` for normal callback-response or case-data workflow tests
- `workflow/dashboard` for callbacks whose main value is persisted dashboard side effects
- `workflow/ga` for GA callbacks that need `GeneralApplicationCaseData`

Rule of thumb:

- if you assert mainly on `result.response()` or `result.caseData()`, use `ccd`
- if you assert mainly by calling dashboard APIs or checking dashboard persistence, use `dashboard`

### Step 2: Choose the base class

Use:

- `extends WorkflowIntegrationTest` for normal CCD workflow tests
- `extends DashboardWorkflowIntegrationTest` for dashboard-side-effect tests
- `extends GAWorkflowIntegrationTest` for GA callback workflow tests

### Step 3: Create or reuse a fixture helper

Each workflow family owns its own fixtures.

Examples:

- `workflow/ccd/fixture/TakeCaseOfflineFixtures`
- `workflow/dashboard/fixture/OrderMadeClaimantFixtures`
- `workflow/ga/fixture/StartGaBusinessProcessFixtures`

A fixture helper should:

- load a minimal template with `CaseDataTemplates.load(...)`
- apply only the data needed by that workflow family
- expose `caseData()`
- expose `caseReference()` if the test needs it
- stay small and obvious

Good:

```java
public static CaseData caseData() {
    return CaseDataTemplates.load("create-claim-start");
}
```

Also fine when the test needs a little shaping:

```java
public static CaseData caseData() {
    return CaseDataTemplates.load("claim-issued", template ->
        CaseDataTemplates.set(template, "respondent1", new PartyBuilder().individual().build())
    );
}
```

GA example:

```java
public static GeneralApplicationCaseData caseData() {
    return CaseDataTemplates.load("ga/application-submitted", GeneralApplicationCaseData.class).copy()
        .parentCaseReference("1644495739087775")
        .build();
}
```

Bad signs:

- one giant shared fixture for unrelated workflows
- fixture methods with unclear names like `buildCase1()` or `defaultCase()`
- hidden logic that mutates lots of fields for no obvious reason

### Step 4: Add a new JSON template only if you need one

Create a new template in:

```text
src/integrationTest/resources/templates/case-data/
```

Use a name that matches the resource or scenario it supports.

Examples:

- `claim-issued.json`
- `create-claim-start.json`
- `hearing-scheduled-claimant.json`

Template rules:

- keep it minimal
- keep it valid
- do not put every possible field into it
- do not create near-duplicates without a reason

If an existing template is close enough, reuse it and shape the rest in the fixture.

### Step 5: Stub only what your scenario needs

`WorkflowIntegrationTest` already handles common workflow setup.

In your concrete test, mock only the dependencies needed for the scenario:

- feature toggles
- IDAM lookups
- location reference data
- any other boundary the callback genuinely depends on

Example:

```java
@MockBean
private FeatureToggleService featureToggleService;

@BeforeEach
void setup() {
    when(featureToggleService.isFeatureEnabled("example-toggle")).thenReturn(true);
}
```
If a scenario needs IDAM behaviour, add the relevant mock/stub in that concrete test rather than putting it in the
workflow base.

### Step 6: Write the workflow

Start from the fixture:

```java
CaseData fixture = TakeCaseOfflineFixtures.caseData();
```

Then execute the flow:

```java
startWorkflow(fixture)
    .eventId(CaseEvent.TAKE_CASE_OFFLINE)
    .aboutToSubmit()
    .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty())
    .then(result -> assertThat(result.caseData().getTakenOfflineDate()).isNotNull());
```

### Step 7: Assert at the right level

Prefer assertions that prove behaviour:

- errors returned by the callback
- fields added, removed, or changed in `CaseData`
- resulting state
- expected service-owned side effects

Avoid weak assertions like:

- only checking HTTP 200
- asserting that the response body is non-null and stopping there
- verifying many internal mocks that do not matter to the behaviour

## How To Name Things

### Test class names

Use:

```text
<WorkflowName>WorkflowTest
```

Examples:

- `CreateClaimWorkflowTest`
- `TakeCaseOfflineWorkflowTest`
- `OrderMadeClaimantWorkflowTest`

### Fixture class names

Use:

```text
<WorkflowName>Fixtures
```

Examples:

- `CreateClaimFixtures`
- `OrderMadeDefendantFixtures`

### Template names

Use lowercase kebab-case names that describe the start state:

- `claim-issued.json`
- `order-made-claimant.json`

## Choosing Between `ccd` and `dashboard`

Put the test in `ccd` when:

- the main thing you care about is callback validation
- the main thing you care about is callback-returned `CaseData`
- the main thing you care about is workflow state across callback steps

Put the test in `dashboard` when:

- the callback persists dashboard notifications
- the callback updates task-list items
- the real value of the test is checking the persisted side effects after callback execution

Do not put a test in `dashboard` just because the underlying callback event name contains "dashboard". Use the package
that matches what the test is really asserting.

For GA callbacks, prefer `workflow/ga` even when the event name mentions dashboard if the workflow test is proving
controller routing and handler behaviour with mocked external boundaries. The current GA applicant notification example
does this by verifying the outbound dashboard client call.

## Migrating an Existing Test

The goal is not to copy the old test line by line.

The goal is to keep the behaviour and throw away setup noise.

### Migration approach

1. Find the real scenario the old test is proving
2. Ignore setup that exists only because the old test had no workflow base
3. Build a minimal fixture/template start state
4. Recreate the callback steps using `startWorkflow(...)`
5. Keep the important assertions
6. Drop assertions that only test wiring noise unless they still matter

### Good migration candidates

Good candidates usually have one or more of these signs:

- multiple callback phases
- repeated callback request building
- lots of boilerplate mocks
- behaviour that currently lives only in slower API journeys
- assertions on callback data or service-owned side effects

### Example migration shape

Old test style:

- manually build callback payload
- stub many unrelated dependencies
- call controller once
- assert one response field

New workflow test style:

- load one template-backed fixture
- stub only scenario-specific dependencies
- chain the real callback steps
- assert the business result clearly

## Common Mistakes

### 1. Using this pattern for the wrong kind of test

If the code does not go through the CCD callback controller, this pattern is probably the wrong tool.

### 2. Building huge fixtures

If your fixture helper becomes difficult to read, stop and move stable state into a JSON template.

### 3. Creating too many templates

Do not create a new JSON template for every tiny variation.
Templates should represent useful start states, not every single scenario.

### 4. Mocking too much

If the callback logic itself is mocked away, the test has lost most of its value.

### 5. Forgetting what `then(...)` is asserting

Inside `then(...)`, you get a `CallbackResult` with:

- `response()` for `ABOUT_TO_START`, `MID`, and `ABOUT_TO_SUBMIT` callback responses
- `submittedResponse()` for `SUBMITTED` callback responses
- `caseData()` for the rehydrated `CaseData` after non-submitted callback steps
- `rawBody()` for the raw JSON body if you really need it

`caseData()` is rehydrated by applying callback response data over the previous `CaseData`, so chained callbacks keep
their working state. If a callback clears a field and the response omits null values, assert the missing key through
`response().getData()` rather than expecting the merged `caseData()` view to show `null`.

For `SUBMITTED` callbacks, assert confirmation content through `submittedResponse()` or `rawBody()`. Submitted callbacks
do not update the builder's current `CaseData`.

In most tests, `response()`, `submittedResponse()`, and `caseData()` should be enough.

### 6. Forgetting `eventId(...)`

Set the event before calling `aboutToStart()`, `mid(...)`, `aboutToSubmit()`, or `submitted()`.

## Minimal Copy-Paste Template

Use this as a starting point for a normal CCD workflow test:

```java
class ExampleWorkflowTest extends WorkflowIntegrationTest {
  @MockBean
  private FeatureToggleService featureToggleService;

  @BeforeEach
  void setup() {
    when(featureToggleService.someFlag()).thenReturn(true);
  }

  @Test
  void shouldExecuteExampleWorkflow() throws Exception {
    CaseData fixture = ExampleFixtures.caseData();

    startWorkflow(fixture)
      .eventId(CaseEvent.SOME_EVENT)
      .aboutToStart()
      .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty())
      .mid("some-page")
      .then(result -> assertThat(result.caseData().getSomeField()).isEqualTo("expected"))
      .aboutToSubmit()
      .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty());
  }

  @Test
  void shouldAssertSubmittedConfirmationWhenNeeded() throws Exception {
    CaseData fixture = ExampleFixtures.caseData();

    startWorkflow(fixture)
      .eventId(CaseEvent.SOME_EVENT)
      .submitted()
      .then(result -> assertThat(result.submittedResponse().get("confirmationHeader").asText())
        .contains("Expected confirmation"));
  }
}
```

Use this as a starting point for a fixture helper:

```java
public final class ExampleFixtures {

    private static final String EXAMPLE_TEMPLATE = "example-template";

    private ExampleFixtures() {
    }

    public static CaseData caseData() {
        return CaseDataTemplates.load(EXAMPLE_TEMPLATE);
    }
}
```
