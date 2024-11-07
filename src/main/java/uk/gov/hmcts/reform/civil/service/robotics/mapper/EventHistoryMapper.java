package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.builders.BreathingSpaceEventBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.builders.CaseNotesEventBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.builders.CcjEventBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.builders.InformAgreedExtensionDateSpecBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.builders.DefaultJudgmentBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.builders.EventBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.builders.EventBuilderFactory;
import uk.gov.hmcts.reform.civil.service.robotics.builders.InterlocutoryJudgementBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.builders.MiscellaneousJudgmentEventBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.builders.RespondentsLitigationFriendEventBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.builders.TakenOfflineAfterSdoBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;
import uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil;
import uk.gov.hmcts.reform.civil.utils.PredicateUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventHistoryMapper {

    private final BreathingSpaceEventBuilder breathingSpaceEventBuilder;
    private final CaseNotesEventBuilder caseNotesEventBuilder;
    private final CcjEventBuilder ccjEventBuilder;
    private final InformAgreedExtensionDateSpecBuilder informAgreedExtensionDateSpecBuilder;
    private final DefaultJudgmentBuilder defaultJudgementBuilder;
    private final EventBuilderFactory eventBuilderFactory;
    private final EventHistorySequencer eventHistorySequencer;
    private final IStateFlowEngine stateFlowEngine;
    private final InterlocutoryJudgementBuilder interlocutoryJudgementBuilder;
    private final MiscellaneousJudgmentEventBuilder miscellaneousJudgmentEventBuilder;
    private final RespondentsLitigationFriendEventBuilder respondentsLitigationFriendEventBuilder;
    private final TakenOfflineAfterSdoBuilder takenOfflineAfterSdoBuilder;

    public EventHistory buildEvents(CaseData caseData) {
        return buildEvents(caseData, null);
    }

    public EventHistory buildEvents(CaseData caseData, String authToken) {
        EventHistory.EventHistoryBuilder builder = EventHistory.builder()
            .directionsQuestionnaireFiled(List.of(Event.builder().build()));

        stateFlowEngine.evaluate(caseData).getStateHistory()
            .forEach(state -> {
                FlowState.Main flowState = (FlowState.Main) FlowState.fromFullName(state.getName());
                handleEventScenario(caseData, authToken, flowState, builder);
            });

        respondentsLitigationFriendEventBuilder.buildRespondentsLitigationFriendEvent(builder, caseData);
        caseNotesEventBuilder.buildCaseNotesEvents(builder, caseData);
        breathingSpaceEventBuilder.buildBreathingSpaceEvents(builder, caseData);
        interlocutoryJudgementBuilder.buildInterlocutoryJudgment(builder, caseData);
        miscellaneousJudgmentEventBuilder.buildMiscellaneousJudgmentEvent(
            builder,
            caseData,
            PredicateUtils.grantedFlagDefendantPredicate,
            EventHistoryUtil.MISC_TEXT_REQUESTED_IJ,
            EventHistoryUtil.MISC_TEXT_GRANTED_IJ,
            caseData.getDefendantDetails()
        );
        defaultJudgementBuilder.buildDefaultJudgment(builder, caseData);
        miscellaneousJudgmentEventBuilder.buildMiscellaneousJudgmentEvent(
            builder,
            caseData,
            PredicateUtils.grantedFlagDefendantSpecPredicate,
            EventHistoryUtil.MISC_TEXT_REQUESTED_DJ,
            EventHistoryUtil.MISC_TEXT_GRANTED_DJ,
            caseData.getDefendantDetailsSpec()
        );
        informAgreedExtensionDateSpecBuilder.buildEvent(EventHistoryDTO.builder().builder(builder).caseData(caseData).build());
        takenOfflineAfterSdoBuilder.buildClaimTakenOfflineAfterDJ(builder, caseData);
        ccjEventBuilder.buildCcjEvent(builder, caseData);
        return eventHistorySequencer.sortEvents(builder.build());
    }

    private void handleEventScenario(CaseData caseData, String authToken, FlowState.Main flowState, EventHistory.EventHistoryBuilder builder) {
        EventBuilder eventBuilder = eventBuilderFactory.getBuilder(flowState);
        if (eventBuilder != null) {
            EventHistoryDTO eventHistoryDTO = EventHistoryDTO.builder().caseData(caseData).builder(builder).authToken(authToken).build();
            eventBuilder.buildEvent(eventHistoryDTO);
        }
    }
}
