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

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREGISTERED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.UNREPRESENTED;
import static uk.gov.hmcts.reform.civil.enums.UnrepresentedOrUnregisteredScenario.getDefendantNames;

@Component
@Order(92)
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
            && hasState(caseData, FlowState.Main.TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT)
            && !getDefendantNames(UNREPRESENTED, caseData).isEmpty()
            && !getDefendantNames(UNREGISTERED, caseData).isEmpty();
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        String dateToken = timelineHelper.now().toLocalDate().toString();
        List<String> unrepresented = getDefendantNames(UNREPRESENTED, caseData);
        List<String> unregistered = getDefendantNames(UNREGISTERED, caseData);
        LocalDateTime submittedDate = caseData.getSubmittedDate();

        String unrepresentedText = textFormatter.unrepresentedAndUnregistered(
            1,
            dateToken,
            format("Unrepresented defendant and unregistered defendant solicitor firm. Unrepresented defendant: %s",
                unrepresented.get(0))
        );
        String unregisteredText = textFormatter.unrepresentedAndUnregistered(
            2,
            dateToken,
            format("Unrepresented defendant and unregistered defendant solicitor firm. Unregistered defendant solicitor firm: %s",
                unregistered.get(0))
        );

        builder.miscellaneous(
            Event.builder()
                .eventSequence(sequenceGenerator.nextSequence(builder.build()))
                .eventCode(EventType.MISCELLANEOUS.getCode())
                .dateReceived(submittedDate)
                .eventDetailsText(unrepresentedText)
                .eventDetails(EventDetails.builder().miscText(unrepresentedText).build())
                .build()
        );
        builder.miscellaneous(
            Event.builder()
                .eventSequence(sequenceGenerator.nextSequence(builder.build()))
                .eventCode(EventType.MISCELLANEOUS.getCode())
                .dateReceived(submittedDate)
                .eventDetailsText(unregisteredText)
                .eventDetails(EventDetails.builder().miscText(unregisteredText).build())
                .build()
        );
    }

    private boolean hasState(CaseData caseData, FlowState.Main target) {
        StateFlow flow = stateFlowEngine.evaluate(caseData);
        return flow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(target.fullName()::equals);
    }
}
