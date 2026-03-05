package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CASE_ID;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.ga.utils.EmailFooterUtils.RAISE_QUERY_LR;

@ExtendWith(MockitoExtension.class)
public class HearingScheduledNotificationServiceTest {

    @InjectMocks
    private HearingScheduledNotificationService hearingScheduledNotificationService;
    @Mock
    private SolicitorEmailValidation solicitorEmailValidation;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private GaForLipService gaForLipService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationsSignatureConfiguration configuration;

    private static final Long CASE_REFERENCE = 111111L;
    private static final LocalDate GA_HEARING_DATE_SAMPLE = LocalDate.now().plusDays(10);
    private static final LocalTime GA_HEARING_TIME_SAMPLE = LocalTime.of(15, 30, 0);
    private static final String DUMMY_EMAIL = "hmcts.civil@gmail.com";
    private static final String PARTY_REFERENCE = "Claimant Reference: Not provided - Defendant Reference: Not provided";
    private static final String CUSTOM_PARTY_REFERENCE = "Claimant Reference: ABC limited - Defendant Reference: Defendant Ltd";
    private Map<String, String> customProp = new HashMap<>();

    @BeforeEach
    void setup() {
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

    private Map<String, String> getNotificationDataMap(boolean customEmailReference) {
        HashMap<String, String> properties = new HashMap<>();
        properties.put(NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString());
        properties.put(NotificationDataGA.GENAPP_REFERENCE, CASE_ID.toString());
        properties.put(NotificationDataGA.GA_HEARING_DATE, DateFormatHelper.formatLocalDate(
                                           GA_HEARING_DATE_SAMPLE, DateFormatHelper.DATE));
        properties.put(NotificationDataGA.GA_HEARING_TIME, GA_HEARING_TIME_SAMPLE.toString());
        properties.put(NotificationDataGA.PARTY_REFERENCE, customEmailReference ? CUSTOM_PARTY_REFERENCE : PARTY_REFERENCE);
        properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
        properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
        properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
            + "\n For all other matters, call 0300 123 7050");
        return properties;
    }

    private Map<String, String> getNotificationDataMapLip(YesOrNo isLipAppln, YesOrNo isLipRespondent) {
        customProp.put(NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString());
        customProp.put(NotificationDataGA.GENAPP_REFERENCE, CASE_ID.toString());
        customProp.put(NotificationDataGA.GA_HEARING_DATE, DateFormatHelper.formatLocalDate(
            GA_HEARING_DATE_SAMPLE, DateFormatHelper.DATE));
        customProp.put(NotificationDataGA.CASE_TITLE, "Test Claimant1 Name v Test Defendant1 Name");

        customProp.put(NotificationDataGA.GA_HEARING_TIME, GA_HEARING_TIME_SAMPLE.toString());
        customProp.put(NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE);
        customProp.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
        customProp.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        customProp.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        customProp.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        customProp.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        customProp.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
            + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        customProp.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        customProp.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        customProp.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
            + "\n For all other matters, call 0300 123 7050");
        if (isLipAppln == YES) {
            customProp.put(NotificationDataGA.GA_LIP_APPLICANT_NAME, "Test Applicant Name");
        }

        if (isLipRespondent == YES) {
            customProp.put(NotificationDataGA.GA_LIP_RESP_NAME, "Test Defendant1 Name");
        }
        return customProp;
    }

    @Test
    void notificationShouldSendToDefendantsWhenInvoked() {
        when(notificationsProperties.getHearingNoticeTemplate())
            .thenReturn("general-apps-notice-of-hearing-template-id");
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .build();
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().ccdState(CaseState.CASE_PROGRESSION).build());

        when(solicitorEmailValidation
                 .validateSolicitorEmail(any(), any()))
            .thenReturn(caseData);

        hearingScheduledNotificationService.sendNotificationForDefendant(caseData);
        verify(notificationService, times(2)).sendMail(
            DUMMY_EMAIL,
            "general-apps-notice-of-hearing-template-id",
            getNotificationDataMap(false),
            "general-apps-notice-of-hearing-" + CASE_REFERENCE
        );
    }

    @Test
    void notificationShouldSendEmailReferenceWhenSolicitorReferenceisPresent() {
        when(notificationsProperties.getHearingNoticeTemplate())
            .thenReturn("general-apps-notice-of-hearing-template-id");
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .emailPartyReference("Claimant Reference: ABC limited - Defendant Reference: Defendant Ltd")
            .build();
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().ccdState(CaseState.CASE_PROGRESSION).build());

        when(solicitorEmailValidation
                 .validateSolicitorEmail(any(), any()))
            .thenReturn(caseData);

        hearingScheduledNotificationService.sendNotificationForDefendant(caseData);
        verify(notificationService, times(2)).sendMail(
            DUMMY_EMAIL,
            "general-apps-notice-of-hearing-template-id",
            getNotificationDataMap(true),
            "general-apps-notice-of-hearing-" + CASE_REFERENCE
        );
    }

    @Test
    void notificationShouldSendToClaimantWhenInvoked() {
        when(notificationsProperties.getHearingNoticeTemplate())
            .thenReturn("general-apps-notice-of-hearing-template-id");
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .build();

        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().ccdState(CaseState.CASE_PROGRESSION).build());
        when(solicitorEmailValidation
                 .validateSolicitorEmail(any(), any()))
            .thenReturn(caseData);

        hearingScheduledNotificationService.sendNotificationForClaimant(caseData);
        verify(notificationService, times(1)).sendMail(
            DUMMY_EMAIL,
            "general-apps-notice-of-hearing-template-id",
            getNotificationDataMap(false),
            "general-apps-notice-of-hearing-" + CASE_REFERENCE
        );
    }

    @Test
    void notificationShouldSendToLipDefendantWhenInvoked() {
        when(gaForLipService.isLipApp(any())).thenReturn(false);
        when(gaForLipService.isLipResp(any())).thenReturn(true);
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        when(notificationsProperties.getLipGeneralAppRespondentEmailTemplate())
            .thenReturn("ga-notice-of-hearing-respondent-template-id");

        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();
        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).surname(Optional.of("surname"))
            .forename("forename").organisationIdentifier("2").build();
        respondentSols.add(element(respondent1));

        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .isGaApplicantLip(YesOrNo.NO)
            .isGaRespondentOneLip(YES)
            .generalAppRespondentSolicitors(respondentSols)
            .defendant2PartyName(null)
            .build();
        when(solicitorEmailValidation
                 .validateSolicitorEmail(any(), any()))
            .thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());

        hearingScheduledNotificationService.sendNotificationForDefendant(caseData);
        verify(notificationService, times(1)).sendMail(
            DUMMY_EMAIL,
            "ga-notice-of-hearing-respondent-template-id",
            getNotificationDataMapLip(NO, YES),
            "general-apps-notice-of-hearing-" + CASE_REFERENCE
        );
    }

    @Test
    void notificationShouldSendToLipApplicantWhenInvoked() {
        when(gaForLipService.isLipApp(any())).thenReturn(true);
        when(gaForLipService.isLipResp(any())).thenReturn(false);
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(notificationsProperties.getLipGeneralAppApplicantEmailTemplate())
            .thenReturn("ga-notice-of-hearing-applicant-template-id");
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .isGaApplicantLip(YesOrNo.YES)
            .isGaRespondentOneLip(YesOrNo.NO)
            .defendant2PartyName(null)
            .build();
        when(solicitorEmailValidation
                 .validateSolicitorEmail(any(), any()))
            .thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");

        hearingScheduledNotificationService.sendNotificationForClaimant(caseData);
        verify(notificationService, times(1)).sendMail(
            DUMMY_EMAIL,
            "ga-notice-of-hearing-applicant-template-id",
            getNotificationDataMapLip(YES, NO),
            "general-apps-notice-of-hearing-" + CASE_REFERENCE
        );
    }

    @Test
    void notificationWelshShouldSendToLipApplicantWhenInvoked() {
        when(gaForLipService.isLipApp(any())).thenReturn(true);
        when(gaForLipService.isLipResp(any())).thenReturn(false);
        when(gaForLipService.isGaForLip(any())).thenReturn(true);
        when(notificationsProperties.getLipGeneralAppApplicantEmailTemplateInWelsh())
            .thenReturn("ga-notice-of-hearing-applicant-welsh-template-id");
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .isGaApplicantLip(YesOrNo.YES)
            .isGaRespondentOneLip(YesOrNo.NO)
            .parentClaimantIsApplicant(YES)
            .applicantBilingualLanguagePreference(YES)
            .defendant2PartyName(null)
            .build();
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        when(solicitorEmailValidation
                 .validateSolicitorEmail(any(), any()))
            .thenReturn(caseData);
        GeneralApplicationCaseData claimantClaimIssueFlag = new GeneralApplicationCaseData().claimantBilingualLanguagePreference(Language.WELSH.toString()).build();
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(claimantClaimIssueFlag);

        hearingScheduledNotificationService.sendNotificationForClaimant(caseData);
        verify(notificationService, times(1)).sendMail(
            DUMMY_EMAIL,
            "ga-notice-of-hearing-applicant-welsh-template-id",
            getNotificationDataMapLip(YES, NO),
            "general-apps-notice-of-hearing-" + CASE_REFERENCE
        );
    }

}
