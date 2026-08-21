package uk.gov.hmcts.reform.civil.model.sdo;

import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2FastTrackCreditHire {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input1;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input5;
    @CCD(label = " ", searchable = false)
    private String input6;
    @CCD(label = " ", searchable = false)
    private String input7;
    @CCD(label = " ", searchable = false)
    private String input8;
    @CCD(label = " ", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate date3;
    @CCD(label = " ", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate date4;
    @CCD(label = " ", searchable = false)
    private List<AddOrRemoveToggle> detailsShowToggle;
    @CCD(label = " ", showCondition = "detailsShowToggle = \"ADD\"", searchable = false)
    private SdoR2FastTrackCreditHireDetails sdoR2FastTrackCreditHireDetails;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String label;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String text;
  @CCD(label = "****", searchable = false, typeOverride = FieldType.Label)
  private String line;
  // ==== end synthesised definition-only fields ====
}
