package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventSequence;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimDismissedPastNotificationDeadlineBuilder extends BaseEventBuilder {

    private final IStateFlowEngine stateFlowEngine;

    @Override
    public Set<FlowState.Main> supportedFlowStates() {
        return Set.of(CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE,
            CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE);
    }

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        log.info("Building event: {} for case id: {} ", eventHistoryDTO.getEventType(), eventHistoryDTO.getCaseData().getCcdCaseReference());
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();

        List<State> caseHistory = stateFlowEngine.evaluate(caseData).getStateHistory();
        State state = caseHistory.get(caseHistory.size() - 1);
        FlowState.Main flowState = (FlowState.Main) FlowState.fromFullName(state.getName());
        String miscText = null;
        if (flowState.equals(CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE)) {
            miscText = "RPA Reason: Claim dismissed. Claimant hasn't notified defendant of the "
                + "claim details within the allowed 2 weeks.";
        } else {
            miscText = "RPA Reason: Claim dismissed. Claimant hasn't taken action since the "
                + "claim was issued.";
        }
        buildClaimDismissedPastNotificationsDeadline(
            builder,
            caseData, miscText);
    }

    private void buildClaimDismissedPastNotificationsDeadline(EventHistory.EventHistoryBuilder builder,
                                                              CaseData caseData, String miscText) {
        builder.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getClaimDismissedDate())
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                    .miscText(miscText)
                    .build())
                .build());
    }
}
