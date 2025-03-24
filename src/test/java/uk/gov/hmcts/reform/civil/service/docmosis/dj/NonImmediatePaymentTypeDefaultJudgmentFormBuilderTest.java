package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.helpers.DateFormatHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.docmosis.dj.DefaultJudgmentForm;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.InterestCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NonImmediatePaymentTypeDefaultJudgmentFormBuilderTest {

    @Mock
    private InterestCalculator interestCalculator;
    @Mock
    private JudgmentAndSettlementAmountsCalculator judgmentAndSettlementAmountsCalculator;
    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private NonImmediatePaymentTypeDefaultJudgmentFormBuilder nonImmediatePaymentTypeDefaultJudgmentFormBuilder;

    @BeforeEach
    void setUp() {
        when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(Organisation.builder().name("org name")
            .contactInformation(Arrays.asList(ContactInformation.builder()
                .addressLine1("addressLine1")
                .addressLine2("addressLine2")
                .addressLine3("addressLine3")
                .postCode("postCode")
                .build())).build()));
    }

    @Test
    void shouldReturnDefaultJudgmentFormWithPaymentPlan_whenPaymentTypeIsNonImmediate_forSpec() {

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .legacyCaseReference("12345")
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build())
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_MONTH)
            .repaymentDate(LocalDate.now().plusMonths(4))
            .repaymentSuggestion("200")
            .build();
        uk.gov.hmcts.reform.civil.model.Party respondent = PartyBuilder.builder().individual().build();

        when(judgmentAndSettlementAmountsCalculator.getClaimFee(any())).thenReturn(new BigDecimal("50.00"));
        when(judgmentAndSettlementAmountsCalculator.getDebtAmount(any())).thenReturn(new BigDecimal("1006.00"));

        DefaultJudgmentForm form = nonImmediatePaymentTypeDefaultJudgmentFormBuilder.getDefaultJudgmentForm(caseData, respondent, CaseEvent.GENERATE_DJ_FORM_SPEC.name(), false);

        assertThat(form.getCaseNumber()).isEqualTo("12345");
        assertThat(form.getDebt()).isEqualTo("1006.00");
        assertThat(form.getCosts()).isEqualTo("50.00");
        assertThat(form.getTotalCost()).isEqualTo("1056.00");
        assertThat(form.getPaymentPlan()).isEqualTo(DJPaymentTypeSelection.REPAYMENT_PLAN.name());
        assertThat(form.getRepaymentFrequency()).isEqualTo("per month");
        assertThat(form.getRepaymentDate()).isEqualTo(DateFormatHelper.formatLocalDate(caseData.getRepaymentDate(), DateFormatHelper.DATE));
        assertThat(form.getInstallmentAmount()).isEqualTo("2.00");
    }

    @Test
    void shouldReturnDefaultJudgmentFormWithPaymentPlan_whenPaymentTypeIsNonImmediate_forUnspec() {

        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .totalClaimAmount(new BigDecimal(2000))
            .claimFee(Fee.builder().calculatedAmountInPence(new BigDecimal(10)).build())
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .repaymentFrequency(RepaymentFrequencyDJ.ONCE_ONE_MONTH)
            .repaymentDate(LocalDate.now().plusMonths(4))
            .repaymentSuggestion("200")
            .build();
        uk.gov.hmcts.reform.civil.model.Party respondent = PartyBuilder.builder().individual().build();

        DefaultJudgmentForm form = nonImmediatePaymentTypeDefaultJudgmentFormBuilder.getDefaultJudgmentForm(caseData, respondent, CaseEvent.GENERATE_DJ_FORM.name(), false);

        assertThat(form.getPaymentPlan()).isEqualTo(DJPaymentTypeSelection.REPAYMENT_PLAN.name());
        assertThat(form.getRepaymentFrequency()).isEqualTo("per month");
        assertThat(form.getRepaymentDate()).isEqualTo(DateFormatHelper.formatLocalDate(caseData.getRepaymentDate(), DateFormatHelper.DATE));
        assertThat(form.getInstallmentAmount()).isEqualTo("2.00");
    }

    @Test
    void shouldReturnDefaultJudgmentFormWithNullValues_whenNoRepaymentDetailsProvided_forUnspec() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .paymentTypeSelection(DJPaymentTypeSelection.REPAYMENT_PLAN)
            .build();
        uk.gov.hmcts.reform.civil.model.Party respondent = PartyBuilder.builder().individual().build();

        DefaultJudgmentForm form = nonImmediatePaymentTypeDefaultJudgmentFormBuilder.getDefaultJudgmentForm(caseData, respondent, CaseEvent.GENERATE_DJ_FORM.name(), false);

        assertThat(form.getPaymentPlan()).isEqualTo(DJPaymentTypeSelection.REPAYMENT_PLAN.name());
        assertThat(form.getRepaymentFrequency()).isNull();
        assertThat(form.getRepaymentDate()).isNull();
        assertThat(form.getInstallmentAmount()).isNull();
    }

    @Test
    void shouldReturnDefaultJudgmentFormWithFormattedPayByDate_whenPaymentSetDateIsProvided_forSpec() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged()
            .paymentTypeSelection(DJPaymentTypeSelection.SET_DATE)
            .paymentSetDate(LocalDate.now().plusDays(5))
            .build();
        uk.gov.hmcts.reform.civil.model.Party respondent = PartyBuilder.builder().individual().build();

        when(judgmentAndSettlementAmountsCalculator.getClaimFee(any())).thenReturn(new BigDecimal("50.00"));
        when(judgmentAndSettlementAmountsCalculator.getDebtAmount(any())).thenReturn(new BigDecimal("1006.00"));

        DefaultJudgmentForm form = nonImmediatePaymentTypeDefaultJudgmentFormBuilder.getDefaultJudgmentForm(caseData, respondent, CaseEvent.GENERATE_DJ_FORM_SPEC.name(), false);

        assertThat(form.getCaseNumber()).isEqualTo("000DC001");
        assertThat(form.getDebt()).isEqualTo("1006.00");
        assertThat(form.getCosts()).isEqualTo("50.00");
        assertThat(form.getTotalCost()).isEqualTo("1056.00");
        assertThat(form.getPaymentPlan()).isEqualTo(DJPaymentTypeSelection.SET_DATE.name());
        assertThat(form.getPayByDate()).isEqualTo(DateFormatHelper.formatLocalDate(caseData.getPaymentSetDate(), DateFormatHelper.DATE));
    }
}
