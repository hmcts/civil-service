package uk.gov.hmcts.reform.civil.service.docmosis.utils;

import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static java.util.Objects.isNull;

public class ClaimantResponseUtils {

    private ClaimantResponseUtils() {
        //NO-OP
    }

    public static String getClaimantRepaymentType(CaseData caseData) {
        PaymentType claimantRepaymentOption = caseData.getApplicant1RepaymentOptionForDefendantSpec();
        if (claimantRepaymentOption == null) {
            return "No payment type selected";
        }
        if (claimantRepaymentOption == PaymentType.REPAYMENT_PLAN) {
            return "By installments";
        } else {
            return claimantRepaymentOption.getDisplayedValue();
        }
    }

    public static LocalDate getClaimantFinalRepaymentDate(CaseData caseData) {
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

    private static long getNumberOfInstallmentsAfterFirst(BigDecimal totalAmount, BigDecimal paymentAmount) {
        return totalAmount.divide(paymentAmount, 0, RoundingMode.CEILING).longValue() - 1;
    }
}
