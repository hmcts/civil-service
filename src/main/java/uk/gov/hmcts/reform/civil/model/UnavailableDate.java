package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.validation.groups.UnavailableDateGroup;
import uk.gov.hmcts.reform.civil.validation.interfaces.IsPresentOrEqualToOrLessThanOneYearInTheFuture;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@IsPresentOrEqualToOrLessThanOneYearInTheFuture(groups = UnavailableDateGroup.class)
public class UnavailableDate {

    @CCD(
            label = "Name of unavailable person",
            showCondition = "unavailableDateType = \"DO NOT SHOW IN UI\"",
            searchable = false
    )
    private String who;
    @CCD(
            label = "Unavailable date",
            hint = "This date cannot be in the past and must not be more than one year in the future",
            showCondition = "unavailableDateType = \"SINGLE_DATE\"",
            searchable = false
    )
    private LocalDate date;
    @CCD(
            label = "Date from",
            hint = "This date cannot be in the past and must not be more than one year in the future",
            showCondition = "unavailableDateType = \"DATE_RANGE\"",
            searchable = false
    )
    private LocalDate fromDate;
    @CCD(
            label = "Date to",
            hint = "This date cannot be in the past and must not be more than one year in the future",
            showCondition = "unavailableDateType = \"DATE_RANGE\"",
            searchable = false
    )
    private LocalDate toDate;
    @CCD(label = "Add a single date or a date range", searchable = false)
    private UnavailableDateType unavailableDateType;
    @CCD(label = "Event added", searchable = false)
    private String eventAdded;
    @CCD(label = "Date added", searchable = false)
    private LocalDate dateAdded;
}
