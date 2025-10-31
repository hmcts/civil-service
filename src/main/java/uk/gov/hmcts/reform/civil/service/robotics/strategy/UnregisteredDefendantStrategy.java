package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREGISTERED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.getDefendantNames;

@Component
@Order(91)
@RequiredArgsConstructor
public class UnregisteredDefendantStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsTimelineHelper timelineHelper;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && caseData.getSubmittedDate() != null
            && hasState(caseData, FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT)
            && !getDefendantNames(UNREGISTERED, caseData).isEmpty();
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        List<String> defendantNames = getDefendantNames(UNREGISTERED, caseData);
        LocalDateTime submittedDate = caseData.getSubmittedDate();

        IntStream.range(0, defendantNames.size())
            .mapToObj(index -> buildEvent(builder, defendantNames, submittedDate, index))
            .forEach(builder::miscellaneous);
    }

    private Event buildEvent(EventHistory.EventHistoryBuilder builder,
                             List<String> defendantNames,
                             LocalDateTime submittedDate,
                             int index) {
        String prefix = defendantNames.size() > 1
            ? format("[%d of %d - %s] ",
                index + 1,
                defendantNames.size(),
                timelineHelper.now().toLocalDate())
            : "";
        String details = textFormatter.unregisteredSolicitor(prefix, defendantNames.get(index));

        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.MISCELLANEOUS.getCode())
            .dateReceived(submittedDate)
            .eventDetailsText(details)
            .eventDetails(EventDetails.builder().miscText(details).build())
            .build();
    }

    private boolean hasState(CaseData caseData, FlowState.Main target) {
        StateFlow flow = stateFlowEngine.evaluate(caseData);
        return flow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(target.fullName()::equals);
    }
}
