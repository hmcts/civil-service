package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.buildPartiesReferences;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.fetchDefendantName;

@SpringBootTest(classes = {
    AgreedExtensionDateApplicantNotificationHandler.class,
    JacksonAutoConfiguration.class
})
class AgreedExtensionDateApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @Autowired
    private AgreedExtensionDateApplicantNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        final String templateId = "template-id";
        final String reference = "agreed-extension-date-applicant-notification-000DC001";
        Map<String, String> expectedNotificationData;
        CaseData caseData;

        @BeforeEach
        void setup() {
            when(notificationsProperties.getClaimantSolicitorAgreedExtensionDate()).thenReturn("template-id");
        }

        @Nested
        class WhenRespondent1SubmitsTimeExtension {

            @BeforeEach
            void setup() {
                caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension().build();
                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent1ResponseDeadline().toLocalDate());
            }

            @Test
            void shouldNotifyApplicantSolicitor_whenInvoked() {
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE");

                verify(notificationService).sendMail(
                    "applicantsolicitor@example.com",
                    templateId,
                    expectedNotificationData,
                    reference
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor1_whenInvoked() {
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    templateId,
                    expectedNotificationData,
                    reference
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor2_whenInvoked() {
                invokeAboutToSubmitWithEvent("NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateId,
                    expectedNotificationData,
                    reference
                );
            }

            @Test
            void shouldNotifyWithCorrectExtensionDate_whenRespondentSolicitor2TimeExtensionExists() {
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension(+5)
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .build();

                Map<String, String> expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent2ResponseDeadline().toLocalDate()
                );

                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    templateId,
                    expectedNotificationData,
                    reference
                );
            }
        }

        @Nested
        class WhenRespondent2SubmitsTimeExtension {

            @BeforeEach
            void setup() {
                caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent2TimeExtension().build();
                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent2ResponseDeadline().toLocalDate());
            }

            @Test
            void shouldNotifyApplicantSolicitor_whenInvoked() {
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE");

                verify(notificationService).sendMail(
                    "applicantsolicitor@example.com",
                    templateId,
                    expectedNotificationData,
                    reference
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor1_whenInvoked() {
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    templateId,
                    expectedNotificationData,
                    reference
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor2() {
                invokeAboutToSubmitWithEvent("NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateId,
                    expectedNotificationData,
                    reference
                );
            }

            @Test
            void shouldNotifyWithCorrectExtensionDate_whenRespondentSolicitor1TimeExtensionExists() {
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .build();

                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent2ResponseDeadline().toLocalDate()
                );

                invokeAboutToSubmitWithEvent("NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateId,
                    expectedNotificationData,
                    reference
                );
            }

            @Test
            void shouldNotifyWithCorrectExtensionDate_whenRespondentSolicitor1ExtendsFirst() {
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .respondent1TimeExtensionDate(LocalDateTime.now().minusDays(1))
                    .build();

                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent2ResponseDeadline().toLocalDate()
                );

                invokeAboutToSubmitWithEvent("NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateId,
                    expectedNotificationData,
                    reference
                );
            }

            @Test
            void shouldNotifyWithCorrectExtensionDate_when1v2DSRespondentSolicitor1ExtendsFirst() {
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .respondent1(PartyBuilder.builder().individual().build())
                    .addRespondent2(YES)
                    .respondent2SameLegalRepresentative(NO)
                    .respondent2(PartyBuilder.builder().soleTrader().build())
                    .respondent1TimeExtensionDate(LocalDateTime.now().minusDays(1))
                    .build();

                invokeAboutToSubmitWithEvent("NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateId,
                    expectedNotificationData,
                    reference
                );
            }

            @Test
            void shouldNotifyWithCorrectExtensionDate_whenRespondentSolicitor2ExtendsFirst() {
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .respondent2TimeExtensionDate(LocalDateTime.now().minusDays(1))
                    .build();

                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent2ResponseDeadline().toLocalDate()
                );

                invokeAboutToSubmitWithEvent("NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateId,
                    expectedNotificationData,
                    reference
                );
            }

            @Test
            void shouldNotifyWithCorrectExtensionDate_when1v2SameSolicitorExtends() {
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .addRespondent2(YES)
                    .respondent2(PartyBuilder.builder().individual().build())
                    .respondent2SameLegalRepresentative(YES)
                    .build();

                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent1ResponseDeadline().toLocalDate()
                );

                invokeAboutToSubmitWithEvent("NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateId,
                    expectedNotificationData,
                    reference
                );
            }
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(LocalDate extensionDate) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                AGREED_EXTENSION_DATE, formatLocalDate(extensionDate, DATE),
                PARTY_REFERENCES, buildPartiesReferences(caseData),
                DEFENDANT_NAME, fetchDefendantName(caseData)
            );
        }

        private void invokeAboutToSubmitWithEvent(String eventId) {
            handler.handle(CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(eventId).build()
            ).build());
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE").build()).build()))
            .isEqualTo("AgreedExtensionDateNotifyApplicantSolicitor1");

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC").build()).build()))
            .isEqualTo("AgreedExtensionDateNotifyRespondentSolicitor1CC");

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE_CC").build()).build()))
            .isEqualTo("AgreedExtensionDateNotifyRespondentSolicitor2CC");
    }
}
