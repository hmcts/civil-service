package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.BOTH_DEFENDANTS;

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

    public Boolean checkIfBothDefendants(CaseData caseData) {
        return BOTH_DEFENDANTS.equals(caseData.getDefendantDetails().getValue().getLabel());
    }
}
