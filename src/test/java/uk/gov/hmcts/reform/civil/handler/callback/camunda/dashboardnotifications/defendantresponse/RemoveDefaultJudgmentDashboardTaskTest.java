package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.helper.DashboardNotificationHelper;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DEFENDANT_RESPONSE_SPEC;

@ExtendWith(MockitoExtension.class)
class RemoveDefaultJudgmentDashboardTaskTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 16, 12, 0);

    @Mock
    private Time time;

    @Mock
    private DashboardNotificationService service;
    @Mock
    private DashboardNotificationHelper dashboardNotificationHelper;

    @Test
    void shouldDeleteOnlyTheRetiredNotificationForClaimant() {
        CaseData caseData = caseData(NOW.minusDays(1));
        when(dashboardNotificationHelper.isAwaitingWelshTranslation(caseData, DEFENDANT_RESPONSE_SPEC)).thenReturn(true);
        when(time.now()).thenReturn(NOW);

        new RemoveDefaultJudgmentDashboardTask(dashboardNotificationHelper, service, time).execute(DashboardTaskContext.civil(caseData, "token"));

        verify(service).deleteByNameAndReferenceAndCitizenRole("Notice.AAA6.DefResponse.ResponseTimeElapsed.Claimant", "123", "CLAIMANT");
    }

    @Test
    void shouldSkipRemovalOutsideTheWelshSubmissionBranch() {
        CaseData caseData = caseData(NOW.minusDays(1));

        new RemoveDefaultJudgmentDashboardTask(dashboardNotificationHelper, service, time).execute(DashboardTaskContext.civil(caseData, "token"));

        verifyNoInteractions(service);
    }

    @Test
    void shouldKeepDefaultJudgmentNotificationBeforeResponseDeadline() {
        CaseData caseData = caseData(NOW.plusDays(1));
        when(dashboardNotificationHelper.isAwaitingWelshTranslation(caseData, DEFENDANT_RESPONSE_SPEC)).thenReturn(true);
        when(time.now()).thenReturn(NOW);

        new RemoveDefaultJudgmentDashboardTask(dashboardNotificationHelper, service, time)
            .execute(DashboardTaskContext.civil(caseData, "token"));

        verifyNoInteractions(service);
    }

    @Test
    void shouldKeepDefaultJudgmentNotificationAtResponseDeadline() {
        CaseData caseData = caseData(NOW);
        when(dashboardNotificationHelper.isAwaitingWelshTranslation(caseData, DEFENDANT_RESPONSE_SPEC)).thenReturn(true);
        when(time.now()).thenReturn(NOW);

        new RemoveDefaultJudgmentDashboardTask(dashboardNotificationHelper, service, time)
            .execute(DashboardTaskContext.civil(caseData, "token"));

        verifyNoInteractions(service);
    }

    private CaseData caseData(LocalDateTime deadline) {
        return CaseData.builder().ccdCaseReference(123L).respondent1ResponseDeadline(deadline).build();
    }
}
