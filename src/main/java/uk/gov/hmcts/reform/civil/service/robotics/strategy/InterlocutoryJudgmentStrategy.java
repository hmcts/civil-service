package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsPartyLookup;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

@Component
@RequiredArgsConstructor
public class InterlocutoryJudgmentStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsTimelineHelper timelineHelper;
    private final RoboticsPartyLookup partyLookup;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null) {
            return false;
        }
        boolean hasHearingSupport = caseData.getHearingSupportRequirementsDJ() != null;
        boolean hasDefendantDetails = caseData.getDefendantDetails() != null;

        if (!hasHearingSupport && !hasDefendantDetails) {
            return false;
        }

        return hasDefendantDetails || !isGrantedForSingleRespondent(caseData);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder,
                           CaseData caseData,
                           String authToken) {
        if (!supports(caseData)) {
            return;
        }

        boolean grantedForSingleRespondent = isGrantedForSingleRespondent(caseData);

        if (caseData.getHearingSupportRequirementsDJ() != null && !grantedForSingleRespondent) {
            builder.interlocutoryJudgment(buildEvent(builder, 0));
            if (caseData.getRespondent2() != null) {
                builder.interlocutoryJudgment(buildEvent(builder, 1));
            }
        }

        if (caseData.getDefendantDetails() != null && !isTakenOfflineAfterSdo(caseData)) {
            builder.miscellaneous(buildMiscEvent(
                builder,
                sequenceGenerator,
                resolveMiscMessage(grantedForSingleRespondent),
                timelineHelper.now()
            ));
        }
    }

    private String resolveMiscMessage(boolean grantedForSingleRespondent) {
        return grantedForSingleRespondent
            ? textFormatter.summaryJudgmentRequested()
            : textFormatter.summaryJudgmentGranted();
    }

    private boolean isTakenOfflineAfterSdo(CaseData caseData) {
        StateFlow flow = stateFlowEngine.evaluate(caseData);
        return flow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.TAKEN_OFFLINE_AFTER_SDO.fullName()::equals);
    }

    private Event buildEvent(EventHistory.EventHistoryBuilder builder, int respondentIndex) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.INTERLOCUTORY_JUDGMENT_GRANTED.getCode())
            .dateReceived(timelineHelper.now())
            .litigiousPartyID(partyLookup.respondentId(respondentIndex))
            .eventDetailsText("")
            .eventDetails(EventDetails.builder().miscText("").build())
            .build();
    }

    private boolean isGrantedForSingleRespondent(CaseData caseData) {
        if (caseData.getRespondent2() == null) {
            return false;
        }

        DynamicList defendantDetails = caseData.getDefendantDetails();
        if (defendantDetails == null) {
            return false;
        }

        DynamicListElement selected = defendantDetails.getValue();
        String label = selected != null ? selected.getLabel() : null;
        return label != null && !label.startsWith("Both");
    }
}
