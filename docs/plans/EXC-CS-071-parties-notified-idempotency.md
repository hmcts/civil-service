# EXC-CS-071 — Skip PUT /partiesNotified when already notified

**JIRA:** DTSCCI-5355  
**Severity:** HIGH  
**Estimated volume eliminated:** ~12,000+ unnecessary HMC PUTs/day (~36% failure rate in peak bins)

---

## Problem

`HearingNoticeSchedulerEventHandler.processHearing` always falls through to `notifyHmc(...)` (which issues `PUT /partiesNotified/{hearingId}`) when a hearing is LISTED but nothing has changed since the last notification.  
HMC responds with `400 "003 Already set"` because `partiesNotifiedDateTime` is already populated for that `(hearingId, requestVersion, receivedDateTime)` tuple.

The error is swallowed to `log.error(...)` in `HearingsService.updatePartiesNotifiedResponse:77` and never surfaced as exception telemetry, so it was previously invisible. Fixing EXC-CS-002 (connection-pool exhaustion) unmasked the real volume.

### Relevant files

| File | Role |
|---|---|
| `src/main/java/uk/gov/hmcts/reform/civil/handler/event/HearingNoticeSchedulerEventHandler.java` | Scheduler handler — **where the fix goes** |
| `src/main/java/uk/gov/hmcts/reform/hmc/service/HearingsService.java` | Feign wrapper — `updatePartiesNotifiedResponse:62` catches and re-throws |
| `src/main/java/uk/gov/hmcts/reform/civil/utils/HmcDataUtils.java` | `hearingDataChanged(PartiesNotifiedResponse, HearingGetResponse)` utility |
| `src/test/java/uk/gov/hmcts/reform/civil/handler/event/HearingNoticeSchedulerEventHandlerTest.java` | Unit tests — new test needed |

### Current control flow (lines 70–101)

```
processHearing(hearingId)
  ├─ getHearingResponse()                     // always fetched
  ├─ if LISTED
  │    ├─ getCase()                           // CCD lookup
  │    ├─ getLatestPartiesNotifiedResponse()  // → partiesNotified (may be null)
  │    ├─ if stateAllowed && dataChanged → triggerHearingNoticeEvent() + return
  │    └─ else → fall-through  ← BUG: calls notifyHmc even when already notified
  └─ if not LISTED → fall-through
       └─ notifyHmc()   ← partiesNotified is null here, guard won't fire
```

The second fall-through path (LISTED + data unchanged) is where ~12k/d unnecessary PUTs originate.

---

## Fix — Option 1 (smallest blast radius)

Add a pre-check guard immediately before `notifyHmc(...)` using `partiesNotified`, which is **already fetched** in the LISTED branch. The guard is a no-op for the non-LISTED branch because `partiesNotified` remains `null`.

### Change 1 — `HearingNoticeSchedulerEventHandler.java`

**Location:** `processHearing`, lines 97–101 (just before `notifyHmc` is called).

Replace:

```java
log.info("Updating parties notified for hearing [{}].", hearingId);
PartiesNotifiedServiceData serviceData = (partiesNotified != null && partiesNotified.getServiceData() != null)
    ? partiesNotified.getServiceData()
    : new PartiesNotifiedServiceData();
notifyHmc(hearingId, hearing, serviceData);
```

With:

```java
LocalDateTime hmcReceivedDateTime = hearing.getHearingResponse().getReceivedDateTime();
if (partiesNotified != null
    && partiesNotified.getResponseReceivedDateTime() != null
    && !partiesNotified.getResponseReceivedDateTime().isBefore(hmcReceivedDateTime)
    && !HmcDataUtils.hearingDataChanged(partiesNotified, hearing)) {
    log.debug("Skipping partiesNotified PUT for hearing [{}] — already notified with current data", hearingId);
    return;
}

log.info("Updating parties notified for hearing [{}].", hearingId);
PartiesNotifiedServiceData serviceData = (partiesNotified != null && partiesNotified.getServiceData() != null)
    ? partiesNotified.getServiceData()
    : new PartiesNotifiedServiceData();
notifyHmc(hearingId, hearing, serviceData);
```

**Guard logic explained:**

| Condition | Purpose |
|---|---|
| `partiesNotified != null` | Only applies in the LISTED branch; non-LISTED path keeps calling PUT as before |
| `responseReceivedDateTime != null` | HMC has previously accepted a notification for this hearing |
| `!responseReceivedDateTime.isBefore(hmcReceivedDateTime)` | The accepted notification is for the current hearing version (receivedDateTime key) |
| `!hearingDataChanged(partiesNotified, hearing)` | Hearing days/venue haven't changed since the last notification |

All four must be true to skip. Any one being false means the PUT is still needed.

---

## Tests — `HearingNoticeSchedulerEventHandlerTest.java`

### Tests to add

#### 1. `shouldSkipPartiesNotifiedPut_whenHearingIsListedAndAlreadyNotifiedWithUnchangedData`

Scenario: LISTED, state allowed, data unchanged, and `responseReceivedDateTime` is at or after `RECEIVED_DATETIME`.

Expected: `hearingsService.updatePartiesNotifiedResponse` is **never** called; `runtimeService.createMessageCorrelation` is **never** called.

Setup sketch:
```java
// Set responseReceivedDateTime to RECEIVED_DATETIME (equal → guard fires)
new PartiesNotifiedResponse()
    .setResponseReceivedDateTime(RECEIVED_DATETIME)   // ← at or after hmcReceivedDateTime
    .setServiceData(new PartiesNotifiedServiceData()
        .setDays(List.of(new HearingDay()
            .setHearingStartDateTime(HEARING_DATE)
            .setHearingEndDateTime(HEARING_DATE.plusHours(1))))
        .setHearingLocation(VENUE_ID))
```

Assertions:
```java
verify(hearingsService, times(0)).updatePartiesNotifiedResponse(any(), any(), anyInt(), any(), any());
verify(runtimeService, times(0)).createMessageCorrelation(any());
```

#### 2. `shouldCallPartiesNotifiedPut_whenHearingIsListedAndDataUnchangedButResponseReceivedBeforeCurrentVersion`

Scenario: LISTED, state allowed, data unchanged, but `responseReceivedDateTime` is **before** `RECEIVED_DATETIME` (i.e., the accepted notification is stale — a newer hearing version exists).

Expected: `hearingsService.updatePartiesNotifiedResponse` **is** called once (guard does not fire).

This is the existing test `shouldNotDispatchCamundaMessage_whenHearingDataMatchesLatestHearingResponseData` — it already uses `LocalDateTime.now()` (2026-05-11) as `responseReceivedDateTime`, which is before `RECEIVED_DATETIME` (2029-12-01), so the guard won't fire. **No change needed** to this test — just add a clarifying comment if desired.

### Tests that must remain unchanged

| Test | Why it stays the same |
|---|---|
| `shouldDispatchExpectedCamundaMessage_whenHearingIsInListedStatusAndPartiesNotifiedResponsesIsEmpty` | `partiesNotified` is null → data changed → triggerHearingNoticeEvent → returns early; guard never reached |
| `shouldNotDispatchCamundaMessage_whenHearingIsNotInListedStatus` | Non-LISTED, `partiesNotified` is null → guard condition `partiesNotified != null` is false → PUT still called |
| `shouldAcknowledgeHearingWithoutNotice_whenHearingIsListedAndPartiesNotifiedIsNullAndCaseStateIsDisallowed` | State is CLOSED, `partiesNotified` is null (no responses) → guard condition `partiesNotified != null` is false → PUT still called |
| `shouldDispatchCamundaMessage_whenHearingVenueDoesNotMatchLatestPartiesNotifiedResponseHearingDate` | Data HAS changed → triggerHearingNoticeEvent → returns early; guard never reached |
| `shouldDispatchCamundaMessage_whenHearingVenueDoesNotMatchLatestPartiesNotifiedResponseHearingVenue` | Data HAS changed → triggerHearingNoticeEvent → returns early; guard never reached |

---

## Out of scope for this fix

| Item | Rationale |
|---|---|
| Non-LISTED path guard (fetch `partiesNotified` for CASE_CLOSED etc.) | Broader change; non-LISTED 400s are a much smaller share; low priority vs. blast radius |
| Narrowing EXC-CS-024 bucket | Separate observability task; not a code change |
| Catching 400 "Already set" as success | Couples to HMC error string; still incurs round-trip; explicitly rejected |
| Retry suppression on HmcException(4xx) | Addresses amplification only, not root cause; rejected |

---

## Verification checklist

- [ ] `./gradlew test --tests "*HearingNoticeSchedulerEventHandlerTest"` passes
- [ ] New test `shouldSkipPartiesNotifiedPut_whenHearingIsListedAndAlreadyNotifiedWithUnchangedData` is green
- [ ] All previously-passing handler tests remain green
- [ ] After deploy: monitor AppDependencies for `PUT /partiesNotified/*` 400 rate — expect ~0 within one scheduler cycle
- [ ] Confirm `log.error("Failed to update partiesNotified...")`  lines in civil-service pods drop to near-zero

---

## Related

- **EXC-CS-002 / DTSCCI-5222** — Connection-pool fix that unmasked this issue; fixing this reduces pool pressure and lowers re-occurrence risk
- **EXC-CS-024** — Parent HmcException bucket; the partiesNotified subset should be re-homed here after this fix lands
- **EXC-CS-001** — Same pattern (un-pre-checked retry against upstream); same structural fix