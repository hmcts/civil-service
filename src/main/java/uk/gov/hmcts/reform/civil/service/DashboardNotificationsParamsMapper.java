package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class DashboardNotificationsParamsMapper {

    private final FeesService feesService;

    public Map<String, Object> mapCaseDataToParams(CaseData caseData) {
        Map<String, Object> params = new HashMap<>();

        Fee fee = feesService
            .getFeeDataByTotalClaimAmount(caseData.getTotalClaimAmount());
        params.put("claimFee", "£" + fee.toPounds().stripTrailingZeros().toPlainString());
        params.put("ccdCaseReference", caseData.getCcdCaseReference());
        params.put("defaultRespondTime", "4pm");

        if (nonNull(caseData.getRespondent1ResponseDeadline())) {
            params.put("responseDeadline", DateUtils.formatDate(caseData.getRespondent1ResponseDeadline()));
        }

        return params;
    }
}
