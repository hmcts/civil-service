package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.YamlNotificationTestUtil;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsToProceedRespondentNotificationHandler.TASK_ID;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsToProceedRespondentNotificationHandler.TASK_ID_CC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsToProceedRespondentNotificationHandler.TASK_ID_CC_MULTITRACK;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsToProceedRespondentNotificationHandler.TASK_ID_MULTITRACK;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsToProceedRespondentNotificationHandler.TASK_ID_RESPONDENT_SOL2_MULTITRACK;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.ClaimantResponseConfirmsToProceedRespondentNotificationHandler.Task_ID_RESPONDENT_SOL2;
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
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseConfirmsToProceedRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @InjectMocks
    private ClaimantResponseConfirmsToProceedRespondentNotificationHandler handler;

    @Nested
    class AboutToSubmitCallback {

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

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked() {
            when(notificationsProperties.getClaimantSolicitorConfirmsToProceed()).thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "claimant-confirms-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenInvoked_spec() {
            when(notificationsProperties.getRespondentSolicitorNotifyToProceedSpec()).thenReturn("spec-template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "spec-template-id",
                getNotificationDataMapSpec(
                    caseData,
                    CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED
                ),
                "claimant-confirms-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor_whenSpecRejectAllNoMediation() {
            when(notificationsProperties.getRespondentSolicitorNotifyToProceedSpecWithAction()).thenReturn(
                "spec-template-no-mediation");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .build().toBuilder()
                .responseClaimMediationSpecRequired(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "spec-template-no-mediation",
                getNotificationDataMapSpec(
                    caseData,
                    CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED
                ),
                "claimant-confirms-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2_whenInvoked_spec() {
            when(notificationsProperties.getRespondentSolicitorNotifyToProceedSpec()).thenReturn("spec-template-id");
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "spec-template-id",
                getNotificationDataMapSpec(
                    caseData,
                    CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED
                ),
                "claimant-confirms-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked_spec() {
            when(notificationsProperties.getClaimantSolicitorConfirmsToProceedSpec()).thenReturn("spec-template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "spec-template-id",
                getNotificationDataMapSpec(
                    caseData,
                    CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC
                ),
                "claimant-confirms-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenSpecRejectAllNoMediation() {
            when(notificationsProperties.getClaimantSolicitorConfirmsToProceedSpecWithAction()).thenReturn(
                "spec-template-no-mediation");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .setClaimTypeToSpecClaim()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
                .build().toBuilder()
                .responseClaimMediationSpecRequired(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "spec-template-no-mediation",
                getNotificationDataMapSpec(
                    caseData,
                    CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC
                ),
                "claimant-confirms-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2withNoIntentionToProceed_whenInvoked() {
            when(notificationsProperties.getClaimantSolicitorConfirmsNotToProceed()).thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.YES)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.NO)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor2withIntentionToProceed_whenInvoked() {
            when(notificationsProperties.getClaimantSolicitorConfirmsToProceed()).thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.NO)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor2@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "claimant-confirms-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondentSolicitor1withNoIntentionToProceed_whenInvoked() {
            when(notificationsProperties.getClaimantSolicitorConfirmsNotToProceed()).thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo.NO)
                .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo.YES)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "respondentsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "claimant-confirms-not-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyApplicantSolicitor_whenInvoked() {
            when(notificationsProperties.getClaimantSolicitorConfirmsToProceed()).thenReturn("template-id");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "applicantsolicitor@example.com",
                "template-id",
                getNotificationDataMap(caseData),
                "claimant-confirms-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotNotifyRespondent_whenInvokedWithNoSolicitorRepresentedWithEmail() {
            when(notificationsProperties.getRespondent1LipClaimUpdatedTemplate()).thenReturn("spec-template-id");

            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .respondent1Represented(null)
                .specRespondent1Represented(YesOrNo.NO)
                .caseAccessCategory(CaseCategory.SPEC_CLAIM)
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                "sole.trader@email.com",
                "spec-template-id",
                getNotificationDataMapLRvLiP(caseData),
                "claimant-confirms-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent_whenMultiTrack() {
            when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn("offline-template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .setMultiTrackClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RES_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_MULTITRACK")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                caseData.getRespondentSolicitor1EmailAddress(),
                "offline-template-id",
                getNotificationDataMap(caseData),
                "claimant-confirms-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent_whenMultiTrack_mintiEligibleClaim() {
            when(notificationsProperties.getClaimantSolicitorConfirmsToProceed()).thenReturn("template-id");
            when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .setMultiTrackClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RES_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_MULTITRACK")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                caseData.getRespondentSolicitor1EmailAddress(),
                "template-id",
                getNotificationDataMap(caseData),
                "claimant-confirms-to-proceed-respondent-notification-000DC001"
            );
        }

        @Test
        void shouldNotifyRespondent2_whenMultiTrack() {
            when(notificationsProperties.getSolicitorCaseTakenOffline()).thenReturn("offline-template-id");

            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                                   .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                     .organisationID("Resp_2_Org_Id")
                                                                     .build()).build())
                .respondentSolicitor2EmailAddress("resp2_sol_email")
                .setMultiTrackClaim()
                .build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId("NOTIFY_RES_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_MULTITRACK")
                    .build()).build();

            handler.handle(params);

            verify(notificationService).sendMail(
                caseData.getRespondentSolicitor2EmailAddress(),
                "offline-template-id",
                getNotificationDataMap(caseData),
                "claimant-confirms-to-proceed-respondent-notification-000DC001"
            );
        }

        @ParameterizedTest
        @EnumSource(value = CaseEvent.class, names = {"NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED",
            "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED",
            "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC",
            "NOTIFY_RES_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_MULTITRACK",
            "NOTIFY_RES_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_MULTITRACK",
            "NOTIFY_APP_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC_MULTITRACK"})
        void shouldNotNotify_whenLip(CaseEvent notificationEvent) {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
                .applicant1Represented(YesOrNo.NO)
                .respondent1Represented(YesOrNo.NO)
                .respondent2Represented(YesOrNo.NO)
                .respondent1OrganisationPolicy(OrganisationPolicy.builder().build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder().build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder().build())
                .setMultiTrackClaim()
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(notificationEvent.name())
                    .build()).build();

            var response = handler.handle(params);

            assertThat(response).isEqualTo(AboutToStartOrSubmitCallbackResponse.builder().build());
            verifyNoInteractions(notificationService);
        }

        @NotNull
        private Map<String, String> getNotificationDataMap(CaseData caseData) {
            Map<String, String> properties = addCommonProperties();
            properties.put(CLAIM_REFERENCE_NUMBER, CASE_ID.toString());
            properties.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name");
            properties.put(CASEMAN_REF, "000DC001");
            return properties;
        }

        @NotNull
        private Map<String, String> getNotificationDataMapLRvLiP(CaseData caseData) {
            Map<String, String> properties = addCommonProperties();
            properties.put(CLAIM_REFERENCE_NUMBER, CASE_ID.toString());
            properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            properties.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
            properties.put(CASEMAN_REF, "000DC001");
            properties.put(LIP_CONTACT, "Email: contactocmc@justice.gov.uk");
            return properties;
        }

        @NotNull
        public Map<String, String> getNotificationDataMapSpec(CaseData caseData, CaseEvent caseEvent) {
            Map<String, String> properties = addCommonProperties();
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganisationName(caseData, caseEvent));
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            properties.put(APPLICANT_ONE_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
            properties.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
            properties.put(CASEMAN_REF, "000DC001");
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
            expectedProperties.put(CNBC_CONTACT, configuration.getCnbcContact());
            return expectedProperties;
        }

        private String getLegalOrganisationName(CaseData caseData, CaseEvent caseEvent) {
            String organisationID;
            organisationID = caseEvent.equals(NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC)
                ? caseData.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID()
                : caseData.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID();
            Optional<Organisation> organisation = organisationService.findOrganisationById(organisationID);
            return organisation.isPresent() ? organisation.get().getName() :
                caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName();
        }

    }

    @Test
    void shouldReturnCorrectCamundaActivityId_whenInvoked() {
        CaseData caseData = CaseDataBuilder.builder().build();
        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                                                 .request(CallbackRequest.builder().eventId(
                                                     "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED").build()).build())).isEqualTo(
            TASK_ID);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                                                 .request(CallbackRequest.builder().eventId(
                                                     "NOTIFY_RESPONDENT_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED").build())
                                                 .build())).isEqualTo(Task_ID_RESPONDENT_SOL2);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData)
                                                 .request(CallbackRequest.builder().eventId(
                                                     "NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC").build()).build())).isEqualTo(
            TASK_ID_CC);

        CaseData caseDataMultitrack = CaseDataBuilder.builder().setMultiTrackClaim().build();

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseDataMultitrack)
                                                 .request(CallbackRequest.builder().eventId(
                                                     "NOTIFY_RES_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_MULTITRACK").build()).build()))
            .isEqualTo(TASK_ID_MULTITRACK);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseDataMultitrack)
                                                 .request(CallbackRequest.builder().eventId(
                                                     "NOTIFY_RES_SOLICITOR2_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_MULTITRACK").build()).build()))
            .isEqualTo(TASK_ID_RESPONDENT_SOL2_MULTITRACK);

        assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseDataMultitrack)
                                                 .request(CallbackRequest.builder().eventId(
                                                     "NOTIFY_APP_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED_CC_MULTITRACK").build()).build()))
            .isEqualTo(TASK_ID_CC_MULTITRACK);
    }

    @Test
    void shouldNotNotifyRespondent_whenInvokedWithNoSolicitorRepresentedAndNoEmail() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .respondent1(PartyBuilder.builder().company().partyEmail(null).build())
            .respondent1Represented(null)
            .specRespondent1Represented(YesOrNo.NO)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId("NOTIFY_RESPONDENT_SOLICITOR1_FOR_CLAIMANT_CONFIRMS_TO_PROCEED")
                .build()).build();

        handler.handle(params);

        verify(notificationService, never()).sendMail(any(), any(), any(), any());
    }

}
