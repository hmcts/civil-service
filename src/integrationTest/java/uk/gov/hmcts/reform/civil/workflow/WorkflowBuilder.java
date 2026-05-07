package uk.gov.hmcts.reform.civil.workflow;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Objects;
import java.util.function.Consumer;

public class WorkflowBuilder {

    private final WorkflowIntegrationTest workflowIntegrationTest;
    private CaseData currentCaseData;
    private CaseData caseDataBefore;
    private String eventId;
    private CallbackResult lastResult;

    WorkflowBuilder(WorkflowIntegrationTest workflowIntegrationTest, CaseData currentCaseData) {
        this.workflowIntegrationTest = workflowIntegrationTest;
        this.currentCaseData = currentCaseData;
    }

    public WorkflowBuilder eventId(CaseEvent event) {
        return eventId(event.name());
    }

    public WorkflowBuilder eventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public WorkflowBuilder caseDataBefore(CaseData caseDataBefore) {
        this.caseDataBefore = caseDataBefore;
        return this;
    }

    public WorkflowBuilder aboutToStart() throws Exception {
        return invoke(CallbackType.ABOUT_TO_START, null);
    }

    public WorkflowBuilder mid(String pageId) throws Exception {
        return invoke(CallbackType.MID, pageId);
    }

    public WorkflowBuilder aboutToSubmit() throws Exception {
        return invoke(CallbackType.ABOUT_TO_SUBMIT, null);
    }

    public WorkflowBuilder then(Consumer<CallbackResult> assertions) {
        assertions.accept(lastResult());
        return this;
    }

    public CallbackResult lastResult() {
        if (lastResult == null) {
            throw new IllegalStateException("No callback has been executed yet");
        }
        return lastResult;
    }

    private WorkflowBuilder invoke(CallbackType callbackType, String pageId) throws Exception {
        Objects.requireNonNull(currentCaseData, "caseData must be provided");
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalStateException("eventId must be set before executing a workflow callback");
        }

        // Each callback step feeds its resulting CaseData into the next step by default.
        lastResult = workflowIntegrationTest.invokeCallback(
            currentCaseData,
            caseDataBefore,
            eventId,
            callbackType,
            pageId
        );
        caseDataBefore = currentCaseData;
        currentCaseData = lastResult.caseData();
        return this;
    }

    public record CallbackResult(
        AboutToStartOrSubmitCallbackResponse response,
        CaseData caseData,
        String rawBody
    ) {
    }
}
