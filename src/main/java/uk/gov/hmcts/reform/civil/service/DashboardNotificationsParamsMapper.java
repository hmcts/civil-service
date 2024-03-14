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
import static uk.gov.hmcts.reform.civil.service.docmosis.utils.ClaimantResponseUtils.getDefendantAdmittedAmount;
import static uk.gov.hmcts.reform.civil.utils.AmountFormatter.formatAmount;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Service
public class DashboardNotificationsParamsMapper {

    public Map<String, Object> mapCaseDataToParams(CaseData caseData) {

        Map<String, Object> params = new HashMap<>();
        params.put("ccdCaseReference", caseData.getCcdCaseReference());
        params.put("defaultRespondTime", "4pm");
        params.put("respondent1PartyName", caseData.getRespondent1().getPartyName());
        params.put("claimantName", getPartyNameBasedOnType(caseData.getApplicant1()));

        if (nonNull(getDefendantAdmittedAmount(caseData))) {
            params.put("defendantAdmittedAmount",
                       this.removeDoubleZeros(formatAmount(getDefendantAdmittedAmount(caseData))));
        }
        if (nonNull(caseData.getRespondToClaimAdmitPartLRspec())) {
            LocalDate whenWillThisAmountBePaid = caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid();
            params.put("defendantAdmittedAmountPaymentDeadlineEn", DateUtils.formatDate(whenWillThisAmountBePaid));
            params.put("defendantAdmittedAmountPaymentDeadlineCy", DateUtils.formatDate(whenWillThisAmountBePaid));
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
            params.put("claimIssueRemissionAmount",
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

        getClaimSettledAmount(caseData).map(amount -> params.put("claimSettledAmount", amount));

        getClaimSettleDate(caseData).map(date -> {
            params.put("claimSettledDateEn", date);
            params.put("claimSettledDateCy", date);
            return Optional.of(date);
        });

        getRespondToSettlementAgreementDeadline(caseData).map(date -> {
            params.put("respondSettlementAgreementDeadlineEn", date);
            params.put("respondSettlementAgreementDeadlineCy", date);
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
}
