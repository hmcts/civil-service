package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;

import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventSequence;

@Slf4j
@Component
@RequiredArgsConstructor
public class TakenOfflineDueToDependantNocBuilder extends BaseEventBuilder {

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        log.info("Building event: {} for case id: {} ", eventHistoryDTO.getEventType(), eventHistoryDTO.getCaseData().getCcdCaseReference());
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();
        buildTakenOfflineDueToDefendantNoc(builder, caseData);
    }

    private void buildTakenOfflineDueToDefendantNoc(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getTakenOfflineDate())
                .eventDetailsText("RPA Reason : Notice of Change filed.")
                .eventDetails(EventDetails.builder()
                    .miscText("RPA Reason : Notice of Change filed.")
                    .build())
                .build());
    }
}
