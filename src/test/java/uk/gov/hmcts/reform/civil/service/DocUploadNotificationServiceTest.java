package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.STARTED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.EmailFooterUtils.RAISE_QUERY_LR;

@SpringBootTest(classes = {
    DocUploadNotificationService.class,
    JacksonAutoConfiguration.class
})
public class DocUploadNotificationServiceTest {

    @Autowired
    private DocUploadNotificationService docUploadNotificationService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationsProperties notificationsProperties;

    @MockBean
    private GaForLipService gaForLipService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private NotificationsSignatureConfiguration configuration;

    private static final Long CASE_REFERENCE = 111111L;
    private static final String PROCESS_INSTANCE_ID = "1";
    private static final String DUMMY_EMAIL = "hmcts.civil@gmail.com";
    private static final String PARTY_REFERENCE = "Claimant Reference: Not provided - Defendant Reference: Not provided";
    private static final String CUSTOM_PARTY_REFERENCE = "Claimant Reference: ABC Ltd - Defendant Reference: Defendant Ltd";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Map<String, String> customProp = new HashMap<>();

    @Nested
    class AboutToSubmitCallback {
        @BeforeEach
        void setup() {
            when(notificationsProperties.getEvidenceUploadTemplate())
                    .thenReturn("general-apps-notice-of-document-template-id");
            when(notificationsProperties.getLipGeneralAppApplicantEmailTemplate())
                .thenReturn("ga-notice-of-document-lip-appln-template-id");
            when(notificationsProperties.getLipGeneralAppApplicantEmailTemplateInWelsh())
                .thenReturn("ga-notice-of-document-lip-appln-welsh-template-id");
            when(notificationsProperties.getLipGeneralAppRespondentEmailTemplate())
                .thenReturn("ga-notice-of-document-lip-respondent-template-id");
            when(notificationsProperties.getLipGeneralAppRespondentEmailTemplateInWelsh())
                .thenReturn("ga-notice-of-document-lip-respondent-welsh-template-id");
            when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
            when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                                 + "\n For all other matters, call 0300 123 7050");
            when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
            when(configuration.getWelshContact()).thenReturn("E-bost: ymholiadaucymraeg@justice.gov.uk");
            when(configuration.getSpecContact()).thenReturn("Email: contactocmc@justice.gov.uk");
            when(configuration.getWelshHmctsSignature()).thenReturn("Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            when(configuration.getWelshPhoneContact()).thenReturn("Ffôn: 0300 303 5174");
            when(configuration.getWelshOpeningHours()).thenReturn("Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            when(featureToggleService.isPublicQueryManagementEnabledGa(any(GeneralApplicationCaseData.class)))
                .thenReturn(true);
            when(featureToggleService.isPublicQueryManagementEnabled(any(CaseData.class))).thenReturn(true);
        }

        @Test
        void appNotificationShouldSendWhenInvoked() {
            CaseData caseData = getCaseData(true, NO, NO);
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(emptyGaCaseDataWithState(CaseState.CASE_PROGRESSION));
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

            CaseData caseData = getCaseData(false, NO, NO);
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(emptyGaCaseDataWithState(CaseState.CASE_PROGRESSION));
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
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(emptyGaCaseDataWithState(CaseState.CASE_PROGRESSION));
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            CaseData caseData = getCaseData(true, NO, YES);
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
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipAppGa(any(GeneralApplicationCaseData.class))).thenReturn(true);
            CaseData caseData = getCaseData(true, YES, NO);
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(emptyGaCaseData());
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
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipAppGa(any(GeneralApplicationCaseData.class))).thenReturn(true);
            CaseData caseData =
                getCaseData(true, YES, NO).toBuilder().applicantBilingualLanguagePreference(YES).build();
            CaseData claimantClaimIssueFlag = toGaCaseData(CaseData.builder().applicantBilingualLanguagePreference(YES)
                .claimantBilingualLanguagePreference("WELSH").build());
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(claimantClaimIssueFlag);
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
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipAppGa(any(GeneralApplicationCaseData.class))).thenReturn(false);
            when(gaForLipService.isLipRespGa(any(GeneralApplicationCaseData.class))).thenReturn(true);

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();
            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                .email(DUMMY_EMAIL).surname(Optional.of("surname")).forename("forename").organisationIdentifier("2").build();
            respondentSols.add(element(respondent1));

            CaseData caseData = getCaseData(true, NO, YES).toBuilder()
                .generalAppRespondentSolicitors(respondentSols).build();
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(emptyGaCaseData());
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
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipAppGa(any(GeneralApplicationCaseData.class))).thenReturn(false);
            when(gaForLipService.isLipRespGa(any(GeneralApplicationCaseData.class))).thenReturn(true);

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();
            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                .email(DUMMY_EMAIL).surname(Optional.of("surname")).forename("forename").organisationIdentifier("2").build();
            respondentSols.add(element(respondent1));

            CaseData caseData = getCaseData(true, NO, YES).toBuilder()
                .generalAppRespondentSolicitors(respondentSols).respondentBilingualLanguagePreference(YES).build();
            CaseData claimantClaimIssueFlag = toGaCaseData(CaseData.builder().respondentBilingualLanguagePreference(YES)
                .respondent1LiPResponse(RespondentLiPResponse.builder().respondent1ResponseLanguage(
                Language.BOTH.toString()).build()).build());
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(claimantClaimIssueFlag);
            docUploadNotificationService.notifyRespondentEvidenceUpload(caseData);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "ga-notice-of-document-lip-respondent-welsh-template-id",
                getNotificationDataMapForLip(NO, YES),
                "general-apps-notice-of-document-upload-" + CASE_REFERENCE
            );
        }

        private Map<String, String> getNotificationDataMapForLip(YesOrNo isLipAppln, YesOrNo isLipRespondent) {

            customProp.put(NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString());
            customProp.put(NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString());
            customProp.put(NotificationDataGA.CASE_TITLE, "CL v DEF");
            customProp.put(NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE);
            customProp.put(NotificationDataGA.WELSH_CONTACT, "I gysylltu â’r llys, dewiswch ‘contact or apply to the court’ ar eich dangosfwrdd.");
            customProp.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            customProp.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            customProp.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            customProp.put(NotificationDataGA.SPEC_CONTACT, "To contact the court, select contact or apply to the court on your dashboard.");
            customProp.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Contact us about your claim by selecting Raise a query from the next steps menu.");
            customProp.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            customProp.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            customProp.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                + "\n For all other matters, call 0300 123 7050");

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
            if (isLipCase) {
                properties.put(NotificationDataGA.WELSH_CONTACT, "I gysylltu â’r llys, dewiswch ‘contact or apply to the court’ ar eich dangosfwrdd.");
            } else {
                properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            }
            properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            if (isLipCase) {
                properties.put(NotificationDataGA.SPEC_CONTACT, "To contact the court, select contact or apply to the court on your dashboard.");
                properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Contact us about your claim by selecting Raise a query from the next steps menu.");
            } else {
                properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
                properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
            }
            properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                + "\n For all other matters, call 0300 123 7050");
            return properties;
        }

        private CaseData getCaseData(boolean isMet, YesOrNo isGaApplicantLip, YesOrNo isGaRespondentOneLip) {

            List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

            GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
                    .email(DUMMY_EMAIL).forename("forename").organisationIdentifier("2").build();

            GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
                    .email(DUMMY_EMAIL).forename("forename").organisationIdentifier("3").build();

            respondentSols.add(element(respondent1));
            respondentSols.add(element(respondent2));

            if (isMet) {

                return toGaCaseData(new CaseDataBuilder()
                        .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                .email(DUMMY_EMAIL).organisationIdentifier("1").build())
                        .generalAppRespondentSolicitors(respondentSols)
                        .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1").build())
                        .applicantPartyName("App")
                        .claimant1PartyName("CL")
                        .defendant1PartyName("DEF")
                        .isGaRespondentOneLip(isGaRespondentOneLip)
                        .isGaApplicantLip(isGaApplicantLip)
                        .businessProcess(BusinessProcess.builder().status(STARTED)
                                .processInstanceId(PROCESS_INSTANCE_ID).build())
                        .gaInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                        .gaUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
                        .parentClaimantIsApplicant(YES)
                        .gaRespondentOrderAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                        .respondentSolicitor1EmailAddress(DUMMY_EMAIL)
                        .respondentSolicitor2EmailAddress(DUMMY_EMAIL)
                        .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("1").build())
                                .build())
                        .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("2").build())
                                .build())
                        .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("3").build())
                                .build())
                        .ccdCaseReference(CASE_REFERENCE)
                        .build());
            } else {
                return toGaCaseData(new CaseDataBuilder()
                        .emailPartyReference("Claimant Reference: ABC Ltd - Defendant Reference: Defendant Ltd")
                        .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id")
                                .email(DUMMY_EMAIL).organisationIdentifier("1").build())
                        .generalAppRespondentSolicitors(respondentSols)
                        .generalAppParentCaseLink(GeneralAppParentCaseLink.builder().caseReference("1").build())
                        .applicantPartyName("App")
                        .claimant1PartyName("CL")
                        .defendant1PartyName("DEF")
                        .isGaRespondentOneLip(isGaRespondentOneLip)
                        .isGaApplicantLip(isGaApplicantLip)
                        .businessProcess(BusinessProcess.builder().status(STARTED)
                                .processInstanceId(PROCESS_INSTANCE_ID).build())
                        .gaInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                        .gaUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
                        .parentClaimantIsApplicant(YES)
                        .gaRespondentOrderAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                        .respondentSolicitor1EmailAddress(DUMMY_EMAIL)
                        .respondentSolicitor2EmailAddress(DUMMY_EMAIL)
                        .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("1").build())
                                .build())
                        .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("2").build())
                                .build())
                        .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                .organisation(Organisation.builder().organisationID("3").build())
                                .build())
                        .ccdCaseReference(CASE_REFERENCE)
                        .build());
            }
        }
    }

    private static CaseData toGaCaseData(CaseData caseData) {
        Long ccdCaseReference = caseData.getCcdCaseReference();
        CaseState ccdState = caseData.getCcdState();
        GeneralApplicationCaseData gaCaseData = OBJECT_MAPPER.convertValue(caseData, GeneralApplicationCaseData.class);
        CaseData rebuilt = OBJECT_MAPPER.convertValue(gaCaseData, CaseData.class);
        return rebuilt.toBuilder()
            .ccdCaseReference(ccdCaseReference)
            .ccdState(ccdState)
            .build();
    }

    private static CaseData emptyGaCaseData() {
        return toGaCaseData(CaseData.builder().build());
    }

    private static CaseData emptyGaCaseDataWithState(CaseState state) {
        return emptyGaCaseData().toBuilder().ccdState(state).build();
    }
}
