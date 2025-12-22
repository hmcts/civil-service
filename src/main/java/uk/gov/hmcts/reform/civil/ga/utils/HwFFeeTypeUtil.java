package uk.gov.hmcts.reform.civil.ga.utils;

import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.HelpWithFeesDetails;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NO_REMISSION_HWF_GA;

public class HwFFeeTypeUtil {

    private HwFFeeTypeUtil() {
    }

    public static GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> updateHwfDetails(GeneralApplicationCaseData caseData) {
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        if (Objects.nonNull(caseData.getGeneralAppHelpWithFees())) {
            if (caseData.getCcdState().equals(CaseState.APPLICATION_ADD_PAYMENT)) {
                caseDataBuilder.hwfFeeType(FeeType.ADDITIONAL);
                if (Objects.isNull(caseData.getAdditionalHwfDetails())) {
                    caseDataBuilder.additionalHwfDetails(HelpWithFeesDetails.builder()
                                                             .hwfFeeType(FeeType.ADDITIONAL)
                                                             .fee(caseData.getGeneralAppPBADetails().getFee())
                                                             .hwfReferenceNumber(caseData.getGeneralAppHelpWithFees().getHelpWithFeesReferenceNumber())
                                                             .build());

                }
            } else {
                caseDataBuilder.hwfFeeType(FeeType.APPLICATION);
                if (Objects.isNull(caseData.getGaHwfDetails())) {
                    caseDataBuilder.gaHwfDetails(HelpWithFeesDetails.builder()
                                                     .hwfFeeType(FeeType.APPLICATION)
                                                     .fee(caseData.getGeneralAppPBADetails().getFee())
                                                     .hwfReferenceNumber(caseData.getGeneralAppHelpWithFees().getHelpWithFeesReferenceNumber())
                                                     .build());

                }
            }
        }
        return caseDataBuilder;
    }

    public static BigDecimal getCalculatedFeeInPence(GeneralApplicationCaseData caseData) {
        if (Objects.nonNull(caseData.getGeneralAppPBADetails())
                && Objects.nonNull(caseData.getGeneralAppPBADetails().getFee())) {
            return caseData.getGeneralAppPBADetails().getFee().getCalculatedAmountInPence();
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal getGaRemissionAmount(GeneralApplicationCaseData caseData) {
        if (Objects.nonNull(caseData.getGaHwfDetails())
                && Objects.nonNull(caseData.getGaHwfDetails().getRemissionAmount())) {
            return caseData.getGaHwfDetails().getRemissionAmount();
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal getAdditionalRemissionAmount(GeneralApplicationCaseData caseData) {
        if (Objects.nonNull(caseData.getAdditionalHwfDetails())
                && Objects.nonNull(caseData.getAdditionalHwfDetails().getRemissionAmount())) {
            return caseData.getAdditionalHwfDetails().getRemissionAmount();
        }
        return BigDecimal.ZERO;
    }

    public static GeneralApplicationCaseData updateOutstandingFee(GeneralApplicationCaseData caseData, String caseEventId) {
        var updatedData = caseData.toBuilder();
        BigDecimal gaRemissionAmount = NO_REMISSION_HWF_GA == CaseEvent.valueOf(caseEventId)
                ? BigDecimal.ZERO
                : getGaRemissionAmount(caseData);
        BigDecimal hearingRemissionAmount = NO_REMISSION_HWF_GA == CaseEvent.valueOf(caseEventId)
                ? BigDecimal.ZERO
                : getAdditionalRemissionAmount(caseData);
        BigDecimal feeAmount = getCalculatedFeeInPence(caseData);
        BigDecimal outstandingFeeAmount;

        if (caseData.isHWFTypeApplication() && BigDecimal.ZERO.compareTo(feeAmount) != 0) {
            outstandingFeeAmount = feeAmount.subtract(gaRemissionAmount);
            updatedData.gaHwfDetails(
                    caseData.getGaHwfDetails().toBuilder()
                            .remissionAmount(gaRemissionAmount)
                            .outstandingFee(outstandingFeeAmount)
                            .build()
            );
        } else if (caseData.isHWFTypeAdditional() && BigDecimal.ZERO.compareTo(feeAmount) != 0) {
            outstandingFeeAmount = feeAmount.subtract(hearingRemissionAmount);
            updatedData.additionalHwfDetails(
                    caseData.getAdditionalHwfDetails().toBuilder()
                            .remissionAmount(hearingRemissionAmount)
                            .outstandingFee(outstandingFeeAmount)
                            .build()
            );
        }
        return updatedData.build();
    }

    public static GeneralApplicationCaseData updateHwfReferenceNumber(GeneralApplicationCaseData caseData) {
        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> updatedData = caseData.toBuilder();

        if (Objects.nonNull(caseData.getFeePaymentOutcomeDetails())
                && caseData.getFeePaymentOutcomeDetails().getHwfNumberAvailable() == YesOrNo.YES) {
            HelpWithFees helpWithFees = HelpWithFees.builder()
                    .helpWithFee(YesOrNo.YES)
                    .helpWithFeesReferenceNumber(caseData.getFeePaymentOutcomeDetails().getHwfNumberForFeePaymentOutcome())
                    .build();
            updatedData.generalAppHelpWithFees(helpWithFees);
            clearHwfReferenceProperties(updatedData);
        }
        return updatedData.build();
    }

    private static void clearHwfReferenceProperties(GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> caseDataBuilder) {
        GeneralApplicationCaseData caseData = caseDataBuilder.build();
        caseDataBuilder.feePaymentOutcomeDetails(caseData.getFeePaymentOutcomeDetails().toBuilder()
                .hwfNumberAvailable(null)
                .hwfNumberForFeePaymentOutcome(null).build());
    }

    public static void updateEventInHwfDetails(GeneralApplicationCaseData caseData,
                                               GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder<?, ?> caseDataBuilder,
                                               CaseEvent eventId) {

        if (caseData.getHwfFeeType().equals(FeeType.ADDITIONAL)) {
            HelpWithFeesDetails additionalFeeDetails =
                Optional.ofNullable(caseData.getAdditionalHwfDetails()).orElse(new HelpWithFeesDetails());
            caseDataBuilder.additionalHwfDetails(additionalFeeDetails.toBuilder().hwfCaseEvent(eventId).build());
        }
        if (caseData.getHwfFeeType().equals(FeeType.APPLICATION)) {
            HelpWithFeesDetails gaHwfDetails =
                Optional.ofNullable(caseData.getGaHwfDetails()).orElse(new HelpWithFeesDetails());
            caseDataBuilder.gaHwfDetails(gaHwfDetails.toBuilder().hwfCaseEvent(eventId).build());

        }
    }
}
