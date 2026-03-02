package uk.gov.hmcts.reform.civil.ga.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class HwFFeeTypeUtilTest {

    @Test
    void updateFeeType_shouldSetAdditionalFeeType_whenCaseStateIsApplicationAddPayment() {
        // Arrange
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .ccdState(CaseState.APPLICATION_ADD_PAYMENT)
                .generalAppHelpWithFees(new HelpWithFees().setHelpWithFeesReferenceNumber("HWF-111-222"))
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee()
                                                                     .setCalculatedAmountInPence(BigDecimal.valueOf(180))
                                                                     .setCode("FEE123")))
                .build();

        // Act
        GeneralApplicationCaseData updatedCaseDataBuilder = HwFFeeTypeUtil.updateHwfDetails(caseData);

        // Assert
        assertThat(updatedCaseDataBuilder.build().getHwfFeeType()).isEqualTo(FeeType.ADDITIONAL);
        assertThat(updatedCaseDataBuilder.build().getAdditionalHwfDetails().getHwfReferenceNumber()).isEqualTo("HWF-111-222");
    }

    @Test
    void updateFeeType_shouldSetApplicationFeeType_whenCaseStateIsNotApplicationAddPayment() {
        // Arrange
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .ccdState(CaseState.AWAITING_RESPONDENT_RESPONSE)
                .generalAppHelpWithFees(new HelpWithFees())
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee()
                                                                 .setCalculatedAmountInPence(BigDecimal.valueOf(180))
                                                                 .setCode("FEE123")))
            .generalAppHelpWithFees(new HelpWithFees().setHelpWithFeesReferenceNumber("HWF-111-222"))
            .build();

        // Act
        GeneralApplicationCaseData updatedCaseDataBuilder = HwFFeeTypeUtil.updateHwfDetails(caseData);

        // Assert
        assertThat(updatedCaseDataBuilder.build().getHwfFeeType()).isEqualTo(FeeType.APPLICATION);
        assertThat(updatedCaseDataBuilder.build().getGaHwfDetails().getHwfReferenceNumber()).isEqualTo("HWF-111-222");
    }

    @Test
    void updateFeeType_shouldNotChangeFeeType_whenGeneralAppHelpWithFeesIsNull() {
        // Arrange
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .ccdState(CaseState.APPLICATION_ADD_PAYMENT)
                .build();

        // Act
        GeneralApplicationCaseData updatedCaseDataBuilder = HwFFeeTypeUtil.updateHwfDetails(caseData);

        // Assert
        assertThat(updatedCaseDataBuilder.build().getHwfFeeType()).isNull();
    }

    @Test
    void getCalculatedFeeInPence_shouldReturnFee_whenFeesIsNotNull() {
        // Arrange
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(
                                new Fee()
                                        .setCalculatedAmountInPence(BigDecimal.valueOf(30000))
                                        .setCode("OOOCM002"))
                        )
                .additionalHwfDetails(new HelpWithFeesDetails())
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
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .build();

        // Act
        BigDecimal feeInPence = HwFFeeTypeUtil.getCalculatedFeeInPence(caseData);

        // Assert
        assertThat(feeInPence).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void should_updateHwfReferenceNumber() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .feePaymentOutcomeDetails(new FeePaymentOutcomeDetails()
                        .setHwfNumberAvailable(YesOrNo.YES)
                        .setHwfNumberForFeePaymentOutcome("hwf"))
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
