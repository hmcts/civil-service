package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;

@Component
public class HwfFeeTypeParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (caseData.getHwfFeeType() != null) {
            params.put("typeOfFee", caseData.getHwfFeeType().getLabel());
        }
    }
}
