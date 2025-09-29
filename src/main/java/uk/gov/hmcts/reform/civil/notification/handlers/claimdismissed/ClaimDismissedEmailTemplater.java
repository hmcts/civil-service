package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.List;

import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE;

@Component
@AllArgsConstructor
public class ClaimDismissedEmailTemplater {

    private final SimpleStateFlowEngine stateFlowEngine;
    private final NotificationsProperties notificationsProperties;

    protected String getTemplateId(CaseData caseData) {
        return this.getSolicitorClaimDismissedProperty(
            stateFlowEngine.evaluate(caseData)
                .getStateHistory()
                .stream()
                .map(State::getName)
                .toList(),
            notificationsProperties
        );
    }

    public String getSolicitorClaimDismissedProperty(List<String> stateHistoryNameList,
                                                     NotificationsProperties notificationsProperties) {
        //scenerio 1: Claim notification does not happen within 4 months of issue
        if (stateHistoryNameList.contains(CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE.fullName())) {
            return notificationsProperties.getSolicitorClaimDismissedWithin4Months();
        } else if (stateHistoryNameList.contains(CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE.fullName())) {
            //scenerio 2: Claims details notification is not completed within 14 days of the claim notification step
            return notificationsProperties.getSolicitorClaimDismissedWithin14Days();
        } else if (stateHistoryNameList.contains(CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE.fullName())) {
            //scenerio 3 Claimant does not give their intention by the given deadline
            return notificationsProperties.getSolicitorClaimDismissedWithinDeadline();
        } else {
            return notificationsProperties.getSolicitorClaimDismissedWithinDeadline();
        }
    }
}
