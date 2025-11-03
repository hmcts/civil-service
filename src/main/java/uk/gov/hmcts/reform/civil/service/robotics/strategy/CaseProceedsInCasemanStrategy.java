package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

@Component
@Order(50)
@RequiredArgsConstructor
public class CaseProceedsInCasemanStrategy implements EventHistoryStrategy {

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
        builder.miscellaneous(buildMiscEvent(builder, sequenceGenerator, message, caseData.getTakenOfflineDate()));
    }
}
