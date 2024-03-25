package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_DEFENDANT_CCJ_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE;

@ExtendWith(MockitoExtension.class)
public class ClaimantCCJResponseDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ClaimantCCJResponseDefendantNotificationHandler handler;

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private DashboardNotificationsParamsMapper dashboardNotificationsParamsMapper;

    public static final String TASK_ID = "GenerateDefendantCCJDashboardNotificationForClaimantResponse";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(CREATE_DEFENDANT_CCJ_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE);
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(
            CallbackParamsBuilder.builder()
                .request(CallbackRequest.builder()
                             .eventId(CREATE_DEFENDANT_CCJ_DASHBOARD_NOTIFICATION_FOR_CLAIMANT_RESPONSE.name())
                             .build())
                .build()))
            .isEqualTo(TASK_ID);
    }

}
