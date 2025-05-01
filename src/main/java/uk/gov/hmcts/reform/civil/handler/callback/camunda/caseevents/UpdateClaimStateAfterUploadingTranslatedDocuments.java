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
import uk.gov.hmcts.reform.civil.config.ToggleConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import uk.gov.hmcts.reform.civil.service.UpdateClaimStateService;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CLAIM_STATE_AFTER_DOC_UPLOADED;

@Service
@RequiredArgsConstructor
public class UpdateClaimStateAfterUploadingTranslatedDocuments extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(UPDATE_CLAIM_STATE_AFTER_DOC_UPLOADED);
    private Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_SUBMIT), this::updateClaimState);
    private static final String TASK_ID = "updateClaimStateAfterTranslateDocumentUploadedID";
    private final ObjectMapper objectMapper;
    private final UpdateClaimStateService updateClaimStateService;
    private final ToggleConfiguration toggleConfiguration;

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    private CallbackResponse updateClaimState(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        caseData.setFeatureToggleWA(toggleConfiguration.getFeatureToggle());
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.previousCCDState(caseData.getCcdState());
        String changeToState = setClaimState(caseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .state(changeToState)
            .build();
    }

    private String setClaimState(CaseData caseData) {
        if (CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT == caseData.getCcdState()) {
            return CaseState.AWAITING_APPLICANT_INTENTION.name();
        } else if (CaseState.CASE_ISSUED == caseData.getCcdState()) {
            return CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT.name();
        } else if (isAwaitingApplicantIntentionAndNotSignedSettlementAgreement(caseData)) {
            return updateClaimStateService.setUpCaseState(caseData);
        }
        return caseData.getCcdState().name();
    }

    private boolean isAwaitingApplicantIntentionAndNotSignedSettlementAgreement(CaseData caseData) {
        return CaseState.AWAITING_APPLICANT_INTENTION == caseData.getCcdState()
            && !caseData.hasApplicant1SignedSettlementAgreement();
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
