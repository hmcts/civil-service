package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialHearingTimeEstimateDJ;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TrialHearingTimeDJ {

    private LocalDate dateFrom;
    private LocalDate dateTo;
    private TrialHearingTimeEstimateDJ hearingDuration;
    private String helpText1;
    private String helpText2;
}
