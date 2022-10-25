package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FastTrackHearingTime {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private FastTrackHearingTimeEstimate hearingDuration;
    private String helpText1;
    private String helpText2;
}
