package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.decisionoutcome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.decisionoutcome.DecisionOutcomeClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.decisionoutcome.DecisionOutcomeDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionOutcomeDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private DecisionOutcomeClaimantDashboardService claimantDashboardService;
    @Mock
    private DecisionOutcomeDefendantDashboardService defendantDashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToDashboardService() {
        DecisionOutcomeClaimantDashboardTask task = new DecisionOutcomeClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyDecisionOutcome(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        DecisionOutcomeDefendantDashboardTask task = new DecisionOutcomeDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyDecisionOutcome(caseData, AUTH_TOKEN);
    }
}
