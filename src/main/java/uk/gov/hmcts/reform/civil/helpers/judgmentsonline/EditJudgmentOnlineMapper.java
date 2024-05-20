package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EditJudgmentOnlineMapper extends JudgmentOnlineMapper {

    @Override
    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {

        JudgmentDetails activeJudgment = caseData.getActiveJudgment();
        if (activeJudgment != null) {
            activeJudgment = activeJudgment.toBuilder()
                .state(getJudgmentState(caseData))
                .instalmentDetails(caseData.getJoInstalmentDetails())
                .paymentPlan(caseData.getJoPaymentPlan())
                .isRegisterWithRTL(caseData.getJoIsRegisteredWithRTL())
                .issueDate(caseData.getJoOrderMadeDate())
                .orderedAmount(caseData.getJoAmountOrdered())
                .costs(caseData.getJoAmountCostOrdered())
                .totalAmount(new BigDecimal(caseData.getJoAmountOrdered()).add(new BigDecimal(Optional.ofNullable(caseData.getJoAmountCostOrdered()).orElse("0"))).toString())
                .build();
        }
        return activeJudgment;
    }

    @Override
    protected JudgmentState getJudgmentState(CaseData caseData) {
        return JudgmentState.MODIFIED;
    }
}
