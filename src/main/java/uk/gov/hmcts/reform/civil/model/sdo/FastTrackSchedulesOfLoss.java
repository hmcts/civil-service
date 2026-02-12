package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import jakarta.validation.constraints.Future;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FastTrackSchedulesOfLoss {

    private String input1;
    @Future(message = "The date entered must be in the future")
    private LocalDate date1;
    private String input2;
    @Future(message = "The date entered must be in the future")
    private LocalDate date2;
    private String input3;
}
