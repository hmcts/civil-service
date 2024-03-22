package uk.gov.hmcts.reform.civil.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.utils.AmountFormatter.formatAmount;
import static uk.gov.hmcts.reform.civil.utils.ClaimantResponseUtils.getDefendantAdmittedAmount;

@Service
public class DashboardNotificationsParamsMapper {

    public static final String CLAIMANT1_ACCEPTED_REPAYMENT_PLAN = "accepted";
    public static final String CLAIMANT1_REJECTED_REPAYMENT_PLAN = "rejected";

    public Map<String, Object> mapCaseDataToParams(CaseData caseData) {

        /**
         * defendantAdmittedAmount <amount> the amount the defendant admitted to owing (inputted during defendant response). For this scenario, it will be the full amount. (if amount is not in whole pounds, then display with a decimal place in pounds and pence. If the amount is in whole pounds, then do not display decimal)
         * <date> the date the defendant inputted when completing during defendant response. (display in following format 'Date Month Year' eg. 22 June 2024) (Qu - 'When did you pay this amount')
         * "defaultRespondTime" <time> display the time the claimant must submit their intention to proceed by
         * <deadline> display the deadline date by which claimant must submit their intention to proceed by  (display in following format 'Date Month Year' eg. 22 June 2024)

         howMuchWasPaid
         whenWasThisAmountPaid
         */

        Map<String, Object> params = new HashMap<>();
        params.put("ccdCaseReference", caseData.getCcdCaseReference());
        params.put("defaultRespondTime", "4pm");
        params.put("respondent1PartyName", caseData.getRespondent1().getPartyName());
        params.put("applicant1PartyName", caseData.getApplicant1().getPartyName());

        if (nonNull(caseData.getApplicant1ResponseDeadline())) {
            LocalDateTime applicant1ResponseDeadline = caseData.getApplicant1ResponseDeadline();
            params.put("applicant1ResponseDeadlineEn", DateUtils.formatDate(applicant1ResponseDeadline));
            params.put("applicant1ResponseDeadlineCy", DateUtils.formatDate(applicant1ResponseDeadline));
        }

        if (nonNull(getDefendantAdmittedAmount(caseData))) {
            params.put(
                "defendantAdmittedAmount",
                "£" + this.removeDoubleZeros(formatAmount(getDefendantAdmittedAmount(caseData)))
            );
        }
        if (nonNull(caseData.getRespondToClaimAdmitPartLRspec())) {
            LocalDate whenWillThisAmountBePaid = caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid();
            params.put("respondent1AdmittedAmountPaymentDeadlineEn", DateUtils.formatDate(whenWillThisAmountBePaid));
            params.put("respondent1AdmittedAmountPaymentDeadlineCy", DateUtils.formatDate(whenWillThisAmountBePaid));
        }
        if (nonNull(caseData.getClaimFee())) {
            params.put(
                "claimFee",
                "£" + this.removeDoubleZeros(caseData.getClaimFee().toPounds().toPlainString())
            );
        }
        if (nonNull(caseData.getRespondent1ResponseDeadline())) {
            LocalDate responseDeadline = caseData.getRespondent1ResponseDeadline().toLocalDate();
            params.put("respondent1ResponseDeadlineEn", DateUtils.formatDate(responseDeadline));
            params.put("respondent1ResponseDeadlineCy", DateUtils.formatDate(responseDeadline));
        }

        if (caseData.getClaimIssueRemissionAmount() != null) {
            params.put(
                "claimIssueRemissionAmount",
                "£" + this.removeDoubleZeros(MonetaryConversions
                                                 .penniesToPounds(caseData.getClaimIssueRemissionAmount()).toPlainString())
            );
        }
        if (caseData.getOutstandingFeeInPounds() != null) {
            params.put(
                "claimIssueOutStandingAmount",
                "£" + this.removeDoubleZeros(caseData.getOutstandingFeeInPounds().toPlainString())
            );
        }
        if (caseData.getHwfFeeType() != null) {
            params.put("typeOfFee", caseData.getHwfFeeType().getLabel());
        }

        getAlreadyPaidAmount(caseData).map(amount -> params.put("admissionPaidAmount", amount));

        getClaimSettledAmount(caseData).map(amount -> params.put("claimSettledAmount", amount));

        getClaimSettleDate(caseData).map(date -> {
            params.put("claimSettledDateEn", date);
            params.put("claimSettledDateCy", date);
            return Optional.of(date);
        });

        getRespondToSettlementAgreementDeadline(caseData).map(date -> {
            params.put("respondent1SettlementAgreementDeadlineEn", date);
            params.put("respondent1SettlementAgreementDeadlineCy", date);
            params.put("claimantSettlementAgreement", getClaimantRepaymentPlanDecision(caseData));
            return Optional.of(date);
        });

        return params;
    }

    private Optional<String> getClaimSettledAmount(CaseData caseData) {
        return Optional.ofNullable(getRespondToClaim(caseData))
            .map(RespondToClaim::getHowMuchWasPaid)
            .map(MonetaryConversions::penniesToPounds)
            .map(amount -> amount.setScale(2))
            .map(BigDecimal::toPlainString)
            .map(this::removeDoubleZeros)
            .map(amount -> "£" + amount);
    }

    private String removeDoubleZeros(String input) {
        return input.replace(".00", "");
    }

    private Optional<String> getClaimSettleDate(CaseData caseData) {
        return Optional.ofNullable(getRespondToClaim(caseData))
            .map(RespondToClaim::getWhenWasThisAmountPaid)
            .map(DateUtils::formatDate);
    }

    private RespondToClaim getRespondToClaim(CaseData caseData) {
        RespondToClaim respondToClaim = null;
        if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.FULL_DEFENCE) {
            respondToClaim = caseData.getRespondToClaim();
        } else if (caseData.getRespondent1ClaimResponseTypeForSpec() == RespondentResponseTypeSpec.PART_ADMISSION) {
            respondToClaim = caseData.getRespondToAdmittedClaim();
        }

        return respondToClaim;
    }

    private Optional<String> getRespondToSettlementAgreementDeadline(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1RespondToSettlementAgreementDeadline())
            .map(LocalDateTime::toLocalDate)
            .map(DateUtils::formatDate);
    }

    private Optional<String> getAlreadyPaidAmount(CaseData caseData) {
        return Optional.ofNullable(getRespondToClaim(caseData)).map(RespondToClaim::getHowMuchWasPaid).map(
            MonetaryConversions::penniesToPounds).map(
            BigDecimal::stripTrailingZeros)
            .map(amount -> amount.setScale(2))
            .map(BigDecimal::toPlainString)
            .map(amount -> "£" + amount);
    }

    private String getClaimantRepaymentPlanDecision(CaseData caseData) {
        if (caseData.hasApplicantAcceptedRepaymentPlan()) {
            return CLAIMANT1_ACCEPTED_REPAYMENT_PLAN;
        }
        return CLAIMANT1_REJECTED_REPAYMENT_PLAN;
    }
}
