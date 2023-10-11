package uk.gov.hmcts.reform.civil.model.docmosis.draft;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.AdditionalLipPartyDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DraftClaimFormMapperTest {

    @Mock
    private InterestCalculator interestCalculator;

    @InjectMocks
    private DraftClaimFormMapper draftClaimFormMapper;

    private static final String INDIVIDUAL_TITLE = "Mr.";
    private static final String INDIVIDUAL_FIRST_NAME = "Hot";
    private static final String INDIVIDUAL_LAST_NAME = "Dog";
    private static final String SOLE_TRADER = "Dog walkers";
    private static final String EMAIL = "email@email.com";
    private static final String COMPANY = "company";
    private static final String ORGANISATION = "organisation";
    private static final BigDecimal INTEREST = new BigDecimal(23);
    private static final BigDecimal TOTAL_CLAIM_AMOUNT = new BigDecimal(150000);
    private static final BigDecimal CLAIM_FEE = new BigDecimal(2000);
    private static final CaseData CASE_DATA = getCaseData();
    private static final String STANDARD_INTEREST_RATE = "8";
    private static final String DIFFERENT_RATE_EXPLANATION = "something different";
    private static final LocalDate INTEREST_FROM_SPECIFIC_DATE = LocalDate.now();
    private static final LocalDateTime SUBMITTED_DATE = LocalDateTime.of(2023, 6, 1, 0, 0, 0);

    @Test
    void should_displayIndividualName_whenPartiesIndividual() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .individualLastName(INDIVIDUAL_LAST_NAME)
                            .individualFirstName(INDIVIDUAL_FIRST_NAME)
                            .individualTitle(INDIVIDUAL_TITLE)
                            .partyEmail(EMAIL)
                            .type(Party.Type.INDIVIDUAL)
                            .build())
            .respondent1(Party.builder()
                             .individualLastName(INDIVIDUAL_LAST_NAME)
                             .individualFirstName(INDIVIDUAL_FIRST_NAME)
                             .individualTitle(INDIVIDUAL_TITLE)
                             .partyEmail(EMAIL)
                             .type(Party.Type.INDIVIDUAL)
                             .build())
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getClaimant().name()).isEqualTo(caseData.getApplicant1().getPartyName());
        assertThat(form.getDefendant().name()).isEqualTo(caseData.getRespondent1().getPartyName());
    }

    @Test
    void shouldDisplay_soleTraderInformation_whenPartiesAreSoleTrader() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .soleTraderLastName(INDIVIDUAL_LAST_NAME)
                            .soleTraderFirstName(INDIVIDUAL_FIRST_NAME)
                            .soleTraderTitle(INDIVIDUAL_TITLE)
                            .soleTraderTradingAs(SOLE_TRADER)
                            .partyEmail(EMAIL)
                            .type(Party.Type.SOLE_TRADER)
                            .build())
            .respondent1(Party.builder()
                             .soleTraderLastName(INDIVIDUAL_LAST_NAME)
                             .soleTraderFirstName(INDIVIDUAL_FIRST_NAME)
                             .soleTraderTitle(INDIVIDUAL_TITLE)
                             .soleTraderTradingAs(SOLE_TRADER)
                             .partyEmail(EMAIL)
                             .type(Party.Type.SOLE_TRADER)
                             .build())
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getClaimant().name()).isEqualTo(caseData.getApplicant1().getPartyName());
        assertThat(form.getDefendant().name()).isEqualTo(caseData.getRespondent1().getPartyName());
        assertThat(form.getClaimant().soleTraderBusinessName()).isEqualTo(SOLE_TRADER);
        assertThat(form.getDefendant().soleTraderBusinessName()).isEqualTo(SOLE_TRADER);
    }

    @Test
    void shouldDisplayCompany_whenPartiesAreCompany() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .companyName(COMPANY)
                            .partyEmail(EMAIL)
                            .type(Party.Type.COMPANY)
                            .build())
            .respondent1(Party.builder()
                             .companyName(COMPANY)
                             .partyEmail(EMAIL)
                             .type(Party.Type.COMPANY)
                             .build())
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getClaimant().name()).isEqualTo(COMPANY);
        assertThat(form.getDefendant().name()).isEqualTo(COMPANY);
    }

    @Test
    void shouldDisplayOrganisationName_whenPartiesAreOrganisation() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .organisationName(ORGANISATION)
                            .partyEmail(EMAIL)
                            .type(Party.Type.ORGANISATION)
                            .build())
            .respondent1(Party.builder()
                             .organisationName(ORGANISATION)
                             .partyEmail(EMAIL)
                             .type(Party.Type.ORGANISATION)
                             .build())
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getClaimant().name()).isEqualTo(ORGANISATION);
        assertThat(form.getDefendant().name()).isEqualTo(ORGANISATION);
    }

    @Test
    void shouldShowInterest_whenExists() {
        //Given
        given(interestCalculator.calculateInterest(CASE_DATA)).willReturn(INTEREST);
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(CASE_DATA);
        //Then
        assertThat(form.getInterestAmount()).isEqualTo(INTEREST.toString());
        assertThat(form.getTotalInterestAmount()).isEqualTo(INTEREST.toString());
    }

    @Test
    void shouldDisplayNull_whenNoInterestExists() {
        //Given
        given(interestCalculator.calculateInterest(CASE_DATA)).willReturn(null);
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(CASE_DATA);
        //Then
        assertThat(form.getInterestAmount()).isNull();
        assertThat(form.getTotalInterestAmount()).isNull();
    }

    @Test
    void shouldMapHowInterestWasCalculated_whenInterestOptionExists() {
        //Given
        CaseData caseData = getCaseData().toBuilder()
            .interestClaimOptions(InterestClaimOptions.SAME_RATE_INTEREST)
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getHowTheInterestWasCalculated())
            .isEqualTo(InterestClaimOptions.SAME_RATE_INTEREST.getDescription());
    }

    @Test
    void shouldReturnNullForHowInterestWasCalculated_whenInterestOptionIsNull() {
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(CASE_DATA);
        //Then
        assertThat(form.getHowTheInterestWasCalculated()).isNull();
    }

    @Test
    void shouldReturnDifferentInterestRate_whenItExists() {
        //Given
        CaseData caseData = getCaseData().toBuilder()
            .sameRateInterestSelection(SameRateInterestSelection.builder()
                                           .differentRate(INTEREST).build())
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getInterestRate()).isEqualTo(INTEREST.toString());
    }

    @Test
    void shouldReturnEightPercentInterestRate_whenDifferentRateIsNull() {
        //Given
        CaseData caseData = getCaseData().toBuilder()
            .sameRateInterestSelection(SameRateInterestSelection.builder().build())
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getInterestRate()).isEqualTo(STANDARD_INTEREST_RATE);
    }

    @Test
    void shouldReturnNull_whenSameInterestSelectionIsNull() {
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(CASE_DATA);
        //Then
        assertThat(form.getInterestRate()).isNull();
    }

    @Test
    void shouldReturnStandardExplanationText_whenNoDifferentRateReason() {
        //Given
        CaseData caseData = getCaseData().toBuilder()
            .sameRateInterestSelection(SameRateInterestSelection.builder()
                                           .build())
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getInterestExplanationText()).isEqualTo(DraftClaimFormMapper.EXPLANATION_OF_INTEREST_RATE);
    }

    @Test
    void shouldReturnDifferentExplanationText_whenDifferentRateReasonExists() {
        //Given
        CaseData caseData = getCaseData().toBuilder()
            .sameRateInterestSelection(SameRateInterestSelection.builder()
                                           .differentRateReason(DIFFERENT_RATE_EXPLANATION)
                                           .build())
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getInterestExplanationText()).isEqualTo(DIFFERENT_RATE_EXPLANATION);
    }

    @Test
    void shouldReturnNullForInterestExplanation_whenNoInterestRateSelection() {
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(CASE_DATA);
        //Then
        assertThat(form.getInterestExplanationText()).isNull();
    }

    @Test
    void shouldReturnInterestFromSpecificDate_whenInterestFromSpecificDateExists() {
        //Given
        CaseData caseData = getCaseData().toBuilder()
            .interestFromSpecificDate(INTEREST_FROM_SPECIFIC_DATE)
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getInterestFromDate()).isEqualTo(INTEREST_FROM_SPECIFIC_DATE);
    }

    @Test
    void shouldReturnInterestFromClaimIssueDate_whenInterestFromSpecificDateIsNull() {
        //Given
        CaseData caseData = getCaseData().toBuilder()
            .submittedDate(SUBMITTED_DATE)
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getInterestFromDate()).isEqualTo(SUBMITTED_DATE.toLocalDate());
    }

    @Test
    void shouldReturnNullForInterestFromDate_whenSubmittedDateIsNull() {
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(CASE_DATA);
        //Then
        assertThat(form.getInterestFromDate()).isNull();
    }

    @Test
    void shouldReturnNullForWhenAreYouPlanningInterestFrom_whenInterestFromIsNull() {
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(CASE_DATA);
        //Then
        assertThat(form.getWhenAreYouClaimingInterestFrom()).isNull();
    }

    @Test
    void shouldReturnInterestStartFromClaimIssue_whenInterestClaimFromTypeIsFromClaimSubmitted() {
        //Given
        CaseData caseData = getCaseData().toBuilder()
            .submittedDate(SUBMITTED_DATE)
            .interestClaimFrom(InterestClaimFromType.FROM_CLAIM_SUBMIT_DATE)
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getWhenAreYouClaimingInterestFrom())
            .isEqualTo(DraftClaimFormMapper.INTEREST_START_FROM_CLAIM_ISSUED_DATE);
    }

    @Test
    void shouldReturnSpecificDescriptionForWhenAreYouPlanningInterestFrom_whenInterestClaimFromTypeIsNotFromClaimSubmitted() {
        //Given
        CaseData caseData = getCaseData().toBuilder()
            .submittedDate(SUBMITTED_DATE)
            .interestClaimFrom(InterestClaimFromType.FROM_A_SPECIFIC_DATE)
            .interestFromSpecificDateDescription(DIFFERENT_RATE_EXPLANATION)
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getWhenAreYouClaimingInterestFrom())
            .isEqualTo(caseData.getInterestFromSpecificDateDescription());
    }

    @Test
    void shouldReturnZeroForTotalClaimAmount_whenTotalClaimAmountIsNull() {
        //Given
        CaseData caseData = CASE_DATA.toBuilder().totalClaimAmount(null).build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getTotalClaimAmount()).isEqualTo("0");
    }

    @Test
    void shouldReturnTotalClaimAmount_whenTotalClaimAmountExists() {
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(CASE_DATA);
        //Then
        assertThat(form.getTotalClaimAmount()).isEqualTo(TOTAL_CLAIM_AMOUNT.toString());
    }

    @Test
    void shouldReturnTotalAmountOfClaimWithInterest_whenInterestIsNotNull() {
        //Given
        CaseData caseData = getCaseData().toBuilder()
            .submittedDate(SUBMITTED_DATE)
            .claimFee(Fee.builder().calculatedAmountInPence(CLAIM_FEE).build())
            .build();
        given(interestCalculator.calculateInterest(caseData)).willReturn(INTEREST);
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getTotalAmountOfClaim())
            .isEqualTo(INTEREST.add(TOTAL_CLAIM_AMOUNT).add(MonetaryConversions.penniesToPounds(CLAIM_FEE)).toString());
    }

    @Test
    void shouldReturnEndInterestDate_whenEndDateDescritptionIsNull() {
        //Given
        CaseData caseData = getCaseData().toBuilder()
            .submittedDate(SUBMITTED_DATE)
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getInterestEndDate()).isNotNull();
    }

    @Test
    void shouldReturnNullForEndInterestDate_whenEndDateDescriptionExists() {
        //Given
        CaseData caseData = getCaseData().toBuilder()
            .submittedDate(SUBMITTED_DATE)
            .breakDownInterestDescription(DIFFERENT_RATE_EXPLANATION)
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getInterestEndDate()).isNull();
    }

    @Test
    void shouldReturnInterestEndDateDescription_whenBreakDownInterestDescriptionExists() {
        //Given
        CaseData caseData = getCaseData().toBuilder()
            .submittedDate(SUBMITTED_DATE)
            .breakDownInterestDescription(DIFFERENT_RATE_EXPLANATION)
            .build();
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(caseData);
        //Then
        assertThat(form.getInterestEndDateDescription()).isEqualTo(DIFFERENT_RATE_EXPLANATION);
    }

    @Test
    void shouldReturnNullForInterestEndDateDescription_whenBreakDownInterestDescriptionIsNull() {
        //When
        DraftClaimForm form = draftClaimFormMapper.toDraftClaimForm(CASE_DATA);
        //Then
        assertThat(form.getInterestEndDateDescription()).isNull();
    }

    private static CaseData getCaseData() {
        CaseData caseData = CaseData.builder()
            .applicant1(Party.builder()
                            .companyName(ORGANISATION)
                            .partyEmail(EMAIL)
                            .type(Party.Type.ORGANISATION)
                            .build())
            .respondent1(Party.builder()
                             .companyName(ORGANISATION)
                             .partyEmail(EMAIL)
                             .type(Party.Type.ORGANISATION)
                             .build())
            .totalClaimAmount(TOTAL_CLAIM_AMOUNT)
            .caseDataLiP(CaseDataLiP.builder()
                             .applicant1AdditionalLipPartyDetails(AdditionalLipPartyDetails
                                                                      .builder()
                                                                      .build())
                             .respondent1AdditionalLipPartyDetails(AdditionalLipPartyDetails
                                                                       .builder()
                                                                       .build())
                             .build())
            .build();
        return caseData;
    }
}
