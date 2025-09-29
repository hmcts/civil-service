package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;

import java.time.LocalDate;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardNotificationsParamsBuilderTest {

    private DashboardNotificationsParamsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new DashboardNotificationsParamsBuilder() {
            @Override
            public void addParams(CaseData caseData, HashMap<String, Object> params) {
                // No-op for testing purposes
            }
        };
    }

    @Test
    void shouldReturnPaymentMessageInWelsh() {
        JudgmentInstalmentDetails instalmentDetails = mock(JudgmentInstalmentDetails.class);
        when(instalmentDetails.getPaymentFrequency()).thenReturn(PaymentFrequency.WEEKLY);
        when(instalmentDetails.getAmount()).thenReturn("10000");
        when(instalmentDetails.getStartDate()).thenReturn(LocalDate.of(2023, 10, 1));

        String result = builder.getStringPaymentMessageInWelsh(instalmentDetails);

        assertThat(result).isEqualTo("mewn rhandaliadau wythnosol o £100.00. Bydd y taliad cyntaf yn ddyledus ar 1 Hydref 2023");
    }

    @Test
    void shouldReturnPaymentMessage() {
        JudgmentInstalmentDetails instalmentDetails = mock(JudgmentInstalmentDetails.class);
        when(instalmentDetails.getPaymentFrequency()).thenReturn(PaymentFrequency.MONTHLY);
        when(instalmentDetails.getAmount()).thenReturn("5000");
        when(instalmentDetails.getStartDate()).thenReturn(LocalDate.of(2023, 10, 1));

        String result = builder.getStringPaymentMessage(instalmentDetails);

        assertThat(result).isEqualTo("in monthly instalments of £50.00. The first payment is due on 1 October 2023");
    }

    @Test
    void shouldRemoveDoubleZeros() {
        String input = "100.00";
        String result = builder.removeDoubleZeros(input);

        assertThat(result).isEqualTo("100");
    }

    @Test
    void shouldReturnRespondToClaimForFullDefence() {
        CaseData caseData = mock(CaseData.class);
        RespondToClaim respondToClaim = mock(RespondToClaim.class);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.FULL_DEFENCE);
        when(caseData.getRespondToClaim()).thenReturn(respondToClaim);

        RespondToClaim result = builder.getRespondToClaim(caseData);

        assertThat(result).isEqualTo(respondToClaim);
    }

    @Test
    void shouldReturnRespondToClaimForPartAdmission() {
        CaseData caseData = mock(CaseData.class);
        RespondToClaim respondToAdmittedClaim = mock(RespondToClaim.class);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        when(caseData.getRespondToAdmittedClaim()).thenReturn(respondToAdmittedClaim);

        RespondToClaim result = builder.getRespondToClaim(caseData);

        assertThat(result).isEqualTo(respondToAdmittedClaim);
    }

    @Test
    void shouldReturnClaimantRepaymentPlanDecisionAccepted() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.hasApplicantAcceptedRepaymentPlan()).thenReturn(true);

        String result = builder.getClaimantRepaymentPlanDecision(caseData);

        assertThat(result).isEqualTo(DashboardNotificationsParamsBuilder.CLAIMANT1_ACCEPTED_REPAYMENT_PLAN);
    }

    @Test
    void shouldReturnClaimantRepaymentPlanDecisionRejected() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.hasApplicantAcceptedRepaymentPlan()).thenReturn(false);

        String result = builder.getClaimantRepaymentPlanDecision(caseData);

        assertThat(result).isEqualTo(DashboardNotificationsParamsBuilder.CLAIMANT1_REJECTED_REPAYMENT_PLAN);
    }

    @Test
    void shouldReturnClaimantRepaymentPlanDecisionAcceptedInWelsh() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.hasApplicantAcceptedRepaymentPlan()).thenReturn(true);

        String result = builder.getClaimantRepaymentPlanDecisionCy(caseData);

        assertThat(result).isEqualTo(DashboardNotificationsParamsBuilder.CLAIMANT1_ACCEPTED_REPAYMENT_PLAN_WELSH);
    }

    @Test
    void shouldReturnClaimantRepaymentPlanDecisionRejectedInWelsh() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.hasApplicantAcceptedRepaymentPlan()).thenReturn(false);

        String result = builder.getClaimantRepaymentPlanDecisionCy(caseData);

        assertThat(result).isEqualTo(DashboardNotificationsParamsBuilder.CLAIMANT1_REJECTED_REPAYMENT_PLAN_WELSH);
    }
}
