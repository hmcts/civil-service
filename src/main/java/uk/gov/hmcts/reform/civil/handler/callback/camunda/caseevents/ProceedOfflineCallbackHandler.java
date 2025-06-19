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
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PROCEEDS_IN_HERITAGE_SYSTEM;

@Service
@RequiredArgsConstructor
public class ProceedOfflineCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(PROCEEDS_IN_HERITAGE_SYSTEM);
    private static final List<String> TASK_IDS =
        Arrays.asList("ProceedOffline", "ProceedOfflineForResponseToDefence", "ProceedOfflineForUnregisteredFirm",
                      "ProceedOfflineForUnRepresentedSolicitor", "ProceedOfflineForNonDefenceResponse");

    private final ObjectMapper objectMapper;
    private final FeatureToggleService featureToggleService;

    @Override
    public List<String> camundaActivityIds(CallbackParams callbackParams) {
        return TASK_IDS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::captureTakenOfflineDate);
    }

    private CallbackResponse captureTakenOfflineDate(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder<?, ?> caseDataUpdated = callbackParams.getCaseData().toBuilder()
            .takenOfflineDate(LocalDateTime.now());

        if (featureToggleService.isLrAdmissionBulkEnabled() && featureToggleService.isJudgmentOnlineLive()) {
            caseDataUpdated.previousCCDState(callbackParams.getCaseData().getCcdState());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.build().toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
