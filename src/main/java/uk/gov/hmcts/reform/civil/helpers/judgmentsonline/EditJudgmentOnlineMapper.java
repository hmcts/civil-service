package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
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
            activeJudgment = activeJudgment.toBuilder()
                .state(getJudgmentState(caseData))
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

}
