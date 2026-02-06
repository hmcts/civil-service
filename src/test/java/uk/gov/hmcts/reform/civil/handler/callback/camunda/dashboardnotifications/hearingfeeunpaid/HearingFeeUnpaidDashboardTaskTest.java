package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.hearingfeeunpaid;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingfeeunpaid.HearingFeeUnpaidClaimantNotificationService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingfeeunpaid.HearingFeeUnpaidDefendantNotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HearingFeeUnpaidDashboardTaskTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private DashboardTaskContext context;
    @Mock
    private HearingFeeUnpaidClaimantNotificationService claimantNotificationService;
    @Mock
    private HearingFeeUnpaidDefendantNotificationService defendantNotificationService;

    private final CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1L).build();

    @BeforeEach
    void setupContext() {
        when(context.caseData()).thenReturn(caseData);
        when(context.authToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void claimantTaskShouldDelegateToDashboardService() {
        HearingFeeUnpaidClaimantDashboardTask task = new HearingFeeUnpaidClaimantDashboardTask(claimantNotificationService);

        task.execute(context);

        verify(claimantNotificationService).notifyHearingFeeUnpaid(caseData, AUTH_TOKEN);
    }

    @Test
    void defendantTaskShouldDelegateToDashboardService() {
        HearingFeeUnpaidDefendantDashboardTask task = new HearingFeeUnpaidDefendantDashboardTask(defendantNotificationService);

        task.execute(context);

        verify(defendantNotificationService).notifyHearingFeeUnpaid(caseData, AUTH_TOKEN);

    }
}
