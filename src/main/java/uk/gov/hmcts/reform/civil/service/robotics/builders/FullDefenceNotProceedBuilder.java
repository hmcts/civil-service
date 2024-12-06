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

import java.util.Set;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_DEFENCE_NOT_PROCEED;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.RPA_CLAIMANTS_INTEND_NOT_TO_PROCEED;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.RPA_CLAIMANT_INTENDS_NOT_TO_PROCEED;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventSequence;

@Slf4j
@Component
@RequiredArgsConstructor
public class FullDefenceNotProceedBuilder extends BaseEventBuilder {

    @Override
    public Set<FlowState.Main> supportedFlowStates() {
        return Set.of(FULL_DEFENCE_NOT_PROCEED);
    }

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        log.info("Building event: {} for case id: {} ", eventHistoryDTO.getEventType(), eventHistoryDTO.getCaseData().getCcdCaseReference());
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();
        buildFullDefenceNotProceed(builder, caseData);
    }

    private void buildFullDefenceNotProceed(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        String miscText = getMultiPartyScenario(caseData).equals(TWO_V_ONE)
            ? RPA_CLAIMANTS_INTEND_NOT_TO_PROCEED
            : RPA_CLAIMANT_INTENDS_NOT_TO_PROCEED;

        builder.miscellaneous(Event.builder()
            .eventSequence(prepareEventSequence(builder.build()))
            .eventCode(MISCELLANEOUS.getCode())
            .dateReceived(caseData.getApplicant1ResponseDate())
            .eventDetailsText(miscText)
            .eventDetails(EventDetails.builder()
                .miscText(miscText)
                .build())
            .build());
    }
}
