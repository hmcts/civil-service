package uk.gov.hmcts.reform.civil.service.robotics.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ClaimFeeUtilityTest {

    private static final BigDecimal CLAIM_FEE_IN_POUNDS = new BigDecimal("100.00");
    private static final BigDecimal CLAIM_FEE_IN_PENCE = new BigDecimal("10000");

    @Nested
    class GetCourtFee {

        @Test
        void shouldReturnNull_whenClaimFeeIsNull() {
            // Given
            CaseData caseData = CaseDataBuilder.builder().build();

            // When
            BigDecimal courtFee = ClaimFeeUtility.getCourtFee(caseData);

            // Then
            assertThat(courtFee).isNull();
        }

        @Test
        void shouldReturnCalculatedFee_whenHwfDetailsAreNull() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .claimFee(Fee.builder().calculatedAmountInPence(CLAIM_FEE_IN_PENCE).build())
                .claimIssuedHwfDetails(null)
                .build();

            // When
            BigDecimal courtFee = ClaimFeeUtility.getCourtFee(caseData);

            // Then
            assertThat(courtFee).isEqualTo(CLAIM_FEE_IN_POUNDS);
        }

        @Test
        void shouldReturnCalculatedFee_whenHwfDetailsArePresentButRemissionAndOutstandingAreNull() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .claimFee(Fee.builder().calculatedAmountInPence(CLAIM_FEE_IN_PENCE).build())
                .claimIssuedHwfDetails(HelpWithFeesDetails.builder().build())
                .build();

            // When
            BigDecimal courtFee = ClaimFeeUtility.getCourtFee(caseData);

            // Then
            assertThat(courtFee).isEqualTo(CLAIM_FEE_IN_POUNDS);
        }

        @Test
        void shouldReturnZero_whenFullRemissionIsGranted() {
            // Given
            CaseData caseData = CaseDataBuilder.builder()
                .claimFee(Fee.builder().calculatedAmountInPence(CLAIM_FEE_IN_PENCE).build())
                .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                           .remissionAmount(CLAIM_FEE_IN_PENCE)
                                           .build())
                .build();

            // When
            BigDecimal courtFee = ClaimFeeUtility.getCourtFee(caseData);

            // Then
            assertThat(courtFee).isZero();
        }

        @Test
        void shouldReturnOutstandingFee_whenPartialRemissionIsGrantedAndOutstandingFeeIsPresent() {
            // Given
            BigDecimal outstandingFee = new BigDecimal("25.00");
            BigDecimal remissionAmount = new BigDecimal("7500"); // 75 pounds
            CaseData caseData = CaseDataBuilder.builder()
                .claimFee(Fee.builder().calculatedAmountInPence(CLAIM_FEE_IN_PENCE).build())
                .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                           .remissionAmount(remissionAmount)
                                           .outstandingFeeInPounds(outstandingFee)
                                           .build())
                .build();

            // When
            BigDecimal courtFee = ClaimFeeUtility.getCourtFee(caseData);

            // Then
            assertThat(courtFee).isEqualTo(outstandingFee);
        }

        @Test
        void shouldReturnCalculatedFee_whenPartialRemissionIsGrantedAndOutstandingFeeIsNull() {
            // Given
            BigDecimal remissionAmount = new BigDecimal("7500"); // 75 pounds
            CaseData caseData = CaseDataBuilder.builder()
                .claimFee(Fee.builder().calculatedAmountInPence(CLAIM_FEE_IN_PENCE).build())
                .claimIssuedHwfDetails(HelpWithFeesDetails.builder()
                                           .remissionAmount(remissionAmount)
                                           .outstandingFeeInPounds(null)
                                           .build())
                .build();

            // When
            BigDecimal courtFee = ClaimFeeUtility.getCourtFee(caseData);

            // Then
            assertThat(courtFee).isEqualTo(CLAIM_FEE_IN_POUNDS);
        }
    }
}
