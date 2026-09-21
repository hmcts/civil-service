package uk.gov.hmcts.reform.civil.helpers.judgmentsonline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.AddressLinesMapper;
import uk.gov.hmcts.reform.civil.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DefaultJudgmentOnlineMapperPendingIssueTest {

    private DefaultJudgmentOnlineMapper defaultJudgmentOnlineMapper;

    @BeforeEach
    void setUp() {
        Time time = mock(Time.class);
        InterestCalculator interestCalculator = new InterestCalculator();
        RoboticsAddressMapper addressMapper = new RoboticsAddressMapper(new AddressLinesMapper());
        defaultJudgmentOnlineMapper = new DefaultJudgmentOnlineMapper(time, interestCalculator, addressMapper);
    }

    @Test
    void shouldCreatePendingIssueActiveJudgmentForBufferedDefaultJudgment() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .partialPayment(YesOrNo.YES)
            .partialPaymentAmount("10")
            .totalClaimAmount(BigDecimal.valueOf(1010))
            .paymentConfirmationDecisionSpec(YesOrNo.YES)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .defendantDetailsSpec(new DynamicList().setValue(new DynamicListElement().setLabel("Test User")))
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_WEEK)
            .repaymentSuggestion("100")
            .repaymentDate(LocalDate.now().plusDays(10))
            .build();

        JudgmentDetails activeJudgment = defaultJudgmentOnlineMapper.addPendingIssueActiveJudgment(caseData);

        assertNotNull(activeJudgment);
        assertEquals(JudgmentState.PENDING_ISSUE, activeJudgment.getState());
        assertEquals(LocalDate.now(), activeJudgment.getRequestDate());
        assertEquals(JudgmentType.DEFAULT_JUDGMENT, activeJudgment.getType());
        assertEquals(YesOrNo.NO, activeJudgment.getIsRegisterWithRTL());
        assertNull(activeJudgment.getOrderedAmount());
        assertNull(activeJudgment.getCosts());
        assertNull(activeJudgment.getClaimFeeAmount());
        assertNull(activeJudgment.getTotalAmount());
        assertEquals(PaymentPlanSelection.PAY_IN_INSTALMENTS, activeJudgment.getPaymentPlan().getType());
        assertEquals(PaymentFrequency.WEEKLY, activeJudgment.getInstalmentDetails().getPaymentFrequency());
        assertEquals("100", activeJudgment.getInstalmentDetails().getAmount());
        assertEquals(1, activeJudgment.getJudgmentId());
        assertNotNull(activeJudgment.getDefendant1Address());
        assertNotNull(activeJudgment.getDefendant1Dob());
    }
}
