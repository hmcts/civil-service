package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.RPA_REASON_ONLY_ONE_OF_THE_RESPONDENT_IS_NOTIFIED;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventSequence;

@Slf4j
@Component
@RequiredArgsConstructor
public class TakenOfflineAfterClaimNotifiedBuilder extends BaseEventBuilder {

    @Override
    public Set<FlowState.Main> supportedFlowStates() {
        return Set.of(TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED);
    }

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        log.info("Building event: {} for case id: {} ", eventHistoryDTO.getEventType(), eventHistoryDTO.getCaseData().getCcdCaseReference());
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();
        buildTakenOfflineAfterClaimNotified(builder, caseData);
    }

    private void buildTakenOfflineAfterClaimNotified(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        builder.miscellaneous(
            List.of(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(caseData.getSubmittedDate())
                    .eventDetailsText(RPA_REASON_ONLY_ONE_OF_THE_RESPONDENT_IS_NOTIFIED)
                    .eventDetails(EventDetails.builder()
                        .miscText(RPA_REASON_ONLY_ONE_OF_THE_RESPONDENT_IS_NOTIFIED)
                        .build())
                    .build()
            ));
    }
}
