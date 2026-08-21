package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2PermissionToRelyOnExpert {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoPermissionToRelyOnExpertTxt;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoPermissionToRelyOnExpertDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoJointMeetingOfExpertsTxt;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoJointMeetingOfExpertsDate;
    @CCD(label = " ", searchable = false)
    private String sdoUploadedToDigitalPortalTxt;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Permission to rely on expert evidence", searchable = false, typeOverride = FieldType.Label)
  private String label;
  @CCD(label = "### Joint meeting of experts", searchable = false, typeOverride = FieldType.Label)
  private String sdoJointMeetingOfExpertsLbl;
  @CCD(label = "and uploaded to the Digital Portal", searchable = false, typeOverride = FieldType.Label)
  private String sdoUploadedToDigitalPortalLbl;
  // ==== end synthesised definition-only fields ====
}
