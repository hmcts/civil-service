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

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class JudgmentOnlineMapperTest {

    private RecordJudgmentOnlineMapper recordJudgmentMapper = new RecordJudgmentOnlineMapper();
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

        //RECORD JUDGMENT 2
        caseData.setJoJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER);
        caseData.setJoAmountCostOrdered("1200");
        caseData.setJoAmountOrdered("1100");
        caseData.setJoPaymentPlan(JudgmentPaymentPlan.builder().type(PaymentPlanSelection.PAY_IMMEDIATELY).build());
        caseData.setJoOrderMadeDate(LocalDate.of(2022, 12, 12));
        //caseData.setCase(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
        caseData.setJoIsRegisteredWithRTL(YES);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            // sort by time, sometimes too fast for time to be different
        }
        activeJudgment = recordJudgmentMapper.addUpdateActiveJudgment(caseData);
        caseData.setActiveJudgment(activeJudgment);

        //SET ASIDE 2
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
        caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
        caseData.setJoSetAsideOrderDate(LocalDate.of(2022, 12, 12));
        setAsideJudgmentOnlineMapper.moveToHistoricJudgment(caseData);

        //RECORD judgment 3
        caseData.setJoJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER);
        caseData.setJoAmountCostOrdered("1200");
        caseData.setJoAmountOrdered("1100");
        caseData.setJoPaymentPlan(JudgmentPaymentPlan.builder().type(PaymentPlanSelection.PAY_IMMEDIATELY).build());
        caseData.setJoOrderMadeDate(LocalDate.of(2022, 12, 12));
        //caseData.setCase(CaseLocationCivil.builder().baseLocation("0123").region("0321").build())
        caseData.setJoIsRegisteredWithRTL(YES);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            // sort by time, sometimes too fast for time to be different
        }
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
        assertTrue(caseData.getHistoricJudgment().get(0).getValue().getLastUpdateTimeStamp().isAfter(caseData.getHistoricJudgment().get(
            1).getValue().getLastUpdateTimeStamp()));
        assertTrue(caseData.getHistoricJudgment().get(1).getValue().getLastUpdateTimeStamp().isAfter(caseData.getHistoricJudgment().get(
            2).getValue().getLastUpdateTimeStamp()));
    }

}
