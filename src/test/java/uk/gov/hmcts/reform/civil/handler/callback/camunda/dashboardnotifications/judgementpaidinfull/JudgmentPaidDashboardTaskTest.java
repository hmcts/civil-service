package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.judgementpaidinfull;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.judgementpaidinfull.JudgmentPaidClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.judgementpaidinfull.JudgmentPaidDefendantDashboardService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JudgmentPaidDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private JudgmentPaidClaimantDashboardService claimantDashboardService;
    @Mock
    private JudgmentPaidDefendantDashboardService defendantDashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToDashboardService() {
        JudgmentPaidClaimantDashboardTask task = new JudgmentPaidClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyJudgmentPaidInFull(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        JudgmentPaidDefendantDashboardTask task = new JudgmentPaidDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyJudgmentPaidInFull(caseData, AUTH_TOKEN);
    }
}
