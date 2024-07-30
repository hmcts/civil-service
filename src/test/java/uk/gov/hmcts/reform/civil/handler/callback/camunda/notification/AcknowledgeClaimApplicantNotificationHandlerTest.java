package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.PART_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.AcknowledgeClaimApplicantNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.AcknowledgeClaimApplicantNotificationHandler.TASK_ID_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_DEADLINE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONSE_INTENTION;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getResponseIntentionForEmail;

@ExtendWith(MockitoExtension.class)
class AcknowledgeClaimApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @InjectMocks
    private AcknowledgeClaimApplicantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotNotifyApplicantSolicitor_whenRecipientIsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .applicantSolicitor1UserDetails(IdamUserDetails.builder().email(null).build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT").build())
                .build();

            handler.handle(params);

            assertThatNoException();
        }

        @Test
        void shouldNotNotifyRespondentSolicitor_whenRecipient1IsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .respondentSolicitor1EmailAddress(null)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT").build())
                .build();

            handler.handle(params);

            assertThatNoException();
        }

        @Test
        void shouldNotNotifyRespondentSolicitor_whenRecipient2IsNull() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateNotificationAcknowledged()
                .respondentSolicitor2EmailAddress(null)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT").build())
                .build();

            handler.handle(params);

            assertThatNoException();
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedWithCcEvent() {
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2WhenSolicitor2RespondsFirst_whenInvokedWithCcEvent() {
            //solicitor 2  acknowledges claim, solicitor 1 does not
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(null)
                .respondent2AcknowledgeNotificationDate(LocalDateTime.now())
                .respondent1ResponseDeadline(null)
                .respondent2ResponseDeadline(LocalDateTime.now().plusDays(14))
                .respondent2ClaimResponseIntentionType(FULL_DEFENCE)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2WhenSolicitor2RespondsLast_whenInvokedWithCcEvent() {
            //solicitor 2 acknowledges claim,solicitor 1 already acknowledged
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2AcknowledgeNotificationDate(LocalDateTime.now())
                .respondent1AcknowledgeNotificationDate(LocalDateTime.now().minusDays(2))
                .respondent1ResponseDeadline(LocalDateTime.now().plusDays(14))
                .respondent2ResponseDeadline(LocalDateTime.now().plusDays(14))
                .respondent2ClaimResponseIntentionType(FULL_DEFENCE)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor1WhenSolicitor1RespondsLast_whenInvokedWithCcEvent() {
            //solicitor 1 acknowledges claim,solicitor 2 already acknowledged
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(LocalDateTime.now())
                .respondent2AcknowledgeNotificationDate(LocalDateTime.now().minusDays(2))
                .respondent2ResponseDeadline(LocalDateTime.now().plusDays(14))
                .respondent1ResponseDeadline(LocalDateTime.now().plusDays(14))
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor1v2SameSolicitor_whenInvokedWithCcEvent() {
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(LocalDateTime.now())
                .respondent2AcknowledgeNotificationDate(LocalDateTime.now().minusDays(2))
                .respondent2ResponseDeadline(LocalDateTime.now().plusDays(14))
                .respondent1ResponseDeadline(LocalDateTime.now().plusDays(14))
                .respondent2ClaimResponseIntentionType(FULL_DEFENCE)
                .respondent1ClaimResponseIntentionType(PART_DEFENCE)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC")
                        .build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2v1SameSolicitor_whenInvokedWithCcEvent() {
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .addApplicant2(YesOrNo.YES)
                .applicant2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(LocalDateTime.now())
                .respondent1ResponseDeadline(LocalDateTime.now().plusDays(14))
                .respondent1ClaimResponseIntentionTypeApplicant2(FULL_DEFENCE)
                .respondent1ClaimResponseIntentionType(PART_DEFENCE)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                    CallbackRequest.builder()
                        .eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor1_whenInvokedWithCcEvent() {
            //solicitor 1 acknowledges claim,solicitor 2 not
            when(notificationsProperties.getRespondentSolicitorAcknowledgeClaim()).thenReturn("template-id");
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent1AcknowledgeNotificationDate(LocalDateTime.now().plusDays(14))
                .respondent2AcknowledgeNotificationDate(null)
                .respondent1ResponseDeadline(LocalDateTime.now())
                .respondent2ResponseDeadline(null)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "acknowledge-claim-applicant-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            // TODO: duplicate code - need to refactor
            LocalDateTime responseDeadline = caseData.getRespondent1ResponseDeadline();
            Party respondent = caseData.getRespondent1();
            MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
            if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
                if ((caseData.getRespondent1AcknowledgeNotificationDate() == null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                    responseDeadline = caseData.getRespondent2ResponseDeadline();
                    respondent = caseData.getRespondent2();
                } else if ((caseData.getRespondent1AcknowledgeNotificationDate() != null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() == null)) {
                    responseDeadline = caseData.getRespondent1ResponseDeadline();
                    respondent = caseData.getRespondent1();
                } else if ((caseData.getRespondent1AcknowledgeNotificationDate() != null)
                    && (caseData.getRespondent2AcknowledgeNotificationDate() != null)) {
                    if (caseData.getRespondent2AcknowledgeNotificationDate()
                        .isAfter(caseData.getRespondent1AcknowledgeNotificationDate())) {
                        responseDeadline = caseData.getRespondent2ResponseDeadline();
                        respondent = caseData.getRespondent2();
                    } else {
                        responseDeadline = caseData.getRespondent1ResponseDeadline();
                        respondent = caseData.getRespondent1();
                    }
                }
            }

            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                RESPONDENT_NAME, respondent.getPartyName(),
                PARTY_REFERENCES, buildPartiesReferences(caseData),
                RESPONSE_DEADLINE, formatLocalDate(responseDeadline.toLocalDate(), DATE),
                RESPONSE_INTENTION, getResponseIntentionForEmail(caseData)
            );
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT").build()).build())).isEqualTo(TASK_ID);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_CLAIM_ACKNOWLEDGEMENT_CC").build()).build())).isEqualTo(TASK_ID_CC);
    }
}
