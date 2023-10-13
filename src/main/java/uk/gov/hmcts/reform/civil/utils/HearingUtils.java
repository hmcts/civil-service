package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.HearingNotes;
import uk.gov.hmcts.reform.civil.model.Party;

import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class HearingUtils {

    private HearingUtils() {

    }

    public static LocalDate addBusinessDays(LocalDate localDate, int days, Set<LocalDate> holidaysSet) {
        if (localDate == null || days <= 0 || holidaysSet.isEmpty()) {
            throw new IllegalArgumentException("Invalid method argument(s)");
        }

        List<LocalDate> holidays = new ArrayList<>(holidaysSet);

        Predicate<LocalDate> isHoliday = holidays::contains;

        Predicate<LocalDate> isWeekend = date
            -> date.getDayOfWeek() == DayOfWeek.SATURDAY
            || date.getDayOfWeek() == DayOfWeek.SUNDAY;

        LocalDate result = localDate;
        while (days > 0) {
            result = result.plusDays(1);
            if (isHoliday.or(isWeekend).negate().test(result)) {
                days--;
            }
        }
        return result;
    }

    public static String getHearingType(CaseData caseData) {
        switch (caseData.getChannel()) {
            case IN_PERSON:
                return caseData.getHearingLocation().getValue().getLabel();
            case VIDEO:
                return "video conference";
            case TELEPHONE:
                return "telephone";
            default:
                return "not defined";
        }
    }

    public static String formatHearingDuration(HearingDuration hearingDuration) {
        switch (hearingDuration) {
            case MINUTES_30:
                return "30 minutes";
            case MINUTES_60:
                return "1 hour";
            case MINUTES_90:
                return "1 and half hours";
            case MINUTES_120:
                return "2 hours";
            case MINUTES_150:
                return "2 and half hours";
            case MINUTES_180:
                return "3 hours";
            case MINUTES_240:
                return "4 hours";
            case DAY_1:
                return "1 day";
            case DAY_2:
                return "2 days";
            default:
                return null;
        }
    }

    public static String formatHearingFee(Fee hearingFee) {
        if (nonNull(hearingFee) && hearingFee.getCalculatedAmountInPence().intValue() > 0) {
            DecimalFormat formatter = new DecimalFormat("£#,###");
            return formatter.format(hearingFee.getCalculatedAmountInPence().intValue() / 100);
        }
        return null;
    }

    public static String getHearingTimeFormatted(String hearingTime) {
        if (isEmpty(hearingTime) || hearingTime.length() != 4 || !hearingTime.matches("[0-9]+")) {
            return null;
        }

        StringBuilder hearingTimeBuilder = new StringBuilder(hearingTime);
        hearingTimeBuilder.insert(2, ':');
        return hearingTimeBuilder.toString();
    }

    public static HearingNotes formatHearingNote(String hearingNotes) {
        return HearingNotes.builder().date(LocalDate.now()).notes(hearingNotes).build();
    }

    public static HearingNotes getHearingNotes(CaseData caseData) {
        if (caseData.getDisposalHearingHearingNotes() != null) {
            return formatHearingNote(caseData.getDisposalHearingHearingNotes());
        } else if (caseData.getFastTrackHearingNotes() != null) {
            return formatHearingNote(caseData.getFastTrackHearingNotes().getInput());
        } else if (caseData.getDisposalHearingHearingNotesDJ() != null) {
            return formatHearingNote(caseData.getDisposalHearingHearingNotesDJ().getInput());
        } else if (caseData.getSdoHearingNotes() != null) {
            return formatHearingNote(caseData.getSdoHearingNotes().getInput());
        } else if (caseData.getTrialHearingHearingNotesDJ() != null) {
            return formatHearingNote(caseData.getTrialHearingHearingNotesDJ().getInput());
        } else {
            return null;
        }
    }

    public static String getClaimantVDefendant(CaseData caseData) {
        StringBuilder name = new StringBuilder(getTypeUserLastName(caseData.getApplicant1()));
        name.append(" v ").append(getTypeUserLastName(caseData.getRespondent1()));
        return name.toString();
    }

    public static String getTypeUserLastName(Party party) {
        return switch (party.getType()) {
            case INDIVIDUAL -> party.getIndividualLastName();
            case COMPANY -> party.getCompanyName();
            case SOLE_TRADER -> party.getSoleTraderLastName();
            default -> party.getOrganisationName();
        };
    }
}
