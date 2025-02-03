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
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordJudgmentOnlineMapper extends JudgmentOnlineMapper {

    private final RoboticsAddressMapper addressMapper;

    @Override
    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {

        BigDecimal orderAmount = JudgmentsOnlineHelper.getMoneyValue(caseData.getJoAmountOrdered());
        BigDecimal costs = JudgmentsOnlineHelper.getMoneyValue(caseData.getJoAmountCostOrdered());
        JudgmentDetails activeJudgment = super.addUpdateActiveJudgment(caseData);
        activeJudgment = super.updateDefendantDetails(activeJudgment, caseData, addressMapper);
        JudgmentDetails activeJudgmentDetails = activeJudgment.toBuilder()
            .createdTimestamp(LocalDateTime.now())
            .state(getJudgmentState(caseData))
            .rtlState(getRtlState(caseData.getJoIsRegisteredWithRTL()))
            .type(JudgmentType.JUDGMENT_FOLLOWING_HEARING)
            .instalmentDetails(caseData.getJoInstalmentDetails())
            .paymentPlan(caseData.getJoPaymentPlan())
            .isRegisterWithRTL(caseData.getJoIsRegisteredWithRTL())
            .issueDate(caseData.getJoOrderMadeDate())
            .orderedAmount(orderAmount.toString())
            .costs(costs.toString())
            .totalAmount(orderAmount.add(costs).toString())
            .build();

        super.updateJudgmentTabDataWithActiveJudgment(activeJudgmentDetails, caseData);

        return activeJudgmentDetails;
    }

    protected JudgmentState getJudgmentState(CaseData caseData) {
        return JudgmentState.ISSUED;
    }

    protected String getRtlState(YesOrNo isRegisterWithRTL) {
        return isRegisterWithRTL == YesOrNo.YES ? JudgmentRTLStatus.ISSUED.getRtlState() : null;
    }
}
