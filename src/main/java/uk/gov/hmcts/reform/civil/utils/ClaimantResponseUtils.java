package uk.gov.hmcts.reform.civil.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static java.util.Objects.isNull;

@Component
@RequiredArgsConstructor
public class ClaimantResponseUtils {

    private final InterestCalculator interestCalculator;

    public String getClaimantRepaymentType(CaseData caseData) {
        PaymentType claimantRepaymentOption = caseData.getApplicant1RepaymentOptionForDefendantSpec();
        if (claimantRepaymentOption == null) {
            return "No payment type selected";
        }
        if (claimantRepaymentOption == PaymentType.REPAYMENT_PLAN) {
            return "By instalments";
        } else {
            return claimantRepaymentOption.getDisplayedValue();
        }
    }

    public String getDefendantRepaymentOption(CaseData caseData) {
        RespondentResponsePartAdmissionPaymentTimeLRspec defendantRepaymentOption = caseData.getDefenceAdmitPartPaymentTimeRouteRequired();
        if (defendantRepaymentOption == null) {
            return "No payment type selected";
        }

        if (defendantRepaymentOption == RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN) {
            return "By instalments";
        } else {
            return defendantRepaymentOption.getDisplayedValue();
        }
    }

    public LocalDate getClaimantFinalRepaymentDate(CaseData caseData) {
        BigDecimal paymentAmount = caseData.getApplicant1SuggestInstalmentsPaymentAmountForDefendantSpec();
        LocalDate firstRepaymentDate = caseData.getApplicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec();
        PaymentFrequencyClaimantResponseLRspec repaymentFrequency = caseData.getApplicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec();

        BigDecimal claimantTotalAmount = caseData.getTotalClaimAmount();
        if (isNull(firstRepaymentDate) || isNull(paymentAmount) || isNull(repaymentFrequency)) {
            return null;
        }
        long numberOfInstallmentsAfterFirst = getNumberOfInstallmentsAfterFirst(claimantTotalAmount, paymentAmount);

        return switch (repaymentFrequency) {
            case ONCE_ONE_WEEK -> firstRepaymentDate.plusWeeks(numberOfInstallmentsAfterFirst);
            case ONCE_TWO_WEEKS -> firstRepaymentDate.plusWeeks(2 * numberOfInstallmentsAfterFirst);
            default -> firstRepaymentDate.plusMonths(numberOfInstallmentsAfterFirst);
        };
    }

    public LocalDate getDefendantFinalRepaymentDate(CaseData caseData) {
        RepaymentPlanLRspec repaymentPlanLRspec = caseData.getRespondent1RepaymentPlan();
        if (isNull(repaymentPlanLRspec)) {
            return null;
        }

        BigDecimal paymentAmountInPence = repaymentPlanLRspec.getPaymentAmount();
        BigDecimal paymentAmount = MonetaryConversions.penniesToPounds(paymentAmountInPence);
        LocalDate firstRepaymentDate = repaymentPlanLRspec.getFirstRepaymentDate();
        PaymentFrequencyLRspec repaymentFrequency = repaymentPlanLRspec.getRepaymentFrequency();

        if (isNull(firstRepaymentDate) || isNull(paymentAmount) || isNull(repaymentFrequency)) {
            return null;
        }

        BigDecimal defendantAdmittedAmount = getDefendantAdmittedAmount(caseData);
        long numberOfInstallmentsAfterFirst = getNumberOfInstallmentsAfterFirst(defendantAdmittedAmount, paymentAmount);
        return switch (repaymentFrequency) {
            case ONCE_ONE_WEEK -> firstRepaymentDate.plusWeeks(numberOfInstallmentsAfterFirst);
            case ONCE_TWO_WEEKS -> firstRepaymentDate.plusWeeks(2 * numberOfInstallmentsAfterFirst);
            default -> firstRepaymentDate.plusMonths(numberOfInstallmentsAfterFirst);
        };
    }

    private long getNumberOfInstallmentsAfterFirst(BigDecimal totalAmount, BigDecimal paymentAmount) {
        return totalAmount.divide(paymentAmount, 0, RoundingMode.CEILING).longValue() - 1;
    }

    public BigDecimal getDefendantAdmittedAmount(CaseData caseData) {
        BigDecimal interest = interestCalculator.calculateInterest(caseData);
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_ADMISSION) {
            return caseData.getTotalClaimAmount().add(getClaimFee(caseData)).add(interest);
        } else {
            return caseData.getRespondToAdmittedClaimOwingAmountPounds();
        }
    }

    private BigDecimal getClaimFee(CaseData caseData) {
        BigDecimal claimFee = MonetaryConversions.penniesToPounds(caseData.getCalculatedClaimFeeInPence());
        if (caseData.isHelpWithFees()
            && caseData.getOutstandingFeeInPounds() != null) {
            claimFee = caseData.getOutstandingFeeInPounds();
        }
        return claimFee;
    }

}
