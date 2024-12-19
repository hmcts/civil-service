package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HearingFeeUtils {

    private HearingFeeUtils() {
        //NO-OP
    }

    public static LocalDate calculateHearingDueDate(LocalDate now, LocalDate hearingDate) {
        LocalDate calculatedHearingDueDate;
        if (now.isBefore(hearingDate.minusDays(36))) {
            calculatedHearingDueDate = hearingDate.minusDays(28);
        } else {
            calculatedHearingDueDate = now.plusDays(7);
        }

        if (calculatedHearingDueDate.isAfter(hearingDate)) {
            calculatedHearingDueDate = hearingDate;
        }

        return calculatedHearingDueDate;
    }

    public static Fee calculateAndApplyFee(HearingFeesService hearingFeesService,
                                           CaseData caseData, String claimTrack) {
        BigDecimal claimAmount = caseData.getClaimAmountInPounds();

        return switch (claimTrack) {
            case "SMALL_CLAIM" -> hearingFeesService.getFeeForHearingSmallClaims(claimAmount);
            case "FAST_CLAIM" -> hearingFeesService.getFeeForHearingFastTrackClaims(claimAmount);
            case "INTERMEDIATE_CLAIM", "MULTI_CLAIM" -> hearingFeesService.getFeeForHearingMultiClaims(claimAmount);
            default -> throw new IllegalArgumentException("Invalid claim track: " + claimTrack);
        };
    }
}
