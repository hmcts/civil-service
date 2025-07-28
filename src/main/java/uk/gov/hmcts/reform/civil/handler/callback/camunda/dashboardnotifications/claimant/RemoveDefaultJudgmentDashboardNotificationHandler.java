package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REMOVE_CLAIMANT_DJ_DASHBOARD_NOTIFICATION;

@Service
@RequiredArgsConstructor
public class RemoveDefaultJudgmentDashboardNotificationHandler extends CallbackHandler {

    private static final String DJ_NOTIFICATION = "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant";
    private static final String CLAIMANT_ROLE = "CLAIMANT";
    private final DashboardNotificationService dashboardNotificationService;

    private static final List<CaseEvent> EVENTS = List.of(REMOVE_CLAIMANT_DJ_DASHBOARD_NOTIFICATION);

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::removePaymentDashboardNotification
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse removePaymentDashboardNotification(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getRespondent1ResponseDeadline().isBefore(LocalDateTime.now())) {
            dashboardNotificationService.deleteByNameAndReferenceAndCitizenRole(DJ_NOTIFICATION,
                                                                                String.valueOf(caseData.getCcdCaseReference()),
                                                                                CLAIMANT_ROLE
            );
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }
}
