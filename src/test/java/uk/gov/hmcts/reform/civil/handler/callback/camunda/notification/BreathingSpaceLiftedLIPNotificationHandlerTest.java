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
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.BreathingSpaceLiftedLIPNotificationHandler.TASK_ID_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.BreathingSpaceLiftedLIPNotificationHandler.TASK_ID_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilderSpec.LEGACY_CASE_REFERENCE;

@SpringBootTest(classes = {
    BreathingSpaceLiftedLIPNotificationHandler.class,
    JacksonAutoConfiguration.class,
})
class BreathingSpaceLiftedLIPNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private BreathingSpaceLiftedLIPNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        private static final String APPLICANT_EMAIL_TEMPLATE = "template-id-applicant";
        private static final String RESPONDENT_EMAIL_TEMPLATE = "template-id-respondent";
        private static final String CLAIMANT_EMAIL_ID = "claimant@email.com";
        private static final String RESPONDENT_EMAIL_ID = "respondent@email.com";
        private static final String REFERENCE_NUMBER = "notify-breathing-space-lifted-000DC001";
        private static final String CLAIMANT = "Mr. John Rambo";
        private static final String DEFENDANT = "Mr. Sole Trader";

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifyLiPApplicantBreathingSpaceLifted()).thenReturn(APPLICANT_EMAIL_TEMPLATE);
            when(notificationsProperties.getNotifyLiPRespondentBreathingSpaceLifted()).thenReturn(RESPONDENT_EMAIL_TEMPLATE);
        }

        @Test
        void shouldNotifyApplicant_BreathingSpaceLifted() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .partyEmail(CLAIMANT_EMAIL_ID)
                                .build())
                .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                                 .build())
                .build();

            CallbackParams params = CallbackParams.builder().type(ABOUT_TO_SUBMIT)
                                    .caseData(caseData)
                                    .request(CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED.name()).build())
                                    .build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                CLAIMANT_EMAIL_ID,
                APPLICANT_EMAIL_TEMPLATE,
                getNotificationDataMap(caseData),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyRespondent_BreathingSpaceLifted() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimSubmitted().build().toBuilder()
                .applicant1(PartyBuilder.builder().individual().build().toBuilder()
                                .partyEmail(CLAIMANT_EMAIL_ID)
                                .build())
                .respondent1(PartyBuilder.builder().soleTrader().build().toBuilder()
                                 .partyEmail(RESPONDENT_EMAIL_ID)
                                 .build())
                .build();

            CallbackParams params = CallbackParams.builder().type(ABOUT_TO_SUBMIT)
                .caseData(caseData)
                .request(CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_BREATHING_SPACE_LIFTED.name()).build())
                .build();

            // When
            handler.handle(params);

            // Then
            verify(notificationService, times(1)).sendMail(
                RESPONDENT_EMAIL_ID,
                RESPONDENT_EMAIL_TEMPLATE,
                getNotificationDataMap(caseData),
                REFERENCE_NUMBER
            );
        }

        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                CLAIMANT_NAME, CLAIMANT,
                RESPONDENT_NAME, DEFENDANT
            );
        }

    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            CaseEvent.NOTIFY_LIP_RESPONDENT_BREATHING_SPACE_LIFTED.name()).build()).build())).isEqualTo(TASK_ID_RESPONDENT);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            CaseEvent.NOTIFY_LIP_APPLICANT_BREATHING_SPACE_LIFTED.name()).build()).build())).isEqualTo(TASK_ID_APPLICANT);
    }

}
