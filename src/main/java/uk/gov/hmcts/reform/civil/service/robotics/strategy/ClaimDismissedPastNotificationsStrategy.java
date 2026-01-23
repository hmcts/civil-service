package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import java.util.EnumMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimDismissedPastNotificationsStrategy implements EventHistoryStrategy {

    private static final Map<FlowState.Main, MessageResolver> MESSAGE_RESOLVERS = new EnumMap<>(FlowState.Main.class);

    static {
        MESSAGE_RESOLVERS.put(
            FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE,
            RoboticsEventTextFormatter::claimDismissedNoActionSinceIssue
        );
        MESSAGE_RESOLVERS.put(
            FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE,
            RoboticsEventTextFormatter::claimDismissedNoClaimDetailsWithinWindow
        );
    }

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsEventTextFormatter textFormatter;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            ;
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        // The flow-state aware overload is used by the mapper; keep this as a no-op to avoid double-emitting.
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder,
                           CaseData caseData,
                           String authToken,
                           FlowState.Main flowState) {
        if (!supports(caseData) || flowState == null) {
            return;
        }
        MessageResolver resolver = MESSAGE_RESOLVERS.get(flowState);
        if (resolver == null) {
            return;
        }
        log.info("Building claim dismissed past notifications robotics event for caseId {}", caseData.getCcdCaseReference());
        String message = resolver.resolve(textFormatter);
        builder.miscellaneous(buildMiscEvent(builder, sequenceGenerator, message, caseData.getClaimDismissedDate()));
    }

    @FunctionalInterface
    private interface MessageResolver {
        String resolve(RoboticsEventTextFormatter formatter);
    }
}
