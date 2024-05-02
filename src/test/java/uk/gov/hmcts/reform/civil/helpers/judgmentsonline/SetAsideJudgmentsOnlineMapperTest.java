package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SetAsideJudgmentsOnlineMapperTest {

    private SetAsideJudgmentOnlineMapper judgmentOnlineMapper = new SetAsideJudgmentOnlineMapper();
    private RecordJudgmentOnlineMapper recordJudgmentMapper = new RecordJudgmentOnlineMapper();

    @Test
    void testIfActiveJudgmentIsHistoricSetAsideApplication() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        //SET ASIDE
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
        caseData.setJoSetAsideOrderType(JudgmentSetAsideOrderType.ORDER_AFTER_APPLICATION);
        caseData.setJoSetAsideOrderDate(LocalDate.of(2022, 12, 12));
        judgmentOnlineMapper.updateHistoricJudgment(caseData);

        assertNull(caseData.getActiveJudgment());
        assertNotNull(caseData.getHistoricJudgment());
        JudgmentDetails historicJudgment = caseData.getHistoricJudgment().get(0).getValue();
        assertEquals(JudgmentState.SET_ASIDE, historicJudgment.getState());
        assertEquals(LocalDate.of(2022, 12, 12), historicJudgment.getSetAsideDate());
    }

    @Test
    void testIfActiveJudgmentIsHistoricSetAsideDefence() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        //SET ASIDE
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGE_ORDER);
        caseData.setJoSetAsideDefenceReceivedDate(LocalDate.of(2022, 12, 12));
        judgmentOnlineMapper.updateHistoricJudgment(caseData);

        assertNull(caseData.getActiveJudgment());
        assertNotNull(caseData.getHistoricJudgment());
        JudgmentDetails historicJudgment = caseData.getHistoricJudgment().get(0).getValue();
        assertEquals(JudgmentState.SET_ASIDE, historicJudgment.getState());
        assertEquals(LocalDate.of(2022, 12, 12), historicJudgment.getSetAsideDate());
    }

    @Test
    void testIfActiveJudgmentIsHistoricSetAsideError() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        //SET ASIDE
        caseData.setJoSetAsideReason(JudgmentSetAsideReason.JUDGMENT_ERROR);
        caseData.setJoSetAsideOrderDate(LocalDate.of(2022, 12, 12));
        judgmentOnlineMapper.updateHistoricJudgment(caseData);

        assertNull(caseData.getActiveJudgment());
        assertNotNull(caseData.getHistoricJudgment());
        JudgmentDetails historicJudgment = caseData.getHistoricJudgment().get(0).getValue();
        assertEquals(JudgmentState.SET_ASIDE_ERROR, historicJudgment.getState());
        assertEquals(LocalDate.now(), historicJudgment.getSetAsideDate());
    }
}
