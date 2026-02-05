package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.Future;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TrialPersonalInjury {

    private String input1;
    @Future(message = "The date entered must be in the future")
    private LocalDate date1;
    private String input2;
    @Future(message = "The date entered must be in the future")
    private LocalDate date2;
    private String input3;
    @Future(message = "The date entered must be in the future")
    private LocalDate date3;
    private String input4;
    @Future(message = "The date entered must be in the future")
    private LocalDate date4;
}
