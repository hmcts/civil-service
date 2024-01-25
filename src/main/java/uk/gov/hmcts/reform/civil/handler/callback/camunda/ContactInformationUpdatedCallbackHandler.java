package uk.gov.hmcts.reform.civil.handler.callback.camunda;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CONTACT_INFORMATION_UPDATED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class ContactInformationUpdatedCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        CONTACT_INFORMATION_UPDATED
    );
    private static final String TASK_ID = "ContactInformationUpdated";
    private final RuntimeService runTimeService;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::handleContactInformationUpdated);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    private CallbackResponse handleContactInformationUpdated(CallbackParams callbackParams) {
        CaseData caseData =  callbackParams.getCaseData();
        updateCamundaVars(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toBuilder().contactDetailsUpdatedEvent(null).build().toMap(objectMapper))
            .build();
    }

    private void updateCamundaVars(CaseData caseData) {
        runTimeService.setVariable(
            caseData.getBusinessProcess().getProcessInstanceId(),
            "submittedByCaseworker",
            YES.equals(caseData.getContactDetailsUpdatedEvent().getSubmittedByCaseworker())
        );
    }
}
