package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventSequence;

@Slf4j
@Component
@RequiredArgsConstructor
public class FullDefenceNotProceedBuilder extends BaseEventBuilder {

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();
        buildFullDefenceNotProceed(builder, caseData);
    }

    private void buildFullDefenceNotProceed(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        String miscText = getMultiPartyScenario(caseData).equals(TWO_V_ONE)
            ? "RPA Reason: Claimants intend not to proceed."
            : "RPA Reason: Claimant intends not to proceed.";

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
