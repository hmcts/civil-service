package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.requestforreconsideration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.requestforreconsideration.RequestForReconsiderationClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.requestforreconsideration.RequestForReconsiderationDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RequestForReconsiderationDashboardTaskTest {

    @Mock
    private RequestForReconsiderationClaimantDashboardService claimantService;
    @Mock
    private RequestForReconsiderationDefendantDashboardService defendantService;

    private final CaseData caseData = CaseData.builder().ccdCaseReference(123L).build();

    @Test
    void shouldNotifyClaimantWhenDefendantRequestsReconsideration() {
        new RequestForReconsiderationClaimantDashboardTask(claimantService)
            .execute(DashboardTaskContext.civil(caseData, "token"));

        verify(claimantService).notifyRequestForReconsideration(caseData, "token");
        verifyNoInteractions(defendantService);
    }

    @Test
    void shouldNotifyDefendantWhenClaimantRequestsReconsideration() {
        new RequestForReconsiderationDefendantDashboardTask(defendantService)
            .execute(DashboardTaskContext.civil(caseData, "token"));

        verify(defendantService).notifyRequestForReconsideration(caseData, "token");
        verifyNoInteractions(claimantService);
    }
}
