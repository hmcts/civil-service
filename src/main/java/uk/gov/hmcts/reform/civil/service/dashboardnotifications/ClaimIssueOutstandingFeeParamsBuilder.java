package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;

@Component
public class ClaimIssueOutstandingFeeParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (caseData.getOutstandingFeeInPounds() != null) {
            params.put(
                "claimIssueOutStandingAmount",
                "£" + this.removeDoubleZeros(caseData.getOutstandingFeeInPounds().toPlainString())
            );
        }
    }
}
