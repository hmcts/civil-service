package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.bundlecreation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.bundlecreation.BundleCreationClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.bundlecreation.BundleCreationDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BundleCreationDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private BundleCreationClaimantDashboardService claimantService;
    @Mock
    private BundleCreationDefendantDashboardService defendantService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseData.builder().ccdCaseReference(1L).build();

    @BeforeEach
    void setup() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskDelegatesToService() {
        BundleCreationClaimantDashboardTask task = new BundleCreationClaimantDashboardTask(claimantService);

        task.execute(context);

        verify(claimantService).notifyBundleCreated(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskDelegatesToService() {
        BundleCreationDefendantDashboardTask task = new BundleCreationDefendantDashboardTask(defendantService);

        task.execute(context);

        verify(defendantService).notifyBundleCreated(caseData, AUTH_TOKEN);
    }
}
