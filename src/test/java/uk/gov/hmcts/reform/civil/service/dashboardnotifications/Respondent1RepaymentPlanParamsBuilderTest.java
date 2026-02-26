package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Respondent1RepaymentPlanParamsBuilderTest {

    private Respondent1RepaymentPlanParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new Respondent1RepaymentPlanParamsBuilder();
    }

    @Test
    void shouldAddRepaymentPlanParamsWhenRespondent1RepaymentPlanIsPresent() {
        // Arrange
        CaseData caseData = mock(CaseData.class);
        RepaymentPlanLRspec repaymentPlan = new RepaymentPlanLRspec()
            .setFirstRepaymentDate(LocalDate.parse("2023-10-15"))
            .setPaymentAmount(new BigDecimal("10000"))
            .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH)
            ;

        when(caseData.getRespondent1RepaymentPlan()).thenReturn(repaymentPlan);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).containsEntry("instalmentAmount", "£100");
        assertThat(params).containsEntry("instalmentStartDateEn", "15 October 2023");
        assertThat(params).containsEntry("instalmentStartDateCy", "15 Hydref 2023");
        assertThat(params).containsEntry("paymentFrequency", "every month");
        assertThat(params).containsEntry("paymentFrequencyWelsh", "bob mis");
        assertThat(params).containsEntry("firstRepaymentDateEn", "15 October 2023");
        assertThat(params).containsEntry("firstRepaymentDateCy", "15 Hydref 2023");
    }

    @Test
    void shouldNotAddRepaymentPlanParamsWhenRespondent1RepaymentPlanIsNull() {
        // Arrange
        CaseData caseData = mock(CaseData.class);

        when(caseData.getRespondent1RepaymentPlan()).thenReturn(null);

        HashMap<String, Object> params = new HashMap<>();

        // Act
        builder.addParams(caseData, params);

        // Assert
        assertThat(params).isEmpty();
    }
}
