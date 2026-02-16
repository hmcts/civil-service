package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

@Slf4j
@Component
@RequiredArgsConstructor
public class CaseProceedsInCasemanStrategy implements EventHistoryStrategy {

    private final RoboticsEventTextFormatter textFormatter;
    private final RoboticsSequenceGenerator sequenceGenerator;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
                && caseData.getTakenOfflineDate() != null
                && caseData.getOrderSDODocumentDJ() != null;
    }

    @Override
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info(
                "Building case proceeds in Caseman robotics event for caseId {}",
                caseData.getCcdCaseReference());
        String message = textFormatter.caseProceedsInCaseman();
        List<Event> updatedMiscellaneousEvents1 =
                eventHistory.getMiscellaneous() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(eventHistory.getMiscellaneous());
        updatedMiscellaneousEvents1.add(
                buildMiscEvent(eventHistory, sequenceGenerator, message, caseData.getTakenOfflineDate()));
        eventHistory.setMiscellaneous(updatedMiscellaneousEvents1);
    }

    @Override
    public void contribute(
            EventHistory eventHistory, CaseData caseData, String authToken, FlowState.Main flowState) {
        if (caseData == null || flowState != FlowState.Main.TAKEN_OFFLINE_AFTER_SDO) {
            return;
        }
        log.info(
                "Building case proceeds in Caseman robotics event for caseId {}",
                caseData.getCcdCaseReference());
        String message = textFormatter.caseProceedsInCaseman();
        List<Event> updatedMiscellaneousEvents2 =
                eventHistory.getMiscellaneous() == null
                        ? new ArrayList<>()
                        : new ArrayList<>(eventHistory.getMiscellaneous());
        updatedMiscellaneousEvents2.add(
                buildMiscEvent(eventHistory, sequenceGenerator, message, caseData.getTakenOfflineDate()));
        eventHistory.setMiscellaneous(updatedMiscellaneousEvents2);
    }
}
