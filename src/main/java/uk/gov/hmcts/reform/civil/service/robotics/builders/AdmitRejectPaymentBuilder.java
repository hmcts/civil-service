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

import static uk.gov.hmcts.reform.civil.model.robotics.EventType.MISCELLANEOUS;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.FULL_ADMIT_REJECT_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowState.Main.PART_ADMIT_REJECT_REPAYMENT;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.RPA_REASON_MANUAL_DETERMINATION;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.prepareEventSequence;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.EventHistoryUtil.setApplicant1ResponseDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdmitRejectPaymentBuilder extends BaseEventBuilder {

    @Override
    public Set<FlowState.Main> supportedFlowStates() {
        return Set.of(PART_ADMIT_REJECT_REPAYMENT,
            FULL_ADMIT_REJECT_REPAYMENT);
    }

    @Override
    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        log.info("Building event {} for case {} ", eventHistoryDTO.getEventType(), eventHistoryDTO.getCaseData().getCcdCaseReference());
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();
        buildSpecAdmitRejectRepayment(builder, caseData);
    }

    private void buildSpecAdmitRejectRepayment(EventHistory.EventHistoryBuilder builder,
                                               CaseData caseData) {
        if (caseData.hasApplicantRejectedRepaymentPlan()) {
            builder.miscellaneous(
                Event.builder()
                    .eventSequence(prepareEventSequence(builder.build()))
                    .eventCode(MISCELLANEOUS.getCode())
                    .dateReceived(setApplicant1ResponseDate(caseData))
                    .eventDetailsText(RPA_REASON_MANUAL_DETERMINATION)
                    .eventDetails(EventDetails.builder()
                        .miscText(RPA_REASON_MANUAL_DETERMINATION)
                        .build())
                    .build());
        }
    }
}
