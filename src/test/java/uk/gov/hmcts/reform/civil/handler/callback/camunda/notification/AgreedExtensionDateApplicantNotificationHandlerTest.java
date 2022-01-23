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
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.AgreedExtensionDateApplicantNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.AgreedExtensionDateApplicantNotificationHandler.TASK_ID_CC;
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

        @BeforeEach
        void setup() {
            when(notificationsProperties.getClaimantSolicitorAgreedExtensionDate()).thenReturn("template-id");
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "agreed-extension-date-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvokedWithCcEvent() {
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "agreed-extension-date-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2WhenSolicitor2RespondsFirst_whenInvokedWithCcEvent() {
            //solicitor 2  extends the date, solicitor 1 not extended date
            LocalDate extensionDate = now().plusDays(14);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondentSolicitor1AgreedDeadlineExtension(null)
                .respondentSolicitor2AgreedDeadlineExtension(extensionDate)
                .respondent1TimeExtensionDate(null)
                .respondent2TimeExtensionDate(LocalDateTime.now())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "agreed-extension-date-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2WhenSolicitor2RespondsLast_whenInvokedWithCcEvent() {
            //solicitor 2  extends date,solicitor 1 already extended date
            LocalDate extensionDate = now().plusDays(14);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondentSolicitor2AgreedDeadlineExtension(extensionDate.plusDays(1))
                .respondent1TimeExtensionDate(LocalDateTime.now().minusDays(2))
                .respondent2TimeExtensionDate(LocalDateTime.now())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "agreed-extension-date-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor1WhenSolicitor1RespondsLast_whenInvokedWithCcEvent() {
            //solicitor 1  extends date,solicitor 2 already extended date
            LocalDate extensionDate = now().plusDays(14);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondentSolicitor2AgreedDeadlineExtension(extensionDate.plusDays(1))
                .respondent1TimeExtensionDate(LocalDateTime.now())
                .respondent2TimeExtensionDate(LocalDateTime.now().minusDays(2))
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "agreed-extension-date-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor1_whenInvokedWithCcEvent() {
            //solicitor 1  extends date,solicitor 2 not extended date
            LocalDate extensionDate = now().plusDays(14);
            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedTimeExtension()
                .addRespondent2(YES)
                .respondent2SameLegalRepresentative(NO)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondentSolicitor1AgreedDeadlineExtension(extensionDate)
                .respondentSolicitor2AgreedDeadlineExtension(null)
                .respondent1TimeExtensionDate(LocalDateTime.now())
                .respondent2TimeExtensionDate(null)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC").build())
                .build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "agreed-extension-date-applicant-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            LocalDate extensionDate = caseData.getRespondentSolicitor1AgreedDeadlineExtension();
            MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);
            if (multiPartyScenario == ONE_V_TWO_TWO_LEGAL_REP) {
                if ((caseData.getRespondent1TimeExtensionDate() == null)
                    && (caseData.getRespondent2TimeExtensionDate() != null)) {
                    extensionDate = caseData.getRespondentSolicitor2AgreedDeadlineExtension();
                } else if ((caseData.getRespondent1TimeExtensionDate() != null)
                    && (caseData.getRespondent2TimeExtensionDate() != null)) {
                    if (caseData.getRespondent2TimeExtensionDate()
                        .isAfter(caseData.getRespondent1TimeExtensionDate())) {
                        extensionDate = caseData.getRespondentSolicitor2AgreedDeadlineExtension();
                    } else {
                        extensionDate = caseData.getRespondentSolicitor1AgreedDeadlineExtension();
                    }
                }
            }
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                "agreedExtensionDate", formatLocalDate(extensionDate, DATE)
            );
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE").build()).build())).isEqualTo(TASK_ID);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_CC").build()).build())).isEqualTo(TASK_ID_CC);
    }
}
