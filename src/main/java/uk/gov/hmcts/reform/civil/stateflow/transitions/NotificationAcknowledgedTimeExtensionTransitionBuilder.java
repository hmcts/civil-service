package uk.gov.hmcts.reform.civil.stateflow.transitions;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.allResponsesReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.awaitingResponsesNonFullDefenceReceived;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.caseDismissedAfterClaimAcknowledgedExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.claimDismissalOutOfTime;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.notificationAcknowledged;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.respondentTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaff;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.ALL_RESPONSES_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_BY_STAFF;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;

@Component
public class NotificationAcknowledgedTimeExtensionTransitionBuilder extends MidTransitionBuilder {
    public NotificationAcknowledgedTimeExtensionTransitionBuilder(FeatureToggleService featureToggleService) {
        super(NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION, featureToggleService);
    }

    @Override
    void setUpTransitions() {
        this.moveTo(ALL_RESPONSES_RECEIVED)
            .onlyWhen(notificationAcknowledged.and(respondentTimeExtension).and(allResponsesReceived).and(claimDismissalOutOfTime.negate())
                .and(takenOfflineByStaff.negate()))
            .moveTo(AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED)
            .onlyWhen(notificationAcknowledged.and(respondentTimeExtension)
                .and(awaitingResponsesFullDefenceReceived)
                .and(not(caseDismissedAfterClaimAcknowledgedExtension)))
            .moveTo(AWAITING_RESPONSES_NOT_FULL_DEFENCE_RECEIVED)
            .onlyWhen(notificationAcknowledged.and(respondentTimeExtension)
                .and(awaitingResponsesNonFullDefenceReceived))
            .moveTo(TAKEN_OFFLINE_BY_STAFF)
            .onlyWhen(takenOfflineByStaffAfterNotificationAcknowledgedTimeExtension)
            .moveTo(PAST_CLAIM_DISMISSED_DEADLINE_AWAITING_CAMUNDA)
            .onlyWhen(caseDismissedAfterClaimAcknowledgedExtension)
            .moveTo(TAKEN_OFFLINE_SDO_NOT_DRAWN).onlyWhen(takenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension);
    }
}
