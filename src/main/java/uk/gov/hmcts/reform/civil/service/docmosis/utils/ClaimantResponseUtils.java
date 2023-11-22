package uk.gov.hmcts.reform.civil.service.docmosis.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static java.util.Objects.isNull;

public class ClaimantResponseUtils {

    private ClaimantResponseUtils() {
        //NO-OP
    }

    public static String getRepaymentTypeField(CaseData caseData) {
        if (isNull(caseData.getApplicant1RepaymentOptionForDefendantSpec())) {
            return "No payment type selected";
        }
        return switch (caseData.getApplicant1RepaymentOptionForDefendantSpec()) {
            case IMMEDIATELY -> "Immediately";
            case SET_DATE -> "By set date";
            case REPAYMENT_PLAN -> "By installments";
        };
    }

    public static LocalDate getFinalRepaymentDate(CaseData caseData) {
        BigDecimal paymentAmount = caseData.getApplicant1SuggestInstalmentsPaymentAmountForDefendantSpec();
        LocalDate firstRepaymentDate = caseData.getApplicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec();
        PaymentFrequencyClaimantResponseLRspec repaymentFrequency = caseData.getApplicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec();

        BigDecimal totalamount = caseData.getTotalClaimAmount();
        if (firstRepaymentDate != null && paymentAmount != null && repaymentFrequency != null) {
            long installmentsAfterFirst = totalamount.divide(MonetaryConversions.penniesToPounds(paymentAmount), 0, RoundingMode.CEILING).longValue() - 1;
            return switch (repaymentFrequency) {
                case ONCE_ONE_WEEK -> firstRepaymentDate.plusWeeks(installmentsAfterFirst);
                case ONCE_TWO_WEEKS -> firstRepaymentDate.plusWeeks(2 * installmentsAfterFirst);
                default -> firstRepaymentDate.plusMonths(installmentsAfterFirst);
            };
        }
        return null;
    }

    public static String getClaimantResponseToDefendantAdmission(CaseData caseData) {
        RespondentResponseTypeSpec respondentResponseTypeSpec = caseData.getRespondent1ClaimResponseTypeForSpec();

        if (respondentResponseTypeSpec == null) {
            return "No respondent response type";
        }
        return switch (respondentResponseTypeSpec) {
            case PART_ADMISSION -> "I accept part admission";
            case FULL_ADMISSION -> "I accept full admission";
            default -> "";
        };
    }


}
