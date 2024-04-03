package uk.gov.hmcts.reform.civil.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.utils.AmountFormatter.formatAmount;
import static uk.gov.hmcts.reform.civil.utils.ClaimantResponseUtils.getDefendantAdmittedAmount;

@Service
public class DashboardNotificationsParamsMapper {

    public static final String CLAIMANT1_ACCEPTED_REPAYMENT_PLAN = "accepted";
    public static final String CLAIMANT1_REJECTED_REPAYMENT_PLAN = "rejected";

    public HashMap<String, Object> mapCaseDataToParams(CaseData caseData) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("ccdCaseReference", caseData.getCcdCaseReference());
        params.put("defaultRespondTime", "4pm");
        params.put("respondent1PartyName", caseData.getRespondent1().getPartyName());
        params.put("applicant1PartyName", caseData.getApplicant1().getPartyName());

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
        if (nonNull(caseData.getApplicant1ResponseDeadline())) {
            LocalDate applicantResponseDeadline = caseData.getApplicant1ResponseDeadline().toLocalDate();
            params.put("applicant1ResponseDeadlineEn", DateUtils.formatDate(applicantResponseDeadline));
            params.put("applicant1ResponseDeadlineCy", DateUtils.formatDate(applicantResponseDeadline));
        }
        if (nonNull(caseData.getRespondent1ResponseDeadline())) {
            LocalDate respondentResponseDeadline = caseData.getRespondent1ResponseDeadline().toLocalDate();
            params.put("respondent1ResponseDeadlineEn", DateUtils.formatDate(respondentResponseDeadline));
            params.put("respondent1ResponseDeadlineCy", DateUtils.formatDate(respondentResponseDeadline));
        }

        if (caseData.getClaimIssueRemissionAmount() != null) {
            params.put(
                "claimIssueRemissionAmount",
                "£" + this.removeDoubleZeros(MonetaryConversions.penniesToPounds(
                    caseData.getClaimIssueRemissionAmount()).toPlainString())
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

        getAlreadyPaidAmount(caseData).ifPresent(amount -> params.put("admissionPaidAmount", amount));

        getClaimSettledAmount(caseData).ifPresent(amount -> params.put("claimSettledAmount", amount));

        getClaimSettleDate(caseData).ifPresent(date -> {
            params.put("claimSettledDateEn", date);
            params.put("claimSettledDateCy", date);
        });

        getRespondToSettlementAgreementDeadline(caseData).ifPresent(date -> {
            params.put("respondent1SettlementAgreementDeadlineEn", date);
            params.put("respondent1SettlementAgreementDeadlineCy", date);
            params.put("claimantSettlementAgreement", getClaimantRepaymentPlanDecision(caseData));
        });

        LocalDate claimSettleDate = caseData.getApplicant1ClaimSettleDate();
        if (nonNull(claimSettleDate)) {
            params.put("applicant1ClaimSettledDateEn", DateUtils.formatDate(claimSettleDate));
            params.put("applicant1ClaimSettledDateCy", DateUtils.formatDate(claimSettleDate));
        }

        if (nonNull(caseData.getRespondent1RepaymentPlan())) {
            getInstalmentAmount(caseData).ifPresent(amount -> params.put("instalmentAmount", amount));
            getInstalmentStartDate(caseData).ifPresent(dateEn -> params.put("instalmentStartDateEn", dateEn));
            getInstalmentStartDate(caseData).ifPresent(dateCy -> params.put("instalmentStartDateCy", dateCy));
            params.put(
                "instalmentTimePeriodEn",
                getInstalmentTimePeriod(caseData.getRespondent1RepaymentPlan().getRepaymentFrequency())
            );
            params.put(
                "instalmentTimePeriodCy",
                getInstalmentTimePeriod(caseData.getRespondent1RepaymentPlan().getRepaymentFrequency())
            );
        }

        if (nonNull(caseData.getRespondent1RepaymentPlan())) {
            params.put("installmentAmount", "£" + this.removeDoubleZeros(MonetaryConversions.penniesToPounds(
                caseData.getRespondent1RepaymentPlan().getPaymentAmount()).toPlainString()));

            params.put(
                "paymentFrequency",
                caseData.getRespondent1RepaymentPlan().getRepaymentFrequency().getDashboardLabel()
            );
            getFirstRepaymentDate(caseData).ifPresent(date -> {
                params.put("firstRepaymentDateEn", date);
                params.put("firstRepaymentDateCy", date);
            });
        }

        if (nonNull(caseData.getApplicant1ResponseDeadline())) {
            String date = DateUtils.formatDate(caseData.getApplicant1ResponseDeadline());
            params.put("applicant1ResponseDeadlineEn", date);
            params.put("applicant1ResponseDeadlineCy", date);
        }

        return params;
    }

    private Optional<String> getFirstRepaymentDate(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1RepaymentPlan())
            .map(RepaymentPlanLRspec::getFirstRepaymentDate)
            .map(DateUtils::formatDate);
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
            .map(this::removeDoubleZeros)
            .map(amount -> "£" + amount);
    }

    private String getClaimantRepaymentPlanDecision(CaseData caseData) {
        if (caseData.hasApplicantAcceptedRepaymentPlan()) {
            return CLAIMANT1_ACCEPTED_REPAYMENT_PLAN;
        }
        return CLAIMANT1_REJECTED_REPAYMENT_PLAN;
    }

    private String getInstalmentTimePeriod(PaymentFrequencyLRspec repaymentFrequency) {
        return switch (repaymentFrequency) {
            case ONCE_ONE_WEEK -> "week";
            case ONCE_TWO_WEEKS -> "2 weeks";
            case ONCE_THREE_WEEKS -> "3 weeks";
            case ONCE_FOUR_WEEKS -> "4 weeks";
            case ONCE_ONE_MONTH -> "month";
            default -> null;
        };
    }

    private Optional<String> getInstalmentStartDate(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1RepaymentPlan().getFirstRepaymentDate())
            .map(DateUtils::formatDate);
    }

    private Optional<String> getInstalmentAmount(CaseData caseData) {
        return Optional.ofNullable(caseData.getRespondent1RepaymentPlan())
            .map(RepaymentPlanLRspec::getPaymentAmount)
            .map(MonetaryConversions::penniesToPounds)
            .map(amount -> amount.setScale(2))
            .map(BigDecimal::toPlainString)
            .map(this::removeDoubleZeros)
            .map(amount -> "£" + amount);
    }
}
