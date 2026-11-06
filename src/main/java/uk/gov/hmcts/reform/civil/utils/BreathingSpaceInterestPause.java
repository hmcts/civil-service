package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.StoredBreathingSpace;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public final class BreathingSpaceInterestPause {

    private BreathingSpaceInterestPause() {
    }

    public static long countPausedDays(CaseData caseData, LocalDate interestFromDate, LocalDate interestToDate) {
        long pausedDays = 0;
        for (BreathingSpaceDateRange dateRange : getBreathingSpaceDateRanges(caseData)) {
            pausedDays += countDaysBreathingSpaceOverlapsInterestPeriod(
                dateRange.start(),
                dateRange.end(),
                interestFromDate,
                interestToDate
            );
        }
        return pausedDays;
    }

    private static List<BreathingSpaceDateRange> getBreathingSpaceDateRanges(CaseData caseData) {
        List<BreathingSpaceDateRange> dateRanges = getStoredBreathingSpaceDateRanges(caseData);
        addCurrentBreathingSpaceDateRangeIfMissing(caseData, dateRanges);
        return dateRanges;
    }

    private static List<BreathingSpaceDateRange> getStoredBreathingSpaceDateRanges(CaseData caseData) {
        if (caseData.getBreathing() == null
            || caseData.getBreathing().getStoredBreathingSpace() == null) {
            return new ArrayList<>();
        }
        List<BreathingSpaceDateRange> dateRanges = new ArrayList<>();
        for (Element<StoredBreathingSpace> storedBreathingSpace : caseData.getBreathing().getStoredBreathingSpace()) {
            if (storedBreathingSpace == null || storedBreathingSpace.getValue() == null) {
                continue;
            }
            StoredBreathingSpace storedCycle = storedBreathingSpace.getValue();
            if (storedCycle.getEnter() == null || storedCycle.getEnter().getStart() == null) {
                continue;
            }
            dateRanges.add(new BreathingSpaceDateRange(
                storedCycle.getEnter().getStart(),
                getBreathingSpaceEndDateOrToday(storedCycle.getLift())
            ));
        }
        return dateRanges;
    }

    private static void addCurrentBreathingSpaceDateRangeIfMissing(CaseData caseData,
                                                                   List<BreathingSpaceDateRange> dateRanges) {
        if (caseData.getBreathing() == null || caseData.getBreathing().getEnter() == null) {
            return;
        }
        LocalDate start = caseData.getBreathing().getEnter().getStart();
        if (start == null) {
            return;
        }
        boolean alreadyStored = dateRanges.stream().anyMatch(dateRange -> start.equals(dateRange.start()));
        if (!alreadyStored) {
            dateRanges.add(new BreathingSpaceDateRange(
                start,
                getBreathingSpaceEndDateOrToday(caseData.getBreathing().getLift())
            ));
        }
    }

    private static LocalDate getBreathingSpaceEndDateOrToday(BreathingSpaceLiftInfo lift) {
        if (lift == null || lift.getExpectedEnd() == null) {
            return LocalDate.now();
        }
        return lift.getExpectedEnd();
    }

    private record BreathingSpaceDateRange(LocalDate start, LocalDate end) {
    }

    private static long countDaysBreathingSpaceOverlapsInterestPeriod(LocalDate breathingSpaceStart,
                                                                      LocalDate breathingSpaceEnd,
                                                                      LocalDate interestFromDate,
                                                                      LocalDate interestToDate) {
        LocalDate firstInterestDay = interestFromDate.plusDays(1);
        LocalDate firstPausedInterestDay = breathingSpaceStart.isAfter(firstInterestDay)
            ? breathingSpaceStart
            : firstInterestDay;
        LocalDate lastPausedInterestDay = breathingSpaceEnd.isBefore(interestToDate)
            ? breathingSpaceEnd
            : interestToDate;

        if (lastPausedInterestDay.isBefore(firstPausedInterestDay)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(firstPausedInterestDay, lastPausedInterestDay) + 1;
    }
}
