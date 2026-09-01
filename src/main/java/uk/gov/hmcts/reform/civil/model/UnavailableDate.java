package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.config.LenientLocalDateDeserializer;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.validation.groups.UnavailableDateGroup;
import uk.gov.hmcts.reform.civil.validation.interfaces.IsPresentOrEqualToOrLessThanOneYearInTheFuture;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@IsPresentOrEqualToOrLessThanOneYearInTheFuture(groups = UnavailableDateGroup.class)
public class UnavailableDate {

    private String who;
    @JsonDeserialize(using = LenientLocalDateDeserializer.class)
    private LocalDate date;
    @JsonDeserialize(using = LenientLocalDateDeserializer.class)
    private LocalDate fromDate;
    @JsonDeserialize(using = LenientLocalDateDeserializer.class)
    private LocalDate toDate;
    private UnavailableDateType unavailableDateType;
    private String eventAdded;
    @JsonDeserialize(using = LenientLocalDateDeserializer.class)
    private LocalDate dateAdded;
}
