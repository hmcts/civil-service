package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class HearingNotes {

    private final LocalDate date;
    private final String notes;

}
