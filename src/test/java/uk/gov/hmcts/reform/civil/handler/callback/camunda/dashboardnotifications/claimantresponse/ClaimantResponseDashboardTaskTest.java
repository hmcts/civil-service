package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse.ClaimantCcjResponseClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse.ClaimantCcjResponseDefendantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse.ClaimantResponseClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse.ClaimantResponseDefendantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse.JudgmentByAdmissionIssuedDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private ClaimantResponseClaimantDashboardService claimantResponseClaimantDashboardService;
    @Mock
    private ClaimantResponseDefendantDashboardService claimantResponseDefendantDashboardService;
    @Mock
    private ClaimantCcjResponseClaimantDashboardService claimantCcjResponseClaimantDashboardService;
    @Mock
    private ClaimantCcjResponseDefendantDashboardService claimantCcjResponseDefendantDashboardService;
    @Mock
    private JudgmentByAdmissionIssuedClaimantDashboardService judgmentByAdmissionIssuedClaimantDashboardService;
    @Mock
    private JudgmentByAdmissionIssuedDefendantDashboardService judgmentByAdmissionIssuedDefendantDashboardService;
    @Mock
    private DashboardTaskContext context;

    private CaseData caseData;

    @BeforeEach
    void setupContext() {
        caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1L);

        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantResponseClaimantTaskShouldDelegateToService() {
        ClaimantResponseClaimantDashboardTask task =
            new ClaimantResponseClaimantDashboardTask(claimantResponseClaimantDashboardService);

        task.execute(context);

        verify(claimantResponseClaimantDashboardService).notifyClaimant(caseData, AUTH_TOKEN);
    }

    @Test
    void claimantResponseDefendantTaskShouldDelegateToService() {
        ClaimantResponseDefendantDashboardTask task =
            new ClaimantResponseDefendantDashboardTask(claimantResponseDefendantDashboardService);

        task.execute(context);

        verify(claimantResponseDefendantDashboardService).notifyDefendant(caseData, AUTH_TOKEN);
    }

    @Test
    void claimantCcjResponseClaimantTaskShouldDelegateToService() {
        ClaimantCcjResponseClaimantDashboardTask task =
            new ClaimantCcjResponseClaimantDashboardTask(claimantCcjResponseClaimantDashboardService);

        task.execute(context);

        verify(claimantCcjResponseClaimantDashboardService).notifyClaimant(caseData, AUTH_TOKEN);
    }

    @Test
    void claimantCcjResponseDefendantTaskShouldDelegateToService() {
        ClaimantCcjResponseDefendantDashboardTask task =
            new ClaimantCcjResponseDefendantDashboardTask(claimantCcjResponseDefendantDashboardService);

        task.execute(context);

        verify(claimantCcjResponseDefendantDashboardService).notifyDefendant(caseData, AUTH_TOKEN);
    }

    @Test
    void judgmentByAdmissionIssuedClaimantTaskShouldDelegateToService() {
        JudgmentByAdmissionIssuedClaimantDashboardTask task =
            new JudgmentByAdmissionIssuedClaimantDashboardTask(judgmentByAdmissionIssuedClaimantDashboardService);

        task.execute(context);

        verify(judgmentByAdmissionIssuedClaimantDashboardService).notifyClaimant(caseData, AUTH_TOKEN);
    }

    @Test
    void judgmentByAdmissionIssuedDefendantTaskShouldDelegateToService() {
        JudgmentByAdmissionIssuedDefendantDashboardTask task =
            new JudgmentByAdmissionIssuedDefendantDashboardTask(judgmentByAdmissionIssuedDefendantDashboardService);

        task.execute(context);

        verify(judgmentByAdmissionIssuedDefendantDashboardService).notifyDefendant(caseData, AUTH_TOKEN);
    }
}
