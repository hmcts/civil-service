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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.UpdateClaimStateService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CLAIMANT_INTENTION_CLAIM_STATE;

@Service
@RequiredArgsConstructor
public class UpdateClaimantIntentionClaimStateCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(UPDATE_CLAIMANT_INTENTION_CLAIM_STATE);

    public static final String TASK_ID = "updateClaimantIntentionClaimStateID";
    private Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_SUBMIT), this::updateCaseState);
    private final ObjectMapper objectMapper;
    private final UpdateClaimStateService updateClaimStateService;
    private final ToggleConfiguration toggleConfiguration;

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse updateCaseState(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.featureToggleWA(toggleConfiguration.getFeatureToggle());
        caseDataBuilder.previousCCDState(caseData.getCcdState());
        CaseData updatedData = caseDataBuilder.build();
        AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder response =
            AboutToStartOrSubmitCallbackResponse.builder()
                .data(updatedData.toMap(objectMapper));
        if (isClaimantNotBilingualAndNotSignedSettlementAgreement(updatedData)) {
            response.state(updateClaimStateService.setUpCaseState(updatedData));
        }
        return response.build();
    }

    private boolean isClaimantNotBilingualAndNotSignedSettlementAgreement(CaseData caseData) {
        return !caseData.isClaimantBilingual() && !caseData.hasApplicant1SignedSettlementAgreement();
    }
}
