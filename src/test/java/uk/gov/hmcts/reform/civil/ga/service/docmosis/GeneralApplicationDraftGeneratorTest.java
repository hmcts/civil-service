package uk.gov.hmcts.reform.civil.ga.service.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.documentmanagement.SecuredDocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingType;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GARespondentRepresentative;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.GADraftForm;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUnavailabilityDates;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.RELIEF_FROM_SANCTIONS;
import static uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder.CUSTOMER_REFERENCE;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.GENERAL_APPLICATION_DRAFT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SuppressWarnings("ALL")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    GeneralApplicationDraftGenerator.class
})
class GeneralApplicationDraftGeneratorTest extends GeneralApplicationBaseCallbackHandlerTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    private static final byte[] bytes = {1, 2, 3, 4, 5, 6};
    private static final String STRING_CONSTANT = "STRING_CONSTANT";
    private static final Long CHILD_CCD_REF = 1646003133062762L;
    private static final Long PARENT_CCD_REF = 1645779506193000L;
    private static final String DUMMY_EMAIL = "hmcts.civil@gmail.com";
    private static final String DUMMY_TELEPHONE_NUM = "234345435435";
    @MockBean
    private SecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @MockBean
    ListGeneratorService listGeneratorService;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private GaForLipService gaForLipService;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    GeneralApplicationDraftGenerator generalApplicationDraftGenerator;
    List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();
    DynamicListElement location1 = DynamicListElement.builder()
        .code(String.valueOf(UUID.randomUUID())).label("Site Name 2 - Address2 - 28000").build();

    @Test
    void shouldNotGenerateApplicationDraftDocument() {
        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(NO, YES);

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(GENERAL_APPLICATION_DRAFT)))
            .thenReturn(new DocmosisDocument(GENERAL_APPLICATION_DRAFT.getDocumentTitle(), bytes));

        when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");
        when(listGeneratorService.claimantsName(caseData)).thenReturn("Test Claimant1 Name");
        when(listGeneratorService.defendantsName(caseData)).thenReturn("Test Defendant1 Name");
        Map<String, String> refMap = new HashMap<>();
        refMap.put("applicantSolicitor1Reference", "app1ref");
        refMap.put("respondentSolicitor1Reference", "resp1ref");
        Map<String, Object> caseDataContent = new HashMap<>();
        caseDataContent.put("solicitorReferences", refMap);
        CaseDetails parentCaseDetails = CaseDetails.builder().data(caseDataContent).build();
        when(coreCaseDataService.getCase(PARENT_CCD_REF)).thenReturn(parentCaseDetails);
        generalApplicationDraftGenerator.generate(caseData, BEARER_TOKEN);

        verify(documentManagementService).uploadDocument(
            BEARER_TOKEN,
            new PDF(any(), any(), DocumentType.GENERAL_APPLICATION_DRAFT)
        );
        verify(documentGeneratorService).generateDocmosisDocument(any(GADraftForm.class),
                                                                  eq(GENERAL_APPLICATION_DRAFT));
        var templateData = generalApplicationDraftGenerator.getTemplateData(caseData);
        assertThat(templateData.getIsCasePastDueDate()).isTrue();
    }

    @Test
    void shouldGenerateApplicationDraftDocumentWithNoticeButRespondentNotRespondedOnTime() {
        GeneralApplicationCaseData caseData = getSampleGeneralAppCaseDataWithDeadLineReached(NO, YES);
        caseData.setGeneralAppSubmittedDateGAspec(null);
        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(GENERAL_APPLICATION_DRAFT)))
            .thenReturn(new DocmosisDocument(GENERAL_APPLICATION_DRAFT.getDocumentTitle(), bytes));

        when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");
        when(listGeneratorService.claimantsName(caseData)).thenReturn("Test Claimant1 Name");
        when(listGeneratorService.defendantsName(caseData)).thenReturn("Test Defendant1 Name");
        Map<String, String> refMap = new HashMap<>();
        refMap.put("applicantSolicitor1Reference", "app1ref");
        refMap.put("respondentSolicitor1Reference", "resp1ref");
        Map<String, Object> caseDataContent = new HashMap<>();
        caseDataContent.put("solicitorReferences", refMap);
        caseDataContent.put("generalAppSubmittedDateGAspec", LocalDateTime.now());
        CaseDetails parentCaseDetails = CaseDetails.builder().data(caseDataContent).build();
        when(coreCaseDataService.getCase(PARENT_CCD_REF)).thenReturn(parentCaseDetails);

        generalApplicationDraftGenerator.generate(caseData, BEARER_TOKEN);

        verify(documentManagementService).uploadDocument(
            BEARER_TOKEN,
            new PDF(any(), any(), DocumentType.GENERAL_APPLICATION_DRAFT)
        );
        verify(documentGeneratorService).generateDocmosisDocument(any(GADraftForm.class), eq(GENERAL_APPLICATION_DRAFT));
        var templateData = generalApplicationDraftGenerator.getTemplateData(caseData);
        assertThat(templateData.getIsCasePastDueDate()).isTrue();
        assertNotNull(templateData.getSubmittedDate());
        assertNotNull(templateData.getIssueDate());
        assertEquals(String.valueOf(CHILD_CCD_REF), templateData.getApplicationId());
    }

    @Test
    void shouldGenerateDocumentWithApplicantAndRespondent1Response_1v1_test() {
        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();
        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).organisationIdentifier("org2").build();

        respondentSols.add(element(respondent1));
        GeneralApplicationCaseData caseData = getCase(respondentSols, NO);

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(GENERAL_APPLICATION_DRAFT)))
            .thenReturn(new DocmosisDocument(GENERAL_APPLICATION_DRAFT.getDocumentTitle(), bytes));

        when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");
        when(listGeneratorService.claimantsName(caseData)).thenReturn("Test Claimant1 Name");
        when(listGeneratorService.defendantsName(caseData)).thenReturn("Test Defendant1 Name");
        Map<String, String> refMap = new HashMap<>();
        refMap.put("applicantSolicitor1Reference", "app1ref");
        refMap.put("respondentSolicitor1Reference", "resp1ref");
        Map<String, Object> caseDataContent = new HashMap<>();
        caseDataContent.put("solicitorReferences", refMap);
        CaseDetails parentCaseDetails = CaseDetails.builder().data(caseDataContent).build();
        when(coreCaseDataService.getCase(PARENT_CCD_REF)).thenReturn(parentCaseDetails);
        var templateData = generalApplicationDraftGenerator.getTemplateData(caseData);
        assertThatRespondentFieldsAreCorrect_DraftApp(templateData, caseData);
    }

    @Test
    void shouldGenerateDocumentWithApplicantAndRespondent1ResponseJudgeUncloaks() {
        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();
        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).organisationIdentifier("org2").build();

        respondentSols.add(element(respondent1));
        GeneralApplicationCaseData caseData = getCase(respondentSols, NO).copy()
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
            .applicationIsCloaked(NO)
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(GENERAL_APPLICATION_DRAFT)))
            .thenReturn(new DocmosisDocument(GENERAL_APPLICATION_DRAFT.getDocumentTitle(), bytes));

        when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");
        when(listGeneratorService.claimantsName(caseData)).thenReturn("Test Claimant1 Name");
        when(listGeneratorService.defendantsName(caseData)).thenReturn("Test Defendant1 Name");
        Map<String, String> refMap = new HashMap<>();
        refMap.put("applicantSolicitor1Reference", "app1ref");
        refMap.put("respondentSolicitor1Reference", "resp1ref");
        Map<String, Object> caseDataContent = new HashMap<>();
        caseDataContent.put("solicitorReferences", refMap);
        CaseDetails parentCaseDetails = CaseDetails.builder().data(caseDataContent).build();
        when(coreCaseDataService.getCase(PARENT_CCD_REF)).thenReturn(parentCaseDetails);
        var templateData = generalApplicationDraftGenerator.getTemplateData(caseData);
        assertThat(templateData.getIsWithNotice()).isEqualTo(YES);
    }

    @Test
    void shouldNotGenerateDocumentWithApplicantAndRespondent1ResponseWhenApplnCloaked() {
        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();
        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).organisationIdentifier("org2").build();

        respondentSols.add(element(respondent1));
        GeneralApplicationCaseData caseData = getCase(respondentSols, NO).copy()
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
            .applicationIsCloaked(YES)
            .build();

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(GENERAL_APPLICATION_DRAFT)))
            .thenReturn(new DocmosisDocument(GENERAL_APPLICATION_DRAFT.getDocumentTitle(), bytes));

        when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");
        when(listGeneratorService.claimantsName(caseData)).thenReturn("Test Claimant1 Name");
        when(listGeneratorService.defendantsName(caseData)).thenReturn("Test Defendant1 Name");
        Map<String, String> refMap = new HashMap<>();
        refMap.put("applicantSolicitor1Reference", "app1ref");
        refMap.put("respondentSolicitor1Reference", "resp1ref");
        Map<String, Object> caseDataContent = new HashMap<>();
        caseDataContent.put("solicitorReferences", refMap);
        CaseDetails parentCaseDetails = CaseDetails.builder().data(caseDataContent).build();
        when(coreCaseDataService.getCase(PARENT_CCD_REF)).thenReturn(parentCaseDetails);
        var templateData = generalApplicationDraftGenerator.getTemplateData(caseData);
        assertThat(templateData.getIsWithNotice()).isEqualTo(NO);
    }

    @Test
    void shouldGenerateDocumentWithApplicantAndRespondentsResponse_1v2_test() {
        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();
        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).organisationIdentifier("org2").build();

        GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).organisationIdentifier("org2").build();

        respondentSols.add(element(respondent1));
        respondentSols.add(element(respondent2));

        GeneralApplicationCaseData caseData = getCase(respondentSols, YES);

        when(documentGeneratorService.generateDocmosisDocument(any(MappableObject.class), eq(GENERAL_APPLICATION_DRAFT)))
            .thenReturn(new DocmosisDocument(GENERAL_APPLICATION_DRAFT.getDocumentTitle(), bytes));

        when(listGeneratorService.applicationType(caseData)).thenReturn("Extend time");
        when(listGeneratorService.claimantsName(caseData)).thenReturn("Test Claimant1 Name");
        when(listGeneratorService.defendantsName(caseData)).thenReturn("Test Defendant1 Name");
        Map<String, String> refMap = new HashMap<>();
        refMap.put("applicantSolicitor1Reference", "app1ref");
        refMap.put("respondentSolicitor1Reference", "resp1ref");
        Map<String, Object> caseDataContent = new HashMap<>();
        caseDataContent.put("solicitorReferences", refMap);
        CaseDetails parentCaseDetails = CaseDetails.builder().data(caseDataContent).build();
        when(coreCaseDataService.getCase(PARENT_CCD_REF)).thenReturn(parentCaseDetails);
        var templateData = generalApplicationDraftGenerator.getTemplateData(caseData);
        assertThatRespondentFieldsAreCorrect_DraftApp(templateData, caseData);
    }

    public List<Element<GARespondentResponse>> getRespondentResponses1nad2(YesOrNo vulQuestion1, YesOrNo vulQuestion2,
                                                                           YesOrNo hasResp1PreferLocation,
                                                                           YesOrNo hasResp2PreferLocation, YesOrNo addRespondent) {

        List<GAHearingSupportRequirements> respSupportReq1 = new ArrayList<>();
        respSupportReq1
            .add(GAHearingSupportRequirements.OTHER_SUPPORT);

        List<GAHearingSupportRequirements> respSupportReq2 = new ArrayList<>();
        respSupportReq2
            .add(GAHearingSupportRequirements.LANGUAGE_INTERPRETER);
        List<Element<GAUnavailabilityDates>> resp1UnavailabilityDates = new ArrayList<>();
        resp1UnavailabilityDates.add(element(GAUnavailabilityDates.builder()
                                         .unavailableTrialDateTo(LocalDate.now().plusDays(5))
                                         .unavailableTrialDateFrom(LocalDate.now()).build()));
        List<Element<GAUnavailabilityDates>> resp2UnavailabilityDates = new ArrayList<>();
        resp2UnavailabilityDates.add(element(GAUnavailabilityDates.builder()
                                         .unavailableTrialDateTo(LocalDate.now().plusDays(3))
                                         .unavailableTrialDateFrom(LocalDate.now()).build()));
        List<Element<GARespondentResponse>> respondentsResponses = new ArrayList<>();
        respondentsResponses
            .add(element(new GARespondentResponse()
                             .setGaHearingDetails(GAHearingDetails.builder()
                                                   .vulnerabilityQuestionsYesOrNo(vulQuestion1)
                                                   .vulnerabilityQuestion("dummy1")
                                                   .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                                   .hearingDuration(GAHearingDuration.HOUR_1)
                                                   .supportRequirement(respSupportReq1)
                                                   .unavailableTrialRequiredYesOrNo(YES)
                                                   .generalAppUnavailableDates(resp1UnavailabilityDates)
                                                   .hearingPreferredLocation(hasResp1PreferLocation == YES
                                                                                 ? DynamicList.builder()
                                                       .listItems(List.of(location1))
                                                       .value(location1).build() : null)
                                                   .build())
                             .setGaRespondentDetails("1L")));
        if (addRespondent == YES) {
            respondentsResponses
                .add(element(new GARespondentResponse()
                                 .setGaHearingDetails(GAHearingDetails.builder()
                                                       .vulnerabilityQuestionsYesOrNo(vulQuestion2)
                                                       .vulnerabilityQuestion("dummy2")
                                                       .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                                       .hearingDuration(GAHearingDuration.MINUTES_30)
                                                       .supportRequirement(respSupportReq2)
                                                       .generalAppUnavailableDates(resp2UnavailabilityDates)
                                                       .hearingPreferredLocation(hasResp2PreferLocation == YES
                                                                                     ? DynamicList.builder()
                                                           .listItems(List.of(location1))
                                                           .value(location1).build() : null)
                                                       .build())
                                 .setGaRespondentDetails("2L")));
        }
        return respondentsResponses;
    }

    private GeneralApplicationCaseData getCase(List<Element<GASolicitorDetailsGAspec>> respondentSols,
                                                 YesOrNo addRespondent) {
        List<GeneralApplicationTypes> types = List.of(
            (GeneralApplicationTypes.SUMMARY_JUDGEMENT));
        DynamicListElement location2 = DynamicListElement.builder()
            .code(String.valueOf(UUID.randomUUID())).label("Site Name 2 - Address2 - 28000").build();
        return new GeneralApplicationCaseData()
            .claimant1PartyName("Test Claimant1 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .ccdCaseReference(CHILD_CCD_REF)
            .generalAppHearingDate(
                GAHearingDateGAspec.builder()
                    .hearingScheduledPreferenceYesNo(YES).hearingScheduledDate(LocalDate.now()).build())
            .generalAppRespondentSolicitors(respondentSols)
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
            .generalAppStatementOfTruth(GAStatementOfTruth.builder().build())
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .hearingPreferredLocation(DynamicList.builder()
                                                                        .listItems(List.of(location2))
                                                                        .value(location1).build())
                                          .vulnerabilityQuestionsYesOrNo(YES)
                                          .vulnerabilityQuestion("dummy2")
                                          .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .hearingDuration(GAHearingDuration.MINUTES_30)
                                          .hearingDetailsEmailID(DUMMY_EMAIL)
                                          .hearingDetailsTelephoneNumber(DUMMY_TELEPHONE_NUM).build())
            .hearingDetailsResp(GAHearingDetails.builder()
                                    .hearingPreferredLocation(
                                        DynamicList.builder()
                                            .listItems(List.of(location1))
                                            .value(location1).build())
                                    .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                    .hearingDuration(GAHearingDuration.MINUTES_30)
                                    .hearingDetailsEmailID(DUMMY_EMAIL)
                                    .hearingDetailsTelephoneNumber(DUMMY_TELEPHONE_NUM)
                                    .build())
            .respondentsResponses(getRespondentResponses1nad2(YES, YES, YES, YES, addRespondent))
            .generalAppRespondent1Representative(
                new GARespondentRepresentative()
                    .setGeneralAppRespondent1Representative(YES)
                    )
            .generalAppType(
                GAApplicationType
                    .builder()
                    .types(types).build())
            .parentClaimantIsApplicant(YES)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                          .setCaseReference(PARENT_CCD_REF.toString()))
            .generalAppSubmittedDateGAspec(LocalDateTime.now())
            .build();
    }

    private GeneralApplicationCaseData getSampleGeneralApplicationCaseData(YesOrNo isConsented, YesOrNo isTobeNotified) {
        return GeneralApplicationCaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                getGeneralApplication(isConsented, isTobeNotified))
            .copy()
            .claimant1PartyName("Test Claimant1 Name")
            .generalAppHearingDate(
                GAHearingDateGAspec.builder()
                    .hearingScheduledPreferenceYesNo(YES).hearingScheduledDate(LocalDate.now()).build())
            .defendant1PartyName("Test Defendant1 Name")
            .ccdCaseReference(CHILD_CCD_REF).build();
    }

    private GeneralApplicationCaseData getSampleGeneralAppCaseDataWithDeadLineReached(YesOrNo isConsented, YesOrNo isTobeNotified) {
        return GeneralApplicationCaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                getGeneralApplicationWithDeadlineReached(isConsented, isTobeNotified))
            .copy()
            .claimant1PartyName("Test Claimant1 Name")
            .generalAppHearingDate(
                GAHearingDateGAspec.builder()
                    .hearingScheduledPreferenceYesNo(YES).hearingScheduledDate(LocalDate.now()).build())
            .defendant1PartyName("Test Defendant1 Name")
            .ccdCaseReference(CHILD_CCD_REF).build();
    }

    private GeneralApplication getGeneralApplication(YesOrNo isConsented, YesOrNo isTobeNotified) {
        DynamicListElement location2 = DynamicListElement.builder()
            .code(String.valueOf(UUID.randomUUID())).label("Site Name 2 - Address2 - 28000").build();
        List<Element<GAUnavailabilityDates>> appUnavailabilityDates = new ArrayList<>();
        appUnavailabilityDates.add(element(GAUnavailabilityDates.builder()
                                                 .unavailableTrialDateTo(LocalDate.now().plusDays(2))
                                                 .unavailableTrialDateFrom(LocalDate.now()).build()));
        return GeneralApplication.builder()
            .generalAppType(GAApplicationType.builder().types(List.of(RELIEF_FROM_SANCTIONS)).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(isConsented).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isTobeNotified).build())
            .generalAppPBADetails(
                GAPbaDetails.builder()
                    .fee(
                        new Fee()
                            .setCode("FE203")
                            .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                            .setVersion("1")
                            )
                    .serviceReqReference(CUSTOMER_REFERENCE)
                    .paymentSuccessfulDate(LocalDateTime.now())
                    .build())
            .generalAppDetailsOfOrder(STRING_CONSTANT)
            .generalAppReasonsOfOrder(STRING_CONSTANT)
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
            .generalAppStatementOfTruth(GAStatementOfTruth.builder().build())
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .hearingPreferredLocation(DynamicList.builder()
                                                                        .listItems(List.of(location2))
                                                                        .value(location2).build())
                                          .vulnerabilityQuestionsYesOrNo(YES)
                                          .vulnerabilityQuestion("dummy2")
                                          .generalAppUnavailableDates(appUnavailabilityDates)
                                          .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .hearingDuration(GAHearingDuration.MINUTES_30)
                                          .hearingDetailsEmailID(DUMMY_EMAIL)
                                          .hearingDetailsTelephoneNumber(DUMMY_TELEPHONE_NUM).build())
            .generalAppRespondentSolicitors(wrapElements(GASolicitorDetailsGAspec.builder()
                                                             .email("abc@gmail.com").build()))
            .isMultiParty(NO)
            .parentClaimantIsApplicant(YES)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                          .setCaseReference(PARENT_CCD_REF.toString()))
            .generalAppSubmittedDateGAspec(LocalDateTime.now())
            .build();
    }

    private GeneralApplication getGeneralApplicationWithDeadlineReached(YesOrNo isConsented, YesOrNo isTobeNotified) {
        List<Element<GAUnavailabilityDates>> appUnavailabilityDates = new ArrayList<>();
        appUnavailabilityDates.add(element(GAUnavailabilityDates.builder()
                                               .unavailableTrialDateTo(LocalDate.now().plusDays(2))
                                               .unavailableTrialDateFrom(LocalDate.now()).build()));
        return GeneralApplication.builder()
            .generalAppType(GAApplicationType.builder().types(List.of(RELIEF_FROM_SANCTIONS)).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(isConsented).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isTobeNotified).build())
            .generalAppHearingDate(
                GAHearingDateGAspec.builder()
                    .hearingScheduledPreferenceYesNo(YES).hearingScheduledDate(LocalDate.now()).build())
            .generalAppPBADetails(
                GAPbaDetails.builder()
                    .fee(
                        new Fee()
                            .setCode("FE203")
                            .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                            .setVersion("1")
                            )
                    .serviceReqReference(CUSTOMER_REFERENCE).build())
            .generalAppDetailsOfOrder(STRING_CONSTANT)
            .generalAppReasonsOfOrder(STRING_CONSTANT)
            .generalAppDateDeadline(LocalDateTime.now().minusDays(2))
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
            .generalAppStatementOfTruth(GAStatementOfTruth.builder().build())
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .hearingPreferredLocation(DynamicList.builder()
                                                                        .listItems(List.of(location1))
                                                                        .value(location1).build())
                                          .vulnerabilityQuestionsYesOrNo(YES)
                                          .vulnerabilityQuestion("dummy2")
                                          .generalAppUnavailableDates(appUnavailabilityDates)
                                          .hearingPreferencesPreferredType(GAHearingType.IN_PERSON)
                                          .hearingDuration(GAHearingDuration.MINUTES_30)
                                          .hearingDetailsEmailID(DUMMY_EMAIL)
                                          .hearingDetailsTelephoneNumber(DUMMY_TELEPHONE_NUM).build())
            .generalAppRespondentSolicitors(wrapElements(GASolicitorDetailsGAspec.builder()
                                                             .email("abc@gmail.com").build()))
            .isMultiParty(NO)
            .parentClaimantIsApplicant(YES)
            .generalAppSubmittedDateGAspec(LocalDateTime.now())
            .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                          .setCaseReference(PARENT_CCD_REF.toString()))
            .build();
    }

    @Test
    void whenUrgentApplicationShouldGenerateDocumentWithApplicantDetails() {
        Map<String, String> refMap = new HashMap<>();
        refMap.put("applicantSolicitor1Reference", "app1ref");
        refMap.put("respondentSolicitor1Reference", "resp1ref");
        Map<String, Object> caseDataContent = new HashMap<>();
        caseDataContent.put("solicitorReferences", refMap);
        CaseDetails parentCaseDetails = CaseDetails.builder().data(caseDataContent).build();
        when(coreCaseDataService.getCase(PARENT_CCD_REF)).thenReturn(parentCaseDetails);

        GeneralApplicationCaseData caseData = getSampleGeneralApplicationCaseData(NO, YES);
        when(listGeneratorService.claimantsName(caseData)).thenReturn("Test Claimant1 Name");
        when(listGeneratorService.defendantsName(caseData))
            .thenReturn("Test Defendant1 Name");

        var templateData = generalApplicationDraftGenerator.getTemplateData(caseData);

        assertThatApplicantFieldsAreCorrect_DraftApp(templateData, caseData);
    }

    private void assertThatApplicantFieldsAreCorrect_DraftApp(GADraftForm templateData, GeneralApplicationCaseData caseData) {
        Assertions.assertAll(
            "DraftApplication data should be as expected",
            () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
            () -> assertEquals(templateData.getClaimantName(), getClaimants(caseData)),
            () -> assertEquals(templateData.getDefendantName(), getDefendants(caseData))
        );
    }

    private void assertThatRespondentFieldsAreCorrect_DraftApp(GADraftForm templateData, GeneralApplicationCaseData caseData) {
        Assertions.assertAll(
            "DraftApplication data should be as expected",
            () -> assertEquals(templateData.getClaimNumber(), caseData.getGeneralAppParentCaseLink().getCaseReference()),
            () -> assertEquals(templateData.getClaimantName(), getClaimants(caseData)),
            () -> assertEquals(templateData.getDefendantName(), getDefendants(caseData))
        );
    }

    @Test
    void test_getReference() {
        Map<String, String> refMap = new HashMap<>();
        refMap.put("applicantSolicitor1Reference", "app1ref");
        refMap.put("respondentSolicitor1Reference", "resp1ref");
        Map<String, Object> caseDataContent = new HashMap<>();
        caseDataContent.put("solicitorReferences", refMap);
        CaseDetails caseDetails = CaseDetails.builder().data(caseDataContent).build();

        assertThat(generalApplicationDraftGenerator.getReference(caseDetails, "applicantSolicitor1Reference")).isEqualTo("app1ref");
        assertThat(generalApplicationDraftGenerator.getReference(caseDetails, "notExist")).isNull();
    }

    private String getClaimants(GeneralApplicationCaseData caseData) {
        List<String> claimantsName = new ArrayList<>();
        claimantsName.add(caseData.getClaimant1PartyName());
        if (caseData.getDefendant2PartyName() != null) {
            claimantsName.add(caseData.getClaimant2PartyName());
        }
        return String.join(", ", claimantsName);
    }

    private String getDefendants(GeneralApplicationCaseData caseData) {
        List<String> defendantsName = new ArrayList<>();
        defendantsName.add(caseData.getDefendant1PartyName());
        if (caseData.getDefendant2PartyName() != null) {
            defendantsName.add(caseData.getDefendant2PartyName());
        }
        return String.join(", ", defendantsName);
    }

}
