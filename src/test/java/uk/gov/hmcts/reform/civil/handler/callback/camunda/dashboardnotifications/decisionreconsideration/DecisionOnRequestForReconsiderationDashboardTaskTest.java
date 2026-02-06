package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.decisionreconsideration;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.decisionreconsideration.DecisionOnRequestForReconsiderationClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.decisionreconsideration.DecisionOnRequestForReconsiderationDefendantDashboardService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DecisionOnRequestForReconsiderationDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private DecisionOnRequestForReconsiderationClaimantDashboardService claimantDashboardService;
    @Mock
    private DecisionOnRequestForReconsiderationDefendantDashboardService defendantDashboardService;
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
        DecisionOnRequestForReconsiderationClaimantDashboardTask task =
            new DecisionOnRequestForReconsiderationClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyDecisionReconsideration(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        DecisionOnRequestForReconsiderationDefendantDashboardTask task =
            new DecisionOnRequestForReconsiderationDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyDecisionReconsideration(caseData, AUTH_TOKEN);
    }
}
