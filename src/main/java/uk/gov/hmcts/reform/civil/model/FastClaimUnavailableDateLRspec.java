package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.dq.SmallClaimUnavailableDateType;
import uk.gov.hmcts.reform.civil.validation.groups.UnavailableDateGroup;
import uk.gov.hmcts.reform.civil.validation.interfaces.IsPresentOrEqualToOrLessThanOneYearInTheFuture;

import java.time.LocalDate;

@Data
@Builder
@IsPresentOrEqualToOrLessThanOneYearInTheFuture(groups = UnavailableDateGroup.class)

public class FastClaimUnavailableDateLRspec {

    private final String who;
    private final LocalDate date;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final String unavailableDateType;
    private final SmallClaimUnavailableDateType smallClaimUnavailableDateType;
}
