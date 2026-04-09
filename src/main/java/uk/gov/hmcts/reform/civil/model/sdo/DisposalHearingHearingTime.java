package uk.gov.hmcts.reform.civil.model.sdo;

import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DisposalHearingHearingTime {

    private String input;
    private LocalDate dateFrom;
    @Future(message = "The date entered must be in the future")
    private LocalDate dateTo;
    private DisposalHearingFinalDisposalHearingTimeEstimate time;
    private String otherHours;
    private String otherMinutes;
}
