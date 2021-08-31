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
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CreateClaimRespondentNotificationHandler.TASK_ID_EMAIL_APP_SOL_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CreateClaimRespondentNotificationHandler.TASK_ID_EMAIL_FIRST_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CreateClaimRespondentNotificationHandler.TASK_ID_EMAIL_SECOND_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.NOTIFICATION_DEADLINE;

@SpringBootTest(classes = {
    CreateClaimRespondentNotificationHandler.class,
    JacksonAutoConfiguration.class,
})
class CreateClaimRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private CreateClaimRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getRespondentSolicitorClaimIssueEmailTemplate())
                .thenReturn("non-multiparty-template-id");

            when(notificationsProperties.getRespondentSolicitorClaimIssueMultipartyEmailTemplate())
                .thenReturn("multiparty-template-id");
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE").build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "non-multiparty-template-id",
                getNotificationDataMap(caseData),
                "create-claim-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithCcEvent() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC").build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "non-multiparty-template-id",
                getNotificationDataMap(caseData),
                "create-claim-respondent-notification-000DC001"
            );
        }

        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                "defendantName", "Mr. Sole Trader",
                "claimNotificationDeadline", formatLocalDate(NOTIFICATION_DEADLINE.toLocalDate(), DATE)
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvolked_InOneVsTwoCaseSameSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .version(V_1)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE")
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "non-multiparty-template-id",
                getNotificationDataMap(caseData),
                "create-claim-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvolked_InOneVsTwoCaseDifferentSolicitor() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .version(V_1)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_ISSUE")
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "non-multiparty-template-id",
                getNotificationDataMap(caseData),
                "create-claim-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedWithMultipartyEnabled() {
            when(featureToggleService.isMultipartyEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefenceAfterNotificationAcknowledgement()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE").build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "multiparty-template-id",
                getNotificationDataMap(caseData),
                "create-claim-respondent-notification-000DC001"
            );
        }

    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE").build()).build())).isEqualTo(TASK_ID_EMAIL_FIRST_SOL);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC").build()).build())).isEqualTo(TASK_ID_EMAIL_APP_SOL_CC);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_ISSUE").build()).build())).isEqualTo(TASK_ID_EMAIL_SECOND_SOL);
    }
}
