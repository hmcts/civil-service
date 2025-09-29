package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;

import static java.util.Objects.nonNull;

@Component
public class Respondent1ResponseDeadlineParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (nonNull(caseData.getRespondent1ResponseDeadline())) {
            LocalDate respondentResponseDeadline = caseData.getRespondent1ResponseDeadline().toLocalDate();
            params.put("respondent1ResponseDeadline", caseData.getRespondent1ResponseDeadline());
            params.put("respondent1ResponseDeadlineEn", DateUtils.formatDate(respondentResponseDeadline));
            params.put("respondent1ResponseDeadlineCy", DateUtils.formatDateInWelsh(respondentResponseDeadline, false));
        }
    }
}
