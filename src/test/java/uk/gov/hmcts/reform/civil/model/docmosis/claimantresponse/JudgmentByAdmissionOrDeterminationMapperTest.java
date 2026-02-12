package uk.gov.hmcts.reform.civil.model.docmosis.claimantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.docmosis.common.RepaymentPlanTemplateData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.JudgementService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class JudgmentByAdmissionOrDeterminationMapperTest {

    @Mock
    private JudgementService judgementService;

    @InjectMocks
    private JudgmentByAdmissionOrDeterminationMapper mapper;

    @BeforeEach
    void setup() {
        lenient().when(judgementService.ccjJudgmentInterest(any())).thenReturn(BigDecimal.ZERO);
        lenient().when(judgementService.ccjJudgmentClaimAmount(any())).thenReturn(BigDecimal.ZERO);
        lenient().when(judgementService.ccjJudgmentClaimFee(any())).thenReturn(BigDecimal.ZERO);
        lenient().when(judgementService.ccjJudgementSubTotal(any())).thenReturn(BigDecimal.ZERO);
        lenient().when(judgementService.ccjJudgmentPaidAmount(any())).thenReturn(BigDecimal.ZERO);
        lenient().when(judgementService.ccjJudgmentFinalTotal(any())).thenReturn(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnNull_whenRepaymentOptionIsNotRepaymentPlan() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.IMMEDIATELY)
            .build();

        // When
        JudgmentByAdmissionOrDetermination form = mapper.toClaimantResponseForm(caseData, null);

        // Then
        assertThat(form.getRepaymentPlan()).isNull();
    }

    @Test
    void shouldReturnRepaymentPlan_whenAcceptedPlanIsTrueAndRespondent1PlanExists() {
        // Given
        LocalDate firstRepaymentDate = LocalDate.now().plusDays(1);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .respondent1RepaymentPlan(RepaymentPlanLRspec.builder()
                                          .firstRepaymentDate(firstRepaymentDate)
                                          .paymentAmount(new BigDecimal("10000"))
                                          .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
                                          .build())
            .build();

        // When
        JudgmentByAdmissionOrDetermination form = mapper.toClaimantResponseForm(caseData, null);

        // Then
        RepaymentPlanTemplateData repaymentPlan = form.getRepaymentPlan();
        assertThat(repaymentPlan).isNotNull();
        assertThat(repaymentPlan.getFirstRepaymentDate()).isEqualTo(firstRepaymentDate);
        assertThat(repaymentPlan.getPaymentAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(repaymentPlan.getPaymentFrequencyDisplay()).isEqualTo("Paid every month");
    }

    @Test
    void shouldReturnRepaymentPlan_whenAcceptedPlanIsTrueAndRespondent2PlanExists() {
        // Given
        LocalDate firstRepaymentDate = LocalDate.now().plusDays(1);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.YES)
            .respondent1RepaymentPlan(null)
            .respondent2RepaymentPlan(RepaymentPlanLRspec.builder()
                                          .firstRepaymentDate(firstRepaymentDate)
                                          .paymentAmount(new BigDecimal("20000"))
                                          .repaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_WEEK)
                                          .build())
            .build();

        // When
        JudgmentByAdmissionOrDetermination form = mapper.toClaimantResponseForm(caseData, null);

        // Then
        RepaymentPlanTemplateData repaymentPlan = form.getRepaymentPlan();
        assertThat(repaymentPlan).isNotNull();
        assertThat(repaymentPlan.getFirstRepaymentDate()).isEqualTo(firstRepaymentDate);
        assertThat(repaymentPlan.getPaymentAmount()).isEqualTo(new BigDecimal("200.00"));
        assertThat(repaymentPlan.getPaymentFrequencyDisplay()).isEqualTo("Paid every week");
    }

    @Test
    void shouldReturnNull_whenAcceptedPlanIsTrueButNoRespondentPlanExists() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.YES)
            .respondent1RepaymentPlan(null)
            .respondent2RepaymentPlan(null)
            .build();

        // When
        JudgmentByAdmissionOrDetermination form = mapper.toClaimantResponseForm(caseData, null);

        // Then
        assertThat(form.getRepaymentPlan()).isNull();
    }

    @Test
    void shouldReturnRepaymentPlan_whenAcceptedPlanIsFalse() {
        // Given
        LocalDate firstRepaymentDate = LocalDate.now().plusDays(1);
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .applicant1RepaymentOptionForDefendantSpec(PaymentType.REPAYMENT_PLAN)
            .applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo.NO)
            .applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo.NO)
            .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(firstRepaymentDate)
            .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(new BigDecimal("30000"))
            .applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec(PaymentFrequencyClaimantResponseLRspec.ONCE_TWO_WEEKS)
            .build();

        // When
        JudgmentByAdmissionOrDetermination form = mapper.toClaimantResponseForm(caseData, null);

        // Then
        RepaymentPlanTemplateData repaymentPlan = form.getRepaymentPlan();
        assertThat(repaymentPlan).isNotNull();
        assertThat(repaymentPlan.getFirstRepaymentDate()).isEqualTo(firstRepaymentDate);
        assertThat(repaymentPlan.getPaymentAmount()).isEqualTo(new BigDecimal("300.00"));
        assertThat(repaymentPlan.getPaymentFrequencyDisplay()).isEqualTo("Every 2 weeks");
    }
}
