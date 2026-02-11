package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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
