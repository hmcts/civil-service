package uk.gov.hmcts.reform.civil.model.docmosis;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;

@Builder(toBuilder = true)
@AllArgsConstructor
public class UnavailableDates {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate unavailableTrialDateFrom;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "d MMMM yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate unavailableTrialDateTo;
}
