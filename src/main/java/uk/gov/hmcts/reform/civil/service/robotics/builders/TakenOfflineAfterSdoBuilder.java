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
public class TakenOfflineAfterSdoBuilder extends BaseEventBuilder {

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();
        buildClaimTakenOfflineAfterSDO(builder, caseData);
    }

    private void buildClaimTakenOfflineAfterSDO(EventHistory.EventHistoryBuilder builder,
                                                CaseData caseData) {
        String detailsText = "RPA Reason: Case Proceeds in Caseman.";
        builder.miscellaneous(
            Event.builder()
                .eventSequence(prepareEventSequence(builder.build()))
                .eventCode(MISCELLANEOUS.getCode())
                .dateReceived(caseData.getTakenOfflineDate())
                .eventDetailsText(detailsText)
                .eventDetails(EventDetails.builder()
                    .miscText(detailsText)
                    .build())
                .build());
    }

    public void buildClaimTakenOfflineAfterDJ(EventHistory.EventHistoryBuilder builder,
                                              CaseData caseData) {
        if (caseData.getTakenOfflineDate() != null && caseData.getOrderSDODocumentDJ() != null) {
            buildClaimTakenOfflineAfterSDO(builder, caseData);
        }
    }
}
