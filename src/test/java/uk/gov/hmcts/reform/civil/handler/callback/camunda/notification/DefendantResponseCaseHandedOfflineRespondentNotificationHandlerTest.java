package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REASON;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_ONE_RESPONSE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_TWO_RESPONSE;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    DefendantResponseCaseHandedOfflineRespondentNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class DefendantResponseCaseHandedOfflineRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private DefendantResponseCaseHandedOfflineRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline()).thenReturn("template-id");
        }

        @Test
        void shouldNotifyDefendantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentCounterClaimAfterNotifyDetails().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "defendant-response-case-handed-offline-respondent-notification-000DC001"
            );
        }
    }

    @Nested
    class AboutToSubmitV1Callback {

        @Test
        void shouldNotifyDefendantSolicitor_when1v1Case() {
            when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOffline())
                .thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .version(V_1)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "defendant-response-case-handed-offline-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyDefendantSolicitor1_when1v2Case() {
            when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty())
                .thenReturn("template-id-multiparty");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .version(V_1)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id-multiparty",
                getNotificationDataMapMultiparty(caseData),
                "defendant-response-case-handed-offline-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyDefendantSolicitor2_when1v2Case() {
            when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty())
                .thenReturn("template-id-multiparty");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .multiPartyClaimTwoDefendantSolicitors()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .version(V_1)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CASE_HANDED_OFFLINE")
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id-multiparty",
                getNotificationDataMapMultiparty(caseData),
                "defendant-response-case-handed-offline-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyDefendantSolicitor1_when1v2SameSolicitorCase() {
            when(notificationsProperties.getSolicitorDefendantResponseCaseTakenOfflineMultiparty())
                .thenReturn("template-id-multiparty");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses()
                .multiPartyClaimOneDefendantSolicitor()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .version(V_1)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CASE_HANDED_OFFLINE")
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id-multiparty",
                getNotificationDataMapMultiparty(caseData),
                "defendant-response-case-handed-offline-respondent-notification-000DC001"
            );
        }
    }

    private Map<String, String> getNotificationDataMap(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
            REASON, caseData.getRespondent1ClaimResponseType().getDisplayedValue()
        );
    }

    private Map<String, String> getNotificationDataMapMultiparty(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
            RESPONDENT_ONE_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
            RESPONDENT_TWO_NAME, getPartyNameBasedOnType(caseData.getRespondent2()),
            RESPONDENT_ONE_RESPONSE, caseData.getRespondent1ClaimResponseType().getDisplayedValue(),
            RESPONDENT_TWO_RESPONSE, caseData.getRespondent2ClaimResponseType().getDisplayedValue()
        );
    }
}
