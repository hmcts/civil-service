package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialHearingTimeEstimateDJ;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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
