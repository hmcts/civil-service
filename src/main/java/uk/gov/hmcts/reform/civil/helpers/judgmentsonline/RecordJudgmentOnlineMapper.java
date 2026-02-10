package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRTLStatus;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class RecordJudgmentOnlineMapper extends JudgmentOnlineMapper {

    private final RoboticsAddressMapper addressMapper;

    public RecordJudgmentOnlineMapper(Time time, RoboticsAddressMapper addressMapper) {
        super(time);
        this.addressMapper = addressMapper;
    }

    @Override
    public JudgmentDetails addUpdateActiveJudgment(CaseData caseData) {
        BigDecimal orderAmount = JudgmentsOnlineHelper.getMoneyValue(caseData.getJoAmountOrdered());
        BigDecimal costs = JudgmentsOnlineHelper.getMoneyValue(caseData.getJoAmountCostOrdered());
        JudgmentDetails activeJudgment = super.addUpdateActiveJudgment(caseData);
        activeJudgment = super.updateDefendantDetails(activeJudgment, caseData, addressMapper);
        activeJudgment
            .setCreatedTimestamp(LocalDateTime.now())
            .setState(getJudgmentState(caseData))
            .setRtlState(getRtlState(caseData.getJoIsRegisteredWithRTL()))
            .setType(JudgmentType.JUDGMENT_FOLLOWING_HEARING)
            .setInstalmentDetails(caseData.getJoInstalmentDetails())
            .setPaymentPlan(caseData.getJoPaymentPlan())
            .setIsRegisterWithRTL(caseData.getJoIsRegisteredWithRTL())
            .setIssueDate(caseData.getJoOrderMadeDate())
            .setOrderedAmount(orderAmount.toString())
            .setCosts(costs.toString())
            .setTotalAmount(orderAmount.add(costs).toString());

        super.updateJudgmentTabDataWithActiveJudgment(activeJudgment, caseData);

        return activeJudgment;
    }

    protected JudgmentState getJudgmentState(CaseData caseData) {
        return JudgmentState.ISSUED;
    }

    protected String getRtlState(YesOrNo isRegisterWithRTL) {
        return isRegisterWithRTL == YesOrNo.YES ? JudgmentRTLStatus.ISSUED.getRtlState() : null;
    }
}
