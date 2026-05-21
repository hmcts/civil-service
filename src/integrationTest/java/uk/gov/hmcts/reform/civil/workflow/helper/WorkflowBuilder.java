package uk.gov.hmcts.reform.civil.workflow.helper;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;

import java.util.Objects;
import java.util.function.Consumer;

public class WorkflowBuilder<T> {

    private final CallbackInvoker<T> callbackInvoker;
    private T current;
    private T caseDataBefore;
    private String eventId;
    private CallbackResult<T> lastResult;

    public WorkflowBuilder(CallbackInvoker<T> callbackInvoker, T current) {
        this.callbackInvoker = callbackInvoker;
        this.current = current;
    }

    public WorkflowBuilder<T> eventId(CaseEvent event) {
        return eventId(event.name());
    }

    public WorkflowBuilder<T> eventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public WorkflowBuilder<T> caseDataBefore(T caseDataBefore) {
        this.caseDataBefore = caseDataBefore;
        return this;
    }

    public WorkflowBuilder<T> aboutToStart() throws Exception {
        return invoke(CallbackType.ABOUT_TO_START, null);
    }

    public WorkflowBuilder<T> mid(String pageId) throws Exception {
        return invoke(CallbackType.MID, pageId);
    }

    public WorkflowBuilder<T> aboutToSubmit() throws Exception {
        return invoke(CallbackType.ABOUT_TO_SUBMIT, null);
    }

    public WorkflowBuilder<T> submitted() throws Exception {
        return invoke(CallbackType.SUBMITTED, null);
    }

    public WorkflowBuilder<T> then(Consumer<CallbackResult<T>> assertions) {
        assertions.accept(lastResult());
        return this;
    }

    public CallbackResult<T> lastResult() {
        if (lastResult == null) {
            throw new IllegalStateException("No callback has been executed yet");
        }
        return lastResult;
    }

    private WorkflowBuilder<T> invoke(CallbackType callbackType, String pageId) throws Exception {
        Objects.requireNonNull(current, "caseData must be provided");
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalStateException("eventId must be set before executing a workflow callback");
        }

        lastResult = callbackInvoker.invoke(
            current,
            caseDataBefore,
            eventId,
            callbackType,
            pageId
        );

        // ABOUT_TO_START, MID and ABOUT_TO_SUBMIT return callback data that becomes the input to the next step.
        // SUBMITTED responses do not return case data in that shape, so the builder leaves currentCaseData unchanged.
        if (lastResult.response() != null) {
            caseDataBefore = current;
            current = lastResult.caseData();
        }
        return this;
    }

    @FunctionalInterface
    @SuppressWarnings("java:S112")
    public interface CallbackInvoker<T> {
        CallbackResult<T> invoke(
            T currentCaseData,
            T caseDataBefore,
            String eventId,
            CallbackType callbackType,
            String pageId
        ) throws Exception;
    }

    public record CallbackResult<T>(
        AboutToStartOrSubmitCallbackResponse response,
        JsonNode submittedResponse,
        T caseData,
        String rawBody
    ) {
    }
}
