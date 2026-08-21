package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingFinalDisposalHearingTimeEstimate;

import jakarta.validation.constraints.Future;
import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DisposalHearingFinalDisposalHearingDJ {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input;
    @CCD(label = " ", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate date;
    @CCD(label = "The time estimate ", searchable = false)
    private DisposalHearingFinalDisposalHearingTimeEstimate time;
    @CCD(ignore = true)
    private String otherHours;
    @CCD(ignore = true)
    private String otherMinutes;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String label;
  // ==== end synthesised definition-only fields ====
}
