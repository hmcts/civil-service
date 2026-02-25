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

    public static GeneralApplicationCaseData updateHwfDetails(GeneralApplicationCaseData caseData) {
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        if (Objects.nonNull(caseData.getGeneralAppHelpWithFees())) {
            if (caseData.getCcdState().equals(CaseState.APPLICATION_ADD_PAYMENT)) {
                caseDataBuilder.hwfFeeType(FeeType.ADDITIONAL);
                if (Objects.isNull(caseData.getAdditionalHwfDetails())) {
                    caseDataBuilder.additionalHwfDetails(new HelpWithFeesDetails()
                        .setHwfFeeType(FeeType.ADDITIONAL)
                        .setFee(caseData.getGeneralAppPBADetails().getFee())
                        .setHwfReferenceNumber(caseData.getGeneralAppHelpWithFees().getHelpWithFeesReferenceNumber()));

                }
            } else {
                caseDataBuilder.hwfFeeType(FeeType.APPLICATION);
                if (Objects.isNull(caseData.getGaHwfDetails())) {
                    caseDataBuilder.gaHwfDetails(new HelpWithFeesDetails()
                        .setHwfFeeType(FeeType.APPLICATION)
                        .setFee(caseData.getGeneralAppPBADetails().getFee())
                        .setHwfReferenceNumber(caseData.getGeneralAppHelpWithFees().getHelpWithFeesReferenceNumber()));

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
        var updatedData = caseData.copy();
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
            HelpWithFeesDetails updatedGaHwfDetails = caseData.getGaHwfDetails().copy();
            updatedGaHwfDetails
                .setRemissionAmount(gaRemissionAmount)
                .setOutstandingFee(outstandingFeeAmount);
            updatedData.gaHwfDetails(updatedGaHwfDetails);
        } else if (caseData.isHWFTypeAdditional() && BigDecimal.ZERO.compareTo(feeAmount) != 0) {
            outstandingFeeAmount = feeAmount.subtract(hearingRemissionAmount);
            HelpWithFeesDetails updatedAdditionalHwfDetails = caseData.getAdditionalHwfDetails().copy();
            updatedAdditionalHwfDetails
                .setRemissionAmount(hearingRemissionAmount)
                .setOutstandingFee(outstandingFeeAmount);
            updatedData.additionalHwfDetails(updatedAdditionalHwfDetails);
        }
        return updatedData.build();
    }

    public static GeneralApplicationCaseData updateHwfReferenceNumber(GeneralApplicationCaseData caseData) {
        GeneralApplicationCaseData updatedData = caseData.copy();

        if (Objects.nonNull(caseData.getFeePaymentOutcomeDetails())
                && caseData.getFeePaymentOutcomeDetails().getHwfNumberAvailable() == YesOrNo.YES) {
            HelpWithFees helpWithFees = new HelpWithFees()
                    .setHelpWithFee(YesOrNo.YES)
                    .setHelpWithFeesReferenceNumber(caseData.getFeePaymentOutcomeDetails().getHwfNumberForFeePaymentOutcome());
            updatedData.generalAppHelpWithFees(helpWithFees);
            clearHwfReferenceProperties(updatedData);
        }
        return updatedData.build();
    }

    private static void clearHwfReferenceProperties(GeneralApplicationCaseData caseDataBuilder) {
        GeneralApplicationCaseData caseData = caseDataBuilder.build();
        caseDataBuilder.feePaymentOutcomeDetails(caseData.getFeePaymentOutcomeDetails().copy()
                .setHwfNumberAvailable(null)
                .setHwfNumberForFeePaymentOutcome(null));
    }

    public static void updateEventInHwfDetails(GeneralApplicationCaseData caseData,
                                               GeneralApplicationCaseData caseDataBuilder,
                                               CaseEvent eventId) {

        if (caseData.getHwfFeeType().equals(FeeType.ADDITIONAL)) {
            HelpWithFeesDetails additionalFeeDetails =
                Optional.ofNullable(caseData.getAdditionalHwfDetails()).orElse(new HelpWithFeesDetails());
            caseDataBuilder.additionalHwfDetails(additionalFeeDetails.copy().setHwfCaseEvent(eventId));
        }
        if (caseData.getHwfFeeType().equals(FeeType.APPLICATION)) {
            HelpWithFeesDetails gaHwfDetails =
                Optional.ofNullable(caseData.getGaHwfDetails()).orElse(new HelpWithFeesDetails());
            caseDataBuilder.gaHwfDetails(gaHwfDetails.copy().setHwfCaseEvent(eventId));

        }
    }
}
