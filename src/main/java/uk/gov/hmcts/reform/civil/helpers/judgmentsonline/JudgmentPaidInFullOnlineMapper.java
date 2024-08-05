package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgmentPaidInFullOnlineMapper extends JudgmentOnlineMapper {

    @Override
    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {

        JudgmentDetails activeJudgment = caseData.getActiveJudgment();
        JudgmentState state = getJudgmentState(caseData);
        YesOrNo isRegisteredWithRTL = activeJudgment.getIsRegisterWithRTL();
        return activeJudgment.toBuilder()
            .state(state)
            .rtlState(getRtlState(isRegisteredWithRTL, state))
            .fullyPaymentMadeDate(caseData.getJoJudgmentPaidInFull().getDateOfFullPaymentMade())
            .lastUpdateTimeStamp(LocalDateTime.now())
            .cancelledTimeStamp(JudgmentState.CANCELLED.equals(state) ? LocalDateTime.now() : null)
            .cancelDate(JudgmentState.CANCELLED.equals(state) ? LocalDate.now() : null)
            .build();
    }

    protected JudgmentState getJudgmentState(CaseData caseData) {
        boolean paidAfter31Days = JudgmentsOnlineHelper.checkIfDateDifferenceIsGreaterThan31Days(
            caseData.getActiveJudgment().getIssueDate(),
            caseData.getJoJudgmentPaidInFull().getDateOfFullPaymentMade()
        );
        return paidAfter31Days ? JudgmentState.SATISFIED : JudgmentState.CANCELLED;
    }

    protected String getRtlState(YesOrNo isRegisteredWithRTL, JudgmentState state) {

        if (isRegisteredWithRTL == YesOrNo.YES) {
            return state == JudgmentState.CANCELLED ? JudgmentRTLStatus.CANCELLED.getRtlState() : JudgmentRTLStatus.SATISFIED.getRtlState();
        }
        return null;
    }
}
