package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventSequence;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1ResponseExists;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant1v2SameSolicitorSameResponse;
import static uk.gov.hmcts.reform.civil.utils.PredicateUtils.defendant2ResponseExists;

@Slf4j
@Component
@AllArgsConstructor
public class CounterClaimBuilder extends BaseEventBuilder {

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        log.info("Building event: {} for case id: {} ", eventHistoryDTO.getEventType(), eventHistoryDTO.getCaseData().getCcdCaseReference());
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();
        buildRespondentCounterClaim(builder, caseData);
    }

    private void buildRespondentCounterClaim(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        String miscText;
        if (defendant1ResponseExists.test(caseData)) {
            miscText = prepareRespondentResponseText(caseData, caseData.getRespondent1(), true);
            builder.miscellaneous(Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getRespondent1ResponseDate())
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                    .miscText(miscText)
                    .build())
                .build());
            if (defendant1v2SameSolicitorSameResponse.test(caseData)) {
                LocalDateTime respondent2ResponseDate = null != caseData.getRespondent2ResponseDate()
                    ? caseData.getRespondent2ResponseDate() : caseData.getRespondent1ResponseDate();
                miscText = prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);
                builder.miscellaneous(Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(respondent2ResponseDate)
                    .eventDetailsText(miscText)
                    .eventDetails(EventDetails.builder()
                        .miscText(miscText)
                        .build())
                    .build());
            }
        }
        if (defendant2ResponseExists.test(caseData)) {
            miscText = prepareRespondentResponseText(caseData, caseData.getRespondent2(), false);
            builder.miscellaneous(Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getRespondent2ResponseDate())
                .eventDetailsText(miscText)
                .eventDetails(EventDetails.builder()
                    .miscText(miscText)
                    .build())
                .build());
        }
    }
}
