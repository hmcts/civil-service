package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FastTrackHearingTime {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<DateToShowToggle> dateToToggle;
    private FastTrackHearingTimeEstimate hearingDuration;
    private String helpText1;
    private String helpText2;
    private String otherHours;
    private String otherMinutes;
}
