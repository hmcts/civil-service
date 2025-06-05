package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.util.HashMap;

@Component
public class HearingDueDateParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (caseData.getHearingDueDate() != null) {
            params.put("hearingDueDate", caseData.getHearingDueDate().atTime(END_OF_DAY));
            params.put("hearingDueDateEn", DateUtils.formatDate(caseData.getHearingDueDate()));
            params.put("hearingDueDateCy", DateUtils.formatDateInWelsh(caseData.getHearingDueDate(), false));
        }
    }
}
