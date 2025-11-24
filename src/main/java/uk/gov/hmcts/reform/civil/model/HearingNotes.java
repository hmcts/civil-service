package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Accessors(chain = true)
@Data
@Builder
public class HearingNotes {

    private LocalDate date;
    private String notes;

}
