package uk.gov.hmcts.reform.civil.model.sdo;

import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2FastTrackCreditHireDetails {

    private String input2;
    private String input3;
    private String input4;
    @Future(message = "The date entered must be in the future")
    private LocalDate date1;
    @Future(message = "The date entered must be in the future")
    private LocalDate date2;
}
