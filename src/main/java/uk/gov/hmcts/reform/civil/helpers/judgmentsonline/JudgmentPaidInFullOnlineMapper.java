package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class JudgmentPaidInFullOnlineMapper extends JudgmentOnlineMapper {

    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData, LocalDate paymentDate) {

        JudgmentDetails activeJudgment = caseData.getActiveJudgment();
        JudgmentState state = getJudgmentState(caseData, paymentDate);
        JudgmentDetails activeJudgmentDetails = activeJudgment.toBuilder()
            .state(state)
            .fullyPaymentMadeDate(nonNull(paymentDate) ? paymentDate : caseData.getJoJudgmentPaidInFull().getDateOfFullPaymentMade())
            .lastUpdateTimeStamp(LocalDateTime.now())
            .rtlState(getJudgmentRTLStatus(state))
            .cancelledTimeStamp(JudgmentState.CANCELLED.equals(state) ? LocalDateTime.now() : null)
            .cancelDate(JudgmentState.CANCELLED.equals(state) ? LocalDate.now() : null)
            .build();

        super.updateJudgmentTabDataWithActiveJudgment(activeJudgmentDetails, caseData);

        return activeJudgmentDetails;
    }

    @Override
    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {
        return addUpdateActiveJudgment(caseData, null);
    }

    protected JudgmentState getJudgmentState(CaseData caseData, LocalDate paymentDate) {
        boolean paidAfter31Days = JudgmentsOnlineHelper.checkIfDateDifferenceIsGreaterThan31Days(
            caseData.getActiveJudgment().getIssueDate(),
            nonNull(paymentDate) ? paymentDate : caseData.getJoJudgmentPaidInFull().getDateOfFullPaymentMade()
        );
        return paidAfter31Days ? JudgmentState.SATISFIED : JudgmentState.CANCELLED;
    }

    protected JudgmentState getJudgmentState(CaseData caseData) {
        return getJudgmentState(caseData, null);
    }

    protected String getJudgmentRTLStatus(JudgmentState state) {
        return JudgmentState.CANCELLED.equals(state) ? JudgmentRTLStatus.CANCELLED.getRtlState() : JudgmentRTLStatus.SATISFIED.getRtlState();
    }
}
