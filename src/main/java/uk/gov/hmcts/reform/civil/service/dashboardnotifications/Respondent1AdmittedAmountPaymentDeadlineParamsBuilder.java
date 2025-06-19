package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.service.DeadlinesCalculator.END_OF_DAY;

@Component
public class Respondent1AdmittedAmountPaymentDeadlineParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (nonNull(caseData.getRespondToClaimAdmitPartLRspec())) {
            LocalDate whenWillThisAmountBePaid = caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid();
            params.put("respondent1AdmittedAmountPaymentDeadline", whenWillThisAmountBePaid.atTime(END_OF_DAY));
            params.put("respondent1AdmittedAmountPaymentDeadlineEn", DateUtils.formatDate(whenWillThisAmountBePaid));
            params.put(
                "respondent1AdmittedAmountPaymentDeadlineCy",
                DateUtils.formatDateInWelsh(whenWillThisAmountBePaid, false)
            );
        }
    }
}
