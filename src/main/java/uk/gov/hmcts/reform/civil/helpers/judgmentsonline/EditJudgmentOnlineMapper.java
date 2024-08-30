package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EditJudgmentOnlineMapper extends JudgmentOnlineMapper {

    @Override
    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {
        BigDecimal orderAmount = JudgmentsOnlineHelper.getMoneyValue(caseData.getJoAmountOrdered());
        BigDecimal costs = JudgmentsOnlineHelper.getMoneyValue(caseData.getJoAmountCostOrdered());
        JudgmentDetails activeJudgment = caseData.getActiveJudgment();
        if (activeJudgment != null) {
            activeJudgment = activeJudgment.toBuilder()
                .state(getJudgmentState(caseData))
                .instalmentDetails(caseData.getJoInstalmentDetails())
                .paymentPlan(caseData.getJoPaymentPlan())
                .isRegisterWithRTL(getIsRegisterWithRtl(activeJudgment, caseData.getJoIsRegisteredWithRTL()))
                .rtlState(getRtlState(caseData.getJoIsRegisteredWithRTL(), activeJudgment.getRtlState()))
                .issueDate(caseData.getJoOrderMadeDate())
                .orderedAmount(orderAmount.toString())
                .costs(costs.toString())
                .totalAmount(orderAmount.add(costs).toString())
                .build();
        }
        return activeJudgment;
    }

    @Override
    protected JudgmentState getJudgmentState(CaseData caseData) {
        return JudgmentState.MODIFIED;
    }

    protected String getRtlState(YesOrNo isJoRegisterWithRTL, String rtlState) {
        if (rtlState != null) {
            return (rtlState.equalsIgnoreCase(JudgmentRTLStatus.ISSUED.getRtlState()) || rtlState.equalsIgnoreCase(
                JudgmentRTLStatus.MODIFIED_EXISTING.getRtlState()))
                ? JudgmentRTLStatus.MODIFIED_EXISTING.getRtlState() : rtlState;
        }
        return isJoRegisterWithRTL == YesOrNo.YES ? JudgmentRTLStatus.ISSUED.getRtlState() : rtlState;
    }

    protected YesOrNo getIsRegisterWithRtl(JudgmentDetails activeJudgment, YesOrNo isJoRegisterWithRTL) {
        return !(JudgmentType.DEFAULT_JUDGMENT.equals(activeJudgment.getType()) || JudgmentType.JUDGMENT_BY_ADMISSION.equals(
            activeJudgment.getType())) ? isJoRegisterWithRTL : activeJudgment.getIsRegisterWithRTL();
    }
}
