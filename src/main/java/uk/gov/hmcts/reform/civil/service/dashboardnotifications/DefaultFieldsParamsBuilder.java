package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;
import java.util.HashMap;

@Component
@Order(1) // Ensures this builder runs first in the chain
public class DefaultFieldsParamsBuilder extends DashboardNotificationsParamsBuilder {

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        params.put("ccdCaseReference", caseData.getCcdCaseReference());
        params.put("legacyCaseReference", caseData.getLegacyCaseReference());
        params.put("defaultRespondTime", "4pm");
        params.put("respondent1PartyName", caseData.getRespondent1().getPartyName());
        params.put("applicant1PartyName", caseData.getApplicant1().getPartyName());
        // Ensures that notifications whose template specifies this will be prioritised when sorting
        params.put("priorityNotificationDeadline", LocalDateTime.now());
    }
}
