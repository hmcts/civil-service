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
import uk.gov.hmcts.reform.civil.YamlNotificationTestUtil;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_2_LR;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_LIP;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsNotToProceedRespondentNotificationHandler.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME_ONE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME_TWO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediationSuccessfulRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

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
    private MediationSuccessfulRespondentNotificationHandler handler;

    public static final String TEMPLATE_ID = "template-id";
    public static final String TEMPLATE_ID_LIP_LR = "template-id-lip-v-lr";
    public static final String RESPONDENT_MAIL = "respondentsolicitor@example.com";
    private static final String REFERENCE_NUMBER = "8372942374";
    public static final String MEDIATION_SUCCESSFUL_RESPONDENT_LIP_NOTIFICATION = "notification-mediation-successful-defendant-LIP-" + REFERENCE_NUMBER;
    public static final String MEDIATION_SUCCESSFUL_RESPONDENT_LR_NOTIFICATION = "notification-mediation-successful-defendant-LR-" + REFERENCE_NUMBER;
    public static final String MEDIATION_SUCCESSFUL_RESPONDENT_2V1_NOTIFICATION = "notification-mediation-successful-defendant-2v1-LR-" + REFERENCE_NUMBER;
    public static final String NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL = "NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL";

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
            when(configuration.getSpecUnspecContact()).thenReturn((String) configMap.get("specUnspecContact"));
            when(configuration.getLipContactEmail()).thenReturn((String) configMap.get("lipContactEmail"));
            when(configuration.getLipContactEmailWelsh()).thenReturn((String) configMap.get("lipContactEmailWelsh"));
        }

        @Test
        void shouldNotifyRespondent_whenInvoked() {
            //Given
            Party respondent1 = PartyBuilder.builder().soleTrader()
                .partyEmail("respondent@example.com")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .respondent1OrgRegistered(null)
                .specRespondent1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(NO)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL")
                    .build()).build();
            //When
            given(notificationsProperties.getNotifyRespondentLiPMediationSuccessfulTemplate()).willReturn(TEMPLATE_ID);
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                "respondent@example.com",
                "template-id",
                getNotificationDataMapSpec(caseData),
                "mediation-successful-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyBilingualRespondent_whenInvoked() {
            //Given
            Party respondent1 = PartyBuilder.builder().soleTrader()
                .partyEmail("respondent@example.com")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .respondent1OrgRegistered(null)
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH").build()).build())
                .specRespondent1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL")
                    .build()).build();
            //When
            given(notificationsProperties.getNotifyRespondentLiPMediationSuccessfulTemplateWelsh()).willReturn("template-id-welsh");
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                "respondent@example.com",
                "template-id-welsh",
                getNotificationDataMapSpec(caseData),
                "mediation-successful-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotSendEmail_whenEventIsCalledAndDefendantHasNoEmail() {
            //Given
            Party respondent1 = PartyBuilder.builder().soleTrader()
                .partyEmail(null)
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .respondent1OrgRegistered(null)
                .specRespondent1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL")
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService, times(0)).sendMail("respondent@example.com",
                                                           "template-id",
                                                           getNotificationDataMapSpec(caseData),
                                                           "mediation-successful-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotSendEmail_whenRespondentIsLR() {
            //Given
            Party respondent1 = PartyBuilder.builder().soleTrader()
                .partyEmail("respondent@example.com")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .specRespondent1Represented(YesOrNo.YES)
                .respondent1Represented(YesOrNo.YES)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL")
                    .build()).build();
            //When
            handler.handle(params);
            //Then
            verify(notificationService, times(0)).sendMail("respondent@example.com",
                                                           "template-id",
                                                           getNotificationDataMapSpec(caseData),
                                                           "mediation-successful-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyDefendantCarmLRvLRNotifyApplicant_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_ONE)
                .setClaimTypeToSpecClaim()
                .legacyCaseReference(REFERENCE_NUMBER)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(
                    CallbackRequest
                        .builder()
                        .eventId(NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL.name())
                    .build())
                .build();
            //When
            when(notificationsProperties.getNotifyLrDefendantSuccessfulMediation()).thenReturn(TEMPLATE_ID);
            when(organisationDetailsService.getRespondent1LegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                RESPONDENT_MAIL,
                TEMPLATE_ID,
                lrDefendantProperties(caseData),
                MEDIATION_SUCCESSFUL_RESPONDENT_LR_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarm1V2SameSolicitorNotifyApplicant_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
                .setClaimTypeToSpecClaim()
                .legacyCaseReference(REFERENCE_NUMBER)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL.name())
                    .build()).build();
            //When
            when(notificationsProperties.getNotifyLrDefendantSuccessfulMediation()).thenReturn(TEMPLATE_ID);
            when(organisationDetailsService.getRespondent1LegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                RESPONDENT_MAIL,
                TEMPLATE_ID,
                lrDefendantProperties(caseData),
                MEDIATION_SUCCESSFUL_RESPONDENT_LR_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarm1V2DifferentSolicitorNotifyApplicant_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)
                .setClaimTypeToSpecClaim()
                .legacyCaseReference(REFERENCE_NUMBER)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL.name())
                    .build()).build();
            //When
            when(notificationsProperties.getNotifyLrDefendantSuccessfulMediation()).thenReturn(TEMPLATE_ID);
            when(organisationDetailsService.getRespondent1LegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                RESPONDENT_MAIL,
                TEMPLATE_ID,
                lrDefendantProperties(caseData),
                MEDIATION_SUCCESSFUL_RESPONDENT_LR_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarm1V2DifferentSolicitorNotifyApplicantEmailSecondDefendant_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)
                .setClaimTypeToSpecClaim()
                .legacyCaseReference(REFERENCE_NUMBER)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_2_LR.name())
                    .build()).build();
            //When
            when(notificationsProperties.getNotifyLrDefendantSuccessfulMediation()).thenReturn(TEMPLATE_ID);
            when(organisationDetailsService.getRespondent1LegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                TEMPLATE_ID,
                lrDefendantProperties(caseData),
                MEDIATION_SUCCESSFUL_RESPONDENT_LR_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarmTwoVOneNotifyApplicant_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .multiPartyClaimTwoApplicants()
                .setClaimTypeToSpecClaim()
                .legacyCaseReference(REFERENCE_NUMBER)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_APPLICANT_MEDIATION_SUCCESSFUL)
                    .build()).build();
            //When
            when(notificationsProperties.getNotifyTwoVOneDefendantSuccessfulMediation()).thenReturn(TEMPLATE_ID);
            when(organisationDetailsService.getRespondent1LegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                RESPONDENT_MAIL,
                TEMPLATE_ID,
                twoVOneDefendantProperties(caseData),
                MEDIATION_SUCCESSFUL_RESPONDENT_2V1_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarmLRVLipNotifyApplicant_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                .setClaimTypeToSpecClaim()
                .legacyCaseReference(REFERENCE_NUMBER)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_2_LR.name())
                    .build()).build();
            //When
            when(notificationsProperties.getNotifyLipSuccessfulMediation()).thenReturn(TEMPLATE_ID);
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                "sole.trader@email.com",
                TEMPLATE_ID,
                lipDefendantProperties(caseData),
                MEDIATION_SUCCESSFUL_RESPONDENT_LIP_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarmLipVLipNotifyApplicant_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                .applicant1Represented(NO)
                .setClaimTypeToSpecClaim()
                .legacyCaseReference(REFERENCE_NUMBER)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_LIP.name())
                    .build()).build();
            //When
            when(notificationsProperties.getNotifyLipSuccessfulMediation()).thenReturn(TEMPLATE_ID);
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                "sole.trader@email.com",
                TEMPLATE_ID,
                lipDefendantProperties(caseData),
                MEDIATION_SUCCESSFUL_RESPONDENT_LIP_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyClaimantCarmLipVLipNotifyApplicantWithBilingualNotification_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiPBilingual()
                .legacyCaseReference(REFERENCE_NUMBER)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_MEDIATION_SUCCESSFUL_DEFENDANT_LIP.name())
                    .build()).build();
            //When
            when(notificationsProperties.getNotifyLipSuccessfulMediationWelsh()).thenReturn(TEMPLATE_ID);
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                "sole.trader@email.com",
                TEMPLATE_ID,
                lipDefendantProperties(caseData),
                MEDIATION_SUCCESSFUL_RESPONDENT_LIP_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyDefendantLipVLrNotifyDefendant_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario.ONE_V_ONE)
                .setClaimTypeToSpecClaim()
                .legacyCaseReference(REFERENCE_NUMBER)
                .applicant1Represented(NO)
                .specRespondent1Represented(YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                .request(
                    CallbackRequest
                        .builder()
                        .eventId(NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL.name())
                        .build())
                .build();
            //When
            when(organisationDetailsService.getRespondent1LegalOrganisationName(any())).thenReturn(ORGANISATION_NAME);
            when(notificationsProperties.getNotifyLrDefendantSuccessfulMediationForLipVLrClaim()).thenReturn(TEMPLATE_ID_LIP_LR);
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                RESPONDENT_MAIL,
                TEMPLATE_ID_LIP_LR,
                lrDefendantProperties(caseData),
                MEDIATION_SUCCESSFUL_RESPONDENT_LR_NOTIFICATION
            );
        }

        @Test
        void shouldNotifyDefendantLrVLipNotifyDefendant_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            Party respondent1 = PartyBuilder.builder().soleTrader()
                .partyEmail("respondent@example.com")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .respondent1OrgRegistered(null)
                .specRespondent1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YES)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL.name())
                    .build()).build();
            //When
            given(notificationsProperties.getNotifyRespondentLiPMediationSuccessfulTemplate()).willReturn(TEMPLATE_ID);
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                "respondent@example.com",
                "template-id",
                getNotificationDataMapSpec(caseData),
                "mediation-successful-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotSendNotificationToDefendantForLipVLip_whenInvoked() {
            //Given
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(false);
            when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

            Party respondent1 = PartyBuilder.builder().soleTrader()
                .partyEmail("respondent@example.com")
                .build();
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1(respondent1)
                .respondent1OrgRegistered(null)
                .specRespondent1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(NO)
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(NOTIFY_RESPONDENT_MEDIATION_SUCCESSFUL.name())
                    .build()).build();
            //When
            given(notificationsProperties.getNotifyRespondentLiPMediationSuccessfulTemplate()).willReturn(TEMPLATE_ID);
            handler.handle(params);
            //Then
            verify(notificationService).sendMail(
                "respondent@example.com",
                "template-id",
                getNotificationDataMapSpec(caseData),
                "mediation-successful-respondent-notification-000DC001"
            );
        }

        @NotNull
        public Map<String, String> getNotificationDataMapSpec(CaseData caseData) {
            Map<String, String> expectedProperties = new HashMap<>(addCommonProperties());
            expectedProperties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
            expectedProperties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
            expectedProperties.put(DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            return expectedProperties;
        }

    }

    public Map<String, String> lrDefendantProperties(CaseData caseData) {
        Map<String, String> properties = new HashMap<>(addCommonProperties());
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent1LegalOrganisationName(caseData));
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        properties.put(CLAIMANT_NAME, caseData.getApplicant1().getPartyName());
        properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
        properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
        return properties;
    }

    public Map<String, String> twoVOneDefendantProperties(CaseData caseData) {
        Map<String, String> properties = new HashMap<>(addCommonProperties());
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent1LegalOrganisationName(caseData));
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        properties.put(CLAIMANT_NAME_ONE, caseData.getApplicant1().getPartyName());
        properties.put(CLAIMANT_NAME_TWO, caseData.getApplicant2().getPartyName());
        properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
        properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
        return properties;
    }

    public Map<String, String> lipDefendantProperties(CaseData caseData) {
        Map<String, String> properties = new HashMap<>(addCommonProperties());
        properties.put(PARTY_NAME, caseData.getRespondent1().getPartyName());
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        return properties;
    }

    @NotNull
    public Map<String, String> addCommonProperties() {
        Map<String, String> expectedProperties = new HashMap<>();
        expectedProperties.put(PHONE_CONTACT, configuration.getPhoneContact());
        expectedProperties.put(OPENING_HOURS, configuration.getOpeningHours());
        expectedProperties.put(HMCTS_SIGNATURE, configuration.getHmctsSignature());
        expectedProperties.put(WELSH_PHONE_CONTACT, configuration.getWelshPhoneContact());
        expectedProperties.put(WELSH_OPENING_HOURS, configuration.getWelshOpeningHours());
        expectedProperties.put(WELSH_HMCTS_SIGNATURE, configuration.getWelshHmctsSignature());
        expectedProperties.put(SPEC_UNSPEC_CONTACT, configuration.getSpecUnspecContact());
        expectedProperties.put(LIP_CONTACT, configuration.getLipContactEmail());
        expectedProperties.put(LIP_CONTACT_WELSH, configuration.getLipContactEmailWelsh());
        return expectedProperties;
    }
}
