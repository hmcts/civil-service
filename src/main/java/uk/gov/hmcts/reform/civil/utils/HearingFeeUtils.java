package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static java.util.Objects.nonNull;

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
        BigDecimal claimAmount;
        if (nonNull(caseData.getClaimValue())) {
            claimAmount = caseData.getClaimValue().toPounds();
        } else if (nonNull(caseData.getTotalInterest())) {
            claimAmount = caseData.getTotalClaimAmount()
                .add(caseData.getTotalInterest())
                .setScale(2, RoundingMode.UNNECESSARY);
        } else {
            claimAmount = caseData.getTotalClaimAmount().setScale(2, RoundingMode.UNNECESSARY);
        }

        if (claimTrack.equals("SMALL_CLAIM")) {
            return hearingFeesService.getFeeForHearingSmallClaims(claimAmount);
        } else if (claimTrack.equals("FAST_CLAIM")) {
            return hearingFeesService.getFeeForHearingFastTrackClaims(claimAmount);
        } else {
            return hearingFeesService.getFeeForHearingMultiClaims(claimAmount);
        }
    }
}
