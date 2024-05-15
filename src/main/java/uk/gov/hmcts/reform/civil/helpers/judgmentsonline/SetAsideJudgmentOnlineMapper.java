package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SetAsideJudgmentOnlineMapper extends JudgmentOnlineMapper {

    @Override
    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {

        JudgmentDetails activeJudgment = caseData.getActiveJudgment();
        return activeJudgment.toBuilder()
            .state(getJudgmentState(caseData))
            .setAsideDate(getSetAsideDate(caseData))
            .lastUpdateTimeStamp(LocalDateTime.now())
            .cancelledTimeStamp(LocalDateTime.now())
            .build();
    }

    protected JudgmentState getJudgmentState(CaseData caseData) {
        return JudgmentSetAsideReason.JUDGE_ORDER
            .equals(caseData.getJoSetAsideReason()) ? JudgmentState.SET_ASIDE : JudgmentState.SET_ASIDE_ERROR;
    }

    private LocalDate getSetAsideDate(CaseData caseData) {
        return JudgmentSetAsideReason.JUDGE_ORDER
            .equals(caseData.getJoSetAsideReason()) ? (JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION
            .equals(caseData.getJoSetAsideOrderType()) ? caseData.getJoSetAsideOrderDate() : caseData.getJoSetAsideDefenceReceivedDate()) : LocalDate.now();
    }
}
