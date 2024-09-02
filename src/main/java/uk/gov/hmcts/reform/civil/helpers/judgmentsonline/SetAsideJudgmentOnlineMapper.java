package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
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
            .rtlState(getJudgmentRTLStatus(activeJudgment.getIsRegisterWithRTL(), activeJudgment.getRtlState()))
            .build();
    }

    protected JudgmentState getJudgmentState(CaseData caseData) {
        return JudgmentSetAsideReason.JUDGE_ORDER
            .equals(caseData.getJoSetAsideReason()) ? JudgmentState.SET_ASIDE : JudgmentState.SET_ASIDE_ERROR;
    }

    protected String getJudgmentRTLStatus(YesOrNo isRegisteredWithRTL, String rtlState){
        return isRegisteredWithRTL.equals(YesOrNo.YES) && (rtlState.equals(JudgmentRTLStatus.ISSUED.getRtlState()) ||
            rtlState.equals(JudgmentRTLStatus.MODIFIED_EXISTING.getRtlState())) ?
            JudgmentRTLStatus.CANCELLED.getRtlState() : rtlState;
    }

    private LocalDate getSetAsideDate(CaseData caseData) {
        LocalDate currentDate = LocalDate.now();
        if (JudgmentSetAsideReason.JUDGE_ORDER.equals(caseData.getJoSetAsideReason())) {
            if (JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION.equals(caseData.getJoSetAsideOrderType())) {
                return caseData.getJoSetAsideOrderDate();
            } else {
                return caseData.getJoSetAsideDefenceReceivedDate();
            }
        } else {
            return currentDate;
        }
    }
}
