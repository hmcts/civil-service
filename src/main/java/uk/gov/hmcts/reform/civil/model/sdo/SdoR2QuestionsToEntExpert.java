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
public class SdoR2QuestionsToEntExpert {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoWrittenQuestionsTxt;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoWrittenQuestionsDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoWrittenQuestionsDigPortalTxt;
    @CCD(label = " ", searchable = false)
    private String sdoQuestionsShallBeAnsweredTxt;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoQuestionsShallBeAnsweredDate;
    @CCD(label = " ", searchable = false)
    private String sdoShallBeUploadedTxt;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Written questions of any expert(s)", searchable = false, typeOverride = FieldType.Label)
  private String sdoWrittenQuestionsLbl;
  @CCD(label = "### Questions shall be answered by", searchable = false, typeOverride = FieldType.Label)
  private String sdoQuestionsShallBeAnsweredLbl;
  @CCD(
          label = "and shall be uploaded by the party asking the questions to the Digital Portal",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String sdoShallBeUploadedLbl;
  // ==== end synthesised definition-only fields ====
}
