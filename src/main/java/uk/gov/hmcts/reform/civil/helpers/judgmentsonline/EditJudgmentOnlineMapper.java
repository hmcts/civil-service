package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
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
            boolean alreadyRegistered = activeJudgment.getIsRegisterWithRTL() == YesOrNo.YES;
            activeJudgment = activeJudgment.toBuilder()
                .state(getJudgmentState(caseData))
                .rtlState(getRtlState(caseData, alreadyRegistered))
                .instalmentDetails(caseData.getJoInstalmentDetails())
                .paymentPlan(caseData.getJoPaymentPlan())
                .isRegisterWithRTL(caseData.getJoIsRegisteredWithRTL())
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

    protected String getRtlState(CaseData caseData, boolean alreadyRegistered) {

        if (alreadyRegistered && caseData.getJoIsRegisteredWithRTL() == YesOrNo.YES) {
            return JudgmentRTLStatus.MODIFIED_EXISTING.getRtlState();
        } else if (!alreadyRegistered && caseData.getJoIsRegisteredWithRTL() == YesOrNo.YES) {
            return JudgmentRTLStatus.MODIFIED_RTL_STATE.getRtlState();
        }
        return null;
    }
}
