package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaidInFull;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JudgmentPaidOnlineMapperTest {

    private JudgmentPaidInFullOnlineMapper judgmentOnlineMapper = new JudgmentPaidInFullOnlineMapper();
    private RecordJudgmentOnlineMapper recordJudgmentMapper = new RecordJudgmentOnlineMapper();

    @Test
    void testIfActiveJudgmentIsSatisfied() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        //PAID IN FULL
        caseData.setJoJudgmentPaidInFull(JudgmentPaidInFull.builder()
                                             .dateOfFullPaymentMade(LocalDate.of(2023, 1, 15))
                                             .confirmFullPaymentMade(List.of("CONFIRMED"))
                                             .build());

        judgmentOnlineMapper.updateHistoricJudgment(caseData);

        assertNotNull(caseData.getActiveJudgment());
        assertEquals(JudgmentState.SATISFIED, caseData.getActiveJudgment().getState());
        assertEquals(LocalDate.of(2023, 1, 15), caseData.getActiveJudgment().getFullyPaymentMadeDate());
        assertNull(caseData.getActiveJudgment().getCancelDate());
        assertNull(caseData.getActiveJudgment().getCancelledTimeStamp());
    }

    @Test
    void testIfActiveJudgmentIsHistoricAfterCancelled() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        caseData.setJoJudgmentPaidInFull(JudgmentPaidInFull.builder()
                                             .dateOfFullPaymentMade(LocalDate.of(2012, 12, 15))
                                             .confirmFullPaymentMade(List.of("CONFIRMED"))
                                             .build());

        judgmentOnlineMapper.updateHistoricJudgment(caseData);
        JudgmentDetails historicJudgment = caseData.getHistoricJudgment().get(0).getValue();
        assertNull(caseData.getActiveJudgment());
        assertNotNull(caseData.getHistoricJudgment());
        assertEquals(JudgmentState.CANCELLED, historicJudgment.getState());
        assertEquals(LocalDate.now(), historicJudgment.getCancelDate());
    }
}
