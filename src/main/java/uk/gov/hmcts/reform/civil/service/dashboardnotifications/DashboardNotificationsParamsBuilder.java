package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;

public abstract class DashboardNotificationsParamsBuilder {

    public static final String EN = "EN";
    public static final String WELSH = "WELSH";
    public static final String CLAIMANT1_ACCEPTED_REPAYMENT_PLAN = "accepted";
    public static final String CLAIMANT1_REJECTED_REPAYMENT_PLAN = "rejected";
    public static final String CLAIMANT1_ACCEPTED_REPAYMENT_PLAN_WELSH = "derbyn";
    public static final String CLAIMANT1_REJECTED_REPAYMENT_PLAN_WELSH = "gwrthod";

    public static final int CLAIM_SETTLED_OBJECTION_DEADLINE_DAYS = 19;

    public static final LocalTime END_OF_BUSINESS_DAY = LocalTime.of(16, 0, 0);
    public static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59);

    public abstract void addParams(CaseData caseData, HashMap<String, Object> params);

    protected String getStringPaymentMessageInWelsh(JudgmentInstalmentDetails instalmentDetails) {
        PaymentFrequency paymentFrequency = instalmentDetails.getPaymentFrequency();
        String amount = instalmentDetails.getAmount();
        BigDecimal convertedAmount = MonetaryConversions.penniesToPounds(new BigDecimal(amount));

        String message = switch (paymentFrequency) {
            case WEEKLY -> "mewn rhandaliadau wythnosol o £" + convertedAmount;
            case EVERY_TWO_WEEKS -> "mewn rhandaliadau bob pythefnos o £" + convertedAmount;
            case MONTHLY -> "mewn rhandaliadau misol o £" + convertedAmount;
        };

        return message + ". Bydd y taliad cyntaf yn ddyledus ar " + DateUtils.formatDateInWelsh(instalmentDetails.getStartDate(), false);
    }

    protected String getStringPaymentMessage(JudgmentInstalmentDetails instalmentDetails) {
        PaymentFrequency paymentFrequency = instalmentDetails.getPaymentFrequency();
        String amount = instalmentDetails.getAmount();

        return "in " + getStringPaymentFrequency(paymentFrequency) + " instalments of £"
            + MonetaryConversions.penniesToPounds(new BigDecimal(amount))
            + ". The first payment is due on " + DateUtils.formatDate(instalmentDetails.getStartDate());
    }

    private String getStringPaymentFrequency(PaymentFrequency paymentFrequency) {
        return switch (paymentFrequency) {
            case WEEKLY -> "weekly";
            case EVERY_TWO_WEEKS -> "biweekly";
            case MONTHLY -> "monthly";
        };
    }

    protected String removeDoubleZeros(String input) {
        return input.replace(".00", "");
    }

    protected RespondToClaim getRespondToClaim(CaseData caseData) {
        RespondToClaim respondToClaim = null;
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE) {
            respondToClaim = caseData.getRespondToClaim();
        } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            respondToClaim = caseData.getRespondToAdmittedClaim();
        }

        return respondToClaim;
    }

    protected String getClaimantRepaymentPlanDecision(CaseData caseData) {
        if (caseData.hasApplicantAcceptedRepaymentPlan()) {
            return CLAIMANT1_ACCEPTED_REPAYMENT_PLAN;
        }
        return CLAIMANT1_REJECTED_REPAYMENT_PLAN;
    }

    protected String getClaimantRepaymentPlanDecisionCy(CaseData caseData) {
        if (caseData.hasApplicantAcceptedRepaymentPlan()) {
            return CLAIMANT1_ACCEPTED_REPAYMENT_PLAN_WELSH;
        }
        return CLAIMANT1_REJECTED_REPAYMENT_PLAN_WELSH;
    }
}
