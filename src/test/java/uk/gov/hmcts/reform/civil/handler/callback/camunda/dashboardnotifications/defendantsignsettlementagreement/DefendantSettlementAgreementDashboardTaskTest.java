package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendantsignsettlementagreement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantsignsettlementagreement.DefendantSignSettlementAgreementClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantsignsettlementagreement.DefendantSignSettlementAgreementDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantSettlementAgreementDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private DefendantSignSettlementAgreementClaimantDashboardService claimantDashboardService;
    @Mock
    private DefendantSignSettlementAgreementDefendantDashboardService defendantDashboardService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = new CaseDataBuilder().caseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToService() {
        var task = new DefendantSignSettlementAgreementClaimantDashboardTask(claimantDashboardService);

        task.execute(context);

        verify(claimantDashboardService).notifyDefendantSignSettlementAgreement(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToService() {
        var task = new DefendantSignSettlementAgreementDefendantDashboardTask(defendantDashboardService);

        task.execute(context);

        verify(defendantDashboardService).notifyDefendantSignSettlementAgreement(caseData, AUTH_TOKEN);
    }
}
