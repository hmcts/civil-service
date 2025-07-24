package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVOne;

@Service
@RequiredArgsConstructor
public class JudgementService {

    private static final String JUDGEMENT_BY_COURT = "The Judgement request will be reviewed by the court,"
        + " this case will proceed offline, you will receive any further updates by post.";
    private static final String JUDGEMENT_BY_COURT_NOT_OFFLINE = "The judgment request will be processed and a County"
        + " Court Judgment (CCJ) will be issued, you will receive any further updates by email.";
    private static final String JUDGEMENT_ORDER = "The judgment will order the defendant to pay £%s , including the claim fee and interest, if applicable, as shown:";
    private static final String JUDGEMENT_ORDER_V2 = "The judgment will order the defendant to pay £%s"
        + ", including the claim fee, any fixed costs if claimed and interest if applicable, as shown:";
    private final FeatureToggleService featureToggleService;
    private final InterestCalculator interestCalculator;

    public CCJPaymentDetails buildJudgmentAmountSummaryDetails(CaseData caseData) {
        return CCJPaymentDetails.builder()
            .ccjJudgmentAmountClaimAmount(ccjJudgmentClaimAmount(caseData))
            .ccjJudgmentAmountClaimFee(ccjJudgmentClaimFee(caseData))
            .ccjJudgmentSummarySubtotalAmount(ccjJudgementSubTotal(caseData))
            .ccjJudgmentTotalStillOwed(ccjJudgmentFinalTotal(caseData))
            .ccjJudgmentAmountInterestToDate(ccjJudgmentInterest(caseData))
            .ccjPaymentPaidSomeAmount(caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeAmount())
            .ccjPaymentPaidSomeAmountInPounds(ccjJudgmentPaidAmount(caseData))
            .ccjJudgmentFixedCostAmount(ccjJudgmentFixedCost(caseData))
            .ccjJudgmentFixedCostOption(checkFixedCostOption(caseData))
            .ccjJudgmentStatement(ccjJudgmentStatement(caseData))
            .ccjPaymentPaidSomeOption(caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeOption())
            .ccjJudgmentLipInterest(caseData.getCcjPaymentDetails().getCcjJudgmentLipInterest())
            .build();
    }

    public List<String> validateAmountPaid(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseData.isPaidSomeAmountMoreThanClaimAmount()) {
            errors.add("The amount paid must be less than the full claim amount.");
        }
        return errors;
    }

    public BigDecimal ccjJudgmentClaimAmount(CaseData caseData) {
        BigDecimal claimAmount = caseData.getTotalClaimAmount();
        if (isLrFullAdmitRepaymentPlan(caseData)
            || isLrFullAdmitPayImmediately(caseData)) {
            BigDecimal interest = interestCalculator.calculateInterest(caseData);
            claimAmount = claimAmount.add(interest);
        } else {
            if (caseData.isPartAdmitClaimSpec()) {
                claimAmount = caseData.getRespondToAdmittedClaimOwingAmountPounds();
            }
        }
        return claimAmount.setScale(2);
    }

    public BigDecimal ccjJudgmentClaimFee(CaseData caseData) {
        if (caseData.getOutstandingFeeInPounds() != null) {
            return caseData.getOutstandingFeeInPounds();
        }
        return caseData.isLipvLipOneVOne()
            ? caseData.getCcjPaymentDetails().getCcjJudgmentAmountClaimFee()
            : MonetaryConversions.penniesToPounds(caseData.getClaimFee().getCalculatedAmountInPence());
    }

    public BigDecimal ccjJudgmentPaidAmount(CaseData caseData) {
        return (caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeOption() == YesOrNo.YES)
            ? MonetaryConversions.penniesToPounds(caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeAmount()) : ZERO;
    }

    public BigDecimal ccjJudgmentFixedCost(CaseData caseData) {
        if ((isLrFullAdmitRepaymentPlan(caseData) || isLRPartAdmitRepaymentPlan(caseData) || isLrPayImmediatelyPlan(caseData))
            && nonNull(caseData.getFixedCosts())
            && YesOrNo.YES.equals(caseData.getFixedCosts().getClaimFixedCosts())) {
            BigDecimal claimIssueFixedCost = MonetaryConversions.penniesToPounds(BigDecimal.valueOf(
                Integer.parseInt(caseData.getFixedCosts().getFixedCostAmount())));
            return caseData.getUpFixedCostAmount(ccjJudgmentClaimAmount(caseData))
                .add(claimIssueFixedCost);
        }
        return caseData.getUpFixedCostAmount(ccjJudgmentClaimAmount(caseData));
    }

    public BigDecimal ccjJudgmentInterest(CaseData caseData) {
        return caseData.isLipvLipOneVOne() ? caseData.getCcjPaymentDetails().getCcjJudgmentLipInterest() :
            Optional.ofNullable(caseData.getTotalInterest()).orElse(ZERO);
    }

    public BigDecimal ccjJudgementSubTotal(CaseData caseData) {
        if (isLrFullAdmitRepaymentPlan(caseData) || isLRPartAdmitRepaymentPlan(caseData) || isLrPayImmediatelyPlan(caseData)) {
            return (ccjJudgmentClaimAmount(caseData)
                .add(ccjJudgmentClaimFee(caseData))
                .add(ccjJudgmentFixedCost(caseData))).setScale(2);
        } else {
            return (ccjJudgmentClaimAmount(caseData)
                .add(ccjJudgmentClaimFee(caseData))
                .add(ccjJudgmentInterest(caseData))
                .add(ccjJudgmentFixedCost(caseData))).setScale(2);
        }
    }

    public BigDecimal ccjJudgmentFinalTotal(CaseData caseData) {
        return ccjJudgementSubTotal(caseData)
            .subtract(ccjJudgmentPaidAmount(caseData));
    }

    private String ccjJudgmentStatement(CaseData caseData) {
        if (caseData.isLRvLipOneVOne()
            && featureToggleService.isPinInPostEnabled()) {
            boolean hasPaymentOption = caseData.isPayImmediately() || caseData.isPayByInstallment() || caseData.isPayBySetDate();
            if (featureToggleService.isLrAdmissionBulkEnabled()
                && hasPaymentOption) {
                return String.format(JUDGEMENT_ORDER_V2, ccjJudgementSubTotal(caseData).toString());
            }
            if (featureToggleService.isJudgmentOnlineLive()
                && hasPaymentOption) {
                return JUDGEMENT_BY_COURT_NOT_OFFLINE;
            }
            return JUDGEMENT_BY_COURT;
        } else {
            return String.format(
                isLrFullAdmitRepaymentPlan(caseData) || isLrPayImmediatelyPlan(caseData)
                    ? JUDGEMENT_ORDER_V2 : JUDGEMENT_ORDER, ccjJudgementSubTotal(caseData).toString());
        }
    }

    public boolean isLRAdmissionRepaymentPlan(CaseData caseData) {
        return featureToggleService.isLrAdmissionBulkEnabled()
            && !caseData.isApplicantLiP()
            && (caseData.isPayBySetDate() || caseData.isPayByInstallment());
    }

    public boolean isLrFullAdmitRepaymentPlan(CaseData caseData) {
        return isLRAdmissionRepaymentPlan(caseData)
            && caseData.isFullAdmitClaimSpec();
    }

    public boolean isLRPartAdmitRepaymentPlan(CaseData caseData) {
        return isLRAdmissionRepaymentPlan(caseData)
            && caseData.isPartAdmitClaimSpec();
    }

    public boolean isLrvLrOneVOneBulkAdmissionsEnabled(CaseData caseData) {
        return featureToggleService.isLrAdmissionBulkEnabled()
            && !caseData.isApplicantLiP()
            && isOneVOne(caseData);
    }

    public boolean isLrPayImmediatelyPlan(CaseData caseData) {
        return isLrvLrOneVOneBulkAdmissionsEnabled(caseData)
            && caseData.isPayImmediately();
    }

    public boolean isLrFullAdmitPayImmediately(CaseData caseData) {
        return isLrPayImmediatelyPlan(caseData)
            && caseData.isFullAdmitClaimSpec();
    }

    private YesOrNo checkFixedCostOption(CaseData caseData) {
        return caseData.getCcjPaymentDetails().getCcjJudgmentFixedCostOption();
    }

    public boolean isLipVLipFullAdmitSetDate(CaseData caseData) {
        return caseData.isLipvLipOneVOne() && caseData.isPayBySetDate() && caseData.isFullAdmitClaimSpec();
    }
}
