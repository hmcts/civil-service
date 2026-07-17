package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FastTrackHearingTime {

    @CCD(label = " ", searchable = false)
    private LocalDate dateFrom;
    @CCD(label = "Date to", showCondition = "dateToToggle=\"SHOW\"", searchable = false)
    private LocalDate dateTo;
    @CCD(label = " ", searchable = false)
    private List<DateToShowToggle> dateToToggle;
    @CCD(label = "The time estimate is", searchable = false)
    private FastTrackHearingTimeEstimate hearingDuration;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String helpText1;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String helpText2;
    @CCD(label = "Hours", searchable = false)
    private String otherHours;
    @CCD(label = "Minutes", searchable = false)
    private String otherMinutes;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Date from", searchable = false, typeOverride = FieldType.Label)
  private String dateFromLabel;
  // ==== end synthesised definition-only fields ====
}
