package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil;
import uk.gov.hmcts.reform.civil.utils.PredicateUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.model.robotics.EventType.INTERLOCUTORY_JUDGMENT_GRANTED;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.RESPONDENT_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterlocutoryJudgementBuilder {

    public void buildInterlocutoryJudgment(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        List<Event> events = new ArrayList<>();

        boolean grantedFlag = PredicateUtils.grantedFlagDefendantPredicate.test(caseData);
        if (!grantedFlag && null != caseData.getHearingSupportRequirementsDJ()) {
            events.add(prepareInterlocutoryJudgment(builder, RESPONDENT_ID));

            if (null != caseData.getRespondent2()) {
                events.add(prepareInterlocutoryJudgment(builder, RESPONDENT2_ID));
            }
            builder.interlocutoryJudgment(events);
        }
    }

    private Event prepareInterlocutoryJudgment(EventHistory.EventHistoryBuilder builder,
                                               String litigiousPartyID) {
        return (Event.builder()
            .eventSequence(EventHistoryUtil.prepareEventSequence(builder.build()))
            .eventCode(INTERLOCUTORY_JUDGMENT_GRANTED.getCode())
            .dateReceived(LocalDateTime.now())
            .litigiousPartyID(litigiousPartyID)
            .eventDetailsText("")
            .eventDetails(EventDetails.builder().miscText("")
                .build())
            .build());
    }
}
