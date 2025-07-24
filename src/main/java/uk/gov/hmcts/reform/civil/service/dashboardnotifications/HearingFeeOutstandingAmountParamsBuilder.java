package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;

@Component
public class HearingFeeOutstandingAmountParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (caseData.getOutstandingFeeInPounds() != null) {
            params.put(
                "hearingFeeOutStandingAmount",
                "£" + this.removeDoubleZeros(caseData.getOutstandingFeeInPounds().toPlainString())
            );
        }
    }
}
