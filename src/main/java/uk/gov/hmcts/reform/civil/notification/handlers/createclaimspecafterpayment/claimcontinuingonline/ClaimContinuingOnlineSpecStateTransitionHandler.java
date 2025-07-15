package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment.claimcontinuingonline;

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
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CLAIM_CONTINUING_ONLINE_SPEC_STATE_TRANSITION;

@Service
@RequiredArgsConstructor
public class ClaimContinuingOnlineSpecStateTransitionHandler extends CallbackHandler {

    public static final String TASK_ID = "ClaimContinuingOnlineSpecStateTransition";
    private final ObjectMapper objectMapper;
    private final Time time;
    private static final List<CaseEvent> EVENTS = List.of(CLAIM_CONTINUING_ONLINE_SPEC_STATE_TRANSITION);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::claimContinuingOnlineSpecStateTransition
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse claimContinuingOnlineSpecStateTransition(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime claimNotificationDate = time.now();

        final CaseData.CaseDataBuilder caseDataBuilder
                = caseData.toBuilder().claimNotificationDate(claimNotificationDate);

        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataBuilder.build().toMap(objectMapper))
                .state("AWAITING_RESPONDENT_ACKNOWLEDGEMENT")
                .build();
    }
}
