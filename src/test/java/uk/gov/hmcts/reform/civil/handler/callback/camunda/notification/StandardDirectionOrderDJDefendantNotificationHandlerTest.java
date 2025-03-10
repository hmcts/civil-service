package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT2;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        }

        @Test
        void shouldNotifyDefendantSolicitor_whenInvoked() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
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
        void shouldNotifyDefendantSolicitor2Defendants_whenInvoked() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
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

        @Test
        void shouldNotNotifyDefendantSolicitor2Defendants_whenInvokedAndNo2Defendant() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .addRespondent2(NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT2.name())
                             .build())
                .build();
            handler.handle(params);

            verifyNoInteractions(notificationService);
        }

        @Test
        void shouldReturnRespondent1name_whenInvokedAndNoOrgPolicy() {
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
                getNotificationDataMapRes1(),
                "sdo-dj-order-notification-defendant-000DC001"
            );
        }

        @Test
        void shouldReturnRespondent2name_whenInvokedAndNoOrgPolicy() {
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

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id-sdo",
                getNotificationDataMapRes2(),
                "sdo-dj-order-notification-defendant-000DC001"
            );
        }

        @Test
        public void shouldThrowErrorWhenMissingEmail() {
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
            doThrow(new RuntimeException()).when(notificationService).sendMail(isNull(), any(), any(), any());
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .atStateClaimIssued1v2AndOneDefendantDefaultJudgment()
                .respondentSolicitor1EmailAddress(null)
                .respondent1Represented(YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT.name())
                             .caseDetails(CaseDetails.builder().id(123L).build())
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                any(),
                any(),
                any(),
                any()
            );
        }

        @Test
        void shouldThrowErrorWhenInvalidDefendant2Email() {
            final String invalidEmail = "invalidEmail@123";
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Test Org Name").build()));
            doThrow(new RuntimeException()).when(notificationService).sendMail(eq(invalidEmail), any(), any(), any());
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
                .atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors()
                .respondentSolicitor2EmailAddress(invalidEmail)
                .respondent2Represented(YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId(NOTIFY_DIRECTION_ORDER_DJ_DEFENDANT2.name())
                             .caseDetails(CaseDetails.builder().id(123L).build())
                             .build())
                .build();
            handler.handle(params);

            verify(notificationService).sendMail(
                any(),
                any(),
                any(),
                any()
            );
        }

        private Map<String, String> getNotificationDataMap() {
            return Map.of(
                "legalOrgName", "Test Org Name",
                "claimReferenceNumber", "1594901956117591",
                "partyReferences", "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001"
            );
        }

        private Map<String, String> getNotificationDataMapRes1() {
            return Map.of(
                "legalOrgName", "Mr. Sole Trader",
                "claimReferenceNumber", "1594901956117591",
                "partyReferences", "Claimant reference: 12345 - Defendant reference: 6789",
                CASEMAN_REF, "000DC001"
            );
        }

        private Map<String, String> getNotificationDataMapRes2() {
            return Map.of(
                "legalOrgName", "Mr. John Rambo",
                "claimReferenceNumber", "1594901956117591",
                "partyReferences", "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: 01234",
                CASEMAN_REF, "000DC001"
            );
        }
    }
}
