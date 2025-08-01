package uk.gov.hmcts.reform.civil.handler.callback.user;

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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LIP_CLAIM_SETTLED;

@Service
@RequiredArgsConstructor
public class LIPClaimSettledCallbackHandler extends CallbackHandler {

    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;

    private final Map<String, Callback> callbackMap = Map.of(
        callbackKey(ABOUT_TO_START), this::aboutToStartValidationAndSetup,
        callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
        callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse);

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(
            LIP_CLAIM_SETTLED
        );
    }

    private CallbackResponse aboutToStartValidationAndSetup(CallbackParams callbackParams) {

        List<String> errors = new ArrayList<>();
        CaseData caseData = callbackParams.getCaseData();
        if (featureToggleService.isDefendantNoCOnlineForCase(caseData)
            && caseData.getCcdState() == CaseState.PROCEEDS_IN_HERITAGE_SYSTEM && caseData.isLipvLROneVOne()) {
            errors.add("Event Not Allowed");
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseDataUpdated = callbackParams.getCaseData().toBuilder()
            .previousCCDState(callbackParams.getCaseData().getCcdState())
            .businessProcess(BusinessProcess.ready(LIP_CLAIM_SETTLED))
            .build();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.toMap(objectMapper))
            .build();
    }
}
