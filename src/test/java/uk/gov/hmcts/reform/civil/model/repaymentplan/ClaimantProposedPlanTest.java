package uk.gov.hmcts.reform.civil.model.repaymentplan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;

@ExtendWith(MockitoExtension.class)
class ClaimantProposedPlanTest {

    @Mock
    private RepaymentPlanLRspec repaymentPlan;

    @Test
    void shouldGetRepaymentDate_fromSetDate() {
        //Given
        LocalDate proposedDate = LocalDate.of(2023, 2,1);
        ClaimantProposedPlan claimantProposedPlan = ClaimantProposedPlan
            .builder()
            .repaymentByDate(proposedDate)
            .build();
        //When
        LocalDate result = claimantProposedPlan.getRepaymentDate(new BigDecimal(2345));
        //Then
        assertThat(result).isEqualTo(proposedDate);
    }

    @Test
    void shouldGetRepaymentDate_fromRepaymentPlan() {
        //Given
        LocalDate proposedDate = LocalDate.of(2023, 2,1);
        given(repaymentPlan.finalPaymentBy(any())).willReturn(proposedDate);
        ClaimantProposedPlan claimantProposedPlan = ClaimantProposedPlan
            .builder()
            .repaymentPlanLRspec(repaymentPlan)
            .build();
        //When
        LocalDate result  = claimantProposedPlan.getRepaymentDate(new BigDecimal(2345));
        //Then
        assertThat(result).isEqualTo(proposedDate);
    }

    @Test
    void shouldReturnTrue_whenProposedToPayImmediately() {
        //Given
        ClaimantProposedPlan claimantProposedPlan = ClaimantProposedPlan
            .builder()
            .proposedRepaymentType(IMMEDIATELY)
            .build();
        //Then
        assertThat(claimantProposedPlan.hasProposedPayImmediately()).isTrue();
    }

    @Test
    void shouldReturnTrue_whenProposedToPayBySetDate() {
        //Given
        ClaimantProposedPlan claimantProposedPlan = ClaimantProposedPlan
            .builder()
            .proposedRepaymentType(BY_SET_DATE)
            .build();
        //Then
        assertThat(claimantProposedPlan.hasProposedPayBySetDate());
    }

    @Test
    void shouldReturnTrue_whenProposedToPayByInstallments() {
        //Given
        ClaimantProposedPlan claimantProposedPlan = ClaimantProposedPlan
            .builder()
            .proposedRepaymentType(SUGGESTION_OF_REPAYMENT_PLAN)
            .build();
        //Then
        assertThat(claimantProposedPlan.hasProposedPayByInstallments());
    }

}
