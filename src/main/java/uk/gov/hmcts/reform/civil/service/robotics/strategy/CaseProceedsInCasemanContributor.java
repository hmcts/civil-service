package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.Event;
import uk.gov.hmcts.reform.civil.model.robotics.EventDetails;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.model.robotics.EventType;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

/**
 * Emits the “Case proceeds in Caseman” offline marker when an SDO order has taken the claim offline.
 */
@Component
@Order(50)
@RequiredArgsConstructor
public class CaseProceedsInCasemanContributor implements EventHistoryContributor {

    private final RoboticsEventTextFormatter textFormatter;
    private final RoboticsSequenceGenerator sequenceGenerator;

    @Override
    public boolean supports(CaseData caseData) {
        if (caseData == null || caseData.getTakenOfflineDate() == null) {
            return false;
        }

        boolean sdoDrawnAndFiled = caseData.getOrderSDODocumentDJ() != null;
        boolean takenOfflineAfterSdo = YesOrNo.NO.equals(caseData.getDrawDirectionsOrderRequired())
            && caseData.getReasonNotSuitableSDO() == null
            && caseData.getTakenOfflineByStaffDate() == null;

        return sdoDrawnAndFiled || takenOfflineAfterSdo;
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        String message = textFormatter.caseProceedsInCaseman();
        builder.miscellaneous(
            Event.builder()
                .eventSequence(sequenceGenerator.nextSequence(builder.build()))
                .eventCode(EventType.MISCELLANEOUS.getCode())
                .dateReceived(caseData.getTakenOfflineDate())
                .eventDetailsText(message)
                .eventDetails(EventDetails.builder().miscText(message).build())
                .build()
        );
    }
}
