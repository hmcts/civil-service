package uk.gov.hmcts.reform.civil.ga.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class HwFFeeTypeUtilTest {

    @Test
    void updateFeeType_shouldSetAdditionalFeeType_whenCaseStateIsApplicationAddPayment() {
        // Arrange
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .ccdState(CaseState.APPLICATION_ADD_PAYMENT)
                .generalAppHelpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber("HWF-111-222").build())
                .generalAppPBADetails(GAPbaDetails.builder().fee(Fee.builder()
                                                                     .calculatedAmountInPence(BigDecimal.valueOf(180))
                                                                     .code("FEE123").build()).build())
                .build();

        // Act
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> updatedCaseDataBuilder = HwFFeeTypeUtil.updateHwfDetails(caseData);

        // Assert
        assertThat(updatedCaseDataBuilder.build().getHwfFeeType()).isEqualTo(FeeType.ADDITIONAL);
        assertThat(updatedCaseDataBuilder.build().getAdditionalHwfDetails().getHwfReferenceNumber().equals("HWF-111-222"));
    }

    @Test
    void updateFeeType_shouldSetApplicationFeeType_whenCaseStateIsNotApplicationAddPayment() {
        // Arrange
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .ccdState(CaseState.AWAITING_RESPONDENT_RESPONSE)
                .generalAppHelpWithFees(HelpWithFees.builder().build())
                .generalAppPBADetails(GAPbaDetails.builder().fee(Fee.builder()
                                                                 .calculatedAmountInPence(BigDecimal.valueOf(180))
                                                                 .code("FEE123").build()).build())
            .generalAppHelpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber("HWF-111-222").build())
            .build();

        // Act
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> updatedCaseDataBuilder = HwFFeeTypeUtil.updateHwfDetails(caseData);

        // Assert
        assertThat(updatedCaseDataBuilder.build().getHwfFeeType()).isEqualTo(FeeType.APPLICATION);
        assertThat(updatedCaseDataBuilder.build().getGaHwfDetails().getHwfReferenceNumber().equals("HWF-111-222"));
    }

    @Test
    void updateFeeType_shouldNotChangeFeeType_whenGeneralAppHelpWithFeesIsNull() {
        // Arrange
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .ccdState(CaseState.APPLICATION_ADD_PAYMENT)
                .build();

        // Act
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> updatedCaseDataBuilder = HwFFeeTypeUtil.updateHwfDetails(caseData);

        // Assert
        assertThat(updatedCaseDataBuilder.build().getHwfFeeType()).isNull();
    }

    @Test
    void getCalculatedFeeInPence_shouldReturnFee_whenFeesIsNotNull() {
        // Arrange
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .generalAppPBADetails(GAPbaDetails.builder().fee(
                                Fee.builder()
                                        .calculatedAmountInPence(BigDecimal.valueOf(30000))
                                        .code("OOOCM002").build())
                        .build())
                .additionalHwfDetails(HelpWithFeesDetails.builder().build())
                .hwfFeeType(FeeType.ADDITIONAL)
                .build();

        // Act
        BigDecimal feeInPence = HwFFeeTypeUtil.getCalculatedFeeInPence(caseData);

        // Assert
        assertThat(feeInPence).isEqualTo(BigDecimal.valueOf(30000));
    }

    @Test
    void getCalculatedFeeInPence_shouldReturnZero_whenFeesIsNull() {
        // Arrange
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .build();

        // Act
        BigDecimal feeInPence = HwFFeeTypeUtil.getCalculatedFeeInPence(caseData);

        // Assert
        assertThat(feeInPence).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void should_updateHwfReferenceNumber() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
                .feePaymentOutcomeDetails(FeePaymentOutcomeDetails
                        .builder()
                        .hwfNumberAvailable(YesOrNo.YES)
                        .hwfNumberForFeePaymentOutcome("hwf").build())
                .build();
        GeneralApplicationCaseData updatedCaseData = HwFFeeTypeUtil.updateHwfReferenceNumber(caseData);
        assertThat(updatedCaseData.getGeneralAppHelpWithFees().getHelpWithFeesReferenceNumber())
                .isEqualTo("hwf");
        assertThat(updatedCaseData.getGeneralAppHelpWithFees().getHelpWithFee())
                .isEqualTo(YesOrNo.YES);
        assertThat(updatedCaseData.getFeePaymentOutcomeDetails()
                .getHwfNumberForFeePaymentOutcome()).isNull();
        assertThat(updatedCaseData.getFeePaymentOutcomeDetails()
                .getHwfNumberAvailable()).isNull();
    }
}
