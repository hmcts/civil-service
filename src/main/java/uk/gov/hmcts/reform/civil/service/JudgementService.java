package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;

@Service
@RequiredArgsConstructor
public class JudgementService {

    private static final String JUDGEMENT_BY_COURT = "The Judgement request will be reviewed by the court,"
        + " this case will proceed offline, you will receive any further updates by post.";
    private static final String JUDGEMENT_ORDER = "The judgment will order the defendant to pay £%s , including the claim fee and interest, if applicable, as shown:";
    private final FeatureToggleService featureToggleService;

    public CCJPaymentDetails buildJudgmentAmountSummaryDetails(CaseData caseData) {
        BigDecimal claimAmount = caseData.getTotalClaimAmount();
        MultiPartyScenario multiPartyScenario = getMultiPartyScenario(caseData);

        if (caseData.isAcceptDefendantPaymentPlanForPartAdmitYes()) {
            claimAmount = caseData.getRespondToAdmittedClaimOwingAmountPounds();
        }
        BigDecimal claimFee =  MonetaryConversions.penniesToPounds(caseData.getClaimFee().getCalculatedAmountInPence());
        BigDecimal paidAmount = (caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeOption() == YesOrNo.YES)
            ? MonetaryConversions.penniesToPounds(caseData.getCcjPaymentDetails().getCcjPaymentPaidSomeAmount()) : ZERO;
        BigDecimal fixedCost = caseData.getUpFixedCostAmount(claimAmount);
        BigDecimal subTotal =  claimAmount.add(claimFee).add(caseData.getTotalInterest()).add(fixedCost);
        BigDecimal finalTotal = subTotal.subtract(paidAmount);
        String ccjJudgmentStatement;
        if (caseData.isLRvLipOneVOne(multiPartyScenario)
            && featureToggleService.isPinInPostEnabled()) {
            ccjJudgmentStatement = JUDGEMENT_BY_COURT;
        } else {
            ccjJudgmentStatement = String.format(JUDGEMENT_ORDER, subTotal);
        }

        CCJPaymentDetails ccjPaymentDetails = CCJPaymentDetails.builder()
            .ccjJudgmentAmountClaimAmount(claimAmount)
            .ccjJudgmentAmountClaimFee(claimFee)
            .ccjJudgmentSummarySubtotalAmount(subTotal)
            .ccjJudgmentTotalStillOwed(finalTotal)
            .ccjJudgmentAmountInterestToDate(caseData.getTotalInterest())
            .ccjPaymentPaidSomeAmountInPounds(paidAmount)
            .ccjJudgmentFixedCostAmount(fixedCost)
            .ccjJudgmentStatement(ccjJudgmentStatement)
            .build();
        return ccjPaymentDetails;
    }

    public List<String> validateAmountPaid(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        if (caseData.isPaidSomeAmountMoreThanClaimAmount()) {
            errors.add("The amount paid must be less than the full claim amount.");
        }
        return errors;
    }
}
