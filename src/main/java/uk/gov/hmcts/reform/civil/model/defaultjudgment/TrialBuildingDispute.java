package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import javax.validation.constraints.Future;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TrialBuildingDispute {

    private String input1;
    private String input2;
    private String input3;
    @Future(message = "The date entered must be in the future")
    private LocalDate date1;
    private String input4;
    @Future(message = "The date entered must be in the future")
    private LocalDate date2;
}
