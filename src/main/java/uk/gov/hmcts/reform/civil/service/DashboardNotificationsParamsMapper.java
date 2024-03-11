package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

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
        params.put("respondent1PartyName", caseData.getRespondent1().getPartyName());

        if (nonNull(caseData.getClaimFee())) {
            params.put(
                "claimFee",
                "£" + caseData.getClaimFee().toPounds().stripTrailingZeros().toPlainString()
            );
        }

        if (nonNull(caseData.getRespondent1ResponseDeadline())) {
            params.put("respondent1ResponseDeadline",
                       DateUtils.formatDate(caseData.getRespondent1ResponseDeadline().toLocalDate()));
        }

        if (nonNull(caseData.getRespondToClaimAdmitPartLRspec())
            && nonNull(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid())) {
            params.put("responseToClaimAdmitPartPaymentDeadline",
                       DateUtils.formatDate(caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid()));
        }

        if (nonNull(caseData.getTotalClaimAmount())) {
            params.put(
                "fullAdmitPayImmediatelyPaymentAmount",
                "£" + caseData.getTotalClaimAmount().stripTrailingZeros().toPlainString()
            );
        }

        if (caseData.getHwfFeeType() != null) {
            params.put("typeOfFee", caseData.getHwfFeeType().getLabel());
        }
        return params;
    }
}
