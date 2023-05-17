package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;

public class HearingFeeUtils {

    private HearingFeeUtils() {
        //NO-OP
    }

    public static LocalDate calculateHearingDueDate(LocalDate now, LocalDate hearingDate) {
        LocalDate calculatedHearingDueDate;
        if (now.isBefore(hearingDate.minusDays(36))) {
            calculatedHearingDueDate = now.plusDays(28);
        } else {
            calculatedHearingDueDate = now.plusDays(7);
        }

        if (calculatedHearingDueDate.isAfter(hearingDate)) {
            calculatedHearingDueDate = hearingDate;
        }

        return calculatedHearingDueDate;
    }

    public static Fee calculateAndApplyFee(HearingFeesService hearingFeesService,
                                           CaseData caseData, AllocatedTrack allocatedTrack) {
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

        if (SMALL_CLAIM.equals(allocatedTrack)) {
            return hearingFeesService.getFeeForHearingSmallClaims(claimAmount);
        } else if (FAST_CLAIM.equals(allocatedTrack)) {
            return hearingFeesService.getFeeForHearingFastTrackClaims(claimAmount);
        } else {
            return hearingFeesService.getFeeForHearingMultiClaims(claimAmount);
        }
    }
}
