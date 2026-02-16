package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
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
public class RespondentFullAdmissionStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsRespondentResponseSupport respondentResponseSupport;
    private final RoboticsEventTextFormatter textFormatter;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null && hasFullAdmissionState(caseData);
    }

    @Override
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info(
                "Building respondent full admission robotics events for caseId {}",
                caseData.getCcdCaseReference());

        if (defendant1ResponseExists.test(caseData)) {
            LocalDateTime respondent1ResponseDate = caseData.getRespondent1ResponseDate();
            addReceiptOfAdmission(eventHistory, respondent1ResponseDate, RESPONDENT_ID);
            addMiscellaneous(
                    eventHistory, caseData, caseData.getRespondent1(), true, respondent1ResponseDate);
            addLipVsLrMisc(eventHistory, caseData);

            if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
                LocalDateTime respondent2ResponseDate =
                        respondentResponseSupport.resolveRespondent2ResponseDate(caseData);
                addReceiptOfAdmission(eventHistory, respondent2ResponseDate, RESPONDENT2_ID);
                addMiscellaneous(
                        eventHistory, caseData, caseData.getRespondent2(), false, respondent2ResponseDate);
            }
        }

        if (defendant2ResponseExists.test(caseData)) {
            LocalDateTime respondent2ResponseDate =
                    respondentResponseSupport.resolveRespondent2ResponseDate(caseData);
            addReceiptOfAdmission(eventHistory, respondent2ResponseDate, RESPONDENT2_ID);
            addMiscellaneous(
                    eventHistory, caseData, caseData.getRespondent2(), false, respondent2ResponseDate);
        }
    }

    private void addReceiptOfAdmission(
            EventHistory builder, LocalDateTime responseDate, String partyId) {
        List<Event> updatedReceiptOfAdmissionEvents1 =
                builder.getReceiptOfAdmission() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(builder.getReceiptOfAdmission());
        updatedReceiptOfAdmissionEvents1.add(
                createEvent(
                        sequenceGenerator.nextSequence(builder),
                        EventType.RECEIPT_OF_ADMISSION.getCode(),
                        responseDate,
                        partyId,
                        null,
                        null));
        builder.setReceiptOfAdmission(updatedReceiptOfAdmissionEvents1);
    }

    private void addMiscellaneous(
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

        List<Event> updatedMiscellaneousEvents2 =
                builder.getMiscellaneous() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(builder.getMiscellaneous());
        updatedMiscellaneousEvents2.add(
                buildLipVsLrMiscEvent(builder, sequenceGenerator, textFormatter));
        builder.setMiscellaneous(updatedMiscellaneousEvents2);
    }

    private boolean hasFullAdmissionState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
                .map(State::getName)
                .anyMatch(FlowState.Main.FULL_ADMISSION.fullName()::equals);
    }
}
