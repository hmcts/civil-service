package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.citizenhearingfeepayment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.citizenhearingfeepayment.CitizenHearingFeePaymentDashboardService;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CitizenHearingFeePaymentDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private CitizenHearingFeePaymentDashboardService dashboardService;
    @Mock
    private CaseData caseData;

    private CitizenHearingFeePaymentDashboardTask task;

    @BeforeEach
    void setUp() {
        task = new CitizenHearingFeePaymentDashboardTask(dashboardService);
    }

    @Test
    void shouldDelegateToService() {
        task.notifyDashboard(caseData, AUTH_TOKEN);

        verify(dashboardService).notifyCitizenHearingFeePayment(caseData, AUTH_TOKEN);
    }
}
