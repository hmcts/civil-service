package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardNotificationsParamsMapper {

    public Map<String, Object> mapCaseDataToParams(CaseData caseData) {

        Map<String, Object> params = new HashMap<>();

        params.put("ccdCaseReference", caseData.getCcdCaseReference());
        params.put(
            "claimFee",
            "£" + MonetaryConversions.penniesToPounds(caseData.getClaimFee().getCalculatedAmountInPence())
        );
        params.put("defaultRespondTime", "4pm");
        params.put("responseDeadline", DateUtils.formatDate(caseData.getRespondent1ResponseDeadline()));
        params.put("defendantName", caseData.getRespondent1().getPartyName());

        return params;
    }
}
