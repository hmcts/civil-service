package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardNotificationsParamsMapper {

    public Map<String, Object> mapCaseDataToParams(CaseData caseData){
        LocalDate currentDate = LocalDate.now();
        long daysDifference = ChronoUnit.DAYS.between(caseData.getRespondent1ResponseDeadline(), currentDate);

        Map<String, Object> params = new HashMap<>();
        params.put("ccdCaseReference", caseData.getCcdCaseReference());
        params.put("defaultRespondTime", "4pm");
        params.put("responseDeadline", DateUtils.formatDate(caseData.getRespondent1ResponseDeadline()));
        params.put("daysLeftToRespond", daysDifference);

        return params;
    }
}
