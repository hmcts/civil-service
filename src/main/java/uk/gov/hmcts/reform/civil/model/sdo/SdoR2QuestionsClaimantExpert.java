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
public class SdoR2QuestionsClaimantExpert {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoDefendantMayAskTxt;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoDefendantMayAskDate;
    @CCD(label = " ", searchable = false)
    private String sdoQuestionsShallBeAnsweredTxt;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoQuestionsShallBeAnsweredDate;
    @CCD(label = " ", searchable = false)
    private String sdoUploadedToDigitalPortalTxt;
    @CCD(label = " ", searchable = false)
    private SdoR2ApplicationToRelyOnFurther sdoApplicationToRelyOnFurther;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Defendant(s) may ask questions", searchable = false, typeOverride = FieldType.Label)
  private String sdoDefendantMayAskLbl;
  @CCD(label = "### Questions shall be answered by", searchable = false, typeOverride = FieldType.Label)
  private String sdoQuestionsShallBeAnsweredLbl;
  @CCD(label = "and uploaded to the Digital Portal", searchable = false, typeOverride = FieldType.Label)
  private String sdoUploadedToDigitalPortalLbl;
  // ==== end synthesised definition-only fields ====
}
