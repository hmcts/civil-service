package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.STARTED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.ga.utils.EmailFooterUtils.RAISE_QUERY_LR;

@ExtendWith(MockitoExtension.class)
public class DocUploadNotificationServiceTest {

    @InjectMocks
    private DocUploadNotificationService docUploadNotificationService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private GaForLipService gaForLipService;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private NotificationsSignatureConfiguration configuration;

    @Mock
    private OrganisationService organisationService;

    private static final Long CASE_REFERENCE = 111111L;
    private static final String PROCESS_INSTANCE_ID = "1";
    private static final String DUMMY_EMAIL = "hmcts.civil@gmail.com";
    private static final String PARTY_REFERENCE = "Claimant Reference: Not provided - Defendant Reference: Not provided";
    private static final String CUSTOM_PARTY_REFERENCE = "Claimant Reference: ABC Ltd - Defendant Reference: Defendant Ltd";
    private static final String LEGACY_CASE_REFERENCE = "000MC001";
    private static final String APPLICANT_ORG_NAME = "Applicant Organisation";
    private static final String RESPONDENT_ORG_NAME = "Respondent Organisation";
    private static final String UPLOADED_DOCUMENT_NAMES = "applicant evidence.pdf\nrespondent evidence.pdf";

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getWelshContact()).thenReturn("E-bost: ymholiadaucymraeg@justice.gov.uk");
            when(configuration.getSpecContact()).thenReturn("Email: contactocmc@justice.gov.uk");
            when(configuration.getWelshHmctsSignature()).thenReturn(
                "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            when(configuration.getWelshPhoneContact()).thenReturn("Ffôn: 0300 303 5174");
            when(configuration.getWelshOpeningHours()).thenReturn(
                "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            lenient().when(organisationService.findOrganisationById("1"))
                .thenReturn(Optional.of(
                    new uk.gov.hmcts.reform.civil.prd.model.Organisation().setName(APPLICANT_ORG_NAME)
                ));
            lenient().when(organisationService.findOrganisationById("2"))
                .thenReturn(Optional.of(
                    new uk.gov.hmcts.reform.civil.prd.model.Organisation().setName(RESPONDENT_ORG_NAME)
                ));
            lenient().when(organisationService.findOrganisationById("3"))
                .thenReturn(Optional.of(
                    new uk.gov.hmcts.reform.civil.prd.model.Organisation().setName(RESPONDENT_ORG_NAME)
                ));
        }

        @Test
        void appNotificationShouldSendWhenInvoked() {
            when(notificationsProperties.getEvidenceUploadTemplate())
                .thenReturn("general-apps-notice-of-document-template-id");
            GeneralApplicationCaseData caseData = getCaseData(true, NO, NO);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(parentCaseData());
            docUploadNotificationService.notifyApplicantEvidenceUpload(caseData);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-apps-notice-of-document-template-id",
                getNotificationDataMap(false, false),
                "general-apps-notice-of-document-upload-" + CASE_REFERENCE
            );
        }

        @Test
        void appNotificationWithSolicitorReferenceAdded() {
            when(notificationsProperties.getEvidenceUploadTemplate())
                .thenReturn("general-apps-notice-of-document-template-id");
            GeneralApplicationCaseData caseData = getCaseData(false, NO, NO);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(parentCaseData());
            docUploadNotificationService.notifyApplicantEvidenceUpload(caseData);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-apps-notice-of-document-template-id",
                getNotificationDataMap(true, false),
                "general-apps-notice-of-document-upload-" + CASE_REFERENCE
            );
        }

        @Test
        void respNotificationShouldSendTwice1V2() {
            when(notificationsProperties.getEvidenceUploadTemplate())
                .thenReturn("general-apps-notice-of-document-template-id");
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(parentCaseData());
            when(configuration.getSpecUnspecContact()).thenReturn(
                "Email for Specified Claims: contactocmc@justice.gov.uk "
                    + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            GeneralApplicationCaseData caseData = getCaseData(true, NO, YES);
            docUploadNotificationService.notifyRespondentEvidenceUpload(caseData);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-apps-notice-of-document-template-id",
                getNotificationDataMap(false, true),
                "general-apps-notice-of-document-upload-" + CASE_REFERENCE
            );
        }

        @Test
        void lipApplicantNotificationShouldSendWhenInvoked() {
            when(notificationsProperties.getLipGeneralAppApplicantEmailTemplate())
                .thenReturn("ga-notice-of-document-lip-appln-template-id");
            when(configuration.getSpecUnspecContact()).thenReturn(
                "Email for Specified Claims: contactocmc@justice.gov.uk "
                    + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            GeneralApplicationCaseData caseData = getCaseData(true, YES, NO);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(parentCaseData());
            docUploadNotificationService.notifyApplicantEvidenceUpload(caseData);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "ga-notice-of-document-lip-appln-template-id",
                getNotificationDataMapForLip(YES, NO),
                "general-apps-notice-of-document-upload-" + CASE_REFERENCE
            );
        }

        @Test
        void lipApplicantNotificationShouldSendWhenInvoked_whenMainClaimIssuedInWelsh() {
            when(notificationsProperties.getLipGeneralAppApplicantEmailTemplateInWelsh())
                .thenReturn("ga-notice-of-document-lip-appln-welsh-template-id");
            when(configuration.getSpecUnspecContact()).thenReturn(
                "Email for Specified Claims: contactocmc@justice.gov.uk "
                    + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            GeneralApplicationCaseData caseData =
                getCaseData(true, YES, NO).copy().applicantBilingualLanguagePreference(YES).build();
            GeneralApplicationCaseData claimantClaimIssueFlag = new GeneralApplicationCaseData().applicantBilingualLanguagePreference(
                    YES)
                .claimantBilingualLanguagePreference("WELSH")
                .legacyCaseReference(LEGACY_CASE_REFERENCE).build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(claimantClaimIssueFlag);
            docUploadNotificationService.notifyApplicantEvidenceUpload(caseData);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "ga-notice-of-document-lip-appln-welsh-template-id",
                getNotificationDataMapForLip(YES, NO),
                "general-apps-notice-of-document-upload-" + CASE_REFERENCE
            );
        }

        @Test
        void lipRespondentNotificationShouldSend() {
            when(notificationsProperties.getLipGeneralAppRespondentEmailTemplate())
                .thenReturn("ga-notice-of-document-lip-respondent-template-id");
            when(configuration.getSpecUnspecContact()).thenReturn(
                "Email for Specified Claims: contactocmc@justice.gov.uk "
                    + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            when(gaForLipService.isLipApp(any())).thenReturn(false);
            when(gaForLipService.isLipResp(any())).thenReturn(true);

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();
            GASolicitorDetailsGAspec respondent1 = new GASolicitorDetailsGAspec().setId("id")
                .setEmail(DUMMY_EMAIL).setSurname(Optional.of("surname")).setForename("forename").setOrganisationIdentifier("2");
            respondentSols.add(element(respondent1));

            GeneralApplicationCaseData caseData = getCaseData(true, NO, YES).copy()
                .generalAppRespondentSolicitors(respondentSols).build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(parentCaseData());
            docUploadNotificationService.notifyRespondentEvidenceUpload(caseData);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "ga-notice-of-document-lip-respondent-template-id",
                getNotificationDataMapForLip(NO, YES),
                "general-apps-notice-of-document-upload-" + CASE_REFERENCE
            );
        }

        @Test
        void lipRespondentNotificationShouldSend_whenRespondentResponseInWelsh() {
            when(notificationsProperties.getLipGeneralAppRespondentEmailTemplateInWelsh())
                .thenReturn("ga-notice-of-document-lip-respondent-welsh-template-id");
            when(configuration.getSpecUnspecContact()).thenReturn(
                "Email for Specified Claims: contactocmc@justice.gov.uk "
                    + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            when(gaForLipService.isLipApp(any())).thenReturn(false);
            when(gaForLipService.isLipResp(any())).thenReturn(true);

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();
            GASolicitorDetailsGAspec respondent1 = new GASolicitorDetailsGAspec().setId("id")
                .setEmail(DUMMY_EMAIL).setSurname(Optional.of("surname")).setForename("forename").setOrganisationIdentifier("2");
            respondentSols.add(element(respondent1));

            GeneralApplicationCaseData caseData = getCaseData(true, NO, YES).copy()
                .generalAppRespondentSolicitors(respondentSols).respondentBilingualLanguagePreference(YES).build();
            GeneralApplicationCaseData claimantClaimIssueFlag = new GeneralApplicationCaseData().respondentBilingualLanguagePreference(YES)
                .respondent1LiPResponse(new RespondentLiPResponse().setRespondent1ResponseLanguage(Language.BOTH.toString()))
                .legacyCaseReference(LEGACY_CASE_REFERENCE).build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(claimantClaimIssueFlag);
            docUploadNotificationService.notifyRespondentEvidenceUpload(caseData);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "ga-notice-of-document-lip-respondent-welsh-template-id",
                getNotificationDataMapForLip(NO, YES),
                "general-apps-notice-of-document-upload-" + CASE_REFERENCE
            );
        }

        @Test
        void shouldUseNotProvidedWhenApplicantOrganisationIdentifierIsMissing() {
            GeneralApplicationCaseData caseData = getCaseData(true, NO, NO).copy()
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("id").setEmail(DUMMY_EMAIL))
                .build();

            Map<String, String> properties = docUploadNotificationService.addProperties(caseData, parentCaseData());

            assertThat(properties)
                .containsEntry(NotificationDataGA.CLAIM_LEGAL_ORG_NAME_SPEC, "Not provided")
                .containsEntry(NotificationData.UPLOADED_DOCUMENTS, UPLOADED_DOCUMENT_NAMES);
        }

        @Test
        void shouldUseNotProvidedWhenApplicantOrganisationCannotBeFound() {
            GeneralApplicationCaseData caseData = getCaseData(true, NO, NO).copy()
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("id")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("missing-org"))
                .build();
            when(organisationService.findOrganisationById("missing-org")).thenReturn(Optional.empty());

            Map<String, String> properties = docUploadNotificationService.addProperties(caseData, parentCaseData());

            assertThat(properties).containsEntry(NotificationDataGA.CLAIM_LEGAL_ORG_NAME_SPEC, "Not provided");
        }

        @Test
        void shouldUseNotProvidedWhenCaseHasNoUploadedDocumentsOrLegacyReference() {
            GeneralApplicationCaseData caseData = getCaseData(true, NO, NO).gaAddlDoc(null);
            GeneralApplicationCaseData mainCaseData = new GeneralApplicationCaseData()
                .ccdState(CaseState.CASE_PROGRESSION)
                .build();

            Map<String, String> properties = docUploadNotificationService.addProperties(caseData, mainCaseData);

            assertThat(properties)
                .containsEntry(NotificationData.CASEMAN_REF, "Not provided")
                .containsEntry(NotificationData.UPLOADED_DOCUMENTS, "Not provided");
        }

        @Test
        void shouldUseNotProvidedWhenUploadedDocumentsHaveNoNames() {
            GeneralApplicationCaseData caseData = getCaseData(true, NO, NO).gaAddlDoc(List.of(
                element(new CaseDocument()),
                new Element<>()
            ));

            Map<String, String> properties = docUploadNotificationService.addProperties(caseData, parentCaseData());

            assertThat(properties).containsEntry(NotificationData.UPLOADED_DOCUMENTS, "Not provided");
        }

        private Map<String, String> getNotificationDataMapForLip(YesOrNo isLipAppln, YesOrNo isLipRespondent) {

            Map<String, String> customProp = new HashMap<>();
            customProp.put(NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString());
            customProp.put(NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString());
            customProp.put(NotificationDataGA.CASE_TITLE, "CL v DEF");
            customProp.put(NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE);
            customProp.put(NotificationData.CASEMAN_REF, LEGACY_CASE_REFERENCE);
            customProp.put(
                NotificationDataGA.CLAIM_LEGAL_ORG_NAME_SPEC,
                isLipRespondent == YES ? RESPONDENT_ORG_NAME : APPLICANT_ORG_NAME
            );
            customProp.put(NotificationData.UPLOADED_DOCUMENTS, UPLOADED_DOCUMENT_NAMES);
            customProp.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            customProp.put(
                NotificationDataGA.WELSH_HMCTS_SIGNATURE,
                "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF"
            );
            customProp.put(
                NotificationDataGA.WELSH_OPENING_HOURS,
                "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm"
            );
            customProp.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            customProp.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            customProp.put(
                NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
                    + "\n Email for Damages Claims: damagesclaims@justice.gov.uk"
            );
            customProp.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            customProp.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            customProp.put(
                NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                    + "\n For all other matters, call 0300 123 7050"
            );

            if (isLipAppln == YES) {
                customProp.put(NotificationDataGA.GA_LIP_APPLICANT_NAME, "App");
            }

            if (isLipRespondent == YES) {
                customProp.put(NotificationDataGA.GA_LIP_RESP_NAME, "DEF");
            }
            return customProp;
        }

        private Map<String, String> getNotificationDataMap(boolean customReferencePresent, boolean isLipCase) {
            HashMap<String, String> properties = new HashMap<>();
            if (customReferencePresent) {
                properties.put(NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString());
                properties.put(NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString());
                properties.put(NotificationDataGA.PARTY_REFERENCE, CUSTOM_PARTY_REFERENCE);
            } else {
                properties.put(NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString());
                properties.put(NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString());
                properties.put(NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE);
            }
            properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            properties.put(NotificationData.CASEMAN_REF, LEGACY_CASE_REFERENCE);
            properties.put(
                NotificationDataGA.CLAIM_LEGAL_ORG_NAME_SPEC,
                isLipCase ? RESPONDENT_ORG_NAME : APPLICANT_ORG_NAME
            );
            properties.put(NotificationData.UPLOADED_DOCUMENTS, UPLOADED_DOCUMENT_NAMES);
            properties.put(
                NotificationDataGA.WELSH_HMCTS_SIGNATURE,
                "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF"
            );
            properties.put(
                NotificationDataGA.WELSH_OPENING_HOURS,
                "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm"
            );
            properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            if (isLipCase) {
                properties.put(
                    NotificationData.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
                        + "\n Email for Damages Claims: damagesclaims@justice.gov.uk"
                );
            } else {
                properties.put(NotificationData.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
            }
            properties.put(NotificationData.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            properties.put(NotificationData.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            properties.put(
                NotificationData.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                    + "\n For all other matters, call 0300 123 7050"
            );
            return properties;
        }

        private GeneralApplicationCaseData parentCaseData() {
            return new GeneralApplicationCaseData()
                .ccdState(CaseState.CASE_PROGRESSION)
                .legacyCaseReference(LEGACY_CASE_REFERENCE)
                .build();
        }

        private GeneralApplicationCaseData getCaseData(boolean isMet, YesOrNo isGaApplicantLip, YesOrNo isGaRespondentOneLip) {

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = new GASolicitorDetailsGAspec().setId("id")
                .setEmail(DUMMY_EMAIL).setForename("forename").setOrganisationIdentifier("2");

            GASolicitorDetailsGAspec respondent2 = new GASolicitorDetailsGAspec().setId("id")
                .setEmail(DUMMY_EMAIL).setForename("forename").setOrganisationIdentifier("3");

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            if (isMet) {

                GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder()
                    .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("id")
                                                  .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                    .generalAppRespondentSolicitors(respondentSols)
                    .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("1"))
                    .applicantPartyName("App")
                    .claimant1PartyName("CL")
                    .defendant1PartyName("DEF")
                    .isGaRespondentOneLip(isGaRespondentOneLip)
                    .isGaApplicantLip(isGaApplicantLip)
                    .businessProcess(new BusinessProcess().setStatus(STARTED)
                                         .setProcessInstanceId(PROCESS_INSTANCE_ID))
                    .gaInformOtherParty(new GAInformOtherParty().setIsWithNotice(YES))
                    .gaUrgencyRequirement(new GAUrgencyRequirement().setGeneralAppUrgency(NO))
                    .parentClaimantIsApplicant(YES)
                    .gaRespondentOrderAgreement(new GARespondentOrderAgreement().setHasAgreed(NO))
                    .respondentSolicitor1EmailAddress(DUMMY_EMAIL)
                    .respondentSolicitor2EmailAddress(DUMMY_EMAIL)
                    .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID("1")))
                    .respondent1OrganisationPolicy(new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID("2")))
                    .respondent2OrganisationPolicy(new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID("3")))
                    .ccdCaseReference(CASE_REFERENCE)
                    .build();
                return caseData.gaAddlDoc(uploadedDocuments());
            } else {
                GeneralApplicationCaseData caseData = new GeneralApplicationCaseDataBuilder()
                    .emailPartyReference("Claimant Reference: ABC Ltd - Defendant Reference: Defendant Ltd")
                    .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("id")
                                                  .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                    .generalAppRespondentSolicitors(respondentSols)
                    .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("1"))
                    .applicantPartyName("App")
                    .claimant1PartyName("CL")
                    .defendant1PartyName("DEF")
                    .isGaRespondentOneLip(isGaRespondentOneLip)
                    .isGaApplicantLip(isGaApplicantLip)
                    .businessProcess(new BusinessProcess().setStatus(STARTED)
                                         .setProcessInstanceId(PROCESS_INSTANCE_ID))
                    .gaInformOtherParty(new GAInformOtherParty().setIsWithNotice(YES))
                    .gaUrgencyRequirement(new GAUrgencyRequirement().setGeneralAppUrgency(NO))
                    .parentClaimantIsApplicant(YES)
                    .gaRespondentOrderAgreement(new GARespondentOrderAgreement().setHasAgreed(NO))
                    .respondentSolicitor1EmailAddress(DUMMY_EMAIL)
                    .respondentSolicitor2EmailAddress(DUMMY_EMAIL)
                    .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID("1")))
                    .respondent1OrganisationPolicy(new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID("2")))
                    .respondent2OrganisationPolicy(new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID("3")))
                    .ccdCaseReference(CASE_REFERENCE)
                    .build();
                return caseData.gaAddlDoc(uploadedDocuments());
            }
        }

        private List<Element<CaseDocument>> uploadedDocuments() {
            return List.of(
                element(new CaseDocument().setDocumentName("applicant evidence.pdf")),
                element(new CaseDocument().setDocumentName("respondent evidence.pdf"))
            );
        }
    }
}
