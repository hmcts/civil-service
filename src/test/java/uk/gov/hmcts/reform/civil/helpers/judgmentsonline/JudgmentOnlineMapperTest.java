package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class JudgmentOnlineMapperTest {

    private RoboticsAddressMapper addressMapper = new RoboticsAddressMapper(new AddressLinesMapper());
    private RecordJudgmentOnlineMapper recordJudgmentMapper = new RecordJudgmentOnlineMapper(addressMapper);
    private SetAsideJudgmentOnlineMapper setAsideJudgmentOnlineMapper = new SetAsideJudgmentOnlineMapper();

    @Test
    void moveToHistoricJudgment() {

        CaseData caseData = CaseDataBuilder.builder().buildJudgmentOnlineCaseDataWithPaymentByDate();

        //Record Judgment 1
        JudgmentDetails activeJudgment = recordJudgmentMapper.addUpdateActiveJudgment(caseData);
        caseData.setActiveJudgment(activeJudgment);

        //SET ASIDE 1
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
        caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
        caseData.setJoSetAsideOrderDate(LocalDate.of(2022, 12, 12));
        setAsideJudgmentOnlineMapper.moveToHistoricJudgment(caseData);

        assertNull(caseData.getActiveJudgment());
        assertNotNull(caseData.getHistoricJudgment());
        JudgmentDetails historicJudgment = caseData.getHistoricJudgment().get(0).getValue();
        assertEquals(historicJudgment.getJudgmentId(), 1);

        try {
            // if this test runs too fast, the time is the same and sort does not work as expected
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //RECORD JUDGMENT 2
        caseData.setJoJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER);
        caseData.setJoAmountCostOrdered("1200");
        caseData.setJoAmountOrdered("1100");
        caseData.setJoPaymentPlan(JudgmentPaymentPlan.builder().type(PaymentPlanSelection.PAY_IMMEDIATELY).build());
        caseData.setJoOrderMadeDate(LocalDate.of(2022, 12, 12));
        caseData.setJoIsRegisteredWithRTL(YES);
        activeJudgment = recordJudgmentMapper.addUpdateActiveJudgment(caseData);
        caseData.setActiveJudgment(activeJudgment);

        //SET ASIDE 2
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
        caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
        caseData.setJoSetAsideOrderDate(LocalDate.of(2022, 12, 12));
        setAsideJudgmentOnlineMapper.moveToHistoricJudgment(caseData);

        try {
            // if this test runs too fast, the time is the same and sort does not work as expected
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //RECORD judgment 3
        caseData.setJoJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER);
        caseData.setJoAmountCostOrdered("1200");
        caseData.setJoAmountOrdered("1100");
        caseData.setJoPaymentPlan(JudgmentPaymentPlan.builder().type(PaymentPlanSelection.PAY_IMMEDIATELY).build());
        caseData.setJoOrderMadeDate(LocalDate.of(2022, 12, 12));
        caseData.setJoIsRegisteredWithRTL(YES);
        activeJudgment = recordJudgmentMapper.addUpdateActiveJudgment(caseData);
        caseData.setActiveJudgment(activeJudgment);

        //SET ASIDE 3
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
        caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
        caseData.setJoSetAsideOrderDate(LocalDate.of(2022, 12, 12));
        setAsideJudgmentOnlineMapper.moveToHistoricJudgment(caseData);

        assertNull(caseData.getActiveJudgment());
        assertNotNull(caseData.getHistoricJudgment());
        assertEquals(caseData.getHistoricJudgment().get(0).getValue().getJudgmentId(), 3);
        assertEquals(caseData.getHistoricJudgment().get(1).getValue().getJudgmentId(), 2);
        assertEquals(caseData.getHistoricJudgment().get(2).getValue().getJudgmentId(), 1);
        assertEquals(caseData.getHistoricJudgment().get(0).getValue().getLastUpdateTimeStamp().isAfter(caseData.getHistoricJudgment().get(
            1).getValue().getLastUpdateTimeStamp()), true);
        assertEquals(caseData.getHistoricJudgment().get(1).getValue().getLastUpdateTimeStamp().isAfter(caseData.getHistoricJudgment().get(
            2).getValue().getLastUpdateTimeStamp()), true);
    }

}
