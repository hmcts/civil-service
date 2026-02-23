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
public class DisposalHearingSchedulesOfLoss {

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
