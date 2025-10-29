package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.EventHistoryContributor;

import java.util.List;

import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventHistoryMapper {

    private final IStateFlowEngine stateFlowEngine;
    private final EventHistorySequencer eventHistorySequencer;
    private final RoboticsRespondentResponseSupport respondentResponseSupport;
    private final List<EventHistoryContributor> eventHistoryContributors;
    public static final String RECORD_JUDGMENT = "Judgment recorded.";
    public static final String QUERIES_ON_CASE = "There has been a query on this case";

    public EventHistory buildEvents(CaseData caseData) {
        return buildEvents(caseData, null);
    }

    public EventHistory buildEvents(CaseData caseData, String authToken) {
        EventHistory.EventHistoryBuilder builder = EventHistory.builder()
            .directionsQuestionnaireFiled(List.of(Event.builder().build()));

        stateFlowEngine.evaluate(caseData).getStateHistory()
            .forEach(state -> {
                FlowState.Main flowState = (FlowState.Main) FlowState.fromFullName(state.getName());
                switch (flowState) {
                    case TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT:
                    case TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT:
                    case TAKEN_OFFLINE_UNREGISTERED_DEFENDANT:
                        break;
                    case NOTIFICATION_ACKNOWLEDGED:
                        break;
                    case NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION, CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION:
                        break;
                    case FULL_DEFENCE:
                        break;
                    case PART_ADMISSION:
                        break;
                    // AWAITING_RESPONSES states would only happen in 1v2 diff sol after 1 defendant responses.
                    // These states will not show in the history mapper after the second defendant response.
                    // It can share the same RPA builder as DIVERGENT_RESPOND state because it builds events according
                    // to defendant response
                    // DIVERGENT_RESPOND states would only happen in 1v2 diff sol after both defendant responds.
                    case AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED:
                    case AWAITING_RESPONSES_FULL_ADMIT_RECEIVED:
                    case AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED:
                    case DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE:
                    case DIVERGENT_RESPOND_GO_OFFLINE:
                        break;
                    case FULL_DEFENCE_NOT_PROCEED:
                        break;
                    case FULL_DEFENCE_PROCEED:
                        break;
                    case TAKEN_OFFLINE_BY_STAFF:
                        break;
                    case CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE:
                        break;
                    case CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE:
                    case CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE:
                    case TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED:
                    case TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE:
                        break;
                    default:
                        break;
                }
            });

        eventHistoryContributors.stream()
            .filter(contributor -> contributor.supports(caseData))
            .forEach(contributor -> contributor.contribute(builder, caseData, authToken));
        EventHistory eventHistory = eventHistorySequencer.sortEvents(builder.build());
        log.info("Event history: {}", eventHistory);
        return eventHistory;
    }

    public String prepareRespondentResponseText(CaseData caseData, Party respondent, boolean isRespondent1) {
        return respondentResponseSupport.prepareRespondentResponseText(caseData, respondent, isRespondent1);
    }

    public String prepareEventDetailsText(DQ dq, String preferredCourtCode) {
        return RoboticsDirectionsQuestionnaireSupport.prepareEventDetailsText(dq, preferredCourtCode);
    }

    public boolean isStayClaim(DQ dq) {
        return RoboticsDirectionsQuestionnaireSupport.isStayClaim(dq);
    }

    public String getPreferredCourtCode(DQ dq) {
        return RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode(dq);
    }

    public String prepareFullDefenceEventText(DQ dq, CaseData caseData, boolean isRespondent1, Party respondent) {
        return respondentResponseSupport.prepareFullDefenceEventText(dq, caseData, isRespondent1, respondent);
    }

    public String evaluateRespondent2IntentionType(CaseData caseData) {
        if (caseData.getRespondent2ClaimResponseIntentionType() != null) {
            return caseData.getRespondent2ClaimResponseIntentionType().getLabel();
        }
        return caseData.getRespondent1ClaimResponseIntentionType() != null
            ? caseData.getRespondent1ClaimResponseIntentionType().getLabel()
            : null;
    }

}
