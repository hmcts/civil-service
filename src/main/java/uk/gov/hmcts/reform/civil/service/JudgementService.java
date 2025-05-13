package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgementService {

    private static final String JUDGEMENT_BY_COURT = "The Judgement request will be reviewed by the court,"
        + " this case will proceed offline, you will receive any further updates by post.";
    private static final String JUDGEMENT_BY_COURT_NOT_OFFLINE = "The judgment request will be processed and a County"
        + " Court Judgment (CCJ) will be issued, you will receive any further updates by email.";
    private static final String JUDGEMENT_ORDER = "The judgment will order the defendant to pay £%s , including the claim fee and interest, if applicable, as shown:";
    private static final String JUDGEMENT_ORDER_V2 = "The judgment will order the defendant to pay £%s which include the amounts shown:";
    private final FeatureToggleService featureToggleService;
    private final InterestCalculator interestCalculator;

    public CCJPaymentDetails buildJudgmentAmountSummaryDetails(CaseData caseData) {
        log.info("Creating payment details");
        return CCJPaymentDetails.builder()
            .ccjJudgmentAmountClaimAmount(ccjJudgmentClaimAmount(caseData))
            .ccjJudgmentAmountClaimFee(ccjJudgmentClaimFee(caseData))
            .ccjJudgmentSummarySubtotalAmount(ccjJudgementSubTotal(caseData))
            .ccjJudgmentTotalStillOwed(ccjJudgmentFinalTotal(caseData))
            .ccjJudgmentAmountInterestToDate(ccjJudgmentInterest(caseData))
            .ccjPaymentPaidSomeAmount(caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeAmount())
            .ccjPaymentPaidSomeAmountInPounds(ccjJudgmentPaidAmount(caseData))
            .ccjJudgmentFixedCostAmount(ccjJudgmentFixedCost(caseData))
            .ccjJudgmentFixedCostOption(caseData.getCcjPaymentDetails()
                                            .getCcjJudgmentFixedCostOption())
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
        if (isJOFullAdmitRepaymentPlan(caseData)) {
            BigDecimal interest = interestCalculator.calculateInterestForJO(caseData);
            log.info("Interest added to claim amount {}", interest.toString());
            claimAmount = claimAmount.add(interest);
            log.info("Claim amount {}", claimAmount.toString());
        } else {
            if (caseData.isPartAdmitClaimSpec()) {
                claimAmount = caseData.getRespondToAdmittedClaimOwingAmountPounds();
            }
        }

        return claimAmount;
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

    private BigDecimal ccjJudgmentFixedCost(CaseData caseData) {
        if (isJOFullAdmitRepaymentPlan(caseData)
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
        if (isJOFullAdmitRepaymentPlan(caseData)) {
            log.info("Adding interest from claim amount");
            return ccjJudgmentClaimAmount(caseData)
                .add(ccjJudgmentClaimFee(caseData))
                .add(ccjJudgmentFixedCost(caseData));
        } else {
            return ccjJudgmentClaimAmount(caseData)
                .add(ccjJudgmentClaimFee(caseData))
                .add(ccjJudgmentInterest(caseData))
                .add(ccjJudgmentFixedCost(caseData));
        }
    }

    public BigDecimal ccjJudgmentFinalTotal(CaseData caseData) {
        return ccjJudgementSubTotal(caseData)
            .subtract(ccjJudgmentPaidAmount(caseData));
    }

    private String ccjJudgmentStatement(CaseData caseData) {
        if (caseData.isLRvLipOneVOne()
            && featureToggleService.isPinInPostEnabled()) {
            if (featureToggleService.isJudgmentOnlineLive()
                && (caseData.isPayImmediately() || caseData.isPayByInstallment() || caseData.isPayBySetDate())) {
                return JUDGEMENT_BY_COURT_NOT_OFFLINE;
            }
            return JUDGEMENT_BY_COURT;
        } else {
            return String.format(
                isJOFullAdmitRepaymentPlan(caseData)
                    ? JUDGEMENT_ORDER_V2 : JUDGEMENT_ORDER, ccjJudgementSubTotal(caseData));
        }
    }

    private boolean isJOFullAdmitRepaymentPlan(CaseData caseData) {
        return caseData.isFullAdmitClaimSpec()
            && (caseData.isPayBySetDate() || caseData.isPayByInstallment())
            && isLRvLR(caseData);
    }

    private boolean isLRvLR(CaseData caseData) {
        return !caseData.isApplicantLiP() && !caseData.isRespondent1LiP() && !caseData.isRespondent2LiP();
    }
}
