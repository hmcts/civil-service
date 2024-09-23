package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

public class MediationUnavailableDatesUtils {

    private MediationUnavailableDatesUtils() {
        //NO-OP
    }

    public static final String UNAVAILABLE_DATE_RANGE_MISSING = "Please provide at least one valid Date from if you cannot attend hearing within next 3 months.";
    public static final String INVALID_UNAVAILABILITY_RANGE = "Unavailability Date From cannot be after Unavailability Date To. Please enter valid range.";
    public static final String INVALID_UNAVAILABLE_DATE_BEFORE_TODAY = "Unavailability Date must not be before today.";
    public static final String INVALID_UNAVAILABLE_DATE_FROM_BEFORE_TODAY = "Unavailability Date From must not be before today.";
    public static final String INVALID_UNAVAILABLE_DATE_TO_WHEN_MORE_THAN_3_MONTHS = "Unavailability Date To must not be more than three months in the future.";
    public static final String INVALID_UNAVAILABLE_DATE_WHEN_MORE_THAN_3_MONTHS = "Unavailability Date must not be more than three months in the future.";

    public static void checkUnavailable(List<String> errors,
                                  List<Element<UnavailableDate>> datesUnavailableList) {
        if (isEmpty(datesUnavailableList)) {
            errors.add(UNAVAILABLE_DATE_RANGE_MISSING);
        } else {
            for (Element<UnavailableDate> dateRange : datesUnavailableList) {
                var unavailableDateType = dateRange.getValue().getUnavailableDateType();
                LocalDate dateFrom = dateRange.getValue().getFromDate();
                LocalDate dateTo = dateRange.getValue().getToDate();
                if (unavailableDateType.equals(UnavailableDateType.SINGLE_DATE)) {
                    if (dateRange.getValue().getDate().isBefore(LocalDate.now())) {
                        errors.add(INVALID_UNAVAILABLE_DATE_BEFORE_TODAY);
                    } else if (dateRange.getValue().getDate().isAfter(LocalDate.now().plusMonths(3))) {
                        errors.add(INVALID_UNAVAILABLE_DATE_WHEN_MORE_THAN_3_MONTHS);
                    }
                } else if (unavailableDateType.equals(UnavailableDateType.DATE_RANGE)) {
                    if (dateTo != null && dateTo.isBefore(dateFrom)) {
                        errors.add(INVALID_UNAVAILABILITY_RANGE);
                    } else if (dateFrom != null && dateFrom.isBefore(LocalDate.now())) {
                        errors.add(INVALID_UNAVAILABLE_DATE_FROM_BEFORE_TODAY);
                    } else if (dateTo != null && dateTo.isAfter(LocalDate.now().plusMonths(3))) {
                        errors.add(INVALID_UNAVAILABLE_DATE_TO_WHEN_MORE_THAN_3_MONTHS);
                    }
                }
            }
        }
    }

}
