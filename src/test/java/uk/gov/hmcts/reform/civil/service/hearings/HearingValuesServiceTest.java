package uk.gov.hmcts.reform.civil.service.hearings;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.SneakyThrows;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.exception.RestException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.config.ManageCaseBaseUrlConfiguration;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.enums.hearing.CategoryType;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.civil.exceptions.NotEarlyAdopterCourtException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.hearingvalues.CaseCategoryModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.HearingLocationModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.IndividualDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.JudiciaryModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.OrganisationDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PartyDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.VocabularyModel;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.EarlyAdoptersService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.util.Lists.emptyList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.SINGLE_DATE;
import static uk.gov.hmcts.reform.civil.enums.hearing.HMCLocationType.COURT;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyType.IND;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyType.ORG;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsMapper.getCaseFlags;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.ScreenFlowMapper.getScreenFlow;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    JacksonAutoConfiguration.class
})
public class HearingValuesServiceTest {

    @Mock
    private CoreCaseDataService caseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private ManageCaseBaseUrlConfiguration manageCaseBaseUrlConfiguration;
    @Mock
    private PaymentsConfiguration paymentsConfiguration;
    @Mock
    private CaseCategoriesService caseCategoriesService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private CaseFlagsInitialiser caseFlagsInitialiser;
    @Mock
    private EarlyAdoptersService earlyAdoptersService;
    @Mock
    private FeatureToggleService featuretoggleService;

    @Autowired
    private ObjectMapper mapper;

    @InjectMocks
    private HearingValuesService hearingValuesService;

    private static final String APPLICANT_ORG_ID = "QWERTY A";
    private static final String RESPONDENT_ONE_ORG_ID = "QWERTY R";
    private static final String APPLICANT_LR_ORG_NAME = "Applicant LR Org name";
    private static final String RESPONDENT_ONE_LR_ORG_NAME = "Respondent 1 LR Org name";
    private static final String BASE_LOCATION_ID = "1234";
    private static final String WELSH_REGION_ID = "7";

    @BeforeEach
    void prepare() {
        ReflectionTestUtils.setField(hearingValuesService, "mapper", mapper);
        when(earlyAdoptersService.isPartOfHmcLipEarlyAdoptersRollout(any(CaseData.class))).thenReturn(true);
    }

    @Test
    void shouldReturnExpectedHearingValuesWhenCaseDataIsReturned() throws Exception {
        WelshLanguageRequirements applicant1WelshLang = new WelshLanguageRequirements();
        applicant1WelshLang.setCourt(Language.ENGLISH);
        Applicant1DQ applicant1DQ = new Applicant1DQ();
        applicant1DQ.setApplicant1DQLanguage(applicant1WelshLang);
        WelshLanguageRequirements respondent1WelshLang = new WelshLanguageRequirements();
        respondent1WelshLang.setCourt(Language.WELSH);
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQLanguage(respondent1WelshLang);

        Long caseId = 1L;
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .caseReference(caseId)
            .caseAccessCategory(UNSPEC_CLAIM)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION_ID)
                                        .setRegion(WELSH_REGION_ID))
            .applicant1DQ(applicant1DQ)
            .respondent1DQ(respondent1DQ)
            .build();
        Flags flags = new Flags();
        flags.setPartyName("party name");
        Party applicant1 = caseData.getApplicant1();
        applicant1.setFlags(flags);
        caseData.setApplicant1(applicant1);
        CaseDetails caseDetails = CaseDetails.builder()
            .data(caseData.toMap(mapper))
            .id(caseId).build();

        when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);
        when(organisationService.findOrganisationById(APPLICANT_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(APPLICANT_LR_ORG_NAME)
                                        .build()));
        when(organisationService.findOrganisationById(RESPONDENT_ONE_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(RESPONDENT_ONE_LR_ORG_NAME)
                                        .build()));
        given(manageCaseBaseUrlConfiguration.getManageCaseBaseUrl()).willReturn("http://localhost:3333");
        given(paymentsConfiguration.getSiteId()).willReturn("AAA7");

        Category inPerson = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag("Y").build();
        Category video = Category.builder().categoryKey("HearingChannel").key("VID").valueEn("Video").activeFlag("Y").build();
        Category telephone = Category.builder().categoryKey("HearingChannel").key("TEL").valueEn("Telephone").activeFlag("Y").build();
        CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(inPerson, video, telephone)).build();

        when(categoryService.findCategoryByCategoryIdAndServiceId(anyString(), eq("HearingChannel"), anyString())).thenReturn(
            Optional.of(categorySearchResult));

        HearingLocationModel expectedLocation = new HearingLocationModel();
        expectedLocation.setLocationId(BASE_LOCATION_ID);
        expectedLocation.setLocationType(COURT);
        List<HearingLocationModel> expectedHearingLocation = List.of(expectedLocation);

        JudiciaryModel expectedJudiciary = new JudiciaryModel();

        List<CaseCategoryModel> expectedCaseCategories = getExpectedCaseCategories();
        VocabularyModel vocabularyModel = new VocabularyModel();
        ServiceHearingValuesModel expected = new ServiceHearingValuesModel();
        expected.setHmctsServiceID("AAA7");
        expected.setHmctsInternalCaseName("Mr. John Rambo v Mr. Sole Trader");
        expected.setPublicCaseName("John Rambo v Sole Trader");
        expected.setCaseAdditionalSecurityFlag(false);
        expected.setCaseCategories(expectedCaseCategories);
        expected.setCaseDeepLink("http://localhost:3333/cases/case-details/1");
        expected.setCaseRestrictedFlag(false);
        expected.setExternalCaseReference(null);
        expected.setCaseManagementLocationCode(BASE_LOCATION_ID);
        expected.setCaseSLAStartDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        expected.setAutoListFlag(false);
        expected.setHearingType(null);
        expected.setHearingWindow(null);
        expected.setDuration(0);
        expected.setHearingPriorityType("Standard");
        expected.setNumberOfPhysicalAttendees(0);
        expected.setHearingInWelshFlag(true);
        expected.setHearingLocations(expectedHearingLocation);
        expected.setFacilitiesRequired(null);
        expected.setListingComments(null);
        expected.setHearingRequester("");
        expected.setPrivateHearingRequiredFlag(false);
        expected.setCaseInterpreterRequiredFlag(false);
        expected.setPanelRequirements(null);
        expected.setLeadJudgeContractType("");
        expected.setJudiciary(expectedJudiciary);
        expected.setHearingIsLinkedFlag(false);
        expected.setParties(getExpectedPartyModel());
        expected.setScreenFlow(getScreenFlow());
        expected.setVocabulary(List.of(vocabularyModel));
        expected.setHearingChannels(null);
        expected.setCaseFlags(getCaseFlags(caseData));

        ServiceHearingValuesModel actual = hearingValuesService.getValues(caseId, "auth");
        ServiceHearingValuesModel actualFromCaseData = hearingValuesService.getValues(caseData, "auth");

        verify(caseDetailsConverter).toCaseData(eq(caseDetails.getData()));
        verify(caseDataService, times(0)).triggerEvent(any(), any(), any());
        assertThat(actual).isEqualTo(expected);
        assertThat(actualFromCaseData).isEqualTo(expected);
    }

    @SneakyThrows
    @Test
    void shouldTriggerEventIfPartyIdMissingFromApplicant1() throws Exception {
        Long caseId = 1L;
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .caseReference(caseId)
            .applicant1(PartyBuilder.builder().individual().build())
            .respondent1(PartyBuilder.builder().company().build())
            .respondent2(PartyBuilder.builder().company().build())
            .multiPartyClaimTwoDefendantSolicitors()
            .caseAccessCategory(UNSPEC_CLAIM)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION_ID)
                                        .setRegion(WELSH_REGION_ID))
            .build();
        Flags flags = new Flags();
        flags.setPartyName("party name");
        caseData.getApplicant1().setFlags(flags);
        CaseDetails caseDetails = CaseDetails.builder()
            .data(caseData.toMap(mapper))
            .id(caseId).build();

        when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);
        when(organisationService.findOrganisationById(APPLICANT_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(APPLICANT_LR_ORG_NAME)
                                        .build()));
        when(organisationService.findOrganisationById(RESPONDENT_ONE_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(RESPONDENT_ONE_LR_ORG_NAME)
                                        .build()));
        given(manageCaseBaseUrlConfiguration.getManageCaseBaseUrl()).willReturn("http://localhost:3333");
        given(paymentsConfiguration.getSiteId()).willReturn("AAA7");

        hearingValuesService.getValues(caseId, "auth");

        verify(caseDataService).triggerEvent(eq(caseId), eq(CaseEvent.UPDATE_MISSING_FIELDS), any());
    }

    @SneakyThrows
    @Test
    void shouldTriggerEventIfCaseFlagsMissingFromApplicant1() throws Exception {
        Long caseId = 1L;
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .caseReference(caseId)
            .applicant1(PartyBuilder.builder().individual().build())
            .respondent1(PartyBuilder.builder().company().build())
            .respondent2(PartyBuilder.builder().company().build())
            .caseAccessCategory(UNSPEC_CLAIM)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION_ID)
                                        .setRegion(WELSH_REGION_ID))
            .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .data(caseData.toMap(mapper))
            .id(caseId).build();

        when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);
        when(organisationService.findOrganisationById(APPLICANT_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(APPLICANT_LR_ORG_NAME)
                                        .build()));
        when(organisationService.findOrganisationById(RESPONDENT_ONE_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(RESPONDENT_ONE_LR_ORG_NAME)
                                        .build()));
        given(manageCaseBaseUrlConfiguration.getManageCaseBaseUrl()).willReturn("http://localhost:3333");
        given(paymentsConfiguration.getSiteId()).willReturn("AAA7");

        hearingValuesService.getValues(caseId, "auth");

        verify(caseDataService).triggerEvent(eq(caseId), eq(CaseEvent.UPDATE_MISSING_FIELDS), any());
    }

    @SneakyThrows
    @Test
    void shouldTriggerEventIfUnavailableDatesMissingFromApplicant1() throws Exception {
        Long caseId = 1L;
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .caseReference(caseId)
            .applicant1(PartyBuilder.builder().individual().build())
            .respondent1(PartyBuilder.builder().company().build())
            .respondent2(PartyBuilder.builder().company().build())
            .multiPartyClaimTwoDefendantSolicitors()
            .caseAccessCategory(UNSPEC_CLAIM)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION_ID)
                                        .setRegion(WELSH_REGION_ID))
            .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .data(caseData.toMap(mapper))
            .id(caseId).build();

        when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);
        when(organisationService.findOrganisationById(APPLICANT_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(APPLICANT_LR_ORG_NAME)
                                        .build()));
        when(organisationService.findOrganisationById(RESPONDENT_ONE_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(RESPONDENT_ONE_LR_ORG_NAME)
                                        .build()));
        given(manageCaseBaseUrlConfiguration.getManageCaseBaseUrl()).willReturn("http://localhost:3333");
        given(paymentsConfiguration.getSiteId()).willReturn("AAA7");

        hearingValuesService.getValues(caseId, "auth");

        verify(caseDataService).triggerEvent(eq(caseId), eq(CaseEvent.UPDATE_MISSING_FIELDS), any());
    }

    @Test
    void shouldNotTriggerEventIfPartyIdCaseFlagsUnavailableDatesExistsForApplicant1() throws Exception {
        WelshLanguageRequirements applicant1WelshLang = new WelshLanguageRequirements();
        applicant1WelshLang.setCourt(Language.ENGLISH);
        UnavailableDate unavailableDate1 = new UnavailableDate();
        unavailableDate1.setUnavailableDateType(SINGLE_DATE);
        unavailableDate1.setDate(LocalDate.of(2023, 10, 20));
        Hearing applicant1Hearing = new Hearing();
        applicant1Hearing.setUnavailableDates(wrapElements(List.of(unavailableDate1)));
        Applicant1DQ applicant1DQ = new Applicant1DQ();
        applicant1DQ.setApplicant1DQLanguage(applicant1WelshLang);
        applicant1DQ.setApplicant1DQHearing(applicant1Hearing);
        WelshLanguageRequirements respondent1WelshLang = new WelshLanguageRequirements();
        respondent1WelshLang.setCourt(Language.WELSH);
        UnavailableDate unavailableDate2 = new UnavailableDate();
        unavailableDate2.setUnavailableDateType(SINGLE_DATE);
        unavailableDate2.setDate(LocalDate.of(2023, 10, 20));
        Hearing respondent1Hearing = new Hearing();
        respondent1Hearing.setUnavailableDates(wrapElements(List.of(unavailableDate2)));
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQLanguage(respondent1WelshLang);
        respondent1DQ.setRespondent1DQHearing(respondent1Hearing);

        Long caseId = 1L;
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .caseReference(caseId)
            .applicant1(PartyBuilder.builder().individual().build())
            .respondent1(PartyBuilder.builder().company().build())
            .caseAccessCategory(UNSPEC_CLAIM)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION_ID)
                                        .setRegion(WELSH_REGION_ID))
            .applicant1DQ(applicant1DQ)
            .respondent1DQ(respondent1DQ)
            .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .data(caseData.toMap(mapper))
            .id(caseId).build();

        when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);
        when(organisationService.findOrganisationById(APPLICANT_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(APPLICANT_LR_ORG_NAME)
                                        .build()));
        when(organisationService.findOrganisationById(RESPONDENT_ONE_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(RESPONDENT_ONE_LR_ORG_NAME)
                                        .build()));
        given(manageCaseBaseUrlConfiguration.getManageCaseBaseUrl()).willReturn("http://localhost:3333");
        given(paymentsConfiguration.getSiteId()).willReturn("AAA7");

        verify(caseDataService, times(0))
            .triggerEvent(eq(caseId), eq(CaseEvent.UPDATE_MISSING_FIELDS), any());
    }

    @SneakyThrows
    @Test
    void shouldThrowFeinExceptionIfCaseDataServiceThrowsException() {
        WelshLanguageRequirements applicant1WelshLang = new WelshLanguageRequirements();
        applicant1WelshLang.setCourt(Language.ENGLISH);
        Applicant1DQ applicant1DQ = new Applicant1DQ();
        applicant1DQ.setApplicant1DQLanguage(applicant1WelshLang);
        WelshLanguageRequirements respondent1WelshLang = new WelshLanguageRequirements();
        respondent1WelshLang.setCourt(Language.WELSH);
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQLanguage(respondent1WelshLang);
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .applicant1(PartyBuilder.builder().individual().build())
            .respondent1(PartyBuilder.builder().company().build())
            .respondent2(PartyBuilder.builder().company().build())
            .caseAccessCategory(UNSPEC_CLAIM)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION_ID)
                                        .setRegion(WELSH_REGION_ID))
            .applicant1DQ(applicant1DQ)
            .respondent1DQ(respondent1DQ)
            .build();
        Long caseId = 1L;
        CaseDetails caseDetails = CaseDetails.builder()
            .data(caseData.toMap(mapper))
            .id(caseId).build();

        when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);
        doThrow(FeignException.GatewayTimeout.class)
            .when(caseDataService).triggerEvent(any(), any(), any());

        assertThrows(FeignException.GatewayTimeout.class, () -> {
            hearingValuesService.getValues(caseId, "auth");
        });
    }

    @NotNull
    private List<CaseCategoryModel> getExpectedCaseCategories() {
        CaseCategoryModel caseType = new CaseCategoryModel();
        caseType.setCategoryParent("");
        caseType.setCategoryType(CategoryType.CASE_TYPE);
        caseType.setCategoryValue("AAA7-SMALL_CLAIM");
        CaseCategoryModel caseSubtype = new CaseCategoryModel();
        caseSubtype.setCategoryParent("AAA7-SMALL_CLAIM");
        caseSubtype.setCategoryType(CategoryType.CASE_SUBTYPE);
        caseSubtype.setCategoryValue("AAA7-SMALL_CLAIM");

        List<CaseCategoryModel> expectedCaseCategories = List.of(caseType, caseSubtype);

        when(caseCategoriesService.getCaseCategoriesFor(eq(CategoryType.CASE_TYPE), any(),  any())).thenReturn(
            caseType
        );
        when(caseCategoriesService.getCaseCategoriesFor(eq(CategoryType.CASE_SUBTYPE), any(),  any())).thenReturn(
            caseSubtype
        );
        return expectedCaseCategories;
    }

    @Test
    @SneakyThrows
    void shouldReturnExpectedHearingValuesWhenCaseDataIs() {
        var caseId = 1L;

        doThrow(new NotFoundException("", new RestException("", "", 500))).when(caseDataService).getCase(caseId);

        assertThrows(
            CaseNotFoundException.class,
            () -> hearingValuesService.getValues(caseId, "auth"));
    }

    @Nested
    class EarlyAdopter {
        Applicant1DQ applicant1DQ;
        Respondent1DQ respondent1DQ;

        @BeforeEach
        void setup() {
            WelshLanguageRequirements applicant1WelshLang = new WelshLanguageRequirements();
            applicant1WelshLang.setCourt(Language.ENGLISH);
            applicant1DQ = new Applicant1DQ();
            applicant1DQ.setApplicant1DQLanguage(applicant1WelshLang);
            WelshLanguageRequirements respondent1WelshLang = new WelshLanguageRequirements();
            respondent1WelshLang.setCourt(Language.WELSH);
            respondent1DQ = new Respondent1DQ();
            respondent1DQ.setRespondent1DQLanguage(respondent1WelshLang);
            when(organisationService.findOrganisationById(APPLICANT_ORG_ID))
                .thenReturn(Optional.of(Organisation.builder()
                                            .name(APPLICANT_LR_ORG_NAME)
                                            .build()));
            when(organisationService.findOrganisationById(RESPONDENT_ONE_ORG_ID))
                .thenReturn(Optional.of(Organisation.builder()
                                            .name(RESPONDENT_ONE_LR_ORG_NAME)
                                            .build()));
            given(manageCaseBaseUrlConfiguration.getManageCaseBaseUrl()).willReturn("http://localhost:3333");
            given(paymentsConfiguration.getSiteId()).willReturn("AAA7");

            Category inPerson = Category.builder().categoryKey("HearingChannel").key("INTER").valueEn("In Person").activeFlag("Y").build();
            Category video = Category.builder().categoryKey("HearingChannel").key("VID").valueEn("Video").activeFlag("Y").build();
            Category telephone = Category.builder().categoryKey("HearingChannel").key("TEL").valueEn("Telephone").activeFlag("Y").build();
            CategorySearchResult categorySearchResult = CategorySearchResult.builder().categories(List.of(inPerson, video, telephone)).build();
            when(categoryService.findCategoryByCategoryIdAndServiceId(anyString(), eq("HearingChannel"), anyString())).thenReturn(
                Optional.of(categorySearchResult));
        }

        @SneakyThrows
        @Test
        void shouldThrowErrorIfLocationIsNotWhiteListed() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(UNSPEC_CLAIM)
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION_ID)
                                            .setRegion(WELSH_REGION_ID))
                .applicant1Represented(YesOrNo.NO)
                .applicant1DQ(applicant1DQ)
                .respondent1DQ(respondent1DQ)
                .build();
            Flags flags = new Flags();
            flags.setPartyName("party name");
            Party applicant1 = caseData.getApplicant1();
            applicant1.setFlags(flags);
            caseData.setApplicant1(applicant1);

            Long caseId = 1L;
            CaseDetails caseDetails = CaseDetails.builder()
                .data(caseData.toMap(mapper))
                .id(caseId).build();
            when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);
            when(earlyAdoptersService.isPartOfHmcLipEarlyAdoptersRollout(any(CaseData.class))).thenReturn(false);

            assertThrows(NotEarlyAdopterCourtException.class, () -> {
                hearingValuesService.getValues(caseId, "auth");
            });
        }

        @Test
        void shouldThrowNotThrowErrorIfLocationIsWhiteListed() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(UNSPEC_CLAIM)
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION_ID)
                                            .setRegion(WELSH_REGION_ID))
                .applicant1DQ(applicant1DQ)
                .respondent1DQ(respondent1DQ)
                .build();
            Flags flags = new Flags();
            flags.setPartyName("party name");
            Party applicant1 = caseData.getApplicant1();
            applicant1.setFlags(flags);
            caseData.setApplicant1(applicant1);

            Long caseId = 1L;
            CaseDetails caseDetails = CaseDetails.builder()
                .data(caseData.toMap(mapper))
                .id(caseId).build();
            when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);
            when(earlyAdoptersService.isPartOfHmcLipEarlyAdoptersRollout(any(CaseData.class))).thenReturn(true);

            assertDoesNotThrow(() -> hearingValuesService.getValues(caseId, "auth"));
        }

        @SneakyThrows
        @Test
        void shouldNotThrowErrorIfLocationIsNotWhiteListedButWelshEnabled() {
            when(featuretoggleService.isWelshEnabledForMainCase()).thenReturn(true);
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(UNSPEC_CLAIM)
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION_ID)
                                            .setRegion(WELSH_REGION_ID))
                .applicant1Represented(YesOrNo.NO)
                .applicant1DQ(applicant1DQ)
                .respondent1DQ(respondent1DQ)
                .build();
            Flags flags = new Flags();
            flags.setPartyName("party name");
            Party applicant1 = caseData.getApplicant1();
            applicant1.setFlags(flags);
            caseData.setApplicant1(applicant1);

            Long caseId = 1L;
            CaseDetails caseDetails = CaseDetails.builder()
                .data(caseData.toMap(mapper))
                .id(caseId).build();
            when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);
            when(earlyAdoptersService.isPartOfHmcLipEarlyAdoptersRollout(any(CaseData.class))).thenReturn(false);

            assertDoesNotThrow(() -> hearingValuesService.getValues(caseId, "auth"));
        }

        @SneakyThrows
        @Test
        void shouldNotThrowErrorIfApplicantLiPPresent() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(UNSPEC_CLAIM)
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION_ID)
                                            .setRegion(WELSH_REGION_ID))
                .applicant1Represented(YesOrNo.NO)
                .applicant1DQ(applicant1DQ)
                .respondent1DQ(respondent1DQ)
                .build();
            Flags flags = new Flags();
            flags.setPartyName("party name");
            Party applicant1 = caseData.getApplicant1();
            applicant1.setFlags(flags);
            caseData.setApplicant1(applicant1);

            Long caseId = 1L;
            CaseDetails caseDetails = CaseDetails.builder()
                .data(caseData.toMap(mapper))
                .id(caseId).build();
            when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);

            when(earlyAdoptersService.isPartOfHmcLipEarlyAdoptersRollout(any(CaseData.class))).thenReturn(true);
            assertDoesNotThrow(() -> hearingValuesService.getValues(caseId, "auth"));
        }

        @SneakyThrows
        @Test
        void shouldNotThrowErrorIfRespondentLiPPresentAndHmcLipToggleOn() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(UNSPEC_CLAIM)
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION_ID)
                                            .setRegion(WELSH_REGION_ID))
                .respondent1Represented(YesOrNo.NO)
                .applicant1DQ(applicant1DQ)
                .respondent1DQ(respondent1DQ)
                .build();
            Flags flags = new Flags();
            flags.setPartyName("party name");
            Party applicant1 = caseData.getApplicant1();
            applicant1.setFlags(flags);
            caseData.setApplicant1(applicant1);

            Long caseId = 1L;
            CaseDetails caseDetails = CaseDetails.builder()
                .data(caseData.toMap(mapper))
                .id(caseId).build();
            when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);

            when(earlyAdoptersService.isPartOfHmcLipEarlyAdoptersRollout(any(CaseData.class))).thenReturn(true);
            assertDoesNotThrow(() -> hearingValuesService.getValues(caseId, "auth"));
        }

        @SneakyThrows
        @Test
        void shouldNotThrowErrorIfRespondent2LiPPresentAndHmcLipEnabled() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(UNSPEC_CLAIM)
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION_ID)
                                            .setRegion(WELSH_REGION_ID))
                .respondent2Represented(YesOrNo.NO)
                .applicant1DQ(applicant1DQ)
                .respondent1DQ(respondent1DQ)
                .build();
            Flags flags = new Flags();
            flags.setPartyName("party name");
            Party applicant1 = caseData.getApplicant1();
            applicant1.setFlags(flags);
            caseData.setApplicant1(applicant1);

            Long caseId = 1L;
            CaseDetails caseDetails = CaseDetails.builder()
                .data(caseData.toMap(mapper))
                .id(caseId).build();
            when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);

            when(earlyAdoptersService.isPartOfHmcLipEarlyAdoptersRollout(any(CaseData.class))).thenReturn(true);
            assertDoesNotThrow(() -> hearingValuesService.getValues(caseId, "auth"));
        }

        @SneakyThrows
        @Test
        void shouldNotThrowErrorIfNoLiPPresent() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(UNSPEC_CLAIM)
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION_ID)
                                            .setRegion(WELSH_REGION_ID))
                .applicant1DQ(applicant1DQ)
                .respondent1DQ(respondent1DQ)
                .build();
            Flags flags = new Flags();
            flags.setPartyName("party name");
            Party applicant1 = caseData.getApplicant1();
            applicant1.setFlags(flags);
            caseData.setApplicant1(applicant1);

            Long caseId = 1L;
            CaseDetails caseDetails = CaseDetails.builder()
                .data(caseData.toMap(mapper))
                .id(caseId).build();
            when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);

            when(earlyAdoptersService.isPartOfHmcLipEarlyAdoptersRollout(any(CaseData.class))).thenReturn(true);
            assertDoesNotThrow(() -> hearingValuesService.getValues(caseId, "auth"));
        }

        @Test
        void shouldThrowNotThrowErrorIfEaCourtIsYes() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .caseAccessCategory(UNSPEC_CLAIM)
                .caseManagementLocation(new CaseLocationCivil().setBaseLocation(BASE_LOCATION_ID)
                                            .setRegion(WELSH_REGION_ID))
                .eaCourtLocation(YesOrNo.YES)
                .applicant1DQ(applicant1DQ)
                .respondent1DQ(respondent1DQ)
                .build();
            Flags flags = new Flags();
            flags.setPartyName("party name");
            Party applicant1 = caseData.getApplicant1();
            applicant1.setFlags(flags);
            caseData.setApplicant1(applicant1);

            Long caseId = 1L;
            CaseDetails caseDetails = CaseDetails.builder()
                .data(caseData.toMap(mapper))
                .id(caseId).build();
            when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
            when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);

            when(earlyAdoptersService.isPartOfHmcLipEarlyAdoptersRollout(any(CaseData.class))).thenReturn(true);
            assertDoesNotThrow(() -> hearingValuesService.getValues(caseId, "auth"));
        }
    }

    private List<PartyDetailsModel> getExpectedPartyModel() {
        PartyDetailsModel applicantPartyDetails = buildExpectedIndividualPartyDetails(
            "app-1-party-id",
            "John",
            "Rambo",
            "Mr. John Rambo",
            "CLAI",
            "rambo@email.com"
        );

        PartyDetailsModel applicantSolicitorParty = buildExpectedOrganisationPartyObject(
            APPLICANT_LR_ORG_NAME,
            APPLICANT_ORG_ID
        );

        PartyDetailsModel respondentPartyDetails = buildExpectedIndividualPartyDetails(
            "res-1-party-id",
            "Sole",
            "Trader",
            "Mr. Sole Trader",
            "DEFE",
            "sole.trader@email.com"
        );

        PartyDetailsModel respondentSolicitorParty = buildExpectedOrganisationPartyObject(
            RESPONDENT_ONE_LR_ORG_NAME,
            RESPONDENT_ONE_ORG_ID
        );

        return List.of(applicantPartyDetails, applicantSolicitorParty,
                       respondentPartyDetails, respondentSolicitorParty);
    }

    private PartyDetailsModel buildExpectedIndividualPartyDetails(String partyId, String firstName, String lastName,
                                                                  String partyName, String partyRole,
                                                                  String email) {
        List<String> hearingChannelEmail = email == null ? emptyList() : List.of(email);
        IndividualDetailsModel individualDetails = new IndividualDetailsModel();
        individualDetails.setFirstName(firstName);
        individualDetails.setLastName(lastName);
        individualDetails.setInterpreterLanguage(null);
        individualDetails.setReasonableAdjustments(emptyList());
        individualDetails.setVulnerableFlag(false);
        individualDetails.setVulnerabilityDetails(null);
        individualDetails.setHearingChannelEmail(hearingChannelEmail);
        individualDetails.setHearingChannelPhone(List.of("0123456789"));
        individualDetails.setRelatedParties(emptyList());
        individualDetails.setCustodyStatus(null);

        PartyDetailsModel partyDetails = new PartyDetailsModel();
        partyDetails.setPartyID(partyId);
        partyDetails.setPartyType(IND);
        partyDetails.setPartyName(partyName);
        partyDetails.setPartyRole(partyRole);
        partyDetails.setIndividualDetails(individualDetails);
        partyDetails.setOrganisationDetails(null);
        partyDetails.setUnavailabilityDOW(null);
        partyDetails.setUnavailabilityRanges(null);
        partyDetails.setHearingSubChannel(null);
        return partyDetails;
    }

    private PartyDetailsModel buildExpectedOrganisationPartyObject(String name,
                                                                   String cftOrganisationID) {
        OrganisationDetailsModel organisationDetails = new OrganisationDetailsModel();
        organisationDetails.setName(name);
        organisationDetails.setOrganisationType(ORG.getLabel());
        organisationDetails.setCftOrganisationID(cftOrganisationID);

        PartyDetailsModel partyDetails = new PartyDetailsModel();
        partyDetails.setPartyID(cftOrganisationID);
        partyDetails.setPartyType(ORG);
        partyDetails.setPartyName(name);
        partyDetails.setPartyRole("LGRP");
        partyDetails.setIndividualDetails(null);
        partyDetails.setOrganisationDetails(organisationDetails);
        partyDetails.setUnavailabilityDOW(null);
        partyDetails.setUnavailabilityRanges(null);
        partyDetails.setHearingSubChannel(null);
        return partyDetails;
    }
}
