package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.aop.support.AopUtils;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.EventHistoryStrategy;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.AcknowledgementOfServiceStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.BreathingSpaceEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseNotesStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseProceedsInCasemanStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.CaseQueriesStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.CertificateOfSatisfactionOrCancellationStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimDetailsNotifiedEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimDismissedPastDeadlineStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimDismissedPastNotificationsStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimIssuedEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimNotifiedEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.ClaimantResponseStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.ConsentExtensionEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.DefaultJudgmentEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.DefendantNoCDeadlineStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.GeneralApplicationStrikeOutStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.InterlocutoryJudgmentStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.JudgmentByAdmissionStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.MediationEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.RespondentCounterClaimStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.RespondentDivergentResponseStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.RespondentFullAdmissionStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.RespondentFullDefenceStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.RespondentLitigationFriendStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.RespondentPartAdmissionStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.SdoNotDrawnStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.SetAsideJudgmentStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.SpecRejectRepaymentPlanStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.SummaryJudgmentStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineAfterClaimDetailsNotifiedStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineAfterClaimNotifiedStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineByStaffEventStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflinePastApplicantResponseStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.TakenOfflineSpecDefendantNocStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.UnregisteredDefendantStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.UnrepresentedAndUnregisteredDefendantStrategy;
import uk.gov.hmcts.reform.civil.service.robotics.strategy.UnrepresentedDefendantStrategy;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventHistoryMapper {

    private final EventHistorySequencer eventHistorySequencer;
    private final RoboticsRespondentResponseSupport respondentResponseSupport;
    private final List<EventHistoryStrategy> eventHistoryStrategies;
    private final IStateFlowEngine stateFlowEngine;
    public static final String RECORD_JUDGMENT = "Judgment recorded.";
    public static final String QUERIES_ON_CASE = "There has been a query on this case";

    public EventHistory buildEvents(CaseData caseData) {
        return buildEvents(caseData, null);
    }

    public EventHistory buildEvents(CaseData caseData, String authToken) {
        EventHistory.EventHistoryBuilder builder = EventHistory.builder()
            .directionsQuestionnaireFiled(List.of(Event.builder().build()));

        Map<Class<?>, EventHistoryStrategy> registry = buildStrategyRegistry();
        Set<Class<? extends EventHistoryStrategy>> invoked = new HashSet<>();

        stateFlowEngine.evaluate(caseData).getStateHistory()
            .forEach(state -> dispatchState(builder, caseData, authToken, registry, invoked, state));

        runGlobalStrategies(builder, caseData, authToken, registry, invoked);

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

    private void dispatchState(EventHistory.EventHistoryBuilder builder,
                               CaseData caseData,
                               String authToken,
                               Map<Class<?>, EventHistoryStrategy> registry,
                               Set<Class<? extends EventHistoryStrategy>> invoked,
                               State state) {
        FlowState.Main flowState = (FlowState.Main) FlowState.fromFullName(state.getName());
        List<Class<? extends EventHistoryStrategy>> stateStrategies = STATE_STRATEGY_MAP.get(flowState);
        if (stateStrategies == null || stateStrategies.isEmpty()) {
            return;
        }
        for (Class<? extends EventHistoryStrategy> strategyClass : stateStrategies) {
            EventHistoryStrategy strategy = registry.get(strategyClass);
            if (strategy == null) {
                continue;
            }
            strategy.contribute(builder, caseData, authToken);
            invoked.add(strategyClass);
        }
    }

    private Map<Class<?>, EventHistoryStrategy> buildStrategyRegistry() {
        Map<Class<?>, EventHistoryStrategy> registry = new HashMap<>();
        for (EventHistoryStrategy strategy : eventHistoryStrategies) {
            registry.put(AopUtils.getTargetClass(strategy), strategy);
        }
        return registry;
    }

    private void runGlobalStrategies(EventHistory.EventHistoryBuilder builder,
                                     CaseData caseData,
                                     String authToken,
                                     Map<Class<?>, EventHistoryStrategy> registry,
                                     Set<Class<? extends EventHistoryStrategy>> invoked) {
        for (Class<? extends EventHistoryStrategy> strategyClass : GLOBAL_STRATEGIES_ORDER) {
            EventHistoryStrategy strategy = registry.get(strategyClass);
            if (strategy == null || invoked.contains(strategyClass) || !strategy.supports(caseData)) {
                continue;
            }
            strategy.contribute(builder, caseData, authToken);
            invoked.add(strategyClass);
        }

        // Fallback: run any remaining non-state strategies once
        eventHistoryStrategies.stream()
            .filter(strategy -> !invoked.contains(AopUtils.getTargetClass(strategy)))
            .filter(strategy -> strategy.supports(caseData))
            .forEach(strategy -> {
                strategy.contribute(builder, caseData, authToken);
                invoked.add(targetClass(strategy));
            });
    }

    private static final Map<FlowState.Main, List<Class<? extends EventHistoryStrategy>>> STATE_STRATEGY_MAP =
        Map.ofEntries(
            Map.entry(Main.TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT, List.of(UnrepresentedDefendantStrategy.class)),
            Map.entry(Main.TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT,
                      List.of(UnrepresentedAndUnregisteredDefendantStrategy.class)),
            Map.entry(Main.TAKEN_OFFLINE_UNREGISTERED_DEFENDANT, List.of(UnregisteredDefendantStrategy.class)),
            Map.entry(Main.CLAIM_ISSUED, List.of(ClaimIssuedEventStrategy.class)),
            Map.entry(Main.CLAIM_NOTIFIED, List.of(ClaimNotifiedEventStrategy.class)),
            Map.entry(Main.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED, List.of(TakenOfflineAfterClaimNotifiedStrategy.class)),
            Map.entry(Main.CLAIM_DETAILS_NOTIFIED, List.of(ClaimDetailsNotifiedEventStrategy.class)),
            Map.entry(Main.NOTIFICATION_ACKNOWLEDGED, List.of(AcknowledgementOfServiceStrategy.class)),
            Map.entry(Main.NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION, List.of(ConsentExtensionEventStrategy.class)),
            Map.entry(Main.CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION, List.of(ConsentExtensionEventStrategy.class)),
            Map.entry(Main.FULL_DEFENCE, List.of(RespondentFullDefenceStrategy.class)),
            Map.entry(Main.FULL_ADMISSION, List.of(RespondentFullAdmissionStrategy.class)),
            Map.entry(Main.PART_ADMISSION, List.of(RespondentPartAdmissionStrategy.class)),
            Map.entry(Main.COUNTER_CLAIM, List.of(RespondentCounterClaimStrategy.class)),
            Map.entry(Main.AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED, List.of(RespondentDivergentResponseStrategy.class)),
            Map.entry(Main.AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED,
                      List.of(RespondentDivergentResponseStrategy.class)),
            Map.entry(Main.AWAITING_RESPONSES_FULL_ADMIT_RECEIVED, List.of(RespondentDivergentResponseStrategy.class)),
            Map.entry(Main.DIVERGENT_RESPOND_GENERATE_DQ_GO_OFFLINE,
                      List.of(RespondentDivergentResponseStrategy.class)),
            Map.entry(Main.DIVERGENT_RESPOND_GO_OFFLINE, List.of(RespondentDivergentResponseStrategy.class)),
            Map.entry(Main.FULL_DEFENCE_NOT_PROCEED, List.of(ClaimantResponseStrategy.class)),
            Map.entry(Main.FULL_DEFENCE_PROCEED, List.of(ClaimantResponseStrategy.class)),
            Map.entry(Main.TAKEN_OFFLINE_BY_STAFF,
                      List.of(TakenOfflineByStaffEventStrategy.class, GeneralApplicationStrikeOutStrategy.class)),
            Map.entry(Main.CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE,
                      List.of(ClaimDismissedPastDeadlineStrategy.class)),
            Map.entry(Main.CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE,
                      List.of(ClaimDismissedPastNotificationsStrategy.class)),
            Map.entry(Main.CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE,
                      List.of(ClaimDismissedPastNotificationsStrategy.class)),
            Map.entry(Main.TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED,
                      List.of(TakenOfflineAfterClaimDetailsNotifiedStrategy.class)),
            Map.entry(Main.TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE,
                      List.of(TakenOfflinePastApplicantResponseStrategy.class)),
            Map.entry(Main.TAKEN_OFFLINE_SDO_NOT_DRAWN, List.of(SdoNotDrawnStrategy.class)),
            Map.entry(Main.TAKEN_OFFLINE_AFTER_SDO,
                      List.of(CaseProceedsInCasemanStrategy.class, SummaryJudgmentStrategy.class)),
            Map.entry(Main.PART_ADMIT_REJECT_REPAYMENT, List.of(SpecRejectRepaymentPlanStrategy.class)),
            Map.entry(Main.FULL_ADMIT_REJECT_REPAYMENT, List.of(SpecRejectRepaymentPlanStrategy.class)),
            Map.entry(Main.IN_MEDIATION, List.of(MediationEventStrategy.class)),
            Map.entry(Main.TAKEN_OFFLINE_SPEC_DEFENDANT_NOC,
                      List.of(TakenOfflineSpecDefendantNocStrategy.class, DefendantNoCDeadlineStrategy.class)),
            Map.entry(Main.TAKEN_OFFLINE_SPEC_DEFENDANT_NOC_AFTER_JBA,
                      List.of(TakenOfflineSpecDefendantNocStrategy.class, DefendantNoCDeadlineStrategy.class))
        );

    private static final List<Class<? extends EventHistoryStrategy>> GLOBAL_STRATEGIES_ORDER = List.of(
        RespondentLitigationFriendStrategy.class,
        CaseNotesStrategy.class,
        BreathingSpaceEventStrategy.class,
        InterlocutoryJudgmentStrategy.class,
        DefaultJudgmentEventStrategy.class,
        JudgmentByAdmissionStrategy.class,
        SetAsideJudgmentStrategy.class,
        CertificateOfSatisfactionOrCancellationStrategy.class,
        CaseQueriesStrategy.class
    );

    @SuppressWarnings("unchecked")
    private Class<? extends EventHistoryStrategy> targetClass(EventHistoryStrategy strategy) {
        return (Class<? extends EventHistoryStrategy>) AopUtils.getTargetClass(strategy);
    }
}
