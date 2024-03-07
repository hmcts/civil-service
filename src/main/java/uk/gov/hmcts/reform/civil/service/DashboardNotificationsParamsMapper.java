package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.utils.AmountFormatter;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

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
        params.put("defendantName", caseData.getRespondent1().getPartyName());

        if (nonNull(caseData.getClaimFee())) {
            params.put(
                "claimFee",
                "£" + MonetaryConversions.penniesToPounds(caseData.getClaimFee().getCalculatedAmountInPence())
                    .stripTrailingZeros().toPlainString()
            );
        }

        if (nonNull(caseData.getRespondent1ResponseDeadline())) {
            params.put("responseDeadline", DateUtils.formatDate(caseData.getRespondent1ResponseDeadline()));
        }
        setClaimSettledAmountAndDate(params, caseData);
        return params;
    }

    private void setClaimSettledAmountAndDate(Map<String, Object> params, CaseData caseData) {
        RespondToClaim respondToClaim = null;
        if (caseData.getRespondent1ClaimResponseTypeForSpec() ==  RespondentResponseTypeSpec.FULL_DEFENCE) {
            respondToClaim = caseData.getRespondToClaim();
        } else if(caseData.getRespondent1ClaimResponseTypeForSpec() ==  RespondentResponseTypeSpec.PART_ADMISSION) {
            respondToClaim = caseData.getRespondToAdmittedClaim();
        }
        params.put("claimSettledAmount", Optional.ofNullable(respondToClaim).map(RespondToClaim::getHowMuchWasPaid).map(AmountFormatter::formatAmount).orElse(null));
        params.put("claimSettledDate", Optional.ofNullable(respondToClaim).map(RespondToClaim::getWhenWasThisAmountPaid).map(DateUtils::formatDate).orElse(null));
    }
}
