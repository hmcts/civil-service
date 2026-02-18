package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.createEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsPartyLookup;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterlocutoryJudgmentStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsPartyLookup partyLookup;
    private final RoboticsEventTextFormatter textFormatter;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null) {
            return false;
        }
        boolean hasHearingSupport = caseData.getHearingSupportRequirementsDJ() != null;
        boolean hasDefendantDetails = caseData.getDefendantDetails() != null;

        if (!hasHearingSupport && !hasDefendantDetails) {
            return false;
        }

        return hasDefendantDetails || !isGrantedForSingleRespondent(caseData);
    }

    @Override
    public void contribute(EventHistory eventHistory, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        log.info(
                "Building InterlocutoryJudgmentStrategy events for caseId {}",
                caseData.getCcdCaseReference());

        boolean grantedForSingleRespondent = isGrantedForSingleRespondent(caseData);

        if (caseData.getHearingSupportRequirementsDJ() != null && !grantedForSingleRespondent) {
            List<Event> updatedInterlocutoryJudgmentEvents1 =
                    eventHistory.getInterlocutoryJudgment() == null
                            ? new ArrayList<>()
                            : new ArrayList<>(eventHistory.getInterlocutoryJudgment());
            updatedInterlocutoryJudgmentEvents1.add(buildEvent(eventHistory, 0));
            eventHistory.setInterlocutoryJudgment(updatedInterlocutoryJudgmentEvents1);
            if (caseData.getRespondent2() != null) {
                List<Event> updatedInterlocutoryJudgmentEvents2 =
                        eventHistory.getInterlocutoryJudgment() == null
                                ? new ArrayList<>()
                                : new ArrayList<>(eventHistory.getInterlocutoryJudgment());
                updatedInterlocutoryJudgmentEvents2.add(buildEvent(eventHistory, 1));
                eventHistory.setInterlocutoryJudgment(updatedInterlocutoryJudgmentEvents2);
            }
        }

        if (caseData.getDefendantDetails() != null) {
            List<Event> updatedMiscellaneousEvents3 =
                    eventHistory.getMiscellaneous() == null
                            ? new ArrayList<>()
                            : new ArrayList<>(eventHistory.getMiscellaneous());
            updatedMiscellaneousEvents3.add(
                    buildMiscEvent(
                            eventHistory,
                            sequenceGenerator,
                            resolveMiscMessage(grantedForSingleRespondent),
                            LocalDateTime.now()));
            eventHistory.setMiscellaneous(updatedMiscellaneousEvents3);
        }
    }

    private String resolveMiscMessage(boolean grantedForSingleRespondent) {
        return grantedForSingleRespondent
                ? textFormatter.summaryJudgmentRequested()
                : textFormatter.summaryJudgmentGranted();
    }

    private Event buildEvent(EventHistory builder, int respondentIndex) {
        return createEvent(
                sequenceGenerator.nextSequence(builder),
                EventType.INTERLOCUTORY_JUDGMENT_GRANTED.getCode(),
                LocalDateTime.now(),
                partyLookup.respondentId(respondentIndex),
                "",
                new EventDetails().setMiscText(""));
    }

    private boolean isGrantedForSingleRespondent(CaseData caseData) {
        if (caseData.getRespondent2() == null) {
            return false;
        }

        DynamicList defendantDetails = caseData.getDefendantDetails();
        if (defendantDetails == null) {
            return false;
        }

        DynamicListElement selected = defendantDetails.getValue();
        String label = selected != null ? selected.getLabel() : null;
        return label != null && !label.startsWith("Both");
    }
}
