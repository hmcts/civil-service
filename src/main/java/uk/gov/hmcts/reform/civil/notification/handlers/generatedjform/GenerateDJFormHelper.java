package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.BOTH_DEFENDANTS;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;

@Component
public class GenerateDJFormHelper {

    public boolean checkDefendantRequested(final CaseData caseData, boolean isRespondent1) {
        if (caseData.getDefendantDetails() != null) {
            String defendantDetailsLabel = caseData.getDefendantDetails().getValue().getLabel();
            String respondentPartyName = null;
            if (isRespondent1 && caseData.getRespondent1() != null) {
                respondentPartyName = caseData.getRespondent1().getPartyName();
                return StringUtils.equals(respondentPartyName, defendantDetailsLabel);
            } else if (caseData.getRespondent2() != null) {
                respondentPartyName = caseData.getRespondent2().getPartyName();
                return StringUtils.equals(respondentPartyName, defendantDetailsLabel);
            }
        }
        return false;
    }

    protected Map<String, String> updateRespondent2Properties(Map<String, String> properties, CaseData caseData) {
        properties.put(DEFENDANT_NAME_INTERIM, caseData.getRespondent2().getPartyName());
        return properties;
    }

    public Boolean checkIfBothDefendants(CaseData caseData) {
        return BOTH_DEFENDANTS.equals(caseData.getDefendantDetails().getValue().getLabel());
    }
}
