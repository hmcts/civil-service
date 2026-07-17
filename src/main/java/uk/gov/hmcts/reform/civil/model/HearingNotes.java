package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HearingNotes {

    @CCD(label = "Order date", searchable = false)
    private LocalDate date;
    @CCD(label = "Notes", searchable = false)
    private String notes;

}
