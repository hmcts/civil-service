package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaidInFull;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class JudgmentPaidOnlineMapperTest {

    private InterestCalculator interestCalculator = new InterestCalculator();
    private RoboticsAddressMapper addressMapper = new RoboticsAddressMapper(new AddressLinesMapper());
    private JudgmentPaidInFullOnlineMapper judgmentPaidInFullOnlineMapper = new JudgmentPaidInFullOnlineMapper();
    private RecordJudgmentOnlineMapper recordJudgmentMapper = new RecordJudgmentOnlineMapper(addressMapper);
    private DefaultJudgmentOnlineMapper defaultJudgmentOnlineMapper = new DefaultJudgmentOnlineMapper(interestCalculator, addressMapper);

    @BeforeEach
    public void setUp() {
        interestCalculator = new InterestCalculator();
        defaultJudgmentOnlineMapper = new DefaultJudgmentOnlineMapper(interestCalculator, addressMapper);
    }

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
        assertEquals(PaymentPlanSelection.PAY_IMMEDIATELY, caseData.getJoPaymentPlanSelected());

    }
}
