package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

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
import static uk.gov.hmcts.reform.civil.utils.EmailFooterUtils.RAISE_QUERY_LR;

@SpringBootTest(classes = {
    HearingScheduledNotificationService.class,
    JacksonAutoConfiguration.class
})
public class HearingScheduledNotificationServiceTest {

    @Autowired
    private HearingScheduledNotificationService hearingScheduledNotificationService;
    @MockBean
    private SolicitorEmailValidation solicitorEmailValidation;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;
    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private GaForLipService gaForLipService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private NotificationsSignatureConfiguration configuration;

    private static final Long CASE_REFERENCE = 111111L;
    private static final LocalDate GA_HEARING_DATE_SAMPLE = LocalDate.now().plusDays(10);
    private static final LocalTime GA_HEARING_TIME_SAMPLE = LocalTime.of(15, 30, 0);
    private static final String DUMMY_EMAIL = "hmcts.civil@gmail.com";
    private static final String PARTY_REFERENCE = "Claimant Reference: Not provided - Defendant Reference: Not provided";
    private static final String CUSTOM_PARTY_REFERENCE = "Claimant Reference: ABC limited - Defendant Reference: Defendant Ltd";
    private Map<String, String> customProp = new HashMap<>();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new Jdk8Module())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeEach
    void setup() {
        when(notificationsProperties.getHearingNoticeTemplate())
            .thenReturn("general-apps-notice-of-hearing-template-id");
        when(notificationsProperties.getLipGeneralAppApplicantEmailTemplate())
            .thenReturn("ga-notice-of-hearing-applicant-template-id");
        when(notificationsProperties.getLipGeneralAppApplicantEmailTemplateInWelsh())
            .thenReturn("ga-notice-of-hearing-applicant-welsh-template-id");
        when(notificationsProperties.getLipGeneralAppRespondentEmailTemplate())
            .thenReturn("ga-notice-of-hearing-respondent-template-id");
        when(notificationsProperties.getLipGeneralAppRespondentEmailTemplateInWelsh())
            .thenReturn("ga-notice-of-hearing-respondent-welsh-template-id");
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getWelshContact()).thenReturn("E-bost: ymholiadaucymraeg@justice.gov.uk");
        when(configuration.getSpecContact()).thenReturn("Email: contactocmc@justice.gov.uk");
        when(configuration.getWelshHmctsSignature()).thenReturn("Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        when(configuration.getWelshPhoneContact()).thenReturn("Ffôn: 0300 303 5174");
        when(configuration.getWelshOpeningHours()).thenReturn("Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        when(solicitorEmailValidation.validateSolicitorEmail(
            any(CaseData.class),
            any(GeneralApplicationCaseData.class)
        )).thenAnswer(invocation -> invocation.getArgument(1));
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
        properties.put(NotificationData.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        properties.put(NotificationData.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        properties.put(NotificationData.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        properties.put(NotificationData.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
        properties.put(NotificationData.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        properties.put(NotificationData.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        properties.put(NotificationData.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
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
        customProp.put(NotificationData.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        customProp.put(NotificationData.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        customProp.put(NotificationData.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        customProp.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        customProp.put(NotificationData.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
            + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        customProp.put(NotificationData.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        customProp.put(NotificationData.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        customProp.put(NotificationData.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
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

        CaseData caseData = toGaCaseData(CaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .build());
        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(emptyGaCaseDataWithState(CaseState.CASE_PROGRESSION));

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

        CaseData caseData = toGaCaseData(CaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .emailPartyReference("Claimant Reference: ABC limited - Defendant Reference: Defendant Ltd")
            .build());
        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(emptyGaCaseDataWithState(CaseState.CASE_PROGRESSION));

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
        CaseData caseData = toGaCaseData(CaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .build());

        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(emptyGaCaseDataWithState(CaseState.CASE_PROGRESSION));

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
        when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();
        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).surname(Optional.of("surname"))
            .forename("forename").organisationIdentifier("2").build();
        respondentSols.add(element(respondent1));

        CaseData caseData = toGaCaseData(CaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .isGaApplicantLip(YesOrNo.NO)
            .isGaRespondentOneLip(YES)
            .generalAppRespondentSolicitors(respondentSols)
            .defendant2PartyName(null)
            .build());
        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(emptyGaCaseData());

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
        when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
        CaseData caseData = toGaCaseData(CaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .isGaApplicantLip(YesOrNo.YES)
            .isGaRespondentOneLip(YesOrNo.NO)
            .defendant2PartyName(null)
            .build());
        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(emptyGaCaseData());
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
        when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
        CaseData caseData = toGaCaseData(CaseDataBuilder.builder().hearingScheduledApplication(YesOrNo.NO)
            .isGaApplicantLip(YesOrNo.YES)
            .isGaRespondentOneLip(YesOrNo.NO)
            .parentClaimantIsApplicant(YES)
            .applicantBilingualLanguagePreference(YES)
            .defendant2PartyName(null)
            .build());
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        CaseData claimantClaimIssueFlag = toGaCaseData(CaseData.builder().claimantBilingualLanguagePreference(Language.WELSH.toString()).build());
        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(claimantClaimIssueFlag);

        hearingScheduledNotificationService.sendNotificationForClaimant(caseData);
        verify(notificationService, times(1)).sendMail(
            DUMMY_EMAIL,
            "ga-notice-of-hearing-applicant-welsh-template-id",
            getNotificationDataMapLip(YES, NO),
            "general-apps-notice-of-hearing-" + CASE_REFERENCE
        );
    }

    private static CaseData toGaCaseData(CaseDataBuilder builder) {
        return toGaCaseData(builder.build());
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
