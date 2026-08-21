package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2SmallClaimsHearingLengthOther {

    @CCD(label = "Days", hint = "For example,2", searchable = false)
    private Integer trialLengthDays;
    @CCD(label = "Hours", hint = "For example,4", searchable = false)
    private Integer trialLengthHours;
    @CCD(label = "Minutes", hint = "For example,0", searchable = false)
    private Integer trialLengthMinutes;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "**Length of hearing**", searchable = false, typeOverride = FieldType.Label)
  private String title;
  // ==== end synthesised definition-only fields ====
}
