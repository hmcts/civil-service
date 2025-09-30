package uk.gov.hmcts.reform.civil.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.utils.HwFFeeTypeService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class HwFFeeTypeServiceTest {

    @Test
    void updateFeeType_shouldSetAdditionalFeeType_whenCaseStateIsApplicationAddPayment() {
        // Arrange
        CaseData caseData = CaseData.builder()
                .ccdState(CaseState.APPLICATION_ADD_PAYMENT)
                .generalAppHelpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber("HWF-111-222").build())
                .generalAppPBADetails(GAPbaDetails.builder().fee(Fee.builder()
                                                                     .calculatedAmountInPence(BigDecimal.valueOf(180))
                                                                     .code("FEE123").build()).build())
                .build();

        // Act
        CaseData.CaseDataBuilder updatedCaseDataBuilder = HwFFeeTypeService.updateHwfDetails(caseData);

        // Assert
        assertThat(updatedCaseDataBuilder.build().getHwfFeeType()).isEqualTo(FeeType.ADDITIONAL);
        assertThat(updatedCaseDataBuilder.build().getAdditionalHwfDetails().getHwfReferenceNumber().equals("HWF-111-222"));
    }

    @Test
    void updateFeeType_shouldSetApplicationFeeType_whenCaseStateIsNotApplicationAddPayment() {
        // Arrange
        CaseData caseData = CaseData.builder()
                .ccdState(CaseState.AWAITING_RESPONDENT_RESPONSE)
                .generalAppHelpWithFees(HelpWithFees.builder().build())
                .generalAppPBADetails(GAPbaDetails.builder().fee(Fee.builder()
                                                                 .calculatedAmountInPence(BigDecimal.valueOf(180))
                                                                 .code("FEE123").build()).build())
            .generalAppHelpWithFees(HelpWithFees.builder().helpWithFeesReferenceNumber("HWF-111-222").build())
            .build();

        // Act
        CaseData.CaseDataBuilder updatedCaseDataBuilder = HwFFeeTypeService.updateHwfDetails(caseData);

        // Assert
        assertThat(updatedCaseDataBuilder.build().getHwfFeeType()).isEqualTo(FeeType.APPLICATION);
        assertThat(updatedCaseDataBuilder.build().getGaHwfDetails().getHwfReferenceNumber().equals("HWF-111-222"));
    }

    @Test
    void updateFeeType_shouldNotChangeFeeType_whenGeneralAppHelpWithFeesIsNull() {
        // Arrange
        CaseData caseData = CaseData.builder()
                .ccdState(CaseState.APPLICATION_ADD_PAYMENT)
                .build();

        // Act
        CaseData.CaseDataBuilder updatedCaseDataBuilder = HwFFeeTypeService.updateHwfDetails(caseData);

        // Assert
        assertThat(updatedCaseDataBuilder.build().getHwfFeeType()).isNull();
    }

    @Test
    void getCalculatedFeeInPence_shouldReturnFee_whenFeesIsNotNull() {
        // Arrange
        CaseData caseData = CaseData.builder()
                .generalAppPBADetails(GAPbaDetails.builder().fee(
                                Fee.builder()
                                        .calculatedAmountInPence(BigDecimal.valueOf(30000))
                                        .code("OOOCM002").build())
                        .build())
                .additionalHwfDetails(HelpWithFeesDetails.builder().build())
                .hwfFeeType(FeeType.ADDITIONAL)
                .build();

        // Act
        BigDecimal feeInPence = HwFFeeTypeService.getCalculatedFeeInPence(caseData);

        // Assert
        assertThat(feeInPence).isEqualTo(BigDecimal.valueOf(30000));
    }

    @Test
    void getCalculatedFeeInPence_shouldReturnZero_whenFeesIsNull() {
        // Arrange
        CaseData caseData = CaseData.builder()
                .build();

        // Act
        BigDecimal feeInPence = HwFFeeTypeService.getCalculatedFeeInPence(caseData);

        // Assert
        assertThat(feeInPence).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void should_updateHwfReferenceNumber() {
        CaseData caseData = CaseData.builder()
                .feePaymentOutcomeDetails(FeePaymentOutcomeDetails
                        .builder()
                        .hwfNumberAvailable(YesOrNo.YES)
                        .hwfNumberForFeePaymentOutcome("hwf").build())
                .build();
        CaseData updatedCaseData = HwFFeeTypeService.updateHwfReferenceNumber(caseData);
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
