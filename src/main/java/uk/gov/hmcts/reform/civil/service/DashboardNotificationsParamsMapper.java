package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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
        if (caseData.getClaimIssueRemissionAmount() != null) {
            params.put(
                "claimIssueRemissionAmount",
                MonetaryConversions.penniesToPounds(caseData.getClaimIssueRemissionAmount()).stripTrailingZeros()
                    .toPlainString()
            );
        }
        if (caseData.getOutstandingFeeInPounds() != null) {
            params.put(
                "claimIssueOutStandingAmount",
                caseData.getOutstandingFeeInPounds().stripTrailingZeros().toPlainString()
            );
        }
        params.put("claimIssuePaymentDueDate", DateUtils.formatDate(LocalDateTime.now()));
        return params;
    }
}
