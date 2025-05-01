package uk.gov.hmcts.reform.civil.service.citizen.repaymentplan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.RepaymentDecisionType;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.repaymentplan.ClaimantProposedPlan;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;

@ExtendWith(MockitoExtension.class)
class RepaymentPlanDecisionCalculatorTest {

    private static final double CALCULATED_INCOME = 2466.56;
    private static final double CALCULATED_EXPENDITURE = 1500;
    private static final double CALCULATED_ALLOWANCE = 400;
    //Disposable income is 566.56;
    private static final BigDecimal AFFORDABLE_CLAIM_TOTAL = new BigDecimal(300);
    private static final BigDecimal UNAFFORDABLE_CLAIM_TOTAL = new BigDecimal(1000);

    @Mock
    private IncomeCalculator incomeCalculator;
    @Mock
    private ExpenditureCalculator expenditureCalculator;
    @Mock
    private AllowanceCalculator allowanceCalculator;
    @Mock
    private CaseData caseData;
    @Mock
    private Respondent1DQ respondent1DQ;
    @Mock
    private RepaymentPlanLRspec defendantRepaymentPlan;

    @Mock
    private ClaimantProposedPlan claimantProposedPlan;
    @InjectMocks
    private RepaymentPlanDecisionCalculator repaymentPlanDecisionCalculator;

    @BeforeEach
    void setUp() {
        given(incomeCalculator.calculateTotalMonthlyIncome(any(), any(), any())).willReturn(CALCULATED_INCOME);
        given(expenditureCalculator.calculateTotalExpenditure(any(), any(), any())).willReturn(CALCULATED_EXPENDITURE);
        given(allowanceCalculator.calculateAllowance(any())).willReturn(CALCULATED_ALLOWANCE);
        given(caseData.getRespondent1DQ()).willReturn(respondent1DQ);

    }

    @Nested
    class DecisionCalculationOnPayImmediatelyTest {

        @BeforeEach
        void setUp() {
            given(claimantProposedPlan.hasProposedPayImmediately()).willReturn(true);
        }

        @Test
        void shouldAcceptClaimantProposition_whenDefendantCanAffordIt() {
            //Given
            given(caseData.getRespondToAdmittedClaimOwingAmountPounds()).willReturn(AFFORDABLE_CLAIM_TOTAL);
            //When
            RepaymentDecisionType decision = repaymentPlanDecisionCalculator.calculateRepaymentDecision(
                caseData,
                claimantProposedPlan
            );
            //Then
            assertThat(decision.isInFavourOfDefendant()).isFalse();
        }

        @Test
        void shouldDeclineClaimantProposition_whenDefendantCantAffordIt() {
            //Given
            given(caseData.getRespondToAdmittedClaimOwingAmountPounds()).willReturn(UNAFFORDABLE_CLAIM_TOTAL);
            //When
            RepaymentDecisionType decision = repaymentPlanDecisionCalculator.calculateRepaymentDecision(
                caseData,
                claimantProposedPlan
            );
            //Then
            assertThat(decision.isInFavourOfDefendant()).isTrue();
        }
    }

    @Nested
    class DecisionCalculationOnPayBySetDate {

        private static final LocalDate DEFENDANT_REPAYMENT_DATE = LocalDate.of(2023, 9, 22);
        private static final LocalDate CLAIMANT_PROPOSED_DATE_BEFORE_DEFENDANT_DATE = LocalDate.of(2023, 8, 22);
        private static final LocalDate CLAIMANT_PROPOSED_DATE_AFTER_DEFENDANT_DATE = LocalDate.of(2023, 10, 22);

        @Mock
        private RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec;

        @BeforeEach
        void setUp() {
            given(claimantProposedPlan.hasProposedPayBySetDate()).willReturn(true);
        }

        @Test
        void shouldReturnRepaymentDateByDefendant_whenDefendantChosePayBySetDate() {
            //Given
            given(caseData.getDefenceAdmitPartPaymentTimeRouteRequired()).willReturn(BY_SET_DATE);
            given(caseData.getRespondToClaimAdmitPartLRspec()).willReturn(respondToClaimAdmitPartLRspec);
            given(respondToClaimAdmitPartLRspec.getWhenWillThisAmountBePaid()).willReturn(DEFENDANT_REPAYMENT_DATE);
            given(caseData.getRespondToAdmittedClaimOwingAmountPounds()).willReturn(UNAFFORDABLE_CLAIM_TOTAL);
            given(claimantProposedPlan.getRepaymentByDate()).willReturn(CLAIMANT_PROPOSED_DATE_BEFORE_DEFENDANT_DATE);
            //When
            repaymentPlanDecisionCalculator.calculateRepaymentDecision(
                caseData,
                claimantProposedPlan
            );
            //Then
            verify(caseData).getRespondToClaimAdmitPartLRspec();
        }

        @Test
        void shouldReturnFinalDateOfDefendantRepaymentPlan_whenDefendantChosePayByInstallments() {
            //Given
            given(caseData.getDefenceAdmitPartPaymentTimeRouteRequired()).willReturn(SUGGESTION_OF_REPAYMENT_PLAN);
            given(caseData.getRespondent1RepaymentPlan()).willReturn(defendantRepaymentPlan);
            given(defendantRepaymentPlan.finalPaymentBy(any())).willReturn(DEFENDANT_REPAYMENT_DATE);
            given(caseData.getRespondToAdmittedClaimOwingAmountPounds()).willReturn(UNAFFORDABLE_CLAIM_TOTAL);
            given(claimantProposedPlan.getRepaymentByDate()).willReturn(CLAIMANT_PROPOSED_DATE_BEFORE_DEFENDANT_DATE);
            //When
            repaymentPlanDecisionCalculator.calculateRepaymentDecision(
                caseData,
                claimantProposedPlan
            );
            //Then
            verify(caseData, never()).getRespondToClaimAdmitPartLRspec();
            verify(caseData).getRespondent1RepaymentPlan();
        }

        @Test
        void shouldChooseClaimantPlan_whenDefednantCanAfford() {
            //Given
            given(caseData.getRespondToAdmittedClaimOwingAmountPounds()).willReturn(AFFORDABLE_CLAIM_TOTAL);
            //When
            RepaymentDecisionType decision = repaymentPlanDecisionCalculator.calculateRepaymentDecision(
                caseData,
                claimantProposedPlan
            );
            //Then
            assertThat(decision.isInFavourOfDefendant()).isFalse();
        }

        @Test
        void shouldChooseClaimantPlan_whenDefendantCantAffordAndClaimantPayDateIsAfterDefendantPayDate() {
            //Given
            given(caseData.getDefenceAdmitPartPaymentTimeRouteRequired()).willReturn(BY_SET_DATE);
            given(caseData.getRespondToClaimAdmitPartLRspec()).willReturn(respondToClaimAdmitPartLRspec);
            given(respondToClaimAdmitPartLRspec.getWhenWillThisAmountBePaid()).willReturn(DEFENDANT_REPAYMENT_DATE);
            given(caseData.getRespondToAdmittedClaimOwingAmountPounds()).willReturn(UNAFFORDABLE_CLAIM_TOTAL);
            given(claimantProposedPlan.getRepaymentByDate()).willReturn(CLAIMANT_PROPOSED_DATE_AFTER_DEFENDANT_DATE);
            //When
            RepaymentDecisionType decision = repaymentPlanDecisionCalculator.calculateRepaymentDecision(
                caseData,
                claimantProposedPlan
            );
            //Then
            assertThat(decision.isInFavourOfDefendant()).isFalse();
        }

        @Test
        void shouldChooseDefendantPlan_whenDefendantCantAffordAndClaimantDateIsBeforeDefendantPayDate() {
            //Given
            given(caseData.getDefenceAdmitPartPaymentTimeRouteRequired()).willReturn(BY_SET_DATE);
            given(caseData.getRespondToClaimAdmitPartLRspec()).willReturn(respondToClaimAdmitPartLRspec);
            given(respondToClaimAdmitPartLRspec.getWhenWillThisAmountBePaid()).willReturn(DEFENDANT_REPAYMENT_DATE);
            given(caseData.getRespondToAdmittedClaimOwingAmountPounds()).willReturn(UNAFFORDABLE_CLAIM_TOTAL);
            given(claimantProposedPlan.getRepaymentByDate()).willReturn(CLAIMANT_PROPOSED_DATE_BEFORE_DEFENDANT_DATE);

            //When
            RepaymentDecisionType decision = repaymentPlanDecisionCalculator.calculateRepaymentDecision(
                caseData,
                claimantProposedPlan
            );
            //Then
            assertThat(decision.isInFavourOfDefendant()).isTrue();
        }
    }

    @Nested
    class DecisionCalculationOnPayByInstalments {

        @Mock
        private RepaymentPlanLRspec claimantRepaymentPlan;

        @BeforeEach
        void setUp() {
            given(claimantProposedPlan.hasProposedPayByInstallments()).willReturn(true);
        }

        @Test
        void shouldAcceptClaimantProposal_whenDefendantCanAffordIt() {
            //Given
            given(claimantProposedPlan.getCalculatedPaymentPerMonthFromRepaymentPlan()).willReturn(
                AFFORDABLE_CLAIM_TOTAL.doubleValue());
            //When
            RepaymentDecisionType decision = repaymentPlanDecisionCalculator.calculateRepaymentDecision(
                caseData,
                claimantProposedPlan
            );
            //Then
            assertThat(decision.isInFavourOfDefendant()).isFalse();
        }

        @Test
        void shouldDeclineClaimantProposal_whenDefendantCantAffordIt() {
            //Given
            given(claimantProposedPlan.getCalculatedPaymentPerMonthFromRepaymentPlan()).willReturn(
                UNAFFORDABLE_CLAIM_TOTAL.doubleValue());
            //When
            RepaymentDecisionType decision = repaymentPlanDecisionCalculator.calculateRepaymentDecision(
                caseData,
                claimantProposedPlan
            );
            //Then
            assertThat(decision.isInFavourOfDefendant()).isTrue();
        }
    }
}
