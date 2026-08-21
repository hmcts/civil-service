package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class HearingOtherComments {

    @CCD(
            label = "Is there anything else the court needs to know?",
            hint = "For example, a witness needs to leave the court by 3pm due to caring responsibilities.",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String hearingOtherComments;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Other information", searchable = false, typeOverride = FieldType.Label)
  private String startLabel;
  // ==== end synthesised definition-only fields ====
}
