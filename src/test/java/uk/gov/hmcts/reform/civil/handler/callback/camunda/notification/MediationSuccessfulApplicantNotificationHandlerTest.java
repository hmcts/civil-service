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
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsNotToProceedRespondentNotificationHandler.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_TWO;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@SpringBootTest(classes = {
    MediationSuccessfulApplicantNotificationHandler.class,
    OrganisationDetailsService.class,
    JacksonAutoConfiguration.class
})
class MediationSuccessfulApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private OrganisationDetailsService organisationDetailsService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @Autowired
    private MediationSuccessfulApplicantNotificationHandler handler;

    public static final String TEMPLATE_ID = "template-id";
    public static final String APPLICANT_MAIL = "applicantsolicitor@example.com";
    public static final String MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION_1594901956117591 = "mediation-successful-applicant-notification-1594901956117591";
    public static final String NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL = "NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL";

    @Nested
    class AboutToSubmitCallback {
        private static final String ORGANISATION_NAME = "Org Name";

        @BeforeEach
        void setup() {
            when(notificationsProperties.getNotifyApplicantLRMediationSuccessfulTemplate()).thenReturn("template-id");
            when(notificationsProperties.getNotifyLrVLrClaimantSuccessfulMediation()).thenReturn("template-id");
            when(notificationsProperties.getNotifyLrVLrTwoVOneClaimantSuccessfulMediation()).thenReturn("template-id");
            when(notificationsProperties.getNotifyLrVLrOneVTwoSameSolicitorClaimantSuccessfulMediation()).thenReturn("template-id");
            when(notificationsProperties.getNotifyLrVLrOneVTwoDifferentSolicitorsClaimantSuccessfulMediation()).thenReturn("template-id");
            when(notificationsProperties.getNotifyLrVLipClaimantSuccessfulMediation()).thenReturn("template-id");
            when(notificationsProperties.getNotifyLipVLipClaimantSuccessfulMediation()).thenReturn("template-id");
            when(notificationsProperties.getNotifyLipVLipClaimantSuccessfulMediation()).thenReturn("template-id");
            when(organisationDetailsService.getApplicantLegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);
        }

        @Test
        void shouldNotifyApplicant_whenInvoked() {
            //Given
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL)
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                APPLICANT_MAIL,
                "template-id",
                getNotificationDataMapSpec(caseData),
                "mediation-successful-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyClaimantCarmLRvLRNotifyApplicant_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_ONE)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL)
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                APPLICANT_MAIL,
                TEMPLATE_ID,
                lrVLrClaimantProperties(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION_1594901956117591
            );
        }

        @Test
        void shouldNotifyClaimantCarmLRvLRSameSolicitorNotifyApplicant_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL)
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                APPLICANT_MAIL,
                TEMPLATE_ID,
                lrVLrSameSolicitorProperties(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION_1594901956117591
            );
        }

        @Test
        void shouldNotifyClaimantCarmLRvLRdifferentSolicitorNotifyApplicant_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL)
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                APPLICANT_MAIL,
                TEMPLATE_ID,
                lrVLrDifferentSolicitorProperties(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION_1594901956117591
            );
        }

        @Test
        void shouldNotifyClaimantCarmTwoVOneNotifyApplicant_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL)
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                APPLICANT_MAIL,
                TEMPLATE_ID,
                twoVOneClaimantProperties(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION_1594901956117591
            );
        }

        @Test
        void shouldNotifyClaimantCarmLRVLipNotifyApplicant_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL)
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                APPLICANT_MAIL,
                TEMPLATE_ID,
                lrVLipClaimantProperties(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION_1594901956117591
            );
        }

        @Test
        void shouldNotifyClaimantCarmLipVLipNotifyApplicant_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                .applicant1Represented(NO)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL)
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                APPLICANT_MAIL,
                TEMPLATE_ID,
                lipVLipClaimantProperties(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION_1594901956117591
            );
        }

        @NotNull
        public Map<String, String> getNotificationDataMapSpec(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
            );
        }

    }

    public Map<String, String> lrVLrSameSolicitorProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME_ONE, getPartyNameBasedOnType(caseData.getRespondent1()),
            DEFENDANT_NAME_TWO, getPartyNameBasedOnType(caseData.getRespondent2())
        );
    }

    public Map<String, String> lrVLrClaimantProperties(CaseData caseData) {

        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    public Map<String, String> lrVLrDifferentSolicitorProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME_ONE, getPartyNameBasedOnType(caseData.getRespondent1()),
            DEFENDANT_NAME_TWO, getPartyNameBasedOnType(caseData.getRespondent2())
        );
    }

    public Map<String, String> twoVOneClaimantProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    public Map<String, String> lrVLipClaimantProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, caseData.getApplicant1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    public Map<String, String> lipVLipClaimantProperties(CaseData caseData) {
        return Map.of(
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString()
        );
    }
}

