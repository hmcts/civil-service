package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.validation.groups.UnavailableDateGroup;
import uk.gov.hmcts.reform.civil.validation.interfaces.IsPresentOrEqualToOrLessThanOneYearInTheFuture;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@IsPresentOrEqualToOrLessThanOneYearInTheFuture(groups = UnavailableDateGroup.class)
public class UnavailableDate {

    private final String who;
    private final LocalDate date;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final UnavailableDateType unavailableDateType;
    private final String eventAdded;
    private final LocalDate dateAdded;
}
