package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.service.docmosis.utils.ClaimantResponseUtils.getDefendantAdmittedAmount;
import static uk.gov.hmcts.reform.civil.utils.AmountFormatter.formatAmount;

@Service
@RequiredArgsConstructor
public class DashboardNotificationsParamsMapper {

    public Map<String, Object> mapCaseDataToParams(CaseData caseData) {

        Map<String, Object> params = new HashMap<>();
        params.put("ccdCaseReference", caseData.getCcdCaseReference());
        params.put("defaultRespondTime", "4pm");
        params.put("respondent1PartyName", caseData.getRespondent1().getPartyName());
      
        if (nonNull(getDefendantAdmittedAmount(caseData))) {
            params.put("defendantAdmittedAmount", formatAmount(getDefendantAdmittedAmount(caseData)));
        }
        if (nonNull(caseData.getRespondToClaimAdmitPartLRspec())) {
            LocalDate whenWillThisAmountBePaid = caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid();
            params.put("defendantAdmittedAmountPaymentDeadlineEn", DateUtils.formatDate(whenWillThisAmountBePaid));
            params.put("defendantAdmittedAmountPaymentDeadlineCy", DateUtils.formatDate(whenWillThisAmountBePaid));
        }
        if (nonNull(caseData.getClaimFee())) {
            params.put(
                "claimFee",
                "£" + MonetaryConversions.penniesToPounds(caseData.getClaimFee().getCalculatedAmountInPence())
                    .stripTrailingZeros().toPlainString()
            );
        }
        if (nonNull(caseData.getRespondent1ResponseDeadline())) {
            params.put("respondent1ResponseDeadline",
                       DateUtils.formatDate(caseData.getRespondent1ResponseDeadline().toLocalDate()));
        }
        if (nonNull(caseData.getRespondent1RespondToSettlementAgreementDeadline())) {
            LocalDateTime respondent1SettlementAgreementDeadline = caseData.getRespondent1RespondToSettlementAgreementDeadline();
            params.put("respondent1SettlementAgreementDeadline_En", DateUtils.formatDate(respondent1SettlementAgreementDeadline));
            params.put("respondent1SettlementAgreementDeadline_Cy", DateUtils.formatDate(respondent1SettlementAgreementDeadline));
            params.put("claimantSettlementAgreement", "accepted");
        }
        if (caseData.getClaimIssueRemissionAmount() != null) {
            params.put(
                "claimIssueRemissionAmount",
                "£" + MonetaryConversions.penniesToPounds(caseData.getClaimIssueRemissionAmount()).stripTrailingZeros()
                    .toPlainString()
            );
        }
        if (caseData.getOutstandingFeeInPounds() != null) {
            params.put(
                "claimIssueOutStandingAmount",
                "£" + caseData.getOutstandingFeeInPounds().stripTrailingZeros().toPlainString()
            );
        }
        if (caseData.getHwfFeeType() != null) {
            params.put("typeOfFee", caseData.getHwfFeeType().getLabel());
        }
        return params;
    }
}
