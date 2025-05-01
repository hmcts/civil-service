package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CreateClaimRespondentNotificationHandler.TASK_ID_EMAIL_APP_SOL_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CreateClaimRespondentNotificationHandler.TASK_ID_EMAIL_FIRST_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CreateClaimRespondentNotificationHandler.TASK_ID_EMAIL_SECOND_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
class CreateClaimRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CreateClaimRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked() {
            when(notificationsProperties.getRespondentSolicitorClaimIssueMultipartyEmailTemplate())
                .thenReturn("multiparty-template-id");

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
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

        @Test
        void shouldNotifyApplicantSolicitor_whenInvokedWithCcEvent() {
            when(notificationsProperties.getRespondentSolicitorClaimIssueMultipartyEmailTemplate())
                .thenReturn("multiparty-template-id");

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullDefence().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC").build()).build();

            handler.handle(params);

            String camundaActivityId = handler.camundaActivityId(params);
            assertEquals(TASK_ID_EMAIL_APP_SOL_CC, camundaActivityId);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "multiparty-template-id",
                getNotificationDataMap(caseData),
                "create-claim-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotNotify_whenApplicantEmailIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(null).build())
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE_CC").build()).build();

            handler.handle(params);
            assertThatNoException();
        }

        @Test
        void shouldNotNotify_whenRespondent1RecipientIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .respondentSolicitor1EmailAddress(null)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE").build()).build();

            handler.handle(params);
            assertThatNoException();
        }

        @Test
        void shouldNotNotify_whenRespondent2RecipientIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .respondentSolicitor2EmailAddress(null)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE").build()).build();

            handler.handle(params);
            assertThatNoException();
        }

        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                "defendantName", "Mr. Sole Trader",
                "claimDetailsNotificationDeadline", formatLocalDate(NOTIFICATION_DEADLINE.toLocalDate(), DATE),
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, "000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvolked_InOneVsTwoCaseSameSolicitor() {
            when(notificationsProperties.getRespondentSolicitorClaimIssueMultipartyEmailTemplate())
                .thenReturn("multiparty-template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIM_ISSUE")
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "multiparty-template-id",
                getNotificationDataMap(caseData),
                "create-claim-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvolked_InOneVsTwoCaseDifferentSolicitor() {
            when(notificationsProperties.getRespondentSolicitorClaimIssueMultipartyEmailTemplate())
                .thenReturn("multiparty-template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder()
                .of(ABOUT_TO_SUBMIT, caseData)
                .request(CallbackRequest.builder()
                             .eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIM_ISSUE")
                             .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "multiparty-template-id",
                getNotificationDataMap(caseData),
                "create-claim-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedWithMultipartyEnabled() {
            when(notificationsProperties.getRespondentSolicitorClaimIssueMultipartyEmailTemplate())
                .thenReturn("multiparty-template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateRespondentFullDefence()
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
