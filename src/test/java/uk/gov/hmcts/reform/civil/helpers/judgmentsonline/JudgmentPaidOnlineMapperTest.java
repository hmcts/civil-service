package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaidInFull;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JudgmentPaidOnlineMapperTest {

    @InjectMocks
    private JudgmentPaidInFullOnlineMapper judgmentPaidInFullOnlineMapper;
    @InjectMocks
    private RecordJudgmentOnlineMapper recordJudgmentMapper;
    @MockBean
    private Time time;
    @Mock
    private InterestCalculator interestCalculator;
    @InjectMocks
    private DefaultJudgmentOnlineMapper defaultJudgmentOnlineMapper;

    @Test
    void testIfActiveJudgmentIsSatisfied() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        //PAID IN FULL
        caseData.setJoJudgmentPaidInFull(JudgmentPaidInFull.builder()
                                             .dateOfFullPaymentMade(LocalDate.of(2023, 1, 15))
                                             .confirmFullPaymentMade(List.of("CONFIRMED"))
                                             .build());
        caseData.setActiveJudgment(judgmentPaidInFullOnlineMapper.addUpdateActiveJudgment(caseData));

        assertNotNull(caseData.getActiveJudgment());
        assertEquals(JudgmentState.SATISFIED, caseData.getActiveJudgment().getState());
        assertEquals(LocalDate.of(2023, 1, 15), caseData.getActiveJudgment().getFullyPaymentMadeDate());
        assertNull(caseData.getActiveJudgment().getCancelDate());
        assertNull(caseData.getActiveJudgment().getCancelledTimeStamp());

        assertEquals("Mr. John Rambo", caseData.getJoDefendantName1());
        assertNotNull(caseData.getJoRepaymentSummaryObject());
        assertEquals(PaymentPlanSelection.PAY_IN_INSTALMENTS, caseData.getJoPaymentPlanSelected());
        assertEquals("120", caseData.getJoRepaymentAmount());
        assertNotNull(caseData.getJoRepaymentStartDate());
        assertEquals(PaymentFrequency.MONTHLY, caseData.getJoRepaymentFrequency());
    }

    @Test
    void testIfActiveJudgmentIsHistoricAfterCancelled() {

        CaseData caseData = CaseDataBuilder.builder().buildJudmentOnlineCaseDataWithPaymentByInstalment();
        caseData.setActiveJudgment(recordJudgmentMapper.addUpdateActiveJudgment(caseData));

        caseData.setJoJudgmentPaidInFull(JudgmentPaidInFull.builder()
                                             .dateOfFullPaymentMade(LocalDate.of(2012, 12, 15))
                                             .confirmFullPaymentMade(List.of("CONFIRMED"))
                                             .build());
        caseData.setActiveJudgment(judgmentPaidInFullOnlineMapper.addUpdateActiveJudgment(caseData));

        assertNotNull(caseData.getActiveJudgment());
        assertEquals(JudgmentState.CANCELLED, caseData.getActiveJudgment().getState());
        assertNull(caseData.getHistoricJudgment());
    }

    @Test
    void testIfDefaultActiveJudgmentIsHistoricAfterCancelled() {

        when(interestCalculator.calculateInterest(any(CaseData.class))).thenReturn(BigDecimal.ZERO);
        CaseData caseData = CaseDataBuilder.builder().getDefaultJudgment1v1Case();
        caseData.setActiveJudgment(defaultJudgmentOnlineMapper.addUpdateActiveJudgment(caseData));

        caseData.setJoJudgmentPaidInFull(JudgmentPaidInFull.builder()
                                             .dateOfFullPaymentMade(LocalDate.now().plusDays(15))
                                             .confirmFullPaymentMade(List.of("CONFIRMED"))
                                             .build());
        caseData.setActiveJudgment(judgmentPaidInFullOnlineMapper.addUpdateActiveJudgment(caseData));

        assertNotNull(caseData.getActiveJudgment());
        assertEquals(JudgmentState.CANCELLED, caseData.getActiveJudgment().getState());
        assertNull(caseData.getHistoricJudgment());

        assertEquals("Mr. Sole Trader", caseData.getJoDefendantName1());
        assertNotNull(caseData.getJoRepaymentSummaryObject());
        assertEquals(PaymentPlanSelection.PAY_IMMEDIATELY, caseData.getJoPaymentPlanSelected());

    }
}
