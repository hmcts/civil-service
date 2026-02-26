package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HearingNotes {

    private LocalDate date;
    private String notes;

}
