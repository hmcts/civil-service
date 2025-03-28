package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SETTLE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_SETTLED;

@Service
@RequiredArgsConstructor
public class SettleClaimCallbackHandler extends CallbackHandler {

    protected final ObjectMapper objectMapper;

    private static final List<CaseEvent> EVENTS = List.of(SETTLE_CLAIM);
    private final TaskListService taskListService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::saveJudgmentPaidInFullDetails,
            callbackKey(SUBMITTED), this::inactivateTaskListAndBuildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse saveJudgmentPaidInFullDetails(CallbackParams callbackParams) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = callbackParams.getCaseData().toBuilder();
        caseDataBuilder.previousCCDState(callbackParams.getCaseData().getCcdState());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .state(CASE_SETTLED.name())
            .build();
    }

    private CallbackResponse inactivateTaskListAndBuildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.isApplicantLiP()) {
            taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingTemplate(caseData.getCcdCaseReference().toString(),
                                                                                  "CLAIMANT", "Application.View");
        }
        if (caseData.isRespondent1LiP()) {
            taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingTemplate(caseData.getCcdCaseReference().toString(),
                                                                                  "DEFENDANT", "Application.View");
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# Claim marked as settled")
            .confirmationBody("<br />")
            .build();
    }
}
