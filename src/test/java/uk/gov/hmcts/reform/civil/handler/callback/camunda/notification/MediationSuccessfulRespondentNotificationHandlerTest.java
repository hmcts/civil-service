package uk.gov.hmcts.reform.civil.handler.callback.camunda.notification;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
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
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@ExtendWith(MockitoExtension.class)
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

        @Test
        void shouldNotifyRespondent_whenInvoked() {
            //Given
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
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
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
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
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

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
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

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
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

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
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

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
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
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
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
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
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
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
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
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
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

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
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
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
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

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
            return Map.of(
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()),
                DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()),
                PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
                OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
                SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
                HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
            );
        }

    }

    public Map<String, String> lrDefendantProperties(CaseData caseData) {

        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent1LegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference(),
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
        );
    }

    public Map<String, String> twoVOneDefendantProperties(CaseData caseData) {
        return Map.of(
            CLAIM_LEGAL_ORG_NAME_SPEC, organisationDetailsService.getRespondent1LegalOrganisationName(caseData),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            CLAIMANT_NAME_ONE, caseData.getApplicant1().getPartyName(),
            CLAIMANT_NAME_TWO, caseData.getApplicant2().getPartyName(),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, caseData.getLegacyCaseReference(),
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk \n Email for Damages Claims: damagesclaims@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
        );
    }

    public Map<String, String> lipDefendantProperties(CaseData caseData) {
        return Map.of(
            PARTY_NAME, caseData.getRespondent1().getPartyName(),
            CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString(),
            PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 \n For all other matters, call 0300 123 7050",
            OPENING_HOURS, "Monday to Friday, 8.30am to 5pm",
            LIP_CONTACT, "Email: contactocmc@justice.gov.uk",
            HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service"
        );
    }
}
