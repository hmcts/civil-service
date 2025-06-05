package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_DAY;

@Component
public class Applicant1ClaimSettledParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        LocalDate claimSettleDate = caseData.getApplicant1ClaimSettleDate();
        if (nonNull(claimSettleDate)) {
            params.put("applicant1ClaimSettledObjectionsDeadline",
                claimSettleDate.plusDays(CLAIM_SETTLED_OBJECTION_DEADLINE_DAYS).atTime(END_OF_DAY));
            params.put("applicant1ClaimSettledDateEn", DateUtils.formatDate(claimSettleDate));
            params.put("applicant1ClaimSettledDateCy", DateUtils.formatDateInWelsh(claimSettleDate, false));
        }
    }

}
