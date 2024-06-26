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
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MOVE_TO_DECISION_OUTCOME;
import static uk.gov.hmcts.reform.civil.enums.CaseState.DECISION_OUTCOME;

@Service
@RequiredArgsConstructor
public class DecisionOutcomeCallbackHandler extends CallbackHandler {

    private final ObjectMapper objectMapper;
    protected final FeatureToggleService featureToggleService;
    private static final List<CaseEvent> EVENTS = Collections.singletonList(MOVE_TO_DECISION_OUTCOME);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::changeState,
        callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse);
    }

    private CallbackResponse changeState(CallbackParams callbackParams) {
        if (featureToggleService.isCaseProgressionEnabled()) {
            CaseData caseData = callbackParams.getCaseData().toBuilder()
                .build();
            CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
            caseDataBuilder.businessProcess(BusinessProcess.ready(MOVE_TO_DECISION_OUTCOME));
            return AboutToStartOrSubmitCallbackResponse.builder()
                .state(DECISION_OUTCOME.name()).data(caseDataBuilder.build().toMap(objectMapper))
                .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .state(DECISION_OUTCOME.name())
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
