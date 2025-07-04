package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.util.HashMap;

@Component
public class HearingFeeRemissionParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (caseData.getHearingRemissionAmount() != null) {
            params.put(
                "hearingFeeRemissionAmount",
                "£" + this.removeDoubleZeros(MonetaryConversions.penniesToPounds(
                    caseData.getHearingRemissionAmount()).toPlainString())
            );
        }
    }
}
