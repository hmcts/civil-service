package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.hearingvalues.UnavailabilityRangeModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.SINGLE_DATE;
import static uk.gov.hmcts.reform.civil.enums.hearing.UnavailabilityType.ALL_DAY;

public class UnavailabilityDatesUtils {

    private UnavailabilityDatesUtils() {
                //NO-OP
    }

    public static ArrayList<UnavailabilityRangeModel> rollUpExpertUnavailabilityDates(CaseData.CaseDataBuilder<?, ?> builder, boolean defendantResponse) {
        CaseData caseData = builder.build();
        ArrayList<UnavailabilityRangeModel> unavailabilityDates = new ArrayList<>();

        if (defendantResponse) {
            if (caseData.getRespondent1DQ() != null && caseData.getRespondent1DQ().getHearing() != null
                && caseData.getRespondent1().getUnavailableDates() != null) {
                List<UnavailabilityRangeModel> respondent1UnavailableDates = caseData.getRespondent1DQ().getHearing().getUnavailableDates().stream().
                map( date -> mapUnAvailableDateToRange(date.getValue())).collect(Collectors.toList());
                unavailabilityDates.addAll(respondent1UnavailableDates);
            }
            if (caseData.getRespondent2DQ() != null && caseData.getRespondent2DQ().getHearing() != null
                && caseData.getRespondent2().getUnavailableDates() != null) {
                List<UnavailabilityRangeModel> respondent2UnavailableDates = caseData.getRespondent2DQ().getHearing().getUnavailableDates().stream().
                    map( date -> mapUnAvailableDateToRange(date.getValue())).collect(Collectors.toList());
                unavailabilityDates.addAll(respondent2UnavailableDates);
            }
        } else {
            if (caseData.getApplicant1DQ() != null && caseData.getApplicant1DQ().getHearing() != null
                && caseData.getApplicant1().getUnavailableDates() != null) {
                List<UnavailabilityRangeModel> applicant1UnavailableDates = caseData.getApplicant1DQ().getHearing().getUnavailableDates().stream().
                    map( date -> mapUnAvailableDateToRange(date.getValue())).collect(Collectors.toList());
                unavailabilityDates.addAll(applicant1UnavailableDates);
            }
            if (caseData.getApplicant2DQ() != null && caseData.getApplicant2DQ().getHearing() != null
                && caseData.getApplicant2().getUnavailableDates() != null) {
                List<UnavailabilityRangeModel> applicant2UnavailableDates = caseData.getApplicant2DQ().getHearing().getUnavailableDates().stream().
                    map( date -> mapUnAvailableDateToRange(date.getValue())).collect(Collectors.toList());
                unavailabilityDates.addAll(applicant2UnavailableDates);
            }
        }
        return unavailabilityDates;
    }

/*
    private static void getUnavailableDatesForParty(ArrayList<UnavailabilityRangeModel> unavailabilityDates,
                                                    List<Element<UnavailableDate>> unavailableDates) {
        for (Element<UnavailableDate> dateElement : unavailableDates) {
            if (dateElement.getValue().getUnavailableDateType() != null) {
                if (SINGLE_DATE.equals(dateElement.getValue().getUnavailableDateType())) {
                    unavailabilityDates.add(buildUnavailabilityDateObject(dateElement.getValue().getDate(),
                                                                          dateElement.getValue().getDate()));
                } else if (DATE_RANGE.equals(dateElement.getValue().getUnavailableDateType())) {
                    unavailabilityDates.add(buildUnavailabilityDateObject(dateElement.getValue().getFromDate(),
                                                                          dateElement.getValue().getToDate()));
                }
            } else {
                unavailabilityDates.add(buildUnavailabilityDateObject(dateElement.getValue().getDate(),
                                                                      dateElement.getValue().getDate()));
            }
        }
    }

    private static UnavailabilityRangeModel buildUnavailabilityDateObject(LocalDate fromDate, LocalDate toDate) {
        return UnavailabilityRangeModel.builder()
            .unavailableFromDate(fromDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .unavailableToDate(toDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .unavailabilityType(ALL_DAY)
            .build();
    }
*/


    private static UnavailabilityRangeModel mapUnAvailableDateToRange(UnavailableDate date){
        return UnavailabilityRangeModel.builder()
            .unavailabilityType(ALL_DAY)
            .unavailableFromDate(SINGLE_DATE.equals(date.getUnavailableDateType()) ? date.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : date.getFromDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .unavailableToDate(date.getToDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .build();
    }
}
