package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_DAY;

@Component
public class TrailArrangementParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (nonNull(caseData.getHearingDate())) {
            LocalDate date = caseData.getHearingDate().minusWeeks(4);
            params.put("trialArrangementDeadline", date.atTime(END_OF_DAY));
            params.put("trialArrangementDeadlineEn", DateUtils.formatDate(date));
            params.put("trialArrangementDeadlineCy", DateUtils.formatDateInWelsh(date, false));
        }
    }
}
