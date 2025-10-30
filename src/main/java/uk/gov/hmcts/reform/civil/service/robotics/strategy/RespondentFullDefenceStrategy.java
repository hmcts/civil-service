package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DEFENCE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.DIRECTIONS_QUESTIONNAIRE_FILED;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.STATES_PAID;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.isStayClaim;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1v2SameSolicitorSameResponse;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ResponseExists;

@Component
@Order(42)
@RequiredArgsConstructor
public class RespondentFullDefenceStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsRespondentResponseSupport respondentResponseSupport;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null) {
            return false;
        }

        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        boolean hasState = stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.FULL_DEFENCE.fullName()::equals);

        if (!hasState) {
            return false;
        }

        return defendant1ResponseExists.test(caseData)
            || defendant2ResponseExists.test(caseData)
            || defendant1v2SameSolicitorSameResponse.test(caseData);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        EventHistory existingHistory = builder.build();
        List<Event> defenceEvents = new ArrayList<>(Optional.ofNullable(existingHistory.getDefenceFiled()).orElse(List.of()));
        defenceEvents.removeIf(event -> event.getEventCode() == null);

        List<Event> statesPaidEvents = new ArrayList<>(Optional.ofNullable(existingHistory.getStatesPaid()).orElse(List.of()));
        statesPaidEvents.removeIf(event -> event.getEventCode() == null);

        List<Event> directionsQuestionnaireEvents = new ArrayList<>(
            Optional.ofNullable(existingHistory.getDirectionsQuestionnaireFiled()).orElse(List.of())
        );
        directionsQuestionnaireEvents.removeIf(event -> event.getEventCode() == null);

        if (defendant1ResponseExists.test(caseData)) {
            LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();

            if (caseData.isLRvLipOneVOne() || caseData.isLipvLipOneVOne()) {
                addLipVsLipFullDefenceEvent(builder, caseData, defenceEvents, statesPaidEvents);
            } else {
                RespondToClaim respondToClaim = caseData.getRespondToClaim();
                if (isAllPaid(caseData.getTotalClaimAmount(), respondToClaim)) {
                    statesPaidEvents.add(createDefenceFiledEvent(builder, respondent1ResponseDate, RESPONDENT_ID, true));
                } else {
                    defenceEvents.add(createDefenceFiledEvent(builder, respondent1ResponseDate, RESPONDENT_ID, false));
                }
            }

            Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
            directionsQuestionnaireEvents.add(
                createDirectionsQuestionnaireEvent(
                    builder,
                    caseData,
                    respondent1ResponseDate,
                    RESPONDENT_ID,
                    respondent1DQ,
                    caseData.getRespondent1(),
                    true
                )
            );

            if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
                LocalDateTime respondent2ResponseDate = caseData.getRespondent2ResponseDate() != null
                    ? caseData.getRespondent2ResponseDate()
                    : respondent1ResponseDate;

                if (isAllPaid(caseData.getTotalClaimAmount(), caseData.getRespondToClaim())) {
                    statesPaidEvents.add(createDefenceFiledEvent(builder, respondent1ResponseDate, RESPONDENT2_ID, true));
                }
                defenceEvents.add(createDefenceFiledEvent(builder, respondent2ResponseDate, RESPONDENT2_ID, false));

                directionsQuestionnaireEvents.add(
                    createDirectionsQuestionnaireEvent(
                        builder,
                        caseData,
                        respondent2ResponseDate,
                        RESPONDENT2_ID,
                        respondent1DQ,
                        caseData.getRespondent2(),
                        true
                    )
                );
            }
        }

        if (defendant2ResponseExists.test(caseData)) {
            LocalDateTime respondent2ResponseDate = caseData.getRespondent2ResponseDate();
            RespondToClaim respondToClaim = caseData.getRespondToClaim2();
            if (shouldUseRespondent1Response(caseData)) {
                respondToClaim = caseData.getRespondToClaim();
            }

            if (isAllPaid(caseData.getTotalClaimAmount(), respondToClaim)) {
                statesPaidEvents.add(createDefenceFiledEvent(builder, respondent2ResponseDate, RESPONDENT2_ID, true));
            } else {
                defenceEvents.add(createDefenceFiledEvent(builder, respondent2ResponseDate, RESPONDENT2_ID, false));
            }

            Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
            directionsQuestionnaireEvents.add(
                createDirectionsQuestionnaireEvent(
                    builder,
                    caseData,
                    respondent2ResponseDate,
                    RESPONDENT2_ID,
                    respondent2DQ,
                    caseData.getRespondent2(),
                    false
                )
            );
        }

        builder.defenceFiled(defenceEvents);
        builder.statesPaid(statesPaidEvents);
        builder.clearDirectionsQuestionnaireFiled().directionsQuestionnaireFiled(directionsQuestionnaireEvents);
    }

    private boolean shouldUseRespondent1Response(CaseData caseData) {
        return ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && caseData.getSameSolicitorSameResponse() == YES;
    }

    private void addLipVsLipFullDefenceEvent(EventHistory.EventHistoryBuilder builder,
                                             CaseData caseData,
                                             List<Event> defenceEvents,
                                             List<Event> statesPaidEvents) {
        LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();
        if (caseData.hasDefendantPaidTheAmountClaimed()) {
            statesPaidEvents.add(createDefenceFiledEvent(builder, respondent1ResponseDate, RESPONDENT_ID, true));
        } else {
            defenceEvents.add(createDefenceFiledEvent(builder, respondent1ResponseDate, RESPONDENT_ID, false));
        }
    }

    private Event createDefenceFiledEvent(EventHistory.EventHistoryBuilder builder,
                                          LocalDateTime responseDate,
                                          String partyId,
                                          boolean statesPaid) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(statesPaid ? STATES_PAID.getCode() : DEFENCE_FILED.getCode())
            .dateReceived(responseDate)
            .litigiousPartyID(partyId)
            .build();
    }

    private Event createDirectionsQuestionnaireEvent(EventHistory.EventHistoryBuilder builder,
                                                     CaseData caseData,
                                                     LocalDateTime responseDate,
                                                     String partyId,
                                                     DQ respondentDQ,
                                                     Party respondent,
                                                     boolean isRespondent1) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(DIRECTIONS_QUESTIONNAIRE_FILED.getCode())
            .dateReceived(responseDate)
            .litigiousPartyID(partyId)
            .eventDetailsText(
                respondentResponseSupport.prepareFullDefenceEventText(respondentDQ, caseData, isRespondent1, respondent)
            )
            .eventDetails(EventDetails.builder()
                              .stayClaim(isStayClaim(respondentDQ))
                              .preferredCourtCode(getPreferredCourtCode(respondentDQ))
                              .preferredCourtName("")
                              .build())
            .build();
    }

    private boolean isAllPaid(BigDecimal totalClaimAmount, RespondToClaim respondToClaim) {
        return totalClaimAmount != null
            && Optional.ofNullable(respondToClaim)
                .map(RespondToClaim::getHowMuchWasPaid)
                .map(paid -> MonetaryConversions.penniesToPounds(paid).compareTo(totalClaimAmount) >= 0)
                .orElse(false);
    }
}
