package uk.gov.hmcts.reform.civil.service.hearings;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.exception.RestException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.ManageCaseBaseUrlConfiguration;
import uk.gov.hmcts.reform.civil.config.PaymentsConfiguration;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.enums.hearing.CategoryType;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.hearingvalues.CaseCategoryModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.HearingLocationModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.IndividualDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.JudiciaryModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.OrganisationDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PartyDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.RelatedPartiesModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.VocabularyModel;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.util.Lists.emptyList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.hearing.HMCLocationType.COURT;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyType.IND;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyType.ORG;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.CaseFlagsMapper.getCaseFlags;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.ScreenFlowMapper.getScreenFlow;

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
    private OrganisationService organisationService;
    @Mock
    private DeadlinesCalculator deadlinesCalculator;
    @Autowired
    private ObjectMapper objectMapper;

    @InjectMocks
    private HearingValuesService hearingValuesService;

    private static final String APPLICANT_ORG_ID = "QWERTY A";
    private static final String RESPONDENT_ONE_ORG_ID = "QWERTY R";
    private static final String APPLICANT_LR_ORG_NAME = "Applicant LR Org name";
    private static final String RESPONDENT_ONE_LR_ORG_NAME = "Respondent 1 LR Org name";
    private static final String BASE_LOCATION_ID = "1234";
    private static final String WELSH_REGION_ID = "7";

    @Test
    void shouldReturnExpectedHearingValuesWhenCaseDataIsReturned() {
        Applicant1DQ applicant1DQ = Applicant1DQ.builder().applicant1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.ENGLISH).build()).build();
        Respondent1DQ respondent1DQ = Respondent1DQ.builder().respondent1DQLanguage(
            WelshLanguageRequirements.builder().court(Language.WELSH).build()).build();
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .caseAccessCategory(UNSPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation(BASE_LOCATION_ID)
                                        .region(WELSH_REGION_ID).build())
            .applicant1DQ(applicant1DQ)
            .respondent1DQ(respondent1DQ)
            .build();
        Long caseId = 1L;
        CaseDetails caseDetails = CaseDetails.builder()
            .data(caseData.toMap(objectMapper))
            .id(caseId).build();

        when(caseDataService.getCase(caseId)).thenReturn(caseDetails);
        when(caseDetailsConverter.toCaseData(caseDetails.getData())).thenReturn(caseData);
        when(deadlinesCalculator.getSlaStartDate(caseData)).thenReturn(LocalDate.of(2023, 1, 30));
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

        List<CaseCategoryModel> expectedCaseCategories = getExpectedCaseCategories();

        List<HearingLocationModel> expectedHearingLocation = List.of(HearingLocationModel.builder()
                                                       .locationId(BASE_LOCATION_ID)
                                                       .locationType(COURT)
                                                       .build());

        JudiciaryModel expectedJudiciary = JudiciaryModel.builder().build();

        ServiceHearingValuesModel expected = ServiceHearingValuesModel.builder()
            .hmctsServiceID("AAA7")
            .hmctsInternalCaseName("Mr. John Rambo v Mr. Sole Trader")
            .publicCaseName("'John Rambo' v 'Sole Trader'")
            .caseAdditionalSecurityFlag(false)
            .caseCategories(expectedCaseCategories)
            .caseDeepLink("http://localhost:3333/cases/case-details/1")
            .caseRestrictedFlag(false)
            .externalCaseReference(null)
            .caseManagementLocationCode(BASE_LOCATION_ID)
            .caseSLAStartDate("2023-01-30")
            .autoListFlag(false)
            .hearingType(null)
            .hearingWindow(null)
            .duration(0)
            .hearingPriorityType("Standard")
            .numberOfPhysicalAttendees(0)
            .hearingInWelshFlag(true)
            .hearingLocations(expectedHearingLocation)
            .facilitiesRequired(null)
            .listingComments(null)
            .hearingRequester("")
            .privateHearingRequiredFlag(false)
            .caseInterpreterRequiredFlag(false)
            .panelRequirements(null)
            .leadJudgeContractType("")
            .judiciary(expectedJudiciary)
            .hearingIsLinkedFlag(false)
            .parties(getExpectedPartyModel())
            .screenFlow(getScreenFlow())
            .vocabulary(List.of(VocabularyModel.builder().build()))
            .hearingChannels(null)
            .caseFlags(getCaseFlags(caseData))
            .build();

        ServiceHearingValuesModel actual = hearingValuesService.getValues(caseId, "8AB87C89", "auth");

        verify(caseDetailsConverter).toCaseData(eq(caseDetails.getData()));
        verify(deadlinesCalculator).getSlaStartDate(eq(caseData));
        assertThat(actual).isEqualTo(expected);
    }

    @NotNull
    private List<CaseCategoryModel> getExpectedCaseCategories() {
        CaseCategoryModel caseType = CaseCategoryModel.builder()
            .categoryParent("")
            .categoryType(CategoryType.CASE_TYPE)
            .categoryValue("AAA7-SMALL_CLAIM")
            .build();
        CaseCategoryModel caseSubtype = CaseCategoryModel.builder()
            .categoryParent("AAA7-SMALL_CLAIM")
            .categoryType(CategoryType.CASE_SUBTYPE)
            .categoryValue("AAA7-SMALL_CLAIM")
            .build();

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

        doThrow(new NotFoundException("", new RestException("", new Exception())))
            .when(caseDataService).getCase(caseId);

        assertThrows(
            CaseNotFoundException.class,
            () -> hearingValuesService.getValues(caseId, "8AB87C89", "auth"));
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
        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName(firstName)
            .lastName(lastName)
            .interpreterLanguage(null)
            .reasonableAdjustments(emptyList())
            .vulnerableFlag(false)
            .vulnerabilityDetails(null)
            .hearingChannelEmail(hearingChannelEmail)
            .hearingChannelPhone(List.of("0123456789"))
            .relatedParties(List.of(RelatedPartiesModel.builder().build()))
            .custodyStatus(null)
            .build();

        return PartyDetailsModel.builder()
            .partyID(partyId)
            .partyType(IND)
            .partyName(partyName)
            .partyRole(partyRole)
            .individualDetails(individualDetails)
            .organisationDetails(null)
            .unavailabilityDOW(null)
            .unavailabilityRange(null)
            .hearingSubChannel(null)
            .build();
    }

    private PartyDetailsModel buildExpectedOrganisationPartyObject(String name,
                                                                   String cftOrganisationID) {
        OrganisationDetailsModel organisationDetails = OrganisationDetailsModel.builder()
            .name(name)
            .organisationType(ORG.getLabel())
            .cftOrganisationID(cftOrganisationID)
            .build();

        return PartyDetailsModel.builder()
            .partyID(null)
            .partyType(ORG)
            .partyName(name)
            .partyRole("LGRP")
            .individualDetails(null)
            .organisationDetails(organisationDetails)
            .unavailabilityDOW(null)
            .unavailabilityRange(null)
            .hearingSubChannel(null)
            .build();
    }
}

