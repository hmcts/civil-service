package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialHearingTimeEstimateDJ;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TrialHearingTimeDJ {

    @CCD(label = " ", searchable = false)
    private LocalDate date1;
    @CCD(label = "Date to", showCondition = "dateToToggle=\"SHOW\"", searchable = false)
    private LocalDate date2;

    @CCD(label = " ", searchable = false)
    private List<DateToShowToggle> dateToToggle;
    @CCD(label = "The time estimate is", searchable = false)
    private TrialHearingTimeEstimateDJ hearingTimeEstimate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String helpText1;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String helpText2;
    @CCD(label = "Hours", regex = "^([0-9]|[1-9][0-9]|[1-9][0-9][0-9])$", searchable = false)
    private String otherHours;
    @CCD(label = "Minutes", regex = "[0-9]|[0-5][0-9]", searchable = false)
    private String otherMinutes;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Date from", searchable = false, typeOverride = FieldType.Label)
  private String date1Label;
  // ==== end synthesised definition-only fields ====
}
