package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.stubbing.OngoingStubbing;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.STARTED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.ga.utils.EmailFooterUtils.RAISE_QUERY_LR;

@ExtendWith(MockitoExtension.class)
public class GeneralApplicationCreationNotificationServiceTest {

    @InjectMocks
    private GeneralApplicationCreationNotificationService gaNotificationService;
    @Mock
    private SolicitorEmailValidation solicitorEmailValidation;
    @Mock
    private NotificationService notificationService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private GaCoreCaseDataService coreCaseDataService;

    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationsSignatureConfiguration configuration;
    @Spy
    private GaForLipService gaForLipService;
    @Captor
    ArgumentCaptor<Map<String, String>> argumentCaptor;

    private static final Long CASE_REFERENCE = 111111L;
    private static final String PROCESS_INSTANCE_ID = "1";
    private static final String DUMMY_EMAIL = "hmcts.civil@gmail.com";
    private static final String PARTY_REFERENCE = "Claimant Reference: Not provided - Defendant Reference: Not provided";
    private static final LocalDateTime DUMMY_DATE = LocalDateTime.of(2022, 2, 15, 12, 0, 0);
    public static LocalDateTime NOTIFICATION_DEADLINE = LocalDateTime.of(2022, 2, 15, 12, 0, 0);

    @Nested
    class AboutToSubmitCallback {

        @Test
        void notificationShouldSendIfGa_Urgent_WithNoticeAndFreeFee() {
            GeneralApplicationCaseData caseData = getCaseData(true).copy()
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YES).build())
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee().setCode("FREE")))
                .build();

            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().ccdState(CaseState.CASE_PROGRESSION).build());
            getGeneralApplicationCaseDataOngoingStubbing(caseData);
            when(notificationsProperties.getUrgentGeneralAppRespondentEmailTemplate())
                .thenReturn("general-application-respondent-template-id");
            mockNotificationsSignatureConfiguration();

            gaNotificationService.sendNotification(caseData);

            verify(notificationService, times(2)).sendMail(
                any(), any(), any(), any()
            );
        }

        @Test
        void notificationShouldSendIfGa_Urgent_WithNoticeAndFreeFeeV2() {
            GeneralApplicationCaseData caseData = getCaseData(true).copy()
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YES).build())
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee().setCode("FREE")))
                .generalAppRespondentSolicitors(List.of())
                .build();

            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation
                     .validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            gaNotificationService.sendNotification(caseData);
            verifyNoInteractions(notificationService);
        }

        @Test
        void notificationShouldSendIfGa_Urgent_WithNoticeAndFreeFeeV2Null() {
            GeneralApplicationCaseData caseData = getCaseData(true).copy()
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YES).build())
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee().setCode("FREE")))
                .generalAppRespondentSolicitors(null)
                .build();

            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation
                     .validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            gaNotificationService.sendNotification(caseData);
            verifyNoInteractions(notificationService);
        }

        @Test
        void notificationShouldSendIfGa_Urgent_WithNoticeAndFreeFeeV2OneSolAvailable() {

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                .email(DUMMY_EMAIL).organisationIdentifier("2").forename("LipF").surname(Optional.of("LipS")).build();

            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id").build();

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            GeneralApplicationCaseData caseData = getCaseData(true).copy()
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YES).build())
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee().setCode("FREE")))
                .generalAppRespondentSolicitors(respondentSols)
                .build();

            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation
                     .validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(notificationsProperties.getUrgentGeneralAppRespondentEmailTemplate())
                .thenReturn("general-application-respondent-template-id");
            mockNotificationsSignatureConfiguration();

            gaNotificationService.sendNotification(caseData);

            verify(notificationService).sendMail(
                any(), any(), any(), any()
            );
        }

        @Test
        void notificationShouldSendIfGa_NonUrgent_WithNoticeAndFreeFeeV2OneSolAvailable() {

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                .email(DUMMY_EMAIL).organisationIdentifier("2").forename("LipF").surname(Optional.of("LipS")).build();

            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id").build();

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            GeneralApplicationCaseData caseData = getCaseData(true).copy()
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee().setCode("FREE")))
                .generalAppRespondentSolicitors(respondentSols)
                .build();

            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation
                     .validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(notificationsProperties.getGeneralApplicationRespondentEmailTemplate())
                .thenReturn("general-application-respondent-template-id");
            mockNotificationsSignatureConfiguration();

            gaNotificationService.sendNotification(caseData);

            verify(notificationService).sendMail(
                any(), any(), any(), any()
            );
        }

        @Test
        void notificationShouldSendIfGa_Urgent_WithNoticeAndFeePaid() {
            GeneralApplicationCaseData caseData = getCaseData(true).copy()
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YES).build())
                .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                          .setFee(new Fee().setCode("PAID"))
                                          .setPaymentDetails(new PaymentDetails().setStatus(
                                              PaymentStatus.SUCCESS)))
                .build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation
                     .validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(notificationsProperties.getUrgentGeneralAppRespondentEmailTemplate())
                .thenReturn("general-application-respondent-template-id");
            mockNotificationsSignatureConfiguration();

            gaNotificationService.sendNotification(caseData);

            verify(notificationService, times(2)).sendMail(
                any(), any(), any(), any()
            );
        }

        @Test
        void notificationShouldNotSendIfGa_NonUrgent_WithNoticeAndFreeFee() {
            GeneralApplicationCaseData caseData = getCaseData(false).copy()
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee().setCode("FREE")))
                .build();

            when(solicitorEmailValidation
                     .validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            gaNotificationService.sendNotification(caseData);
            verifyNoInteractions(notificationService);
        }

        @Test
        void notificationShouldNotSendIfGa_NonUrgent_WithNoticeAndFeePaid() {
            GeneralApplicationCaseData caseData = getCaseData(false).copy()
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
                .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                          .setFee(new Fee().setCode("PAID"))
                                          .setPaymentDetails(new PaymentDetails().setStatus(
                                              PaymentStatus.SUCCESS)))
                .build();

            when(solicitorEmailValidation
                     .validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            gaNotificationService.sendNotification(caseData);
            verifyNoInteractions(notificationService);
        }

        @Test
        void notificationShouldSendIfGa_Lip_WithNoticeAndFeePaid_defendantLipIsGaRespondent() {
            GeneralApplicationCaseData caseData = getCaseData(true).copy()
                .isGaRespondentOneLip(YES)
                .parentClaimantIsApplicant(YES)
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                          .setFee(new Fee().setCode("PAID"))
                                          .setPaymentDetails(new PaymentDetails().setStatus(
                                              PaymentStatus.SUCCESS)))
                .build();
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            when(solicitorEmailValidation
                     .validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());
            when(notificationsProperties.getLipGeneralAppRespondentEmailTemplate())
                .thenReturn("general-application-respondent-template-lip-id");
            mockNotificationsSignatureConfiguration();

            gaNotificationService.sendNotification(caseData);

            verify(notificationService, times(2)).sendMail(
                any(), eq("general-application-respondent-template-lip-id"), argumentCaptor.capture(), any()
            );
            assertThat(argumentCaptor.getValue().get("respondentName")).isEqualTo("DEF");
            assertThat(argumentCaptor.getValue().get("ClaimantvDefendant")).isEqualTo("CL v DEF");
        }

        @Test
        void notificationShouldSendIfGa_Lip_WithNoticeAndFeePaid_claimantLipIsGaRespondent() {
            GeneralApplicationCaseData caseData = getCaseData(true).copy()
                .isGaRespondentOneLip(YES)
                .parentClaimantIsApplicant(NO)
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                          .setFee(new Fee().setCode("PAID"))
                                          .setPaymentDetails(new PaymentDetails().setStatus(
                                              PaymentStatus.SUCCESS)))
                .build();
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            when(solicitorEmailValidation
                     .validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());
            when(notificationsProperties.getLipGeneralAppRespondentEmailTemplate())
                .thenReturn("general-application-respondent-template-lip-id");
            mockNotificationsSignatureConfiguration();

            gaNotificationService.sendNotification(caseData);

            verify(notificationService, times(2)).sendMail(
                any(), eq("general-application-respondent-template-lip-id"), argumentCaptor.capture(), any()
            );
            assertThat(argumentCaptor.getValue().get("respondentName")).isEqualTo("CL");
            assertThat(argumentCaptor.getValue().get("ClaimantvDefendant")).isEqualTo("CL v DEF");
        }

        @Test
        void notificationSendShouldContainSolicitorEmailReferenceIfAdded() {
            GeneralApplicationCaseData caseData = getCaseData(true).copy()
                .emailPartyReference("Claimant Reference: ABC limited - Defendant Reference: Defendant Ltd")
                .isGaRespondentOneLip(YES)
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                          .setFee(new Fee().setCode("PAID"))
                                          .setPaymentDetails(new PaymentDetails().setStatus(
                                              PaymentStatus.SUCCESS)))
                .build();
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            when(solicitorEmailValidation
                     .validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());
            when(notificationsProperties.getLipGeneralAppRespondentEmailTemplate())
                .thenReturn("general-application-respondent-template-lip-id");
            mockNotificationsSignatureConfiguration();

            gaNotificationService.sendNotification(caseData);
            verify(notificationService, times(2)).sendMail(
                any(), eq("general-application-respondent-template-lip-id"), argumentCaptor.capture(), any()
            );
            assertThat(argumentCaptor.getValue().get("partyReferences"))
                .isEqualTo("Claimant Reference: ABC limited - Defendant Reference: Defendant Ltd");
        }

        @Test
        void notificationRespondentInWelshShouldSendIfGa_Lip_WithNoticeAndFeePaid() {
            GeneralApplicationCaseData caseData = getCaseData(true).copy()
                .isGaRespondentOneLip(YES)
                .respondentBilingualLanguagePreference(YES)
                .generalAppPBADetails(new GeneralApplicationPbaDetails()
                                          .setFee(new Fee().setCode("PAID"))
                                          .setPaymentDetails(new PaymentDetails().setStatus(
                                              PaymentStatus.SUCCESS)))
                .build();
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            when(solicitorEmailValidation
                     .validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            GeneralApplicationCaseData claimRespondentResponseLan = new GeneralApplicationCaseData().respondentBilingualLanguagePreference(YES)
                .respondent1LiPResponse(new RespondentLiPResponse().setRespondent1ResponseLanguage(
                    Language.BOTH.toString())).build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(claimRespondentResponseLan);
            when(notificationsProperties.getLipGeneralAppRespondentEmailTemplateInWelsh())
                .thenReturn("general-application-respondent-welsh-template-lip-id");
            mockNotificationsSignatureConfiguration();

            gaNotificationService.sendNotification(caseData);

            verify(notificationService, times(2)).sendMail(
                any(), eq("general-application-respondent-welsh-template-lip-id"), argumentCaptor.capture(), any()
            );
            assertThat(argumentCaptor.getValue().get("respondentName")).isEqualTo("DEF");
            assertThat(argumentCaptor.getValue().get("ClaimantvDefendant")).isEqualTo("CL v DEF");
        }

        @Test
        void notificationShouldSendWhenInvoked() {
            GeneralApplicationCaseData caseData = getCaseData(true);

            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().ccdState(CaseState.CASE_PROGRESSION).build());

            when(solicitorEmailValidation
                     .validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(notificationsProperties.getGeneralApplicationRespondentEmailTemplate())
                .thenReturn("general-application-respondent-template-id");
            mockNotificationsSignatureConfiguration();

            gaNotificationService.sendNotification(caseData);

            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-respondent-template-id",
                getNotificationDataMap(false),
                "general-application-respondent-notification-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendWhenInvokedTwice() {
            GeneralApplicationCaseData caseData = getCaseData(true);

            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().ccdState(CaseState.CASE_PROGRESSION).build());

            when(solicitorEmailValidation
                     .validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(notificationsProperties.getGeneralApplicationRespondentEmailTemplate())
                .thenReturn("general-application-respondent-template-id");
            mockNotificationsSignatureConfiguration();

            gaNotificationService.sendNotification(caseData);

            verify(notificationService, times(2)).sendMail(
                any(),
                any(),
                any(),
                any()
            );
        }

        @Test
        void notificationShouldNotSendWhenInvokedWhenConditionsAreNotMet() {
            GeneralApplicationCaseData caseData = getCaseData(false);

            when(solicitorEmailValidation
                     .validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            gaNotificationService.sendNotification(caseData);
            verifyNoInteractions(notificationService);
        }

        private Map<String, String> getNotificationDataMap(boolean isLip) {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                NotificationDataGA.APPLICANT_REFERENCE, "claimant",
                NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE,
                NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.GA_NOTIFICATION_DEADLINE,
                NOTIFICATION_DEADLINE.format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                NotificationDataGA.GA_LIP_RESP_NAME, isLip ? "Lip Resp" : "",
                NotificationDataGA.CASE_TITLE, isLip ? "CL v DEF" : ""
            ));
            properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            if (isLip) {
                properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
                    + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            } else {
                properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
            }
            properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                + "\n For all other matters, call 0300 123 7050");
            return properties;
        }

        private GeneralApplicationCaseData getCaseData(boolean isMet) {

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                .email(DUMMY_EMAIL).organisationIdentifier("2").forename("LipF").surname(Optional.of("LipS")).build();

            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
                .email(DUMMY_EMAIL).organisationIdentifier("3").build();

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            if (isMet) {

                return new GeneralApplicationCaseDataBuilder()
                    .applicantPartyName("App")
                    .claimant1PartyName("CL")
                    .defendant1PartyName("DEF")
                    .ccdCaseReference(CASE_REFERENCE)
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                                  .email(DUMMY_EMAIL).organisationIdentifier("1").build())
                    .generalAppRespondentSolicitors(respondentSols)
                    .businessProcess(new BusinessProcess().setStatus(STARTED)
                                         .setProcessInstanceId(PROCESS_INSTANCE_ID))
                    .gaInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                    .gaUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
                    .parentClaimantIsApplicant(YES)
                    .gaRespondentOrderAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                    .respondentSolicitor1EmailAddress(DUMMY_EMAIL)
                    .respondentSolicitor2EmailAddress(DUMMY_EMAIL)
                    .applicant1OrganisationPolicy(new OrganisationPolicy()
                                                      .setOrganisation(new Organisation().setOrganisationID("1")))
                    .respondent1OrganisationPolicy(new OrganisationPolicy()
                                                       .setOrganisation(new Organisation().setOrganisationID("2")))
                    .respondent2OrganisationPolicy(new OrganisationPolicy()
                                                       .setOrganisation(new Organisation().setOrganisationID("3")))
                    .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                                  .setCaseReference(CASE_REFERENCE.toString()))
                    .generalAppDeadlineNotificationDate(DUMMY_DATE)
                    .build();
            } else {
                return new GeneralApplicationCaseDataBuilder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .applicantPartyName("App")
                    .claimant1PartyName("CL")
                    .defendant1PartyName("DEF")
                    .businessProcess(new BusinessProcess().setStatus(STARTED)
                                         .setProcessInstanceId(PROCESS_INSTANCE_ID))
                    .gaInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
                    .gaUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
                    .gaRespondentOrderAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                    .ccdCaseReference(CASE_REFERENCE)
                    .respondentSolicitor1EmailAddress(DUMMY_EMAIL)
                    .respondentSolicitor2EmailAddress(DUMMY_EMAIL)
                    .applicant1OrganisationPolicy(new OrganisationPolicy()
                                                      .setOrganisation(new Organisation().setOrganisationID("1")))
                    .respondent1OrganisationPolicy(new OrganisationPolicy()
                                                       .setOrganisation(new Organisation().setOrganisationID("2")))
                    .respondent2OrganisationPolicy(new OrganisationPolicy()
                                                       .setOrganisation(new Organisation().setOrganisationID("3")))
                    .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                                  .setCaseReference(CASE_REFERENCE.toString()))
                    .generalAppDeadlineNotificationDate(DUMMY_DATE)
                    .build();
            }
        }
    }

    private void mockNotificationsSignatureConfiguration() {
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getWelshContact()).thenReturn("E-bost: ymholiadaucymraeg@justice.gov.uk");
        when(configuration.getSpecContact()).thenReturn("Email: contactocmc@justice.gov.uk");
        when(configuration.getWelshHmctsSignature()).thenReturn("Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        when(configuration.getWelshPhoneContact()).thenReturn("Ffôn: 0300 303 5174");
        when(configuration.getWelshOpeningHours()).thenReturn("Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
    }

    private OngoingStubbing<GeneralApplicationCaseData> getGeneralApplicationCaseDataOngoingStubbing(GeneralApplicationCaseData caseData) {
        return when(solicitorEmailValidation
                        .validateSolicitorEmail(any(), any()))
            .thenReturn(caseData);
    }
}
