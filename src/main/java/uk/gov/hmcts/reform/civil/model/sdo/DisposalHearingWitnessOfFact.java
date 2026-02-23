package uk.gov.hmcts.reform.civil.model.sdo;

import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DisposalHearingWitnessOfFact {

    private String input3;
    private LocalDate date2;
    private String input4;
    private String input5;
    @Future(message = "The date entered must be in the future")
    private LocalDate date3;
    private String input6;
}
