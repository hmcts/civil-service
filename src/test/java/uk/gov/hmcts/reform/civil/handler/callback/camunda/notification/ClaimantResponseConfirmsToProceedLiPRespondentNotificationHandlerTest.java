package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsToProceedLiPRespondentNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.APPLICANT_ONE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CNBC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LIP_CONTACT_WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ClaimantResponseConfirmsToProceedLiPRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @InjectMocks
    private ClaimantResponseConfirmsToProceedLiPRespondentNotificationHandler handler;

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
        when(configuration.getCnbcContact()).thenReturn((String) configMap.get("cnbcContact"));
    }

    @Nested
    class AboutToSubmitCallback {

        private static final String RESPONDENT_EMAIL_TEMPLATE = "template-id-respondent";
        private static final String BILINGUAL_RESPONDENT_EMAIL_TEMPLATE = "bilingual-id-respondent";

        private static final String RESPONDENT_MEDIATION_EMAIL_TEMPLATE = "template-mediation-id-respondent";
        private static final String RESPONDENT_LR_EMAIL_TEMPLATE = "template-lr-id-respondent";
        private static final String RESPONDENT_EMAIL_ID = "sole.trader@email.com";
        private static final String REFERENCE_NUMBER = "claimant-confirms-to-proceed-respondent-notification-000DC001";
        private static final String DEFENDANT = "Mr. Sole Trader";

        @Test
        void shouldNotifyLipRespondent_whenInvoked() {
            when(notificationsProperties.getRespondent1LipClaimUpdatedTemplate()).thenReturn(RESPONDENT_EMAIL_TEMPLATE);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                RESPONDENT_EMAIL_ID,
                RESPONDENT_EMAIL_TEMPLATE,
                getNotificationDataMap(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyLipRespondent_whenTranslatedDocUploaded() {
            when(notificationsProperties.getRespondent1LipClaimUpdatedTemplate()).thenReturn(RESPONDENT_EMAIL_TEMPLATE);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP().build();
            caseData.setClaimantBilingualLanguagePreference("BOTH");
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED_TRANSLATED_DOC.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                RESPONDENT_EMAIL_ID,
                RESPONDENT_EMAIL_TEMPLATE,
                getNotificationDataMap(),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotNotifyLipRespondent_ifBilingual() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

            caseData.setClaimantBilingualLanguagePreference("BOTH");
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verifyNoInteractions(notificationService);
        }

        @Test
        void shouldNotNotifyLipRespondent_ifNoPartyEmail() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            caseData.getRespondent1().setPartyEmail(null);
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verifyNoInteractions(notificationService);
        }

        @Test
        void shouldNotNotifyLRRespondent_whenApplicantProceeds() {
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            when(notificationsProperties.getNotifyDefendantLRForMediation()).thenReturn(
                RESPONDENT_MEDIATION_EMAIL_TEMPLATE);
            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder()
                                                                                             .name("org name")
                                                                                             .build()));
            when(configuration.getCnbcContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .caseDataLip(CaseDataLiP.builder()
                                 .applicant1SettleClaim(NO)
                                 .build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "respondentsolicitor@example.com",
                RESPONDENT_MEDIATION_EMAIL_TEMPLATE,
                getNotificationDataMapCarm(caseData),
                REFERENCE_NUMBER
            );
        }

        @ParameterizedTest()
        @ValueSource(strings = {"FAST_CLAIM", "INTERMEDIATE_CLAIM", "MULTI_CLAIM"})
        void shouldNotifyLRRespondent_whenApplicantProceedsForLipVSLR(String claimType) {
            when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
            when(notificationsProperties.getRespondentSolicitorNotifyToProceedSpecWithAction()).thenReturn(
                RESPONDENT_EMAIL_TEMPLATE);
            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder()
                                                                                             .name("org name")
                                                                                             .build()));
            when(configuration.getCnbcContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .responseClaimTrack(claimType)
                .applicant1Represented(NO)
                .respondent1Represented(YES)
                .applicant1ProceedWithClaim(YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "respondentsolicitor@example.com",
                RESPONDENT_EMAIL_TEMPLATE,
                getNotificationDataMapLipLr(caseData),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyLRRespondent_whenApplicantProceedsSmallClaimDefendantStatesPaidForLipVSLR() {
            when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
            when(notificationsProperties.getRespondentSolicitorNotifyToProceedInMediation()).thenReturn(
                RESPONDENT_MEDIATION_EMAIL_TEMPLATE);
            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder()
                                                                                             .name("org name")
                                                                                             .build()));
            when(configuration.getCnbcContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .caseDataLip(CaseDataLiP.builder()
                                 .applicant1SettleClaim(NO)
                                 .build())
                .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
                .responseClaimTrack("SMALL_CLAIM")
                .applicant1Represented(NO)
                .respondent1Represented(YES)
                .build();
            caseData = caseData.toBuilder().defenceRouteRequired(HAS_PAID_THE_AMOUNT_CLAIMED).build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "respondentsolicitor@example.com",
                RESPONDENT_MEDIATION_EMAIL_TEMPLATE,
                getNotificationDataMapLipLr(caseData),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyLRRespondent_whenApplicantProceedsSmallClaimForLipVSLR() {
            when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
            when(notificationsProperties.getRespondentSolicitorNotifyToProceedInMediation()).thenReturn(
                RESPONDENT_MEDIATION_EMAIL_TEMPLATE);
            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder()
                                                                                             .name("org name")
                                                                                             .build()));
            when(configuration.getCnbcContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .caseDataLip(CaseDataLiP.builder()
                                 .applicant1SettleClaim(NO)
                                 .build())
                .responseClaimTrack("SMALL_CLAIM")
                .applicant1Represented(NO)
                .respondent1Represented(YES)
                .applicant1ProceedWithClaim(YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "respondentsolicitor@example.com",
                RESPONDENT_MEDIATION_EMAIL_TEMPLATE,
                getNotificationDataMapLipLr(caseData),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyLRRespondent_whenApplicantNoProceedsFullDefence() {
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
            when(notificationsProperties.getRespondentSolicitorNotifyNotToProceedSpec()).thenReturn(
                RESPONDENT_LR_EMAIL_TEMPLATE);
            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder()
                                                                                             .name("org name")
                                                                                             .build()));
            when(configuration.getCnbcContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .caseDataLip(CaseDataLiP.builder()
                                 .applicant1SettleClaim(NO)
                                 .build())
                .build();
            caseData = caseData.toBuilder()
                .respondent1Represented(NO)
                .applicant1Represented(NO)
                .respondent1Represented(YES)
                .defenceRouteRequired(HAS_PAID_THE_AMOUNT_CLAIMED)
                .respondent1ClaimResponseTypeForSpec(FULL_DEFENCE)
                .caseDataLiP(CaseDataLiP.builder().applicant1SettleClaim(YES).build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "respondentsolicitor@example.com",
                RESPONDENT_LR_EMAIL_TEMPLATE,
                getNotificationDataMapLipLr(caseData),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyLRRespondent_whenApplicantNoProceeds() {
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
            when(notificationsProperties.getRespondentSolicitorNotifyNotToProceedSpec()).thenReturn(
                RESPONDENT_LR_EMAIL_TEMPLATE);
            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder()
                                                                                             .name("org name")
                                                                                             .build()));

            when(configuration.getCnbcContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .caseDataLip(CaseDataLiP.builder()
                                 .applicant1SettleClaim(YES)
                                 .build())
                .build();
            caseData = caseData.toBuilder()
                .respondent1Represented(NO)
                .applicant1ProceedWithClaim(NO)
                .applicant1Represented(NO)
                .respondent1Represented(YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "respondentsolicitor@example.com",
                RESPONDENT_LR_EMAIL_TEMPLATE,
                getNotificationDataMapLipLr(caseData),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyLRRespondent_whenApplicantNoProceedsForWelsh() {
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
            when(notificationsProperties.getRespondentSolicitorNotifyNotToProceedSpec()).thenReturn(
                RESPONDENT_LR_EMAIL_TEMPLATE);
            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder()
                                                                                             .name("org name")
                                                                                             .build()));

            when(configuration.getCnbcContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .caseDataLip(CaseDataLiP.builder()
                                 .applicant1SettleClaim(YES)
                                 .build())
                .build();
            caseData = caseData.toBuilder()
                .applicant1ProceedWithClaim(NO)
                .applicant1Represented(NO)
                .respondent1Represented(YES)
                .claimantBilingualLanguagePreference("BOTH")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                "respondentsolicitor@example.com",
                RESPONDENT_LR_EMAIL_TEMPLATE,
                getNotificationDataMapLipLr(caseData),
                REFERENCE_NUMBER
            );
        }

        @Test
        void shouldNotifyLRRespondent_whenApplicantSettlesClaimForWelsh() {
            when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);
            when(featureToggleService.isDefendantNoCOnlineForCase(any())).thenReturn(true);
            when(notificationsProperties.getRespondent1LipClaimUpdatedTemplate()).thenReturn(
                RESPONDENT_LR_EMAIL_TEMPLATE);
            when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder()
                                                                                             .name("org name")
                                                                                             .build()));

            when(configuration.getCnbcContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build();
            caseData = caseData.toBuilder()
                .respondent1Represented(NO)
                .applicant1PartAdmitIntentionToSettleClaimSpec(YES)
                .applicant1Represented(NO)
                .specRespondent1Represented(NO)
                .claimantBilingualLanguagePreference("BOTH")
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService, times(1)).sendMail(
                eq("sole.trader@email.com"),
                eq(RESPONDENT_LR_EMAIL_TEMPLATE),
                any(),
                eq(REFERENCE_NUMBER)
            );
        }

        @Test
        void shouldNotifyLipRespondentWithBilingualTemplateWhenRespondentIsBilingual() {
            when(notificationsProperties.getNotifyDefendantTranslatedDocumentUploaded()).thenReturn(
                BILINGUAL_RESPONDENT_EMAIL_TEMPLATE);

            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued1v1LiP()
                .build().toBuilder()
                .caseDataLiP(CaseDataLiP.builder()
                                 .respondent1LiPResponse(RespondentLiPResponse.builder()
                                                             .respondent1ResponseLanguage(Language.BOTH.toString())
                                                             .build()).build())
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                RESPONDENT_EMAIL_ID,
                BILINGUAL_RESPONDENT_EMAIL_TEMPLATE,
                getNotificationDataMap(),
                REFERENCE_NUMBER
            );
        }

        private Map<String, String> getNotificationDataMap() {
            Map<String, String> properties = new HashMap<>(addCommonProperties());
            properties.put(CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE);
            properties.put(RESPONDENT_NAME, DEFENDANT);
            return properties;
        }

        private Map<String, String> getNotificationDataMapCarm(CaseData caseData) {
            Map<String, String> properties = new HashMap<>(addCommonProperties());
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "org name");
            properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
            properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
            properties.put(APPLICANT_ONE_NAME, "Mr. John Rambo");
            properties.put(CNBC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk");
            return properties;
        }

        private Map<String, String> getNotificationDataMapLipLr(CaseData caseData) {
            Map<String, String> properties = new HashMap<>(addCommonProperties());
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "org name");
            properties.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
            properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
            properties.put(APPLICANT_ONE_NAME, "Mr. John Rambo");
            properties.put(CNBC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk");
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

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name()).build()).build())).isEqualTo(TASK_ID);
        }

    }

}
