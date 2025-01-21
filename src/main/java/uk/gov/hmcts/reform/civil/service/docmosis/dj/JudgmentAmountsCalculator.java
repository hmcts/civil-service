package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.helpers.judgmentsonline.JudgmentsOnlineHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.utils.DefaultJudgmentUtils;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;

import static uk.gov.hmcts.reform.civil.utils.DefaultJudgmentUtils.calculateFixedCosts;

@Component
@RequiredArgsConstructor
public class JudgmentAmountsCalculator {

    private final InterestCalculator interestCalculator;

    public BigDecimal getClaimFee(CaseData caseData) {
        Fee claimfee = caseData.getClaimFee();
        BigDecimal claimFeePounds = MonetaryConversions.penniesToPounds(claimfee.getCalculatedAmountInPence());

        if (caseData.isHelpWithFees()
            && caseData.getOutstandingFeeInPounds() != null) {
            claimFeePounds = caseData.getOutstandingFeeInPounds();
        }
        if (caseData.getPaymentConfirmationDecisionSpec() == YesOrNo.YES
            && caseData.getFixedCosts() == null) {
            claimFeePounds = claimFeePounds.add(calculateFixedCosts(caseData));
        } else if (caseData.getFixedCosts() != null) {
            // if new mandatory fixed costs question was answered at claim issue
            if (YesOrNo.YES.equals(caseData.getClaimFixedCostsOnEntryDJ())) {
                // if new fixed costs was chosen in DJ
                claimFeePounds = claimFeePounds.add(DefaultJudgmentUtils.calculateFixedCostsOnEntry(
                    caseData, getJudgmentAmount(caseData)));
            } else if (YesOrNo.YES.equals(caseData.getFixedCosts().getClaimFixedCosts())) {
                // only claimed new fixed costs at claim issue
                claimFeePounds = claimFeePounds.add(MonetaryConversions.penniesToPounds(
                    BigDecimal.valueOf(Integer.parseInt(
                        caseData.getFixedCosts().getFixedCostAmount()))));
            }
        }
        return claimFeePounds.equals(BigDecimal.ZERO) ? BigDecimal.ZERO : claimFeePounds.setScale(2);
    }

    public BigDecimal getDebtAmount(CaseData caseData) {
        BigDecimal interest = interestCalculator.calculateInterest(caseData);
        var subTotal = caseData.getTotalClaimAmount()
            .add(interest);
        subTotal = subTotal.subtract(getPartialPayment(caseData));

        return subTotal;
    }

    @NotNull
    public  BigDecimal getJudgmentAmount(CaseData caseData) {
        BigDecimal judgmentAmount = calculateJudgmentAmountForFixedCosts(caseData)
            .add(JudgmentsOnlineHelper.getFixedCostsOnCommencement(caseData)).add(getClaimFeePounds(caseData, caseData.getClaimFee()));
        return judgmentAmount;
    }

    private  BigDecimal calculateJudgmentAmountForFixedCosts(CaseData caseData) {
        BigDecimal interest = interestCalculator.calculateInterest(caseData);

        BigDecimal subTotal = caseData.getTotalClaimAmount().add(interest);
        BigDecimal partialPaymentPounds = getPartialPayment(caseData);
        return subTotal.subtract(partialPaymentPounds);
    }

    private BigDecimal getClaimFeePounds(CaseData caseData, Fee claimfee) {
        BigDecimal claimFeePounds;
        if (caseData.getOutstandingFeeInPounds() != null) {
            claimFeePounds = caseData.getOutstandingFeeInPounds();
        } else {
            claimFeePounds = MonetaryConversions.penniesToPounds(claimfee.getCalculatedAmountInPence());
        }
        return claimFeePounds;
    }

    public BigDecimal getPartialPayment(CaseData caseData) {

        BigDecimal partialPaymentPounds = new BigDecimal(0);
        //Check if partial payment was selected by user, and assign value if so.
        if (caseData.getPartialPaymentAmount() != null) {
            var partialPaymentPennies = new BigDecimal(caseData.getPartialPaymentAmount());
            partialPaymentPounds = MonetaryConversions.penniesToPounds(partialPaymentPennies);
        }
        return partialPaymentPounds;
    }
}
