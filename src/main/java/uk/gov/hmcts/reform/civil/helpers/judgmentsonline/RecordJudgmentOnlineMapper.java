package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordJudgmentOnlineMapper extends JudgmentOnlineMapper {

    @Override
    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {

        List<Element<Party>> defendants = new ArrayList<>();
        defendants.add(element(caseData.getRespondent1()));
        if (caseData.isMultiPartyDefendant()) {
            defendants.add(element(caseData.getRespondent2()));
        }
        BigDecimal orderAmount = JudgmentsOnlineHelper.getMoneyValue(caseData.getJoAmountOrdered());
        BigDecimal costs = JudgmentsOnlineHelper.getMoneyValue(caseData.getJoAmountCostOrdered());
        JudgmentDetails activeJudgment = super.addUpdateActiveJudgment(caseData);
        return activeJudgment.toBuilder()
            .createdTimestamp(LocalDateTime.now())
            .state(getJudgmentState(caseData))
            .type(JudgmentType.JUDGMENT_FOLLOWING_HEARING)
            .instalmentDetails(caseData.getJoInstalmentDetails())
            .paymentPlan(caseData.getJoPaymentPlan())
            .isRegisterWithRTL(caseData.getJoIsRegisteredWithRTL())
            .issueDate(caseData.getJoOrderMadeDate())
            .orderedAmount(orderAmount.toString())
            .costs(costs.toString())
            .totalAmount(orderAmount.add(costs).toString())
            .build();
    }

    protected JudgmentState getJudgmentState(CaseData caseData) {
        return JudgmentState.ISSUED;
    }

}
