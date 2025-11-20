package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsPartyLookup;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsTimelineHelper;

@Component
@RequiredArgsConstructor
public class InterlocutoryJudgmentStrategy implements EventHistoryStrategy {

    private final RoboticsSequenceGenerator sequenceGenerator;
    private final RoboticsTimelineHelper timelineHelper;
    private final RoboticsPartyLookup partyLookup;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null || caseData.getHearingSupportRequirementsDJ() == null) {
            return false;
        }
        return !isGrantedForSingleRespondent(caseData);
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder,
                           CaseData caseData,
                           String authToken) {
        if (!supports(caseData)) {
            return;
        }

        builder.interlocutoryJudgment(buildEvent(builder, 0));
        if (caseData.getRespondent2() != null) {
            builder.interlocutoryJudgment(buildEvent(builder, 1));
        }
    }

    private Event buildEvent(EventHistory.EventHistoryBuilder builder, int respondentIndex) {
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.INTERLOCUTORY_JUDGMENT_GRANTED.getCode())
            .dateReceived(timelineHelper.now())
            .litigiousPartyID(partyLookup.respondentId(respondentIndex))
            .eventDetailsText("")
            .eventDetails(EventDetails.builder().miscText("").build())
            .build();
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
