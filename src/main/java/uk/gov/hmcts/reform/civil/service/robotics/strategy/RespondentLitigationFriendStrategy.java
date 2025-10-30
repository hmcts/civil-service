package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(60)
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
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }

        List<Event> events = new ArrayList<>();
        if (caseData.getRespondent1LitigationFriendCreatedDate() != null) {
            events.add(createEvent(
                builder,
                caseData.getRespondent1LitigationFriendCreatedDate(),
                caseData.getRespondent1().getPartyName()
            ));
        }
        if (caseData.getRespondent2LitigationFriendCreatedDate() != null) {
            events.add(createEvent(
                builder,
                caseData.getRespondent2LitigationFriendCreatedDate(),
                caseData.getRespondent2().getPartyName()
            ));
        }

        events.forEach(builder::miscellaneous);
    }

    private Event createEvent(EventHistory.EventHistoryBuilder builder, LocalDateTime createdDate, String partyName) {
        String message = "Litigation friend added for respondent: " + partyName;
        return Event.builder()
            .eventSequence(sequenceGenerator.nextSequence(builder.build()))
            .eventCode(EventType.MISCELLANEOUS.getCode())
            .dateReceived(createdDate)
            .eventDetailsText(message)
            .eventDetails(EventDetails.builder().miscText(message).build())
            .build();
    }
}
