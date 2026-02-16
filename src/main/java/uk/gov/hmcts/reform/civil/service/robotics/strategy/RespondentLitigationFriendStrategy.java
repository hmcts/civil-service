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
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

@Slf4j
@Component
@RequiredArgsConstructor
public class RespondentLitigationFriendStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
                && (caseData.getRespondent1LitigationFriendCreatedDate() != null
                        || caseData.getRespondent2LitigationFriendCreatedDate() != null);
    }

    @Override
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info(
                "Building litigation friend robotics events for caseId {}", caseData.getCcdCaseReference());

        if (caseData.getRespondent1LitigationFriendCreatedDate() != null) {
            List<Event> updatedMiscellaneousEvents1 =
                    eventHistory.getMiscellaneous() == null
                            ? new ArrayList<>()
                            : new ArrayList<>(eventHistory.getMiscellaneous());
            updatedMiscellaneousEvents1.add(
                    buildMiscEvent(
                            eventHistory,
                            sequenceGenerator,
                            "Litigation friend added for respondent: " + caseData.getRespondent1().getPartyName(),
                            caseData.getRespondent1LitigationFriendCreatedDate()));
            eventHistory.setMiscellaneous(updatedMiscellaneousEvents1);
        }
        if (caseData.getRespondent2LitigationFriendCreatedDate() != null) {
            List<Event> updatedMiscellaneousEvents2 =
                    eventHistory.getMiscellaneous() == null
                            ? new ArrayList<>()
                            : new ArrayList<>(eventHistory.getMiscellaneous());
            updatedMiscellaneousEvents2.add(
                    buildMiscEvent(
                            eventHistory,
                            sequenceGenerator,
                            "Litigation friend added for respondent: " + caseData.getRespondent2().getPartyName(),
                            caseData.getRespondent2LitigationFriendCreatedDate()));
            eventHistory.setMiscellaneous(updatedMiscellaneousEvents2);
        }
    }
}
