package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.claimant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REMOVE_PAYMENT_DASHBOARD_NOTIFICATION;

@ExtendWith(MockitoExtension.class)
public class RemovePaymentDashboardNotificationTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private RemovePaymentDashboardNotification handler;

    @Mock
    private DashboardNotificationService dashboardNotificationService;

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(REMOVE_PAYMENT_DASHBOARD_NOTIFICATION);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldRemoveNotification_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStatePendingClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(REMOVE_PAYMENT_DASHBOARD_NOTIFICATION.name()).build()
            ).build();

            handler.handle(params);

            verify(dashboardNotificationService).deleteByNameAndReferenceAndCitizenRole(
                "Notice.AAA6.ClaimIssue.ClaimFee.Required",
                String.valueOf(caseData.getCcdCaseReference()),
                "CLAIMANT"
            );
        }
    }
}
