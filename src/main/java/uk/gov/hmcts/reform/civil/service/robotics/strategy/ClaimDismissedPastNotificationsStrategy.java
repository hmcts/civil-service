package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimDismissedPastNotificationsStrategy implements EventHistoryStrategy {

    private static final Map<FlowState.Main, MessageResolver> MESSAGE_RESOLVERS =
            new EnumMap<>(FlowState.Main.class);

    static {
        MESSAGE_RESOLVERS.put(
                FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE,
                RoboticsEventTextFormatter::claimDismissedNoActionSinceIssue);
        MESSAGE_RESOLVERS.put(
                FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE,
                RoboticsEventTextFormatter::claimDismissedNoClaimDetailsWithinWindow);
    }

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsEventTextFormatter textFormatter;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null;
    }

    @Override
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        // The flow-state aware overload is used by the mapper; keep this as a no-op to avoid
        // double-emitting.
    }

    @Override
    public void contribute(
            EventHistory eventHistory, CaseData caseData, String authToken, FlowState.Main flowState) {
        if (!supports(caseData) || flowState == null) {
            return;
        }
        MessageResolver resolver = MESSAGE_RESOLVERS.get(flowState);
        if (resolver == null) {
            return;
        }
        log.info(
                "Building claim dismissed past notifications robotics event for caseId {}",
                caseData.getCcdCaseReference());
        String message = resolver.resolve(textFormatter);
        List<Event> updatedMiscellaneousEvents1 =
                eventHistory.getMiscellaneous() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(eventHistory.getMiscellaneous());
        updatedMiscellaneousEvents1.add(
                buildMiscEvent(eventHistory, sequenceGenerator, message, caseData.getClaimDismissedDate()));
        eventHistory.setMiscellaneous(updatedMiscellaneousEvents1);
    }

    @FunctionalInterface
    private interface MessageResolver {
        String resolve(RoboticsEventTextFormatter formatter);
    }
}
