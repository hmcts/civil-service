package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.util.HashMap;

import static java.util.Objects.nonNull;

@Component
public class RequestForReconsiderationDeadlineParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (nonNull(caseData.getRequestForReconsiderationDeadline())) {
            params.put("requestForReconsiderationDeadline", caseData.getRequestForReconsiderationDeadline());
            params.put("requestForReconsiderationDeadlineEn",
                DateUtils.formatDate(caseData.getRequestForReconsiderationDeadline()));
            params.put("requestForReconsiderationDeadlineCy",
                DateUtils.formatDateInWelsh(caseData.getRequestForReconsiderationDeadline().toLocalDate(), false));
        }
    }
}
