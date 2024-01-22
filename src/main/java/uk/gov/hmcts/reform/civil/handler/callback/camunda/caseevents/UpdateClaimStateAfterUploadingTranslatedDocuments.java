package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CLAIM_STATE_AFTER_TRANSLATED_DOCUMENT_UPLOADED;

@Service
@RequiredArgsConstructor
public class UpdateClaimStateAfterUploadingTranslatedDocuments extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(UPDATE_CLAIM_STATE_AFTER_TRANSLATED_DOCUMENT_UPLOADED);
    private Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_SUBMIT), this::updateClaimState);
    private static final String TASK_ID = "updateClaimStateAfterTranslateDocumentUploadedID";
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    private CallbackResponse updateClaimState(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        String changeToState = setClaimState(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .state(changeToState)
            .build();
    }

    private String setClaimState(CaseData caseData) {
        if (CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT == caseData.getCcdState()) {
            return CaseState.AWAITING_APPLICANT_INTENTION.name();
        } else if (caseData.getCcdState().equals(CaseState.CASE_ISSUED)) {
            return CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name();
        }
        return caseData.getCcdState().name();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

}
