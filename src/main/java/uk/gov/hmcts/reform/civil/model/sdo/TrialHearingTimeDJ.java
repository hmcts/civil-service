package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialHearingTimeEstimateDJ;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TrialHearingTimeDJ {

    private LocalDate date1;
    private LocalDate date2;

    private List<DateToShowToggle> dateToToggle;
    private TrialHearingTimeEstimateDJ hearingTimeEstimate;
    private String helpText1;
    private String helpText2;
    private String otherHours;
    private String otherMinutes;
}
