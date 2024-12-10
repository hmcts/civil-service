package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.left;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.TAKEN_OFFLINE_SDO_NOT_DRAWN;

@Slf4j
@Component
@RequiredArgsConstructor
public class TakenOfflineSdoNotDrawnBuilder extends BaseEventBuilder {

    @Override
    public Set<FlowState.Main> supportedFlowStates() {
        return Set.of(TAKEN_OFFLINE_SDO_NOT_DRAWN);
    }

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        log.info("Building event: {} for case id: {} ", eventHistoryDTO.getEventType(), eventHistoryDTO.getCaseData().getCcdCaseReference());
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();
        buildSDONotDrawn(builder, caseData);
    }

    private void buildSDONotDrawn(EventHistory.EventHistoryBuilder builder,
                                  CaseData caseData) {

        String miscText = left(format(
            "RPA Reason: Case proceeds offline. "
                + "Judge / Legal Advisor did not draw a Direction's Order: %s",
            caseData.getReasonNotSuitableSDO().getInput()
        ), 250);

        LocalDateTime eventDate = caseData.getUnsuitableSDODate();

        List<String> miscTextList = new ArrayList<>();
        miscTextList.add(miscText);

        List<Event> miscTextEvent = prepareMiscEventList(builder, caseData, miscTextList, eventDate);
        builder.miscellaneous(miscTextEvent);
    }
}
