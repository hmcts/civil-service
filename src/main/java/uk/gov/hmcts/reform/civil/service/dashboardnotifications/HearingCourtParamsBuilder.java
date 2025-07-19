package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;

import static java.util.Objects.nonNull;

@Component
public class HearingCourtParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        if (nonNull(caseData.getHearingLocation())) {
            params.put("hearingCourtEn", caseData.getHearingLocationCourtName());
            params.put("hearingCourtCy", caseData.getHearingLocationCourtName());
        }
    }
}
