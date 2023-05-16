package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;

@Service
@RequiredArgsConstructor
public class JudgementService {

    private static final String JUDGEMENT_BY_COURT = "The Judgement request will be reviewed by the court,"
        + " this case will proceed offline, you will receive any further updates by post.";
    private static final String JUDGEMENT_ORDER = "The judgment will order the defendant to pay £%s , including the claim fee and interest, if applicable, as shown:";
    private final FeatureToggleService featureToggleService;

    public CCJPaymentDetails buildJudgmentAmountSummaryDetails(CaseData caseData) {
        return CCJPaymentDetails.builder()
            .ccjJudgmentAmountClaimAmount(ccjJudgmentClaimAmount(caseData))
            .ccjJudgmentAmountClaimFee(ccjJudgmentClaimFee(caseData))
            .ccjJudgmentSummarySubtotalAmount(ccjJudgementSubTotal(caseData))
            .ccjJudgmentTotalStillOwed(ccjJudgmentFinalTotal(caseData))
            .ccjJudgmentAmountInterestToDate(ccjJudgmentInterest(caseData))
            .ccjPaymentPaidSomeAmountInPounds(ccjJudgmentPaidAmount(caseData))
            .ccjJudgmentFixedCostAmount(ccjJudgmentFixedCost(caseData))
            .ccjJudgmentStatement(ccjJudgmentStatement(caseData))
            .build();
    }

    public List<String> validateAmountPaid(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseData.isPaidSomeAmountMoreThanClaimAmount()) {
            errors.add("The amount paid must be less than the full claim amount.");
        }
        return errors;
    }

    private BigDecimal ccjJudgmentClaimAmount(CaseData caseData) {
        BigDecimal claimAmount = caseData.getTotalClaimAmount();
        if (caseData.isAcceptDefendantPaymentPlanForPartAdmitYes()) {
            claimAmount = caseData.getRespondToAdmittedClaimOwingAmountPounds();
        }
        return claimAmount;
    }

    private BigDecimal ccjJudgmentClaimFee(CaseData caseData) {
        return MonetaryConversions.penniesToPounds(caseData.getClaimFee().getCalculatedAmountInPence());
    }

    private BigDecimal ccjJudgmentPaidAmount(CaseData caseData) {
        return (caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeOption() == YesOrNo.YES)
            ? MonetaryConversions.penniesToPounds(caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeAmount()) : ZERO;
    }

    private BigDecimal ccjJudgmentFixedCost(CaseData caseData) {
        return caseData.getUpFixedCostAmount(ccjJudgmentClaimAmount(caseData));
    }

    private BigDecimal ccjJudgmentInterest(CaseData caseData) {
        return caseData.getTotalInterest();
    }

    private BigDecimal ccjJudgementSubTotal(CaseData caseData) {
        return ccjJudgmentClaimAmount(caseData)
            .add(ccjJudgmentClaimFee(caseData))
            .add(caseData.getTotalInterest())
            .add(ccjJudgmentFixedCost(caseData));
    }

    private BigDecimal ccjJudgmentFinalTotal(CaseData caseData) {
        return ccjJudgementSubTotal(caseData)
            .subtract(ccjJudgmentPaidAmount(caseData));
    }

    private String ccjJudgmentStatement(CaseData caseData) {
        if (caseData.isLRvLipOneVOne()
            && featureToggleService.isPinInPostEnabled()) {
            return JUDGEMENT_BY_COURT;
        } else {
            return String.format(JUDGEMENT_ORDER, ccjJudgementSubTotal(caseData));
        }
    }
}
