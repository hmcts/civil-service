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
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.CreateSDOApplicantsNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;

@SpringBootTest(classes = {
    CreateSDOApplicantsNotificationHandler.class,
    JacksonAutoConfiguration.class
})

class CreateSDOApplicantsNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private OrganisationService organisationService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @Autowired
    private CreateSDOApplicantsNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getSdoOrdered()).thenReturn("template-id");
            when(notificationsProperties.getSdoOrderedSpec()).thenReturn("template-id-spec");
            when(notificationsProperties.getClaimantLipClaimUpdatedTemplate()).thenReturn("template-id-lip");
            when(notificationsProperties.getSdoOrderedSpec()).thenReturn("template-id-spec");
            when(notificationsProperties.getSdoOrderedEA()).thenReturn("template-id-EA");
            when(notificationsProperties.getSdoOrderedSpecEA()).thenReturn("template-id-spec-EA");
            when(notificationsProperties.getNotifyLipUpdateTemplate()).thenReturn("template-id-lip");
            when(notificationsProperties.getNotifyLipUpdateTemplateBilingual()).thenReturn("template-id-lip-bilingual");
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
            when(featureToggleService.isEarlyAdoptersEnabled()).thenReturn(false);
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(),
                "create-sdo-applicants-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantLip_whenInvoked() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder().claimantUserDetails(IdamUserDetails.builder().email("applicantLip@example.com").build())
                .applicant1Represented(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantLip@example.com",
                "template-id-lip",
                getNotificationDataMapLip(),
                "create-sdo-applicants-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantLip_whenInvokedBilingual() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder().claimantUserDetails(IdamUserDetails.builder().email("applicantLip@example.com").build())
                .applicant1Represented(YesOrNo.NO)
                .claimantBilingualLanguagePreference(Language.WELSH.toString())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            // When
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantLip@example.com",
                "template-id-lip-bilingual",
                getNotificationDataMapLip(),
                "create-sdo-applicants-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitorStatement_whenInvoked() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build()
                .toBuilder()
                .applicant1Represented(YesOrNo.YES)
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().organisation(
                    uk.gov.hmcts.reform.ccd.model.Organisation.builder().organisationID("abc1").build()).build())
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .applicantSolicitor1ClaimStatementOfTruth(StatementOfTruth.builder().name("test name").build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();
            // When
            when(organisationService.findOrganisationById(anyString())).thenReturn(Optional.empty());
            handler.handle(params);
            // Then
            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id-spec",
                getNotificationDataMapStatement(),
                "create-sdo-applicants-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapLip() {
            return Map.of(
                PARTY_NAME, "Mr. John Rambo",
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                CLAIMANT_V_DEFENDANT, "Mr. John Rambo V Mr. Sole Trader"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMapStatement() {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                CLAIM_LEGAL_ORG_NAME_SPEC, "test name"
            );
        }
    }

    @Nested
    class AboutToSubmitCallbackEA {

        @BeforeEach
        void setup() {
            when(notificationsProperties.getSdoOrderedEA()).thenReturn("template-id-EA");
            when(notificationsProperties.getSdoOrderedSpecEA()).thenReturn("template-id-spec-EA");
            when(featureToggleService.isEarlyAdoptersEnabled()).thenReturn(true);
            when(organisationService.findOrganisationById(anyString()))
                .thenReturn(Optional.of(Organisation.builder().name("Signer Name").build()));
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id-EA",
                getNotificationDataMap(caseData),
                "create-sdo-applicants-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitorSpec_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id-spec-EA",
                getNotificationDataMap(caseData),
                "create-sdo-applicants-notification-000DC001"
            );
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name"
            );
        }
    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
            "NOTIFY_APPLICANTS_SOLICITOR_SDO_TRIGGERED").build()).build())).isEqualTo(TASK_ID);
    }
}
