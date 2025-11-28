package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsRespondentResponseSupport;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getRespondent1DQOrDefault;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getRespondent2DQOrDefault;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildDirectionsQuestionnaireEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildLipVsLrMiscEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1v2SameSolicitorSameResponse;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ResponseExists;

@Slf4j
@Component
@RequiredArgsConstructor
public class RespondentPartAdmissionStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsRespondentResponseSupport respondentResponseSupport;
    private final RoboticsTimelineHelper timelineHelper;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null) {
            return false;
        }
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        boolean hasState = stateFlow.getStateHistory().stream()
            .map(State::getName)
            .anyMatch(FlowState.Main.PART_ADMISSION.fullName()::equals);

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
        log.info("Building respondent part admission robotics events for caseId {}", caseData.getCcdCaseReference());

        EventHistory existingHistory = builder.build();
        List<Event> directions = Optional.ofNullable(existingHistory.getDirectionsQuestionnaireFiled())
            .orElse(List.of())
            .stream()
            .filter(event -> event.getEventCode() != null)
            .toList();
        builder.clearDirectionsQuestionnaireFiled();
        directions.forEach(builder::directionsQuestionnaire);

        if (defendant1ResponseExists.test(caseData)) {
            addLipVsLrMisc(builder, caseData);

            if (useStatesPaid(caseData)) {
                builder.statesPaid(addStatesPaidEvent(builder, caseData.getRespondent1ResponseDate()));
            } else {
                builder.receiptOfPartAdmission(addReceiptOfPartAdmissionEvent(builder, caseData.getRespondent1ResponseDate(), RESPONDENT_ID));
            }

            addRespondentMisc(builder, caseData, caseData.getRespondent1(), true, caseData.getRespondent1ResponseDate());

            builder.directionsQuestionnaire(buildDirectionsQuestionnaireEvent(
                builder,
                sequenceGenerator,
                caseData.getRespondent1ResponseDate(),
                RESPONDENT_ID,
                getRespondent1DQOrDefault(caseData),
                getPreferredCourtCode(getRespondent1DQOrDefault(caseData)),
                prepareEventDetailsText(caseData, getRespondent1DQOrDefault(caseData), caseData.getRespondent1(), true)
            ));

            if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
                builder.receiptOfPartAdmission(addReceiptOfPartAdmissionEvent(
                    builder,
                    respondentResponseSupport.resolveRespondent2ResponseDate(caseData),
                    RESPONDENT2_ID
                ));
                addRespondentMisc(
                    builder,
                    caseData,
                    caseData.getRespondent2(),
                    false,
                    respondentResponseSupport.resolveRespondent2ResponseDate(caseData)
                );
                builder.directionsQuestionnaire(buildDirectionsQuestionnaireEvent(
                    builder,
                    sequenceGenerator,
                    respondentResponseSupport.resolveRespondent2ResponseDate(caseData),
                    RESPONDENT2_ID,
                    getRespondent1DQOrDefault(caseData),
                    getPreferredCourtCode(getRespondent1DQOrDefault(caseData)),
                    prepareEventDetailsText(caseData, getRespondent1DQOrDefault(caseData), caseData.getRespondent2(), true)
                ));
            }
        }

        if (defendant2ResponseExists.test(caseData)) {
            builder.receiptOfPartAdmission(addReceiptOfPartAdmissionEvent(builder, caseData.getRespondent2ResponseDate(), RESPONDENT2_ID));
            addRespondentMisc(builder, caseData, caseData.getRespondent2(), false, caseData.getRespondent2ResponseDate());

            builder.directionsQuestionnaire(buildDirectionsQuestionnaireEvent(
                builder,
                sequenceGenerator,
                caseData.getRespondent2ResponseDate(),
                RESPONDENT2_ID,
                getRespondent2DQOrDefault(caseData),
                getPreferredCourtCode(getRespondent2DQOrDefault(caseData)),
                prepareEventDetailsText(caseData, getRespondent2DQOrDefault(caseData), caseData.getRespondent2(), false)
            ));
        }
    }

    private boolean useStatesPaid(CaseData caseData) {
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && caseData.getSpecDefenceAdmittedRequired() == YES;
    }

    private Event addReceiptOfPartAdmissionEvent(EventHistory.EventHistoryBuilder builder,
                                                 LocalDateTime responseDate,
                                                 String partyId) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.RECEIPT_OF_PART_ADMISSION.getCode())
            .dateReceived(responseDate)
            .litigiousPartyID(partyId)
            .build();
    }

    private Event addStatesPaidEvent(EventHistory.EventHistoryBuilder builder,
                                     LocalDateTime responseDate) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.STATES_PAID.getCode())
            .dateReceived(responseDate)
            .litigiousPartyID(uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID)
            .build();
    }

    private void addRespondentMisc(EventHistory.EventHistoryBuilder builder,
                                   CaseData caseData,
                                   Party respondent,
                                   boolean isRespondent1,
                                   LocalDateTime responseDate) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return;
        }
        respondentResponseSupport.addRespondentMiscEvent(builder, sequenceGenerator, caseData, respondent, isRespondent1, responseDate);
    }

    private void addLipVsLrMisc(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (!caseData.isLipvLROneVOne()) {
            return;
        }

        builder.miscellaneous(buildLipVsLrMiscEvent(builder, sequenceGenerator, textFormatter, timelineHelper));
    }

    private String prepareEventDetailsText(CaseData caseData,
                                           DQ dq,
                                           Party respondent,
                                           boolean isRespondent1) {
        return respondentResponseSupport.prepareFullDefenceEventText(dq, caseData, isRespondent1, respondent);
    }
}
