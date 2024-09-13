package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT2;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
public class StandardDirectionOrderDJDefendantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private StandardDirectionOrderDJDefendantNotificationHandler handler;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private ObjectMapper objectMapper;

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            when(notificationsProperties.getStandardDirectionOrderDJTemplate()).thenReturn("template-id-sdo");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
        }

        @Test
        void shouldNotifyClaimantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()

                .build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT.name())
                             .build())
                .build();
            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id-sdo",
                getNotificationDataMap(),
                "sdo-dj-order-notification-defendant-000DC001"
            );
        }

        @Test
        void shouldNotifyClaimantSolicitor2Defendants_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT2.name())
                             .build())
                .build();
            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                anyString(),
                eq("template-id-sdo"), anyMap(),
                eq("sdo-dj-order-notification-defendant-000DC001"));
        }

        private Map<String, String> getNotificationDataMap() {
            return Map.of(
                "legalOrgName", "Test Org Name",
                "claimReferenceNumber", "000DC001"
            );
        }
    }
}
