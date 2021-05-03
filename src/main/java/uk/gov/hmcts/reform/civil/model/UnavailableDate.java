package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.validation.groups.UnavailableDateGroup;
import uk.gov.hmcts.reform.civil.validation.interfaces.IsPresentOrEqualToOrLessThanOneYearInTheFuture;

import java.time.LocalDate;

@Data
@Builder
@IsPresentOrEqualToOrLessThanOneYearInTheFuture(groups = UnavailableDateGroup.class)
public class UnavailableDate {

    private final String who;
    private final LocalDate date;
}
