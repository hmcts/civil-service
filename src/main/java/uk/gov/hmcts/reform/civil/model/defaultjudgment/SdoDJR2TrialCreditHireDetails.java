package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SdoDJR2TrialCreditHireDetails {

    private String input2;
    @Future(message = "The date entered must be in the future")
    private LocalDate date1;
    private String input3;
    private String input4;
    private String input5;
    @Future(message = "The date entered must be in the future")
    private LocalDate date2;
}
