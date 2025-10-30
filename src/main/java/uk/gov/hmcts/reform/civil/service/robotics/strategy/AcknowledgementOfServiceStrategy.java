package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.PredicateUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
@Order(30)
@RequiredArgsConstructor
public class AcknowledgementOfServiceStrategy implements EventHistoryStrategy {

    private static final String AOS_EVENT_CODE = EventType.ACKNOWLEDGEMENT_OF_SERVICE_RECEIVED.getCode();
    private static final Set<FlowState.Main> SUPPORTED_STATES = EnumSet.of(
        FlowState.Main.NOTIFICATION_ACKNOWLEDGED,
        FlowState.Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION
    );

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsTimelineHelper timelineHelper;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null) {
            return false;
        }
        boolean respondent1Ack = PredicateUtils.defendant1AckExists.test(caseData);
        boolean respondent2Ack = PredicateUtils.defendant2AckExists.test(caseData);
        if (!respondent1Ack && !respondent2Ack) {
            return false;
        }
        return hasRelevantState(stateFlowEngine.evaluate(caseData));
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        MultiPartyScenario scenario = MultiPartyScenario.getMultiPartyScenario(caseData);
        List<Event> events = switch (scenario) {
            case ONE_V_TWO_TWO_LEGAL_REP -> buildTwoDefendantTwoSolEvents(builder, caseData);
            case ONE_V_TWO_ONE_LEGAL_REP -> buildTwoDefendantSameSolEvents(builder, caseData);
            default -> buildSingleDefendantEvents(builder, caseData);
        };

        if (!events.isEmpty()) {
            builder.acknowledgementOfServiceReceived(events);
        }
    }

    private List<Event> buildTwoDefendantTwoSolEvents(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        List<Event> events = new ArrayList<>();
        if (PredicateUtils.defendant1AckExists.test(caseData)) {
            events.add(buildAosEvent(builder, caseData, true,
                String.format("Defendant: %s has acknowledged: %s",
                    caseData.getRespondent1().getPartyName(),
                    caseData.getRespondent1ClaimResponseIntentionType().getLabel())));
        }
        if (PredicateUtils.defendant2AckExists.test(caseData)) {
            events.add(buildAosEvent(builder, caseData, false,
                String.format("Defendant: %s has acknowledged: %s",
                    caseData.getRespondent2().getPartyName(),
                    evaluateRespondent2IntentionType(caseData))));
        }
        return events;
    }

    private List<Event> buildTwoDefendantSameSolEvents(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        String currentDate = timelineHelper.now().toLocalDate().toString();
        List<Event> events = new ArrayList<>();
        events.add(buildAosEvent(builder, caseData, true,
            String.format("[1 of 2 - %s] Defendant: %s has acknowledged: %s",
                currentDate,
                caseData.getRespondent1().getPartyName(),
                caseData.getRespondent1ClaimResponseIntentionType().getLabel())));
        events.add(buildAosEvent(builder, caseData, false,
            String.format("[2 of 2 - %s] Defendant: %s has acknowledged: %s",
                currentDate,
                caseData.getRespondent2().getPartyName(),
                evaluateRespondent2IntentionType(caseData))));
        return events;
    }

    private List<Event> buildSingleDefendantEvents(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return List.of(buildSpecAosEvent(builder, caseData.getRespondent1AcknowledgeNotificationDate()));
        }

        LocalDateTime dateAcknowledge = caseData.getRespondent1AcknowledgeNotificationDate();
        if (dateAcknowledge == null) {
            return List.of();
        }

        return List.of(buildAosEvent(
            builder,
            caseData,
            true,
            String.format("responseIntention: %s",
                caseData.getRespondent1ClaimResponseIntentionType().getLabel())
        ));
    }

    private Event buildAosEvent(EventHistory.EventHistoryBuilder builder,
                                CaseData caseData,
                                boolean isRespondent1,
                                String text) {
        LocalDateTime date = isRespondent1
            ? caseData.getRespondent1AcknowledgeNotificationDate()
            : caseData.getRespondent2AcknowledgeNotificationDate();

        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(AOS_EVENT_CODE)
            .dateReceived(date)
            .litigiousPartyID(isRespondent1 ? "002" : "003")
            .eventDetails(EventDetails.builder()
                .responseIntention(
                    isRespondent1
                        ? caseData.getRespondent1ClaimResponseIntentionType().getLabel()
                        : evaluateRespondent2IntentionType(caseData))
                .build())
            .eventDetailsText(text)
            .build();
    }

    private Event buildSpecAosEvent(EventHistory.EventHistoryBuilder builder, LocalDateTime dateAcknowledge) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode("38")
            .dateReceived(dateAcknowledge)
            .litigiousPartyID("002")
            .eventDetails(EventDetails.builder()
                .acknowledgeService("Acknowledgement of Service")
                .build())
            .eventDetailsText("Defendant LR Acknowledgement of Service ")
            .build();
    }

    private String evaluateRespondent2IntentionType(CaseData caseData) {
        if (caseData.getRespondent2ClaimResponseIntentionType() != null) {
            return caseData.getRespondent2ClaimResponseIntentionType().getLabel();
        }
        return caseData.getRespondent1ClaimResponseIntentionType().getLabel();
    }

    private boolean hasRelevantState(StateFlow stateFlow) {
        return stateFlow.getStateHistory().stream()
            .map(State::getName)
            .map(FlowState::fromFullName)
            .map(FlowState.Main.class::cast)
            .anyMatch(SUPPORTED_STATES::contains);
    }
}
