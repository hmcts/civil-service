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
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildDirectionsQuestionnaireEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildDefenceOrStatesPaidEvent;
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

        EventBuckets buckets = initialiseBuckets(builder.build());

        processRespondent1(builder, caseData, buckets);
        processRespondent2(builder, caseData, buckets);

        builder.defenceFiled(buckets.defenceEvents);
        builder.statesPaid(buckets.statesPaidEvents);
        builder.clearDirectionsQuestionnaireFiled().directionsQuestionnaireFiled(buckets.directionsQuestionnaireEvents);
    }

    private EventBuckets initialiseBuckets(EventHistory history) {
        List<Event> defenceEvents = sanitise(history.getDefenceFiled());
        List<Event> statesPaidEvents = sanitise(history.getStatesPaid());
        List<Event> directionsQuestionnaireEvents = sanitise(history.getDirectionsQuestionnaireFiled());
        return new EventBuckets(defenceEvents, statesPaidEvents, directionsQuestionnaireEvents);
    }

    private List<Event> sanitise(List<Event> events) {
        List<Event> result = new ArrayList<>(Optional.ofNullable(events).orElse(List.of()));
        result.removeIf(event -> event.getEventCode() == null);
        return result;
    }

    private void processRespondent1(EventHistory.EventHistoryBuilder builder,
                                    CaseData caseData,
                                    EventBuckets buckets) {
        if (!defendant1ResponseExists.test(caseData)) {
            return;
        }

        LocalDateTime responseDate = caseData.getRespondent1ResponseDate();
        if (caseData.isLRvLipOneVOne() || caseData.isLipvLipOneVOne()) {
            addLipVsLipFullDefenceEvent(builder, caseData, buckets, responseDate);
        } else {
            addDefenceOrStatesPaid(builder, caseData, buckets, responseDate, RESPONDENT_ID, caseData.getRespondToClaim());
        }

        Respondent1DQ respondent1DQ = caseData.getRespondent1DQ();
        buckets.directionsQuestionnaireEvents.add(
            createDirectionsQuestionnaireEvent(
                builder,
                caseData,
                responseDate,
                RESPONDENT_ID,
                respondent1DQ,
                caseData.getRespondent1(),
                true
            )
        );

        if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
            handleSameSolicitorResponse(builder, caseData, buckets, responseDate, respondent1DQ);
        }
    }

    private void processRespondent2(EventHistory.EventHistoryBuilder builder,
                                    CaseData caseData,
                                    EventBuckets buckets) {
        if (!defendant2ResponseExists.test(caseData)) {
            return;
        }

        LocalDateTime responseDate = caseData.getRespondent2ResponseDate();
        RespondToClaim respondToClaim = shouldUseRespondent1Response(caseData)
            ? caseData.getRespondToClaim()
            : caseData.getRespondToClaim2();

        addDefenceOrStatesPaid(builder, caseData, buckets, responseDate, RESPONDENT2_ID, respondToClaim);

        Respondent2DQ respondent2DQ = caseData.getRespondent2DQ();
        buckets.directionsQuestionnaireEvents.add(
            createDirectionsQuestionnaireEvent(
                builder,
                caseData,
                responseDate,
                RESPONDENT2_ID,
                respondent2DQ,
                caseData.getRespondent2(),
                false
            )
        );
    }

    private void addDefenceOrStatesPaid(EventHistory.EventHistoryBuilder builder,
                                        CaseData caseData,
                                        EventBuckets buckets,
                                        LocalDateTime responseDate,
                                        String partyId,
                                        RespondToClaim respondToClaim) {
        if (isAllPaid(caseData.getTotalClaimAmount(), respondToClaim)) {
            buckets.statesPaidEvents.add(buildDefenceOrStatesPaidEvent(builder, sequenceGenerator, responseDate, partyId, true));
        } else {
            buckets.defenceEvents.add(buildDefenceOrStatesPaidEvent(builder, sequenceGenerator, responseDate, partyId, false));
        }
    }

    private void addLipVsLipFullDefenceEvent(EventHistory.EventHistoryBuilder builder,
                                             CaseData caseData,
                                             EventBuckets buckets,
                                             LocalDateTime respondent1ResponseDate) {
        if (caseData.hasDefendantPaidTheAmountClaimed()) {
            buckets.statesPaidEvents.add(buildDefenceOrStatesPaidEvent(builder, sequenceGenerator, respondent1ResponseDate, RESPONDENT_ID, true));
        } else {
            buckets.defenceEvents.add(buildDefenceOrStatesPaidEvent(builder, sequenceGenerator, respondent1ResponseDate, RESPONDENT_ID, false));
        }
    }

    private void handleSameSolicitorResponse(EventHistory.EventHistoryBuilder builder,
                                             CaseData caseData,
                                             EventBuckets buckets,
                                             LocalDateTime respondent1ResponseDate,
                                             Respondent1DQ respondent1DQ) {
        LocalDateTime respondent2ResponseDate = Optional.ofNullable(caseData.getRespondent2ResponseDate())
            .orElse(respondent1ResponseDate);

        if (isAllPaid(caseData.getTotalClaimAmount(), caseData.getRespondToClaim())) {
            buckets.statesPaidEvents.add(buildDefenceOrStatesPaidEvent(builder, sequenceGenerator, respondent1ResponseDate, RESPONDENT2_ID, true));
        }
        buckets.defenceEvents.add(buildDefenceOrStatesPaidEvent(builder, sequenceGenerator, respondent2ResponseDate, RESPONDENT2_ID, false));

        buckets.directionsQuestionnaireEvents.add(
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

    private boolean shouldUseRespondent1Response(CaseData caseData) {
        return ONE_V_TWO_ONE_LEGAL_REP.equals(getMultiPartyScenario(caseData))
            && caseData.getSameSolicitorSameResponse() == YES;
    }

    private Event createDirectionsQuestionnaireEvent(EventHistory.EventHistoryBuilder builder,
                                                     CaseData caseData,
                                                     LocalDateTime responseDate,
                                                     String partyId,
                                                     DQ respondentDQ,
                                                     Party respondent,
                                                     boolean isRespondent1) {
        return buildDirectionsQuestionnaireEvent(
            builder,
            sequenceGenerator,
            responseDate,
            partyId,
            respondentDQ,
            getPreferredCourtCode(respondentDQ),
            respondentResponseSupport.prepareFullDefenceEventText(respondentDQ, caseData, isRespondent1, respondent)
        );
    }

    private boolean isAllPaid(BigDecimal totalClaimAmount, RespondToClaim respondToClaim) {
        return totalClaimAmount != null
            && Optional.ofNullable(respondToClaim)
                .map(RespondToClaim::getHowMuchWasPaid)
                .map(paid -> MonetaryConversions.penniesToPounds(paid).compareTo(totalClaimAmount) >= 0)
                .orElse(false);
    }

    private record EventBuckets(List<Event> defenceEvents, List<Event> statesPaidEvents, List<Event> directionsQuestionnaireEvents) {
    }

}
