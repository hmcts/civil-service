package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.flowstate.IStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpecRejectRepaymentPlanStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsEventTextFormatter textFormatter;
    private final RoboticsTimelineHelper timelineHelper;
    private final IStateFlowEngine stateFlowEngine;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
                && caseData.hasApplicantRejectedRepaymentPlan()
                && hasRepaymentRejectionState(caseData);
    }

    @Override
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info(
                "Building spec reject repayment plan robotics event for caseId {}",
                caseData.getCcdCaseReference());

        String message = textFormatter.manualDeterminationRequired();
        List<Event> updatedMiscellaneousEvents1 =
                eventHistory.getMiscellaneous() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(eventHistory.getMiscellaneous());
        updatedMiscellaneousEvents1.add(
                buildMiscEvent(
                        eventHistory, sequenceGenerator, message, resolveApplicant1ResponseDate(caseData)));
        eventHistory.setMiscellaneous(updatedMiscellaneousEvents1);
    }

    private LocalDateTime resolveApplicant1ResponseDate(CaseData caseData) {
        LocalDateTime applicant1ResponseDate = caseData.getApplicant1ResponseDate();
        LocalDateTime now = LocalDateTime.now();
        if (applicant1ResponseDate == null || applicant1ResponseDate.isBefore(now)) {
            return now;
        }
        return applicant1ResponseDate;
    }

    private boolean hasRepaymentRejectionState(CaseData caseData) {
        StateFlow stateFlow = stateFlowEngine.evaluate(caseData);
        return stateFlow.getStateHistory().stream()
                .map(State::getName)
                .anyMatch(
                        name ->
                                FlowState.Main.PART_ADMIT_REJECT_REPAYMENT.fullName().equals(name)
                                        || FlowState.Main.FULL_ADMIT_REJECT_REPAYMENT.fullName().equals(name));
    }
}
