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
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;

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
                    caseData.getRespondentSolicitor1AgreedDeadlineExtension());
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
                invokeAboutToSubmitWithEvent("NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE");

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
                    caseData.getRespondentSolicitor2AgreedDeadlineExtension()
                );

                invokeAboutToSubmitWithEvent("NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
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
                    caseData.getRespondentSolicitor2AgreedDeadlineExtension());
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
                invokeAboutToSubmitWithEvent("NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE");

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
                    caseData.getRespondentSolicitor2AgreedDeadlineExtension()
                );

                invokeAboutToSubmitWithEvent("NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE");

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
                "agreedExtensionDate", formatLocalDate(extensionDate, DATE)
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
            "NOTIFY_RESPONDENT_SOLICITOR2_FOR_AGREED_EXTENSION_DATE").build()).build()))
            .isEqualTo("AgreedExtensionDateNotifyRespondentSolicitor2CC");
    }
}
