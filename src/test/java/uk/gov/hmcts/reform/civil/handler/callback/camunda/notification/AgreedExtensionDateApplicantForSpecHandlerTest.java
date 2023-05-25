package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackException;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.fetchDefendantName;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    AgreedExtensionDateApplicantForSpecNotificationHandler.class,
    JacksonAutoConfiguration.class
})
public class AgreedExtensionDateApplicantForSpecHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private AgreedExtensionDateApplicantForSpecNotificationHandler handler;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationsProperties notificationsProperties;

    @MockBean
    private OrganisationService organisationService;

    @Nested
    class AboutToSubmitCallback {

        final String templateId = "template-id";

        final String templateIdRespondent = "template-id-respondent";
        final String reference = "agreed-extension-date-applicant-notification-spec-000DC001";
        Map<String, String> expectedNotificationData;

        Map<String, String> expectedNotificationDataRespondent;
        CaseData caseData;

        @BeforeEach
        void setup() {
            when(notificationsProperties.getClaimantSolicitorAgreedExtensionDateForSpec())
                .thenReturn("template-id");
            when(notificationsProperties.getRespondentSolicitorAgreedExtensionDateForSpec())
                .thenReturn("template-id-respondent");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
        }

        @Nested
        class WhenRespondent1SubmitsTimeExtension {

            @BeforeEach
            void setup() {
                caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension().build();
                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondentSolicitor1AgreedDeadlineExtension());
                expectedNotificationDataRespondent = getNotificationDataMapRespondent(
                    caseData.getRespondentSolicitor1AgreedDeadlineExtension());
            }

            @Test
            void shouldNotifyApplicantSolicitor_whenInvoked() {
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC");

                verify(notificationService).sendMail(
                    "applicantsolicitor@example.com",
                    templateId,
                    expectedNotificationData,
                    reference
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor1_whenInvoked() {
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }

            @org.junit.jupiter.api.Test
            void shouldNotifyRespondentSolicitor2_whenInvoked() {
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }
        }

        @Nested
        class WhenRespondent2SubmitsTimeExtension {

            @BeforeEach
            void setup() {
                caseData = CaseDataBuilder.builder().atStateNotificationAcknowledgedRespondent1TimeExtension().build();
                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondentSolicitor1AgreedDeadlineExtension());
                expectedNotificationDataRespondent = getNotificationDataMapRespondent(
                    caseData.getRespondentSolicitor1AgreedDeadlineExtension());
            }

            @Test
            void shouldNotifyRespondentSolicitor2() {
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }

            @Test
            void shouldNotifyWithCorrectExtensionDate_whenRespondentSolicitor1TimeExtensionExists() {
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .build();

                expectedNotificationDataRespondent = getNotificationDataMapRespondent(
                    caseData.getRespondentSolicitor2AgreedDeadlineExtension()
                );

                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
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

                expectedNotificationDataRespondent = getNotificationDataMapRespondent(
                    caseData.getRespondentSolicitor2AgreedDeadlineExtension()
                );

                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }

            @org.junit.jupiter.api.Test
            void shouldNotifyWithCorrectExtensionDate_when1v2DSRespondentSolicitor1ExtendsFirst() {
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .respondent1(PartyBuilder.builder().individual().build())
                    .addRespondent2(YES)
                    .respondent2SameLegalRepresentative(NO)
                    .respondent2(PartyBuilder.builder().soleTrader().build())
                    .respondent1TimeExtensionDate(LocalDateTime.now().minusDays(1))
                    .build();

                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }

            @org.junit.jupiter.api.Test
            void shouldNotifyWithCorrectExtensionDate_whenRespondentSolicitor2ExtendsFirst() {
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .respondent2TimeExtensionDate(LocalDateTime.now().minusDays(1))
                    .build();

                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent2ResponseDeadline().toLocalDate()
                );

                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }

            @org.junit.jupiter.api.Test
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

                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }

            @Test
            void shouldNotifyRespondentSolicitor2ClaimStatementOfTruth_whenNoOrgFound() {
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .respondent2TimeExtensionDate(LocalDateTime.now().minusDays(1))
                    .build();

                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent2ResponseDeadline().toLocalDate()
                );
                when(organisationService.findOrganisationById(anyString()))
                    .thenReturn(Optional.empty());
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }

            @Test
            void shouldThrowException_whenInvalidEventId() {
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .respondent2TimeExtensionDate(LocalDateTime.now().minusDays(1))
                    .build();

                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent2ResponseDeadline().toLocalDate()
                );
                CallbackException ex = assertThrows(CallbackException.class, () -> invokeAboutToSubmitWithEvent("Invalid Event"),
                                                    "A CallbackException was expected to be thrown but wasn't.");
                assertThat(ex.getMessage()).contains("Callback handler received unexpected event id");
            }

            @Test
            void testNullRespondentSolicitor1AgreedDeadlineExtension() {
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .respondent2TimeExtensionDate(LocalDateTime.now().minusDays(1))
                    .build();

                expectedNotificationData = getNotificationDataMap(
                    caseData.getRespondent2ResponseDeadline().toLocalDate()
                );
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }

            @Test
            void testRespondent2AgreedDeadlineExtensionIsAfterRespondent1() {
                caseData = CaseDataBuilder.builder()
                    .atStateNotificationAcknowledgedRespondent2TimeExtension()
                    .atStateNotificationAcknowledgedRespondent1TimeExtension()
                    .respondentSolicitor2AgreedDeadlineExtension(LocalDate.now().plusDays(10))
                    .build();

                expectedNotificationDataRespondent = getNotificationDataMapRespondent(
                    caseData.getRespondentSolicitor2AgreedDeadlineExtension()
                );
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor2@example.com",
                    templateIdRespondent,
                    expectedNotificationDataRespondent,
                    reference
                );
            }
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(LocalDate extensionDate) {
            return Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                AGREED_EXTENSION_DATE, formatLocalDate(extensionDate, DATE),
                DEFENDANT_NAME, fetchDefendantName(caseData)
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapRespondent(LocalDate extensionDate) {
            return Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name",
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                AGREED_EXTENSION_DATE, formatLocalDate(extensionDate, DATE)
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
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC").build()).build()))
            .isEqualTo("AgreedExtensionDateNotifyApplicantSolicitor1ForSpec");

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC").build()).build()))
            .isEqualTo("AgreedExtensionDateNotifyRespondentSolicitor1CCForSpec");

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC").build()).build()))
            .isEqualTo("AgreedExtensionDateNotifyRespondentSolicitor2CCForSpec");

    }

    @Test
    void testHandledEvents() {
        assertThat(handler.handledEvents()).contains(NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC);
        assertThat(handler.handledEvents()).contains(NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC);
        assertThat(handler.handledEvents()).contains(NOTIFY_APPLICANT_RESPONDENT2_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC);
    }
}
