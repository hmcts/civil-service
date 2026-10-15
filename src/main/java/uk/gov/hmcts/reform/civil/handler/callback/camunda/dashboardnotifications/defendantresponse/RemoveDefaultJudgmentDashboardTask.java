package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponse;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardServiceTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;

@Component
@RequiredArgsConstructor
public class RemoveDefaultJudgmentDashboardTask extends DashboardServiceTask {

    private static final String DJ_NOTIFICATION = "Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant";
    private static final String CLAIMANT_ROLE = "CLAIMANT";
    private final DashboardNotificationHelper dashboardNotificationHelper;
    private final DashboardNotificationService dashboardNotificationService;

    @Override
    protected void notifyDashboard(CaseData caseData, String authToken) {
        if (!dashboardNotificationHelper.isAwaitingWelshTranslation(caseData, DEFENDANT_RESPONSE_SPEC)) {
            return;
        }
        if (caseData.getRespondent1ResponseDeadline().isBefore(LocalDateTime.now())) {
            dashboardNotificationService.deleteByNameAndReferenceAndCitizenRole(DJ_NOTIFICATION,
                                                                                String.valueOf(caseData.getCcdCaseReference()),
                                                                                CLAIMANT_ROLE
            );
        }
    }
}
