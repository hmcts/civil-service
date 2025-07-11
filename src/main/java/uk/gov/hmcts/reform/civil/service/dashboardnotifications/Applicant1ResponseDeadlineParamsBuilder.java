package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.HashMap;

import static java.util.Objects.nonNull;

@Component
public class Applicant1ResponseDeadlineParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (nonNull(caseData.getApplicant1ResponseDeadline())) {
            LocalDateTime applicant1ResponseDeadline = caseData.getApplicant1ResponseDeadline();
            params.put("applicant1ResponseDeadline", applicant1ResponseDeadline);
            params.put("applicant1ResponseDeadlineEn", DateUtils.formatDate(applicant1ResponseDeadline));
            params.put("applicant1ResponseDeadlineCy",
                DateUtils.formatDateInWelsh(applicant1ResponseDeadline.toLocalDate(), false));
        }
    }
}
