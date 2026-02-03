package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.setasidejudgement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.setasidejudgement.SetAsideJudgementClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.setasidejudgement.SetAsideJudgementDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetAsideJudgementDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private SetAsideJudgementClaimantDashboardService claimantService;
    @Mock
    private SetAsideJudgementDefendantDashboardService defendantService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(444L).build();

    @BeforeEach
    void setUp() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskDelegatesToService() {
        SetAsideJudgementClaimantDashboardTask task =
            new SetAsideJudgementClaimantDashboardTask(claimantService);

        task.execute(context);

        verify(claimantService).notifySetAsideJudgement(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskDelegatesToService() {
        SetAsideJudgementDefendantDashboardTask task =
            new SetAsideJudgementDefendantDashboardTask(defendantService);

        task.execute(context);

        verify(defendantService).notifySetAsideJudgement(caseData, AUTH_TOKEN);
    }
}
