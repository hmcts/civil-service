package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.EnumeratedMiscParams;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREGISTERED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREPRESENTED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.getDefendantNames;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildEnumeratedMiscEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnrepresentedAndUnregisteredDefendantStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsTimelineHelper timelineHelper;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && caseData.getSubmittedDate() != null
            && hasState(caseData)
            && !getDefendantNames(UNREPRESENTED, caseData).isEmpty()
            && !getDefendantNames(UNREGISTERED, caseData).isEmpty();
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        log.info("Building unrepresented and unregistered defendant robotics events for caseId {}", caseData.getCcdCaseReference());
        List<String> unrepresented = getDefendantNames(UNREPRESENTED, caseData);
        List<String> unregistered = getDefendantNames(UNREGISTERED, caseData);
        LocalDateTime submittedDate = caseData.getSubmittedDate();

        List<String> bodies = List.of(
            format("Unrepresented defendant and unregistered defendant solicitor firm. Unrepresented defendant: %s",
                unrepresented.get(0)),
            format("Unrepresented defendant and unregistered defendant solicitor firm. Unregistered defendant solicitor firm: %s",
                unregistered.get(0))
        );

        IntStream.range(0, bodies.size())
            .mapToObj(index -> buildEnumeratedMiscEvent(
                builder,
                sequenceGenerator,
                timelineHelper,
                new EnumeratedMiscParams(
                    submittedDate,
                    index,
                    bodies.size(),
                    bodies.get(index),
                    textFormatter::unrepresentedAndUnregisteredCombined
                )
            ))
            .forEach(builder::miscellaneous);
    }

    private boolean hasState(CaseData caseData) {
        StateFlow flow = stateFlowEngine.evaluate(caseData);
        return flow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT.fullName()::equals);
    }
}
