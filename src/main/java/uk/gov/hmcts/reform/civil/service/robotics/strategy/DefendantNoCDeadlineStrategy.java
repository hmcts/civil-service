package uk.gov.hmcts.reform.civil.service.robotics.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventTextFormatter;
import uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsSequenceGenerator;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.civil.service.robotics.support.RoboticsEventSupport.buildMiscEvent;

@Component
@Order(40)
@RequiredArgsConstructor
public class DefendantNoCDeadlineStrategy implements EventHistoryStrategy {

    private final RoboticsEventTextFormatter textFormatter;
    private final RoboticsSequenceGenerator sequenceGenerator;

    @Override
    public boolean supports(CaseData caseData) {
        return caseData != null
            && caseData.getTakenOfflineDate() != null
            && hasNoCDeadlinePassed(caseData, caseData.getTakenOfflineDate());
    }

    @Override
    public void contribute(EventHistory.EventHistoryBuilder builder, CaseData caseData, String authToken) {
        if (!supports(caseData)) {
            return;
        }
        LocalDateTime takenOfflineDate = requireNonNull(caseData.getTakenOfflineDate());
        String details = textFormatter.claimMovedOfflineAfterNocDeadline();
        builder.miscellaneous(buildMiscEvent(builder, sequenceGenerator, details, takenOfflineDate));
    }

    private boolean hasNoCDeadlinePassed(CaseData caseData, LocalDateTime takenOfflineDate) {
        return (caseData.getAddLegalRepDeadlineRes1() != null
            && takenOfflineDate.isAfter(caseData.getAddLegalRepDeadlineRes1())
            && YesOrNo.NO.equals(caseData.getRespondent1Represented()))
            || (caseData.getAddLegalRepDeadlineRes2() != null
            && takenOfflineDate.isAfter(caseData.getAddLegalRepDeadlineRes2())
            && YesOrNo.NO.equals(caseData.getRespondent2Represented()));
    }
}
