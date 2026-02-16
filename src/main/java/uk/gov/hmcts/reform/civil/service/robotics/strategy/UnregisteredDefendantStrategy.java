package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.EnumeratedMiscParams;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import uk.gov.hmcts.reform.civil.model.robotics.Event;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREGISTERED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.getDefendantNames;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildEnumeratedMiscEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnregisteredDefendantStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsTimelineHelper timelineHelper;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && hasState(caseData);
    }

    @Override
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        log.info("Building unregistered defendant robotics events for caseId {}", caseData.getCcdCaseReference());
        List<String> defendantNames = getDefendantNames(UNREGISTERED, caseData);
        LocalDateTime submittedDate = caseData.getSubmittedDate();

        IntStream.range(0, defendantNames.size())
            .mapToObj(index -> buildEnumeratedMiscEvent(
                    eventHistory,
                sequenceGenerator,
                timelineHelper,
                new EnumeratedMiscParams(
                    submittedDate,
                    index,
                    defendantNames.size(),
                    defendantNames.get(index),
                    textFormatter::unregisteredSolicitor
                )
            ))
            .forEach(event -> {
                List<Event> updatedMiscellaneousEvents = eventHistory.getMiscellaneous() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(eventHistory.getMiscellaneous());
                updatedMiscellaneousEvents.add(event);
                eventHistory.setMiscellaneous(updatedMiscellaneousEvents);
            });
    }

    private boolean hasState(CaseData caseData) {
        StateFlow flow = stateFlowEngine.evaluate(caseData);
        return flow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT.fullName()::equals);
    }
}
