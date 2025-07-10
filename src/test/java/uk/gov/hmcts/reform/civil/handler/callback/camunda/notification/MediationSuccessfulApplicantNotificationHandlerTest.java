package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationDetailsService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsNotToProceedRespondentNotificationHandler.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_TWO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediationSuccessfulApplicantNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationDetailsService organisationDetailsService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

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

        @BeforeEach
        void setUp() {
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getHmctsSignature()).thenReturn((String) configMap.get("hmctsSignature"));
            when(configuration.getPhoneContact()).thenReturn((String) configMap.get("phoneContact"));
            when(configuration.getOpeningHours()).thenReturn((String) configMap.get("openingHours"));
            when(configuration.getWelshHmctsSignature()).thenReturn((String) configMap.get("welshHmctsSignature"));
            when(configuration.getWelshPhoneContact()).thenReturn((String) configMap.get("welshPhoneContact"));
            when(configuration.getWelshOpeningHours()).thenReturn((String) configMap.get("welshOpeningHours"));
            when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
            when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
        }

        @Test
        void shouldNotifyApplicant_whenInvoked() {
            when(notificationsProperties.getNotifyApplicantLRMediationSuccessfulTemplate()).thenReturn(TEMPLATE_ID);
            when(organisationDetailsService.getApplicantLegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
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
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
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
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
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
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
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
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
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
                lrClaimantProperties(caseData, false),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarmLRvLRSameSolicitorNotifyApplicant_whenInvoked() {
            when(notificationsProperties.getNotifyOneVTwoClaimantSuccessfulMediation()).thenReturn(TEMPLATE_ID);
            when(organisationDetailsService.getApplicantLegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
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
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
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
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getRaiseQueryLr()).thenReturn((String) configMap.get("raiseQueryLr"));
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
                lrClaimantProperties(caseData, false),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarmLRVLipNotifyApplicant_whenInvoked() {
            when(notificationsProperties.getNotifyLrClaimantSuccessfulMediation()).thenReturn(TEMPLATE_ID);
            when(organisationDetailsService.getApplicantLegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
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
                lrClaimantProperties(caseData, true),
                MEDIATION_SUCCESSFUL_APPLICANT_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarmLipVLipNotifyApplicant_whenInvoked() {
            when(notificationsProperties.getNotifyLipSuccessfulMediation()).thenReturn(TEMPLATE_ID);
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
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
            Map<String, Object> configMap = YamlNotificationTestUtil.loadNotificationsConfig();
            when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
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
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties(false));
            expectedProperties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            expectedProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData));
            expectedProperties.put(DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            expectedProperties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
            expectedProperties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
            return expectedProperties;
        }

        @NotNull
        public Map<String, String> getNotificationLipDataMapSpec(CaseData caseData) {
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties(true));
            expectedProperties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
            expectedProperties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            expectedProperties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
            return expectedProperties;
        }
    }

    public Map<String, String> lrClaimantProperties(CaseData caseData, boolean isLipCase) {
        Map<String, String> expectedProperties = new HashMap<>(addCommonProperties(isLipCase));
        expectedProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData));
        expectedProperties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        expectedProperties.put(DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        expectedProperties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
        expectedProperties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
        return expectedProperties;
    }

    public Map<String, String> oneVTwoProperties(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(addCommonProperties(false));
        expectedProperties.put(CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getApplicantLegalOrganisationName(caseData));
        expectedProperties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        expectedProperties.put(DEFENDANT_NAME_ONE, getPartyNameBasedOnType(caseData.getRespondent1()));
        expectedProperties.put(DEFENDANT_NAME_TWO, getPartyNameBasedOnType(caseData.getRespondent2()));
        expectedProperties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
        expectedProperties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
        return expectedProperties;
    }

    public Map<String, String> lipProperties(CaseData caseData) {
        Map<String, String> expectedProperties = new HashMap<>(addCommonProperties(true));
        expectedProperties.put(PARTY_NAME, caseData.getApplicant1().getPartyName());
        expectedProperties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        return expectedProperties;
    }

    @NotNull
    public Map<String, String> addCommonProperties(boolean isLipCase) {
        Map<String, String> expectedProperties = new HashMap<>();
        expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
        expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
        expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
        expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
        expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
        expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
        expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
        expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
        if (isLipCase) {
            expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
            expectedProperties.put(CNBC_CONTACT, configuration.getCnbcContact());
        } else {
            expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getRaiseQueryLr());
            expectedProperties.put(CNBC_CONTACT, configuration.getRaiseQueryLr());
        }
        return expectedProperties;
    }
}

