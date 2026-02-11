package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.hearingscheduled;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled.HearingScheduledClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled.HearingScheduledDefendantDashboardService;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingScheduledDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private HearingScheduledClaimantDashboardService claimantDashboardService;
    @Mock
    private HearingScheduledDefendantDashboardService defendantDashboardService;
    @Mock
    private DashboardTaskContext context;
    @Mock
    private CaseData caseData;

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToDashboardService() {
        HearingScheduledClaimantDashboardTask task = new HearingScheduledClaimantDashboardTask(claimantDashboardService);
        task.execute(context);
        verify(context, atLeastOnce()).caseData();
        verify(claimantDashboardService).notifyHearingScheduled(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        HearingScheduledDefendantDashboardTask task = new HearingScheduledDefendantDashboardTask(defendantDashboardService);
        task.execute(context);
        verify(context, atLeastOnce()).caseData();
        verify(defendantDashboardService).notifyHearingScheduled(caseData, AUTH_TOKEN);
    }
}
