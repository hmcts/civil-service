package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingBundleType;

import jakarta.validation.constraints.Future;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TrialHearingTrial {

    private String input1;
    @Future(message = "The date entered must be in the future")
    private LocalDate date1;
    @Future(message = "The date entered must be in the future")
    private LocalDate date2;
    private String input2;
    private String input3;
    private List<DisposalHearingBundleType> type;
}
