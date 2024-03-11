package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class DashboardNotificationsParamsMapper {

    public Map<String, Object> mapCaseDataToParams(CaseData caseData) {

        Map<String, Object> params = new HashMap<>();
        params.put("ccdCaseReference", caseData.getCcdCaseReference());
        params.put("defaultRespondTime", "4pm");
        params.put("respondent1PartyName", caseData.getRespondent1().getPartyName());

        if (nonNull(caseData.getClaimFee())) {
            params.put(
                "claimFee",
                "£" + MonetaryConversions.penniesToPounds(caseData.getClaimFee().getCalculatedAmountInPence())
                    .stripTrailingZeros().toPlainString()
            );
        }

        if (nonNull(caseData.getRespondent1ResponseDeadline())) {
            params.put("respondent1ResponseDeadline", DateUtils.formatDate(caseData.getRespondent1ResponseDeadline()));
        }

        if (caseData.getHwfFeeType() != null) {
            params.put("typeOfFee", caseData.getHwfFeeType().getLabel());
        }

        if (caseData.getHwfFeeType() != null) {
            params.put("typeOfFee", caseData.getHwfFeeType().getLabel());
        }

        params.put("claimSettledAmount", getClaimSettledAmount(caseData));
        params.put("claimSettledDate", getClaimSettleDate(caseData));
        if (nonNull(caseData.getRespondent1RespondToSettlementAgreementDeadline())) {
            params.put("respondent1SettlementAgreementDeadline_En", DateUtils.formatDate(caseData.getRespondent1RespondToSettlementAgreementDeadline()));
            params.put("respondent1SettlementAgreementDeadline_Cy", DateUtils.formatDate(caseData.getRespondent1RespondToSettlementAgreementDeadline()));
            params.put("claimantSettlementAgreement", "accepted");
        }
        return params;
    }

    private String getClaimSettledAmount(CaseData caseData) {
        return Optional.ofNullable(getRespondToClaim(caseData)).map(RespondToClaim::getHowMuchWasPaid).map(
            MonetaryConversions::penniesToPounds).map(
            BigDecimal::stripTrailingZeros).map(amount -> amount.setScale(2)).map(BigDecimal::toPlainString).map(amount -> "£" + amount).orElse(
            null);
    }

    private String getClaimSettleDate(CaseData caseData) {
        return Optional.ofNullable(getRespondToClaim(caseData)).map(RespondToClaim::getWhenWasThisAmountPaid).map(
            DateUtils::formatDate).orElse(null);
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
}
