package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;

import static java.util.Objects.nonNull;

@Component
public class HearingFeeParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (nonNull(caseData.getHearingFee())) {
            params.put(
                "hearingFee",
                "£" + this.removeDoubleZeros(caseData.getHearingFee().toPounds().toPlainString())
            );
        }
    }
}
