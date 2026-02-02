package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingFinalDisposalHearingTimeEstimate;

import jakarta.validation.constraints.Future;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DisposalHearingFinalDisposalHearingDJ {

    private String input;
    @Future(message = "The date entered must be in the future")
    private LocalDate date;
    private DisposalHearingFinalDisposalHearingTimeEstimate time;
    private String otherHours;
    private String otherMinutes;
}
