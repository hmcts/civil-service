package uk.gov.hmcts.reform.civil.model.sdo;

import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DisposalHearingHearingTime {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input;
    @CCD(label = " ", searchable = false)
    private LocalDate dateFrom;
    @CCD(label = " ", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate dateTo;
    @CCD(label = "The time estimate is", searchable = false)
    private DisposalHearingFinalDisposalHearingTimeEstimate time;
    @CCD(label = "Hours", searchable = false)
    private String otherHours;
    @CCD(label = "Minutes", searchable = false)
    private String otherMinutes;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Date from", searchable = false, typeOverride = FieldType.Label)
  private String dateFromLabel;
  @CCD(label = "Date to", searchable = false, typeOverride = FieldType.Label)
  private String dateToLabel;
  // ==== end synthesised definition-only fields ====
}
