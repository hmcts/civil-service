package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
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

import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREPRESENTED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.getDefendantNames;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildEnumeratedMiscEvent;

@Component
@Order(90)
@RequiredArgsConstructor
public class UnrepresentedDefendantStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsTimelineHelper timelineHelper;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && caseData.getSubmittedDate() != null
            && hasState(caseData)
            && !getDefendantNames(UNREPRESENTED, caseData).isEmpty();
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        List<String> defendantNames = getDefendantNames(UNREPRESENTED, caseData);
        LocalDateTime submittedDate = caseData.getSubmittedDate();

        IntStream.range(0, defendantNames.size())
            .mapToObj(index -> buildEnumeratedMiscEvent(
                builder,
                sequenceGenerator,
                timelineHelper,
                new EnumeratedMiscParams(
                    submittedDate,
                    index,
                    defendantNames.size(),
                    defendantNames.get(index),
                    textFormatter::unrepresentedDefendant
                )
            ))
            .forEach(builder::miscellaneous);
    }

    private boolean hasState(CaseData caseData) {
        StateFlow flow = stateFlowEngine.evaluate(caseData);
        return flow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT.fullName()::equals);
    }
}
