package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getRespondent1DQOrDefault;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsDirectionsQuestionnaireSupport.getRespondent2DQOrDefault;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildDirectionsQuestionnaireEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildLipVsLrMiscEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.createEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1v2SameSolicitorSameResponse;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ResponseExists;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

@Slf4j
@Component
@RequiredArgsConstructor
public class RespondentPartAdmissionStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsRespondentResponseSupport respondentResponseSupport;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null) {
            return false;
        }
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);

        return stateFlow.getStateHistory().stream()
                .map(State::getName)
                .anyMatch(FlowState.Main.PART_ADMISSION.fullName()::equals);
    }

    @Override
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info(
                "Building respondent part admission robotics events for caseId {}",
                caseData.getCcdCaseReference());

        DirectionsQuestionnaireCollector dqCollector = new DirectionsQuestionnaireCollector();

        if (defendant1ResponseExists.test(caseData)) {
            addLipVsLrMisc(eventHistory, caseData);

            if (useStatesPaid(caseData)) {
                List<Event> updatedStatesPaidEvents1 =
                        eventHistory.getStatesPaid() == null
                                ? new ArrayList<>()
                                : new ArrayList<>(eventHistory.getStatesPaid());
                updatedStatesPaidEvents1.add(
                        addStatesPaidEvent(eventHistory, caseData.getRespondent1ResponseDate()));
                eventHistory.setStatesPaid(updatedStatesPaidEvents1);
            } else {
                List<Event> updatedReceiptOfPartAdmissionEvents2 =
                        eventHistory.getReceiptOfPartAdmission() == null
                                ? new ArrayList<>()
                                : new ArrayList<>(eventHistory.getReceiptOfPartAdmission());
                updatedReceiptOfPartAdmissionEvents2.add(
                        addReceiptOfPartAdmissionEvent(
                                eventHistory, caseData.getRespondent1ResponseDate(), RESPONDENT_ID));
                eventHistory.setReceiptOfPartAdmission(updatedReceiptOfPartAdmissionEvents2);
            }

            addRespondentMisc(
                    eventHistory,
                    caseData,
                    caseData.getRespondent1(),
                    true,
                    caseData.getRespondent1ResponseDate());

            dqCollector.add(
                    buildDirectionsQuestionnaireEvent(
                            eventHistory,
                            sequenceGenerator,
                            caseData.getRespondent1ResponseDate(),
                            RESPONDENT_ID,
                            getRespondent1DQOrDefault(caseData),
                            getPreferredCourtCode(getRespondent1DQOrDefault(caseData)),
                            prepareEventDetailsText(
                                    caseData, getRespondent1DQOrDefault(caseData), caseData.getRespondent1(), true)));

            if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
                List<Event> updatedReceiptOfPartAdmissionEvents3 =
                        eventHistory.getReceiptOfPartAdmission() == null
                                ? new ArrayList<>()
                                : new ArrayList<>(eventHistory.getReceiptOfPartAdmission());
                updatedReceiptOfPartAdmissionEvents3.add(
                        addReceiptOfPartAdmissionEvent(
                                eventHistory,
                                respondentResponseSupport.resolveRespondent2ResponseDate(caseData),
                                RESPONDENT2_ID));
                eventHistory.setReceiptOfPartAdmission(updatedReceiptOfPartAdmissionEvents3);
                addRespondentMisc(
                        eventHistory,
                        caseData,
                        caseData.getRespondent2(),
                        false,
                        respondentResponseSupport.resolveRespondent2ResponseDate(caseData));
                dqCollector.add(
                        buildDirectionsQuestionnaireEvent(
                                eventHistory,
                                sequenceGenerator,
                                respondentResponseSupport.resolveRespondent2ResponseDate(caseData),
                                RESPONDENT2_ID,
                                getRespondent1DQOrDefault(caseData),
                                getPreferredCourtCode(getRespondent1DQOrDefault(caseData)),
                                prepareEventDetailsText(
                                        caseData,
                                        getRespondent1DQOrDefault(caseData),
                                        caseData.getRespondent2(),
                                        true)));
            }
        }

        if (defendant2ResponseExists.test(caseData)) {
            List<Event> updatedReceiptOfPartAdmissionEvents4 =
                    eventHistory.getReceiptOfPartAdmission() == null
                            ? new ArrayList<>()
                            : new ArrayList<>(eventHistory.getReceiptOfPartAdmission());
            updatedReceiptOfPartAdmissionEvents4.add(
                    addReceiptOfPartAdmissionEvent(
                            eventHistory, caseData.getRespondent2ResponseDate(), RESPONDENT2_ID));
            eventHistory.setReceiptOfPartAdmission(updatedReceiptOfPartAdmissionEvents4);
            addRespondentMisc(
                    eventHistory,
                    caseData,
                    caseData.getRespondent2(),
                    false,
                    caseData.getRespondent2ResponseDate());

            dqCollector.add(
                    buildDirectionsQuestionnaireEvent(
                            eventHistory,
                            sequenceGenerator,
                            caseData.getRespondent2ResponseDate(),
                            RESPONDENT2_ID,
                            getRespondent2DQOrDefault(caseData),
                            getPreferredCourtCode(getRespondent2DQOrDefault(caseData)),
                            prepareEventDetailsText(
                                    caseData,
                                    getRespondent2DQOrDefault(caseData),
                                    caseData.getRespondent2(),
                                    false)));
        }

        eventHistory.setDirectionsQuestionnaireFiled(new ArrayList<>());
        List<Event> eventsToAdd5 = dqCollector.events();
        List<Event> updatedDirectionsQuestionnaireFiledEvents5 =
                eventHistory.getDirectionsQuestionnaireFiled() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(eventHistory.getDirectionsQuestionnaireFiled());
        updatedDirectionsQuestionnaireFiledEvents5.addAll(eventsToAdd5);
        eventHistory.setDirectionsQuestionnaireFiled(updatedDirectionsQuestionnaireFiledEvents5);
    }

    private boolean useStatesPaid(CaseData caseData) {
        return SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                && caseData.getSpecDefenceAdmittedRequired() == YES;
    }

    private Event addReceiptOfPartAdmissionEvent(
            EventHistory builder, LocalDateTime responseDate, String partyId) {
        return createEvent(
                sequenceGenerator.nextSequence(builder),
                EventType.RECEIPT_OF_PART_ADMISSION.getCode(),
                responseDate,
                partyId,
                null,
                null);
    }

    private Event addStatesPaidEvent(EventHistory builder, LocalDateTime responseDate) {
        return createEvent(
                sequenceGenerator.nextSequence(builder),
                EventType.STATES_PAID.getCode(),
                responseDate,
                uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID,
                null,
                null);
    }

    private void addRespondentMisc(
            EventHistory builder,
            CaseData caseData,
            Party respondent,
            boolean isRespondent1,
            LocalDateTime responseDate) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())) {
            return;
        }
        respondentResponseSupport.addRespondentMiscEvent(
                builder, sequenceGenerator, caseData, respondent, isRespondent1, responseDate);
    }

    private void addLipVsLrMisc(EventHistory builder, CaseData caseData) {
        if (!caseData.isLipvLROneVOne()) {
            return;
        }

        List<Event> updatedMiscellaneousEvents6 =
                builder.getMiscellaneous() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(builder.getMiscellaneous());
        updatedMiscellaneousEvents6.add(
                buildLipVsLrMiscEvent(builder, sequenceGenerator, textFormatter));
        builder.setMiscellaneous(updatedMiscellaneousEvents6);
    }

    private String prepareEventDetailsText(
            CaseData caseData, DQ dq, Party respondent, boolean isRespondent1) {
        return respondentResponseSupport.prepareFullDefenceEventText(
                dq, caseData, isRespondent1, respondent);
    }

    private static final class DirectionsQuestionnaireCollector {
        private final List<Event> events = new ArrayList<>();

        private void add(Event event) {
            events.add(event);
        }

        private List<Event> events() {
            return events;
        }
    }
}
