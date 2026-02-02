package uk.gov.hmcts.reform.civil.model.defaultjudgment;

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
public class TrialCreditHire {

    private String input1;
    private String input2;
    @Future(message = "The date entered must be in the future")
    private LocalDate date1;
    private String input3;
    private String input4;
    @Future(message = "The date entered must be in the future")
    private LocalDate date2;
    private String input5;
    @Future(message = "The date entered must be in the future")
    private LocalDate date3;
    private String input6;
    private String input7;
    @Future(message = "The date entered must be in the future")
    private LocalDate date4;
    private String input8;
}
