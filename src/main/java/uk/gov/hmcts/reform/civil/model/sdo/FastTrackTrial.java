package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType;

import java.time.LocalDate;
import java.util.List;
import javax.validation.constraints.Future;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FastTrackTrial {

    private String input1;
    @Future(message = "The date entered must be in the future")
    private LocalDate date1;
    @Future(message = "The date entered must be in the future")
    private LocalDate date2;
    private String input2;
    private String input3;
    private List<FastTrackTrialBundleType> type;
}
