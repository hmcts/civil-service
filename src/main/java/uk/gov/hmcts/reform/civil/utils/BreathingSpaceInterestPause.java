package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.StoredBreathingSpace;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class BreathingSpaceInterestPause {

    private BreathingSpaceInterestPause() {
    }

    public static long countPausedDays(CaseData caseData, LocalDate interestFromDate, LocalDate interestToDate) {
        return mergeOverlappingRanges(getBreathingSpaceDateRanges(caseData)).stream()
            .mapToLong(range -> countDaysBreathingSpaceOverlapsInterestPeriod(
                range.start(),
                range.end(),
                interestFromDate,
                interestToDate
            ))
            .sum();
    }

    private static List<BreathingSpaceDateRange> mergeOverlappingRanges(List<BreathingSpaceDateRange> ranges) {
        if (ranges == null || ranges.isEmpty()) {
            return List.of();
        }

        List<BreathingSpaceDateRange> sorted = ranges.stream()
            .sorted(Comparator.comparing(BreathingSpaceDateRange::start))
            .toList();

        List<BreathingSpaceDateRange> merged = new ArrayList<>();
        BreathingSpaceDateRange current = sorted.getFirst();

        for (BreathingSpaceDateRange next : sorted.subList(1, sorted.size())) {
            if (current.canMergeWith(next)) {
                current = current.mergeWith(next);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        return merged;
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
        public boolean canMergeWith(BreathingSpaceDateRange other) {
            // Overlapping or contiguous (e.g., Jan 1-5/Jan 4-10 and Jan 1-5/Jan 6-10)
            return !other.start.isAfter(this.end.plusDays(1));
        }

        public BreathingSpaceDateRange mergeWith(BreathingSpaceDateRange other) {
            LocalDate latestEnd = other.end.isAfter(this.end) ? other.end : this.end;
            return new BreathingSpaceDateRange(this.start, latestEnd);
        }
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
