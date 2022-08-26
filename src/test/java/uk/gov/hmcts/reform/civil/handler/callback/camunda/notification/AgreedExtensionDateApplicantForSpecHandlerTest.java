package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.config.properties.notification.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.NotificationService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.AGREED_EXTENSION_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.fetchDefendantName;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    AgreedExtensionDateApplicantForSpecNotificationHandler.class
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

    @MockBean
    private FeatureToggleService toggleService;

    @Test
    public void ldBlock() {
        Mockito.when(toggleService.isLrSpecEnabled()).thenReturn(false, true);
        Assert.assertTrue(handler.handledEvents().isEmpty());
        Assert.assertFalse(handler.handledEvents().isEmpty());
    }

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

            @org.junit.jupiter.api.Test
            void shouldNotifyApplicantSolicitor_whenInvoked() {
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC");

                verify(notificationService).sendMail(
                    "applicantsolicitor@example.com",
                    templateId,
                    expectedNotificationData,
                    reference
                );
            }

            @org.junit.jupiter.api.Test
            void shouldNotifyRespondentSolicitor1_whenInvoked() {
                invokeAboutToSubmitWithEvent("NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC");

                verify(notificationService).sendMail(
                    "respondentsolicitor@example.com",
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

    @org.junit.jupiter.api.Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC").build()).build()))
            .isEqualTo("AgreedExtensionDateNotifyApplicantSolicitor1ForSpec");

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANT_SOLICITOR1_FOR_AGREED_EXTENSION_DATE_FOR_SPEC_CC").build()).build()))
            .isEqualTo("AgreedExtensionDateNotifyRespondentSolicitor1CCForSpec");

    }
}
