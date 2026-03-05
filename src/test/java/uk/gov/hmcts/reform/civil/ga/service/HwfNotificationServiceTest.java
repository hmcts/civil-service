package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.HwFMoreInfoRequiredDocuments;
import uk.gov.hmcts.reform.civil.enums.NoRemissionDetailsSummary;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.FEE_PAYMENT_OUTCOME_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.INVALID_HWF_REFERENCE_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MORE_INFORMATION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.PARTIAL_REMISSION_HWF_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_HELP_WITH_FEE_NUMBER_GA;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_ADD_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.APPLICANT_NAME;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.CASE_REFERENCE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.CASE_TITLE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.FEE_AMOUNT;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.HWF_MORE_INFO_DATE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.HWF_MORE_INFO_DATE_IN_WELSH;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.HWF_MORE_INFO_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.HWF_MORE_INFO_DOCUMENTS_WELSH;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.HWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.NO_REMISSION_REASONS;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.NO_REMISSION_REASONS_WELSH;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.PART_AMOUNT;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.REMAINING_AMOUNT;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.TYPE_OF_FEE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.TYPE_OF_FEE_WELSH;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.formatDateInWelsh;

@ExtendWith(MockitoExtension.class)
public class HwfNotificationServiceTest {

    private static final String EMAIL_TEMPLATE_MORE_INFO_HWF = "test-hwf-more-info-id";
    private static final String EMAIL_TEMPLATE_INVALID_HWF_REFERENCE = "test-hwf-invalidrefnumber-id";
    private static final String EMAIL_TEMPLATE_UPDATE_REF_NUMBER = "test-hwf-updaterefnumber-id";
    private static final String EMAIL_TEMPLATE_HWF_PARTIAL_REMISSION = "test-hwf-partialRemission-id";
    private static final String EMAIL_TEMPLATE_NO_REMISSION = "test-hwf-noRemission-id";
    private static final String EMAIL_TEMPLATE_HWF_PAYMENT_OUTCOME = "test-hwf-paymentoutcome-id";
    private static final String EMAIL_TEMPLATE_MORE_INFO_HWF_BILINGUAL = "test-hwf-more-info-bilingual-id";
    private static final String EMAIL_TEMPLATE_INVALID_HWF_REFERENCE_BILINGUAL = "test-hwf-invalidrefnumber-bilingual-id";
    private static final String EMAIL_TEMPLATE_UPDATE_REF_NUMBER_BILINGUAL = "test-hwf-updaterefnumber-bilingual-id";
    private static final String EMAIL_TEMPLATE_HWF_PARTIAL_REMISSION_BILINGUAL = "test-hwf-partialRemission-bilingual-id";
    private static final String EMAIL_TEMPLATE_NO_REMISSION_BILINGUAL = "test-hwf-noremission-bilingual-id";
    private static final String EMAIL_TEMPLATE_HWF_PAYMENT_OUTCOME_WLESH = "test-hwf-paymentoutcome-welsh-id";
    private static final String EMAIL = "test@email.com";
    private static final String APPLICANT = "Mr. John Rambo";
    private static final String CLAIMANT = "Mr. John Rambo";
    private static final String DEFENDANT = "Mr. Joe Doe";
    private static final String GA_REFERENCE = "1111222233334444";
    private static final String HWF_REFERENCE = "000HWF001";
    private static final String REFERENCE_NUMBER = "1";
    private static final String REMISSION_AMOUNT = "100000.00";
    private static final String OUTSTANDING_AMOUNT_IN_POUNDS = "500.00";
    private static final LocalDate NOW = LocalDate.now();

    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private NotificationService notificationService;
    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private SolicitorEmailValidation solicitorEmailValidation;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private NotificationsSignatureConfiguration configuration;
    @InjectMocks
    private HwfNotificationService service;

    private static final GeneralApplicationCaseData  GA_CASE_DATA = GeneralApplicationCaseDataBuilder.builder()
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("1"))
            .ccdState(AWAITING_APPLICATION_PAYMENT)
            .parentClaimantIsApplicant(YesOrNo.YES)
            .ccdCaseReference(1111222233334444L)
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                    .email(EMAIL)
                    .build()).build()
            .copy()
            .applicantPartyName(APPLICANT)
            .claimant1PartyName(CLAIMANT)
            .defendant1PartyName(DEFENDANT)
            .generalAppHelpWithFees(new HelpWithFees().setHelpWithFeesReferenceNumber(
                    HWF_REFERENCE))
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                    .setFee(new Fee().setCalculatedAmountInPence(BigDecimal.valueOf(100000)))
                    )
            .hwfFeeType(FeeType.APPLICATION)
            .build();

    private static final GeneralApplicationCaseData  ADDITIONAL_CASE_DATA = GeneralApplicationCaseDataBuilder.builder()
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference("1"))
            .ccdState(APPLICATION_ADD_PAYMENT)
            .ccdCaseReference(1111222233334444L)
            .parentClaimantIsApplicant(YesOrNo.YES)
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                    .email(EMAIL)
                    .build()).build()
            .copy()
            .applicantPartyName(APPLICANT)
            .claimant1PartyName(CLAIMANT)
            .defendant1PartyName(DEFENDANT)
            .generalAppHelpWithFees(new HelpWithFees().setHelpWithFeesReferenceNumber(
                    HWF_REFERENCE))
            .generalAppPBADetails(new GeneralApplicationPbaDetails()
                    .setFee(new Fee().setCalculatedAmountInPence(BigDecimal.valueOf(100000)))
                    )
            .hwfFeeType(FeeType.ADDITIONAL)
            .build();

    private static final GeneralApplicationCaseData  CIVIL_WELSH_CLA = new GeneralApplicationCaseData().claimantBilingualLanguagePreference("WELSH").build();
    private static final GeneralApplicationCaseData  CIVIL_WELSH_DEF = new GeneralApplicationCaseData()
        .respondent1LiPResponse(new RespondentLiPResponse().setRespondent1ResponseLanguage("BOTH")).build();

    @BeforeEach
    void setup() {
        when(coreCaseDataService.getCase(any())).thenReturn(CaseDetails.builder().build());
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        when(configuration.getWelshContact()).thenReturn("E-bost: ymholiadaucymraeg@justice.gov.uk");
        when(configuration.getSpecContact()).thenReturn("Email: contactocmc@justice.gov.uk");
        when(configuration.getWelshHmctsSignature()).thenReturn("Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        when(configuration.getWelshPhoneContact()).thenReturn("Ffôn: 0300 303 5174");
        when(configuration.getWelshOpeningHours()).thenReturn("Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_MoreInformation_Ga() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
                .setHwfCaseEvent(MORE_INFORMATION_HWF_GA);
        GeneralApplicationCaseData  caseData = GA_CASE_DATA.copy()
            .isGaApplicantLip(YesOrNo.YES)
                .helpWithFeesMoreInformationGa(new HelpWithFeesMoreInformation()
                        .setHwFMoreInfoDocumentDate(NOW)
                        .setHwFMoreInfoRequiredDocuments(
                                getMoreInformationDocumentList()))
                .parentCaseReference(GA_REFERENCE)
                .gaHwfDetails(hwfeeDetails).build();
        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());

        mockNotificationPropertiesEnglish();

        // When
        service.sendNotification(caseData);

        // Then
        verify(notificationService, times(1)).sendMail(
                EMAIL,
                "test-hwf-more-info-id",
                getNotificationDataMapMoreInfoGa(),
                REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_MoreInformation_Ga_Bilingual() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(MORE_INFORMATION_HWF_GA);
        GeneralApplicationCaseData  caseData = GA_CASE_DATA.copy()
            .parentCaseReference(GA_REFERENCE)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .isGaApplicantLip(YesOrNo.YES)
            .helpWithFeesMoreInformationGa(new HelpWithFeesMoreInformation()
                                               .setHwFMoreInfoDocumentDate(NOW)
                                               .setHwFMoreInfoRequiredDocuments(
                                                   getMoreInformationDocumentList()))
            .gaHwfDetails(hwfeeDetails).build();
        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(CIVIL_WELSH_CLA);

        mockNotificationPropertiesBilingual();

        // When
        service.sendNotification(caseData);

        // Then
        verify(notificationService, times(1)).sendMail(
            EMAIL,
            "test-hwf-more-info-bilingual-id",
            getNotificationDataMapMoreInfoGa(),
            REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_MoreInformation_Additional() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
                .setHwfCaseEvent(MORE_INFORMATION_HWF_GA);
        GeneralApplicationCaseData  caseData = ADDITIONAL_CASE_DATA.copy()
                .helpWithFeesMoreInformationAdditional(new HelpWithFeesMoreInformation()
                        .setHwFMoreInfoDocumentDate(NOW)
                        .setHwFMoreInfoRequiredDocuments(
                                getMoreInformationDocumentList()))
                .parentCaseReference(GA_REFERENCE)
                .isGaApplicantLip(YesOrNo.YES)
                .additionalHwfDetails(hwfeeDetails).build();

        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());

        mockNotificationPropertiesEnglish();

        // When
        service.sendNotification(caseData);

        // Then
        verify(notificationService, times(1)).sendMail(
                EMAIL,
                "test-hwf-more-info-id",
                getNotificationDataMapMoreInfoAdditional(),
                REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_MoreInformation_Additional_Bilingual_Def() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(MORE_INFORMATION_HWF_GA);
        GeneralApplicationCaseData  caseData = ADDITIONAL_CASE_DATA.copy()
            .parentCaseReference(GA_REFERENCE)
            .parentClaimantIsApplicant(YesOrNo.NO)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .isGaApplicantLip(YesOrNo.YES)
            .helpWithFeesMoreInformationAdditional(new HelpWithFeesMoreInformation()
                                                       .setHwFMoreInfoDocumentDate(NOW)
                                                       .setHwFMoreInfoRequiredDocuments(
                                                           getMoreInformationDocumentList()))
            .additionalHwfDetails(hwfeeDetails).build();

        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(CIVIL_WELSH_DEF);

        mockNotificationPropertiesBilingual();

        // When
        service.sendNotification(caseData);

        // Then
        verify(notificationService, times(1)).sendMail(
            EMAIL,
            "test-hwf-more-info-bilingual-id",
            getNotificationDataMapMoreInfoAdditional(),
            REFERENCE_NUMBER
        );
    }

    private List<HwFMoreInfoRequiredDocuments> getMoreInformationDocumentList() {
        return Collections.singletonList(HwFMoreInfoRequiredDocuments.CHILD_MAINTENANCE);
    }

    private Map<String, String> getNotificationDataMapMoreInfoGa() {
        HashMap<String, String> properties = new HashMap<>(Map.of(
                HWF_MORE_INFO_DATE, formatLocalDate(NOW, DATE),
                HWF_MORE_INFO_DATE_IN_WELSH, formatDateInWelsh(NOW, false),
                CLAIMANT_NAME, APPLICANT,
                CASE_REFERENCE, GA_REFERENCE,
                TYPE_OF_FEE, FeeType.APPLICATION.getLabel(),
                TYPE_OF_FEE_WELSH, FeeType.APPLICATION.getLabelInWelsh(),
                HWF_MORE_INFO_DOCUMENTS, getMoreInformationDocumentListString(),
                HWF_MORE_INFO_DOCUMENTS_WELSH, getMoreInformationDocumentListStringWelsh(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE
        ));
        properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
        properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
            + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
            + "\n For all other matters, call 0300 123 7050");
        return properties;
    }

    private Map<String, String> getNotificationDataMapMoreInfoAdditional() {
        HashMap<String, String> properties = new HashMap<>(Map.of(
                HWF_MORE_INFO_DATE, formatLocalDate(NOW, DATE),
                HWF_MORE_INFO_DATE_IN_WELSH, formatDateInWelsh(NOW, false),
                CLAIMANT_NAME, APPLICANT,
                CASE_REFERENCE, GA_REFERENCE,
                TYPE_OF_FEE, FeeType.ADDITIONAL.getLabel(),
                TYPE_OF_FEE_WELSH, FeeType.ADDITIONAL.getLabelInWelsh(),
                HWF_MORE_INFO_DOCUMENTS, getMoreInformationDocumentListString(),
                HWF_MORE_INFO_DOCUMENTS_WELSH, getMoreInformationDocumentListStringWelsh(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE
        ));
        properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
        properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
            + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
            + "\n For all other matters, call 0300 123 7050");
        return properties;
    }

    private String getMoreInformationDocumentListString() {
        List<HwFMoreInfoRequiredDocuments> list = getMoreInformationDocumentList();
        StringBuilder documentList = new StringBuilder();
        for (HwFMoreInfoRequiredDocuments doc : list) {
            documentList.append(doc.getName());
            if (!doc.getDescription().isEmpty()) {
                documentList.append(" - ");
                documentList.append(doc.getDescription());
            }
            documentList.append("\n");
            documentList.append("\n");
        }
        return documentList.toString();
    }

    private String getMoreInformationDocumentListStringWelsh() {
        List<HwFMoreInfoRequiredDocuments> list = getMoreInformationDocumentList();
        StringBuilder documentList = new StringBuilder();
        for (HwFMoreInfoRequiredDocuments doc : list) {
            documentList.append(doc.getNameBilingual());
            if (!doc.getDescriptionBilingual().isEmpty()) {
                documentList.append(" - ");
                documentList.append(doc.getDescriptionBilingual());
            }
            documentList.append("\n");
            documentList.append("\n");
        }
        return documentList.toString();
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_RefNumberUpdated_ClaimIssued() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
                .setHwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER_GA)
                ;
        GeneralApplicationCaseData  caseData = GA_CASE_DATA.copy().gaHwfDetails(hwfeeDetails)
            .isGaApplicantLip(YesOrNo.YES)
            .parentCaseReference(GA_REFERENCE).build();
        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());

        mockNotificationPropertiesEnglish();

        // When
        service.sendNotification(caseData);

        // Then
        verify(notificationService, times(1)).sendMail(
                EMAIL,
                "test-hwf-updaterefnumber-id",
                getNotificationCommonDataMapForGa(),
                REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_RefNumberUpdated_Ga_Bilingual_Def() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER_GA)
            ;
        GeneralApplicationCaseData  caseData = GA_CASE_DATA.copy()
            .parentCaseReference(GA_REFERENCE)
            .parentClaimantIsApplicant(YesOrNo.NO)
            .isGaApplicantLip(YesOrNo.YES)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .gaHwfDetails(hwfeeDetails).build();
        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(CIVIL_WELSH_DEF);

        mockNotificationPropertiesBilingual();

        // When
        service.sendNotification(caseData);
        // Then
        verify(notificationService, times(1)).sendMail(
            EMAIL,
            "test-hwf-updaterefnumber-bilingual-id",
            getNotificationCommonDataMapForGa(),
            REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_RefNumberUpdated_Additional() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
                .setHwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER_GA)
                ;
        GeneralApplicationCaseData  caseData = ADDITIONAL_CASE_DATA.copy()
            .isGaApplicantLip(YesOrNo.YES)
            .additionalHwfDetails(hwfeeDetails).parentCaseReference(GA_REFERENCE).build();
        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());

        mockNotificationPropertiesEnglish();

        // When
        service.sendNotification(caseData);

        // Then
        verify(notificationService, times(1)).sendMail(
                EMAIL,
                "test-hwf-updaterefnumber-id",
                getNotificationCommonDataMapForAdditional(),
                REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_RefNumberUpdated_Additional_Bilingual() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(UPDATE_HELP_WITH_FEE_NUMBER_GA)
            ;
        GeneralApplicationCaseData  caseData = ADDITIONAL_CASE_DATA.copy()
            .parentCaseReference(GA_REFERENCE).applicantBilingualLanguagePreference(YesOrNo.YES)
            .isGaApplicantLip(YesOrNo.YES)
            .additionalHwfDetails(hwfeeDetails).build();
        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(CIVIL_WELSH_CLA);

        mockNotificationPropertiesBilingual();

        // When
        service.sendNotification(caseData);
        // Then
        verify(notificationService, times(1)).sendMail(
            EMAIL,
            "test-hwf-updaterefnumber-bilingual-id",
            getNotificationCommonDataMapForAdditional(),
            REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_InvalidRefNumber_ClaimIssued() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
                .setHwfCaseEvent(INVALID_HWF_REFERENCE_GA)
                ;
        GeneralApplicationCaseData  caseData = GA_CASE_DATA.copy().gaHwfDetails(hwfeeDetails)
            .isGaApplicantLip(YesOrNo.YES)
            .parentCaseReference(GA_REFERENCE).build();

        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());

        mockNotificationPropertiesEnglish();

        // When
        service.sendNotification(caseData);

        // Then
        verify(notificationService, times(1)).sendMail(
                EMAIL,
                "test-hwf-invalidrefnumber-id",
                getNotificationCommonDataMapForGa(),
                REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_InvalidRefNumber_ClaimIssued_Bilingual() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(INVALID_HWF_REFERENCE_GA)
            ;
        GeneralApplicationCaseData  caseData = GA_CASE_DATA.copy()
            .isGaApplicantLip(YesOrNo.YES)
            .parentCaseReference(GA_REFERENCE).applicantBilingualLanguagePreference(YesOrNo.YES)
            .gaHwfDetails(hwfeeDetails).build();

        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(CIVIL_WELSH_CLA);

        mockNotificationPropertiesBilingual();

        // When
        service.sendNotification(caseData);
        // Then
        verify(notificationService, times(1)).sendMail(
            EMAIL,
            "test-hwf-invalidrefnumber-bilingual-id",
            getNotificationCommonDataMapForGa(),
            REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_InvalidRefNumber_Hearing() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
                .setHwfCaseEvent(INVALID_HWF_REFERENCE_GA)
                ;

        GeneralApplicationCaseData  caseData = ADDITIONAL_CASE_DATA.copy().additionalHwfDetails(hwfeeDetails)
            .isGaApplicantLip(YesOrNo.YES)
            .parentCaseReference(GA_REFERENCE).build();

        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());

        mockNotificationPropertiesEnglish();

        // When
        service.sendNotification(caseData);

        // Then
        verify(notificationService, times(1)).sendMail(
                EMAIL,
                "test-hwf-invalidrefnumber-id",
                getNotificationCommonDataMapForAdditional(),
                REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_InvalidRefNumber_Hearing_Bilingual_Def() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(INVALID_HWF_REFERENCE_GA)
            ;

        GeneralApplicationCaseData  caseData = ADDITIONAL_CASE_DATA.copy()
            .parentCaseReference(GA_REFERENCE)
            .isGaRespondentOneLip(YesOrNo.YES)
            .parentClaimantIsApplicant(YesOrNo.NO).applicantBilingualLanguagePreference(YesOrNo.YES)
            .additionalHwfDetails(hwfeeDetails).build();

        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(CIVIL_WELSH_DEF);

        mockNotificationPropertiesBilingual();

        // When
        service.sendNotification(caseData);
        // Then
        verify(notificationService, times(1)).sendMail(
            EMAIL,
            "test-hwf-invalidrefnumber-bilingual-id",
            getNotificationCommonDataMapForAdditional(),
            REFERENCE_NUMBER
        );
    }

    private Map<String, String> getNotificationCommonDataMapForGa() {
        HashMap<String, String> properties = new HashMap<>(Map.of(
                CLAIMANT_NAME, APPLICANT,
                CASE_REFERENCE, GA_REFERENCE,
                TYPE_OF_FEE, FeeType.APPLICATION.getLabel(),
                TYPE_OF_FEE_WELSH, FeeType.APPLICATION.getLabelInWelsh(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE
        ));
        properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
        properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
            + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
            + "\n For all other matters, call 0300 123 7050");
        return properties;
    }

    private Map<String, String> getNotificationCommonDataMapForAdditional() {
        HashMap<String, String> properties = new HashMap<>(Map.of(
                CLAIMANT_NAME, APPLICANT,
                CASE_REFERENCE, GA_REFERENCE,
                TYPE_OF_FEE, FeeType.ADDITIONAL.getLabel(),
                TYPE_OF_FEE_WELSH, FeeType.ADDITIONAL.getLabelInWelsh(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE
        ));
        properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
        properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
            + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
            + "\n For all other matters, call 0300 123 7050");
        return properties;
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_PartialRemission_Ga() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
                .setHwfCaseEvent(PARTIAL_REMISSION_HWF_GA)
                .setRemissionAmount(new BigDecimal(REMISSION_AMOUNT))
                .setOutstandingFee(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
                ;

        GeneralApplicationCaseData  caseData = GA_CASE_DATA.copy().gaHwfDetails(hwfeeDetails)
            .isGaApplicantLip(YesOrNo.YES)
            .parentCaseReference(GA_REFERENCE).build();

        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());

        mockNotificationPropertiesEnglish();

        // When
        service.sendNotification(caseData);

        // Then
        verify(notificationService, times(1)).sendMail(
                EMAIL,
                "test-hwf-partialRemission-id",
                getNotificationDataMapPartialRemissionGa(),
                REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_PartialRemission_Ga_Bilingual() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(PARTIAL_REMISSION_HWF_GA)
            .setRemissionAmount(new BigDecimal(REMISSION_AMOUNT))
            .setOutstandingFee(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
            ;

        GeneralApplicationCaseData  caseData = GA_CASE_DATA.copy()
            .isGaApplicantLip(YesOrNo.YES)
            .parentCaseReference(GA_REFERENCE).applicantBilingualLanguagePreference(YesOrNo.YES)
            .gaHwfDetails(hwfeeDetails).build();

        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(CIVIL_WELSH_CLA);

        mockNotificationPropertiesBilingual();

        // When
        service.sendNotification(caseData);
        // Then
        verify(notificationService, times(1)).sendMail(
            EMAIL,
            "test-hwf-partialRemission-bilingual-id",
            getNotificationDataMapPartialRemissionGa(),
            REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_PartialRemission_Additional() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
                .setHwfCaseEvent(PARTIAL_REMISSION_HWF_GA)
                .setRemissionAmount(new BigDecimal(REMISSION_AMOUNT))
                .setOutstandingFee(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
                ;

        GeneralApplicationCaseData  caseData = ADDITIONAL_CASE_DATA.copy().additionalHwfDetails(hwfeeDetails)
            .isGaApplicantLip(YesOrNo.YES)
            .parentCaseReference(GA_REFERENCE).build();

        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());

        mockNotificationPropertiesEnglish();

        // When
        service.sendNotification(caseData);

        // Then
        verify(notificationService, times(1)).sendMail(
                EMAIL,
                "test-hwf-partialRemission-id",
                getNotificationDataMapPartialRemissionAdditional(),
                REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_PartialRemission_Additional_Bilingual_Def() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(PARTIAL_REMISSION_HWF_GA)
            .setRemissionAmount(new BigDecimal(REMISSION_AMOUNT))
            .setOutstandingFee(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
            ;

        GeneralApplicationCaseData  caseData = ADDITIONAL_CASE_DATA.copy()
            .parentCaseReference(GA_REFERENCE)
            .parentClaimantIsApplicant(YesOrNo.NO)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .isGaApplicantLip(YesOrNo.YES)
            .additionalHwfDetails(hwfeeDetails).build();

        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(CIVIL_WELSH_DEF);

        mockNotificationPropertiesBilingual();

        // When
        service.sendNotification(caseData);
        // Then
        verify(notificationService, times(1)).sendMail(
            EMAIL,
            "test-hwf-partialRemission-bilingual-id",
            getNotificationDataMapPartialRemissionAdditional(),
            REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfNoRemission_ClaimIssued() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(NO_REMISSION_HWF_GA)
            .setNoRemissionDetailsSummary(NoRemissionDetailsSummary.INCORRECT_EVIDENCE)
            .setOutstandingFee(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
            ;
        GeneralApplicationCaseData  caseData = GA_CASE_DATA.copy().gaHwfDetails(hwfeeDetails)
            .isGaApplicantLip(YesOrNo.YES)
            .parentCaseReference(GA_REFERENCE).build();
        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());

        mockNotificationPropertiesEnglish();

        // When
        service.sendNotification(caseData);

        // Then
        verify(notificationService, times(1)).sendMail(
            EMAIL,
            "test-hwf-noRemission-id",
            getNotificationDataMapNoRemissionApplication(),
            REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfNoRemission_ClaimIssued_Bilingual() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(NO_REMISSION_HWF_GA)
            .setNoRemissionDetailsSummary(NoRemissionDetailsSummary.INCORRECT_EVIDENCE)
            .setOutstandingFee(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
            ;
        GeneralApplicationCaseData  caseData = GA_CASE_DATA.copy()
            .parentCaseReference(GA_REFERENCE).applicantBilingualLanguagePreference(YesOrNo.YES)
            .isGaApplicantLip(YesOrNo.YES)
            .gaHwfDetails(hwfeeDetails).build();
        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(CIVIL_WELSH_CLA);

        mockNotificationPropertiesBilingual();

        // When
        service.sendNotification(caseData);
        // Then
        verify(notificationService, times(1)).sendMail(
            EMAIL,
            "test-hwf-noremission-bilingual-id",
            getNotificationDataMapNoRemissionApplication(),
            REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfNoRemission_AdditionalFee() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(NO_REMISSION_HWF_GA)
            .setNoRemissionDetailsSummary(NoRemissionDetailsSummary.INCORRECT_EVIDENCE)
            .setOutstandingFee(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
            ;
        GeneralApplicationCaseData  caseData = ADDITIONAL_CASE_DATA.copy().additionalHwfDetails(hwfeeDetails)
            .isGaApplicantLip(YesOrNo.YES)
            .parentCaseReference(GA_REFERENCE).build();
        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());

        mockNotificationPropertiesEnglish();

        // When
        service.sendNotification(caseData);

        // Then
        verify(notificationService, times(1)).sendMail(
            EMAIL,
            "test-hwf-noRemission-id",
            getNotificationDataMapNoRemissionAdditional(),
            REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfNoRemission_AdditionalFee_Bilingual_Def() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(NO_REMISSION_HWF_GA)
            .setNoRemissionDetailsSummary(NoRemissionDetailsSummary.INCORRECT_EVIDENCE)
            .setOutstandingFee(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
            ;
        GeneralApplicationCaseData  caseData = ADDITIONAL_CASE_DATA.copy()
            .parentCaseReference(GA_REFERENCE)
            .parentClaimantIsApplicant(YesOrNo.NO)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .isGaApplicantLip(YesOrNo.YES)
            .additionalHwfDetails(hwfeeDetails).build();
        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(CIVIL_WELSH_DEF);

        mockNotificationPropertiesBilingual();

        // When
        service.sendNotification(caseData);
        // Then
        verify(notificationService, times(1)).sendMail(
            EMAIL,
            "test-hwf-noremission-bilingual-id",
            getNotificationDataMapNoRemissionAdditional(),
            REFERENCE_NUMBER
        );
    }

    private Map<String, String> getNotificationDataMapNoRemissionApplication() {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIMANT_NAME, APPLICANT,
            CASE_REFERENCE, GA_REFERENCE,
            TYPE_OF_FEE, FeeType.APPLICATION.getLabel(),
            TYPE_OF_FEE_WELSH, FeeType.APPLICATION.getLabelInWelsh(),
            HWF_REFERENCE_NUMBER, HWF_REFERENCE,
            FEE_AMOUNT, "5.00",
            NO_REMISSION_REASONS, NoRemissionDetailsSummary.INCORRECT_EVIDENCE.getLabel(),
            NO_REMISSION_REASONS_WELSH, NoRemissionDetailsSummary.INCORRECT_EVIDENCE.getLabelWelsh()
        ));
        properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
        properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
            + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
            + "\n For all other matters, call 0300 123 7050");
        return properties;
    }

    private Map<String, String> getNotificationDataMapNoRemissionAdditional() {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIMANT_NAME, APPLICANT,
            CASE_REFERENCE, GA_REFERENCE,
            TYPE_OF_FEE, FeeType.ADDITIONAL.getLabel(),
            TYPE_OF_FEE_WELSH, FeeType.ADDITIONAL.getLabelInWelsh(),
            HWF_REFERENCE_NUMBER, HWF_REFERENCE,
            FEE_AMOUNT, "5.00",
            NO_REMISSION_REASONS, NoRemissionDetailsSummary.INCORRECT_EVIDENCE.getLabel(),
            NO_REMISSION_REASONS_WELSH, NoRemissionDetailsSummary.INCORRECT_EVIDENCE.getLabelWelsh()
        ));
        properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
        properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
            + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
            + "\n For all other matters, call 0300 123 7050");
        return properties;
    }

    private Map<String, String> getNotificationDataMapPartialRemissionGa() {
        HashMap<String, String> properties = new HashMap<>(Map.of(
                CLAIMANT_NAME, APPLICANT,
                CASE_REFERENCE, GA_REFERENCE,
                TYPE_OF_FEE, FeeType.APPLICATION.getLabel(),
                TYPE_OF_FEE_WELSH, FeeType.APPLICATION.getLabelInWelsh(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE,
                PART_AMOUNT, "1000.00",
                REMAINING_AMOUNT, "5.00"
        ));
        properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
        properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
            + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
            + "\n For all other matters, call 0300 123 7050");
        return properties;
    }

    private Map<String, String> getNotificationDataMapPartialRemissionAdditional() {
        HashMap<String, String> properties = new HashMap<>(Map.of(
                CLAIMANT_NAME, APPLICANT,
                CASE_REFERENCE, GA_REFERENCE,
                TYPE_OF_FEE, FeeType.ADDITIONAL.getLabel(),
                TYPE_OF_FEE_WELSH, FeeType.ADDITIONAL.getLabelInWelsh(),
                HWF_REFERENCE_NUMBER, HWF_REFERENCE,
                PART_AMOUNT, "1000.00",
                REMAINING_AMOUNT, "5.00"
        ));
        properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
        properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
            + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
            + "\n For all other matters, call 0300 123 7050");
        return properties;
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_PaymentOut_Ga() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
                .setHwfCaseEvent(FEE_PAYMENT_OUTCOME_GA)
                .setRemissionAmount(new BigDecimal(REMISSION_AMOUNT))
                .setOutstandingFee(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
                ;

        GeneralApplicationCaseData  caseData = GA_CASE_DATA.copy().gaHwfDetails(hwfeeDetails)
            .isGaApplicantLip(YesOrNo.YES)
            .parentCaseReference(GA_REFERENCE).build();

        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());

        mockNotificationPropertiesEnglish();

        // When
        service.sendNotification(caseData);

        // Then
        verify(notificationService, times(1)).sendMail(
                EMAIL,
                "test-hwf-paymentoutcome-id",
                getNotificationDataMapPaymentOutcome(FeeType.APPLICATION),
                REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_PaymentOut_Ga_inWelsh() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
            .setHwfCaseEvent(FEE_PAYMENT_OUTCOME_GA)
            .setRemissionAmount(new BigDecimal(REMISSION_AMOUNT))
            .setOutstandingFee(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
            ;

        GeneralApplicationCaseData  caseData = GA_CASE_DATA.copy().gaHwfDetails(hwfeeDetails)
            .parentClaimantIsApplicant(YesOrNo.YES)
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .isGaApplicantLip(YesOrNo.YES)
            .parentCaseReference(GA_REFERENCE).build();

        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(CIVIL_WELSH_CLA);

        mockNotificationPropertiesBilingual();

        // When
        service.sendNotification(caseData);

        // Then
        verify(notificationService, times(1)).sendMail(
            EMAIL,
            EMAIL_TEMPLATE_HWF_PAYMENT_OUTCOME_WLESH,
            getNotificationDataMapPaymentOutcome(FeeType.APPLICATION),
            REFERENCE_NUMBER
        );
    }

    @Test
    void shouldNotifyApplicant_HwfOutcome_PaymentOut_Additional() {
        // Given
        HelpWithFeesDetails hwfeeDetails = new HelpWithFeesDetails()
                .setHwfCaseEvent(FEE_PAYMENT_OUTCOME_GA)
                .setRemissionAmount(new BigDecimal(REMISSION_AMOUNT))
                .setOutstandingFee(new BigDecimal(OUTSTANDING_AMOUNT_IN_POUNDS))
                ;

        GeneralApplicationCaseData  caseData = ADDITIONAL_CASE_DATA.copy().additionalHwfDetails(hwfeeDetails)
            .isGaApplicantLip(YesOrNo.YES)
            .parentCaseReference(GA_REFERENCE).build();

        when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData().build());

        mockNotificationPropertiesEnglish();

        // When
        service.sendNotification(caseData);

        // Then
        verify(notificationService, times(1)).sendMail(
                EMAIL,
                "test-hwf-paymentoutcome-id",
                getNotificationDataMapPaymentOutcome(FeeType.ADDITIONAL),
                REFERENCE_NUMBER
        );
    }

    private Map<String, String> getNotificationDataMapPaymentOutcome(FeeType feeType) {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            CLAIMANT_NAME, APPLICANT,
            CASE_REFERENCE, GA_REFERENCE,
            TYPE_OF_FEE, feeType.getLabel(),
            TYPE_OF_FEE_WELSH, feeType.getLabelInWelsh(),
            HWF_REFERENCE_NUMBER, HWF_REFERENCE,
            CASE_TITLE, "Mr. John Rambo v Mr. Joe Doe",
            APPLICANT_NAME, "Mr. John Rambo"
        ));
        properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
        properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
            + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
            + "\n For all other matters, call 0300 123 7050");
        return properties;
    }

    private void mockNotificationPropertiesEnglish() {
        when(notificationsProperties.getNotifyApplicantForHwFMoreInformationNeeded()).thenReturn(
            EMAIL_TEMPLATE_MORE_INFO_HWF);
        when(notificationsProperties.getNotifyApplicantForHwfUpdateRefNumber()).thenReturn(
            EMAIL_TEMPLATE_UPDATE_REF_NUMBER);
        when(notificationsProperties.getNotifyApplicantForHwfInvalidRefNumber()).thenReturn(
            EMAIL_TEMPLATE_INVALID_HWF_REFERENCE);
        when(notificationsProperties.getNotifyApplicantForHwfPartialRemission()).thenReturn(
            EMAIL_TEMPLATE_HWF_PARTIAL_REMISSION);
        when(notificationsProperties.getNotifyApplicantForNoRemission()).thenReturn(
            EMAIL_TEMPLATE_NO_REMISSION);
        when(notificationsProperties.getNotifyApplicantForHwfPaymentOutcome()).thenReturn(
            EMAIL_TEMPLATE_HWF_PAYMENT_OUTCOME);
    }

    private void mockNotificationPropertiesBilingual() {
        when(notificationsProperties.getLipGeneralAppApplicantEmailTemplateInWelsh()).thenReturn(
            EMAIL_TEMPLATE_HWF_PAYMENT_OUTCOME_WLESH);
        when(notificationsProperties.getNotifyApplicantForHwFMoreInformationNeededWelsh()).thenReturn(
            EMAIL_TEMPLATE_MORE_INFO_HWF_BILINGUAL);
        when(notificationsProperties.getNotifyApplicantForHwfUpdateRefNumberBilingual()).thenReturn(
            EMAIL_TEMPLATE_UPDATE_REF_NUMBER_BILINGUAL);
        when(notificationsProperties.getNotifyApplicantForHwfInvalidRefNumberBilingual()).thenReturn(
            EMAIL_TEMPLATE_INVALID_HWF_REFERENCE_BILINGUAL);
        when(notificationsProperties.getNotifyApplicantForHwfPartialRemissionBilingual()).thenReturn(
            EMAIL_TEMPLATE_HWF_PARTIAL_REMISSION_BILINGUAL);
        when(notificationsProperties.getNotifyApplicantForHwfNoRemissionWelsh()).thenReturn(
            EMAIL_TEMPLATE_NO_REMISSION_BILINGUAL);
    }
}
