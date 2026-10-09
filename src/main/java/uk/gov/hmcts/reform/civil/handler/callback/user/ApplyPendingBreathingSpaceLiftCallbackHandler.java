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
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPLY_PENDING_BREATHING_SPACE_LIFT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.LIFT_BREATHING_SPACE_SPEC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Service
@RequiredArgsConstructor
public class ApplyPendingBreathingSpaceLiftCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(APPLY_PENDING_BREATHING_SPACE_LIFT);

    private final ObjectMapper objectMapper;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::applyPendingLift,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    private CallbackResponse applyPendingLift(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (isPendingLiftDue(caseData)) {
            caseData.getBreathing().setActive(NO);
            caseData.getBreathing().setLiftPending(NO);
            caseData.setBusinessProcess(BusinessProcess.ready(LIFT_BREATHING_SPACE_SPEC));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private static boolean isPendingLiftDue(CaseData caseData) {
        BreathingSpaceInfo breathing = caseData.getBreathing();
        if (breathing == null || breathing.getLift() == null || breathing.getLift().getExpectedEnd() == null) {
            return false;
        }

        boolean breathingSpaceStillActive = breathing.getActive() == YES;
        boolean liftIsPending = breathing.getLiftPending() == YES;
        boolean endDateReached = !breathing.getLift().getExpectedEnd().isAfter(LocalDate.now());

        return breathingSpaceStillActive && liftIsPending && endDateReached;
    }
}
