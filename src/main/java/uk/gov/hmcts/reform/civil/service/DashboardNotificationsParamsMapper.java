package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class DashboardNotificationsParamsMapper {

    private final FeesService feesService;

    public Map<String, Object> mapCaseDataToParams(CaseData caseData) {
        // TODO Check for notification variables
        LocalDateTime currentDate = LocalDateTime.now();

        Map<String, Object> params = new HashMap<>();
        params.put("defaultRespondTime", "4pm");
        // TODO: find the correct data
        params.put("date", caseData.getClaimDismissedDeadline());

        if (nonNull(caseData.getClaimDismissedDeadline())) {
            long daysDifference = ChronoUnit.DAYS.between(caseData.getClaimDismissedDeadline(), currentDate);
            params.put("daysLeftToRespond", daysDifference);
        }

        Fee fee = feesService
            .getFeeDataByTotalClaimAmount(caseData.getTotalClaimAmount());
        params.put("claimFee", "£" + fee.toPounds().stripTrailingZeros().toPlainString());

        return params;
    }
}
