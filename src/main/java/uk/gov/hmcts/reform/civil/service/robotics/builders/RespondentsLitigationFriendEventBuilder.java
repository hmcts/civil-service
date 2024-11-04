package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;

@Slf4j
@Component
@RequiredArgsConstructor
public class RespondentsLitigationFriendEventBuilder {

    public void buildRespondentsLitigationFriendEvent(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        boolean respondent1 = caseData.getRespondent1LitigationFriendCreatedDate() != null;
        boolean respondent2 = caseData.getRespondent2LitigationFriendCreatedDate() != null;

        if (respondent1) {
            buildMiscellaneousRespondentLitigationFriendEvent(
                builder,
                caseData.getRespondent1(),
                caseData.getRespondent1LitigationFriendCreatedDate()
            );
        }
        if (respondent2) {
            buildMiscellaneousRespondentLitigationFriendEvent(
                builder,
                caseData.getRespondent2(),
                caseData.getRespondent2LitigationFriendCreatedDate()
            );
        }

    }

    private void buildMiscellaneousRespondentLitigationFriendEvent(EventHistory.EventHistoryBuilder builder,
                                                                   Party respondent, LocalDateTime litigationFriendCreatedDate) {
        String miscText = "Litigation friend added for respondent: " + respondent.getPartyName();
        builder.miscellaneous(
            Event.builder()
                .eventSequence(EventHistoryUtil.prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(litigationFriendCreatedDate)
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                    .miscText(miscText)
                    .build())
                .build());
    }
}
