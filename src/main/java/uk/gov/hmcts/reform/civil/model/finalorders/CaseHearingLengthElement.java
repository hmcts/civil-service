package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseHearingLengthElement {

    @CCD(label = "Days", hint = "For example, 2", searchable = false)
    private String lengthListOtherDays;
    @CCD(label = "Hours", hint = "For example, 4", regex = "^([0-9]|[1-9][0-9]|[1-9][0-9][0-9])$", searchable = false)
    private String lengthListOtherHours;
    @CCD(label = "Minutes", hint = "For example, 0", regex = "[0-9]|[0-5][0-9]", searchable = false)
    private String lengthListOtherMinutes;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Length of hearing", searchable = false, typeOverride = FieldType.Label)
  private String lengthListOtherLabel;
  // ==== end synthesised definition-only fields ====
}
