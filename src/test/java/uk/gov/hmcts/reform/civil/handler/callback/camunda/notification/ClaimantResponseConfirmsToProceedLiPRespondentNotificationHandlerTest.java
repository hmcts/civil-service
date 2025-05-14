package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.LEGACY_CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
public class ClaimantResponseConfirmsToProceedLiPRespondentNotificationHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private ClaimantResponseConfirmsToProceedLiPRespondentNotificationHandler handler;

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
        void shouldNotNotifyLipRespondent_ifRespondentIsBilingual() {
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder()
                                                                              .respondent1ResponseLanguage("BOTH")
                                                                              .build()).build())
                .claimantBilingualLanguagePreference("ENGLISH")
                .respondent1(Party.builder().partyEmail("abc@gmail.com").type(Party.Type.INDIVIDUAL)
                    .individualFirstName("Mr. John").individualLastName("Rambo").build()).build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name())
                    .build()).build();

            handler.handle(params);

            verifyNoInteractions(notificationService);
        }

        @Test
        void shouldNotNotifyLipRespondent_ifRespondentIsBilingualWelshFlagIsON() {
            when(featureToggleService.isGaForWelshEnabled()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
                .caseDataLip(CaseDataLiP.builder().respondent1LiPResponse(RespondentLiPResponse.builder()
                                                                              .respondent1ResponseLanguage("BOTH")
                                                                              .build()).build())
                .claimantBilingualLanguagePreference("ENGLISH")
                .respondent1(Party.builder().partyEmail("abc@gmail.com").type(Party.Type.INDIVIDUAL)
                                 .individualFirstName("Mr. John").individualLastName("Rambo").build()).build();

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
                Map.of(
                    CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                    CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
                    "partyReferences", "Claimant reference: 12345 - Defendant reference: 6789",
                    "Claimant name", "Mr. John Rambo",
                    CASEMAN_REF, "000DC001"
                ),
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
                Map.of(
                    CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                    CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
                    "partyReferences", "Claimant reference: 12345 - Defendant reference: 6789",
                    "Claimant name", "Mr. John Rambo",
                    "casemanRef", "000DC001"
                ),
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
                Map.of(
                    CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                    CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
                    "partyReferences", "Claimant reference: 12345 - Defendant reference: 6789",
                    "Claimant name", "Mr. John Rambo",
                    "casemanRef", "000DC001"
                ),
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
                Map.of(
                    "partyReferences", "Claimant reference: 12345 - Defendant reference: 6789",
                    CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                    CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
                    APPLICANT_ONE_NAME, "Mr. John Rambo",
                    CASEMAN_REF, caseData.getLegacyCaseReference()
                ),
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
                Map.of(
                    "partyReferences", "Claimant reference: 12345 - Defendant reference: 6789",
                    CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
                    CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
                    APPLICANT_ONE_NAME, "Mr. John Rambo",
                    CASEMAN_REF, caseData.getLegacyCaseReference()
                ),
                REFERENCE_NUMBER
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
            return Map.of(
                CLAIM_REFERENCE_NUMBER, LEGACY_CASE_REFERENCE,
                RESPONDENT_NAME, DEFENDANT
            );
        }

        private Map<String, String> getNotificationDataMapCarm(CaseData caseData) {
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
                CLAIM_LEGAL_ORG_NAME_SPEC, "org name",
                PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
                CASEMAN_REF, caseData.getLegacyCaseReference(),
                APPLICANT_ONE_NAME, "Mr. John Rambo"
            );
        }

        @Test
        void shouldReturnCorrectCamundaActivityId_whenInvoked() {
            assertThat(handler.camundaActivityId(CallbackParamsBuilder.builder().request(CallbackRequest.builder().eventId(
                CaseEvent.NOTIFY_LIP_RESPONDENT_CLAIMANT_CONFIRM_TO_PROCEED.name()).build()).build())).isEqualTo(TASK_ID);
        }

    }

}
