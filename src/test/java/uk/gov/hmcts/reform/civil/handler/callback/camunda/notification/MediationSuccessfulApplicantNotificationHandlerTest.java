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
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
class MediationSuccessfulApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationDetailsService organisationDetailsService;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private MediationSuccessfulApplicantNotificationHandler handler;

    private static final String REFERENCE_NUMBER = "8372942374";
    public static final String TEMPLATE_ID = "template-id";
    public static final String TEMPLATE_LIP_ID = "template-lip-id";
    public static final String APPLICANT_MAIL = "applicantsolicitor@example.com";
    public static final String APPLICANT_LIP_MAIL = "applicant@example.com";
    public static final String MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION = "mediation-successful-applicant-notification-" + REFERENCE_NUMBER;
    public static final String MEDIATION_SUCCESSFUL_APPLICANT_LIP_NOTIFICATION = "mediation-successful-applicant-notification-LIP-" + REFERENCE_NUMBER;
    public static final String NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL = "NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL";
    public static final String TASK_ID = "MediationSuccessfulNotifyApplicant";

    @Nested
    class AboutToSubmitCallback {
        private static final String ORGANISATION_NAME = "Org Name";

        @Test
        void shouldNotifyApplicant_whenInvoked() {
            when(notificationsProperties.getNotifyApplicantLRMediationSuccessfulTemplate()).thenReturn(TEMPLATE_ID);
            when(organisationDetailsService.getApplicantLegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);

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
                TEMPLATE_ID,
                getNotificationDataMapSpec(caseData),
                "mediation-successful-applicant-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantLip_whenInvoked() {
            when(notificationsProperties.getNotifyApplicantLiPMediationSuccessfulTemplate()).thenReturn(TEMPLATE_LIP_ID);

            //Given
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            Party applicant1 = PartyBuilder.builder().soleTrader()
                .partyEmail(APPLICANT_LIP_MAIL)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .applicant1(applicant1)
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .legacyCaseReference(REFERENCE_NUMBER)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL)
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                APPLICANT_LIP_MAIL,
                TEMPLATE_LIP_ID,
                getNotificationLipDataMapSpec(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_LIP_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyApplicantLip_whenInvokedClaimIssueInBilingual() {
            when(notificationsProperties.getNotifyApplicantLiPMediationSuccessfulWelshTemplate()).thenReturn(TEMPLATE_LIP_ID);

            //Given
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            Party applicant1 = PartyBuilder.builder().soleTrader()
                .partyEmail(APPLICANT_LIP_MAIL)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .applicant1(applicant1)
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .legacyCaseReference(REFERENCE_NUMBER)
                .build();
            caseData = caseData.toBuilder().claimantBilingualLanguagePreference("BOTH").build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL)
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                APPLICANT_LIP_MAIL,
                TEMPLATE_LIP_ID,
                getNotificationLipDataMapSpec(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_LIP_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyApplicantLipFeatureToggleDisabled_whenInvoked() {
            when(organisationDetailsService.getApplicantLegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);

            //Given
            when(featureToggleService.isLipVLipEnabled()).thenReturn(false);
            Party applicant1 = PartyBuilder.builder().soleTrader()
                .partyEmail(APPLICANT_LIP_MAIL)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .applicant1(applicant1)
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .legacyCaseReference(REFERENCE_NUMBER)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL)
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService, never()).sendMail(
                APPLICANT_LIP_MAIL,
                TEMPLATE_LIP_ID,
                getNotificationLipDataMapSpec(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_LIP_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarmLRvLRNotifyApplicant_whenInvoked() {
            when(notificationsProperties.getNotifyLrClaimantSuccessfulMediation()).thenReturn(TEMPLATE_ID);
            when(organisationDetailsService.getApplicantLegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);

            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_ONE)
                .setClaimTypeToSpecClaim()
                .legacyCaseReference(REFERENCE_NUMBER)
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
                lrClaimantProperties(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarmLRvLRSameSolicitorNotifyApplicant_whenInvoked() {
            when(notificationsProperties.getNotifyOneVTwoClaimantSuccessfulMediation()).thenReturn(TEMPLATE_ID);
            when(organisationDetailsService.getApplicantLegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);

            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
                .setClaimTypeToSpecClaim()
                .legacyCaseReference(REFERENCE_NUMBER)
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
                oneVTwoProperties(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarmLRvLRdifferentSolicitorNotifyApplicant_whenInvoked() {
            when(notificationsProperties.getNotifyOneVTwoClaimantSuccessfulMediation()).thenReturn(TEMPLATE_ID);
            when(organisationDetailsService.getApplicantLegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);

            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)
                .setClaimTypeToSpecClaim()
                .legacyCaseReference(REFERENCE_NUMBER)
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
                oneVTwoProperties(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarmTwoVOneNotifyApplicant_whenInvoked() {
            when(notificationsProperties.getNotifyLrClaimantSuccessfulMediation()).thenReturn(TEMPLATE_ID);
            when(organisationDetailsService.getApplicantLegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);

            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .legacyCaseReference(REFERENCE_NUMBER)
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
                lrClaimantProperties(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarmLRVLipNotifyApplicant_whenInvoked() {
            when(notificationsProperties.getNotifyLrClaimantSuccessfulMediation()).thenReturn(TEMPLATE_ID);
            when(organisationDetailsService.getApplicantLegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);

            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                .setClaimTypeToSpecClaim()
                .legacyCaseReference(REFERENCE_NUMBER)
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
                lrClaimantProperties(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarmLipVLipNotifyApplicant_whenInvoked() {
            when(notificationsProperties.getNotifyLipSuccessfulMediation()).thenReturn(TEMPLATE_ID);

            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                .applicant1Represented(NO)
                .setClaimTypeToSpecClaim()
                .legacyCaseReference(REFERENCE_NUMBER)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL)
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                "rambo@email.com",
                TEMPLATE_ID,
                lipProperties(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarmLipVLipNotifyApplicantWithBilingualNotification_whenInvoked() {
            when(notificationsProperties.getNotifyLipSuccessfulMediationWelsh()).thenReturn(TEMPLATE_ID);

            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiPBilingual()
                .legacyCaseReference(REFERENCE_NUMBER)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL)
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                "rambo@email.com",
                TEMPLATE_ID,
                lipProperties(caseData),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION
            );
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                CaseEvent.NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL.name()).build()).build())).isEqualTo(TASK_ID);
        }

        @NotNull
        public Map<String, String> getNotificationDataMapSpec(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
            );
        }

        @NotNull
        public Map<String, String> getNotificationLipDataMapSpec(CaseData caseData) {
            return Map.of(
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
            );
        }
    }

    public Map<String, String> lrClaimantProperties(CaseData caseData) {

        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1())
        );
    }

    public Map<String, String> oneVTwoProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            DEFENDANT_NAME_ONE, getPartyNameBasedOnType(caseData.getRespondent1()),
            DEFENDANT_NAME_TWO, getPartyNameBasedOnType(caseData.getRespondent2())
        );
    }

    public Map<String, String> lipProperties(CaseData caseData) {
        return Map.of(
            PARTY_NAME, caseData.getApplicant1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString()
        );
    }
}

