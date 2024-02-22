package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardNotificationsParamsMapper {

    public Map<String, Object> mapCaseDataToParams(CaseData caseData){
        // TODO Check for notification variables
        LocalDate currentDate = LocalDate.now();
        long daysDifference = ChronoUnit.DAYS.between(caseData.getClaimDismissedDeadline(), currentDate);

        Map<String, Object> params = new HashMap<>();
        params.put("defaultRespondTime", "4pm");
        // TODO: find the correct data
        params.put("date", caseData.getClaimDismissedDeadline());
        params.put("daysLeftToRespond", daysDifference);

        return params;
    }
}
