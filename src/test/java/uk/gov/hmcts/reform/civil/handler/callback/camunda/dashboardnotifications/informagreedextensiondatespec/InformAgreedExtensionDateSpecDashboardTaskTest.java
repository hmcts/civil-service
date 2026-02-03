package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.informagreedextensiondatespec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.informagreedextensiondatespec.InformAgreedExtensionDateSpecClaimantDashboardService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.informagreedextensiondatespec.InformAgreedExtensionDateSpecDefendantDashboardService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InformAgreedExtensionDateSpecDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer";

    @Mock
    private InformAgreedExtensionDateSpecClaimantDashboardService claimantService;
    @Mock
    private InformAgreedExtensionDateSpecDefendantDashboardService defendantService;
    @Mock
    private DashboardTaskContext context;

    private final CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(111L).build();

    @BeforeEach
    void setUp() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskDelegatesToService() {
        InformAgreedExtensionDateSpecClaimantDashboardTask task =
            new InformAgreedExtensionDateSpecClaimantDashboardTask(claimantService);

        task.execute(context);

        verify(claimantService).notifyInformAgreedExtensionDateSpec(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskDelegatesToService() {
        InformAgreedExtensionDateSpecDefendantDashboardTask task =
            new InformAgreedExtensionDateSpecDefendantDashboardTask(defendantService);

        task.execute(context);

        verify(defendantService).notifyInformAgreedExtensionDateSpec(caseData, AUTH_TOKEN);
    }
}
