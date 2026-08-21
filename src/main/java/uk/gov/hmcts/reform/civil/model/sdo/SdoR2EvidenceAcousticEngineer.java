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
public class SdoR2EvidenceAcousticEngineer {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoEvidenceAcousticEngineerTxt;
    @CCD(label = " ", searchable = false)
    private String sdoInstructionOfTheExpertTxt;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoInstructionOfTheExpertDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoInstructionOfTheExpertTxtArea;
    @CCD(label = " ", searchable = false)
    private String sdoExpertReportTxt;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoExpertReportDate;
    @CCD(label = " ", searchable = false)
    private String sdoExpertReportDigitalPortalTxt;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoWrittenQuestionsTxt;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoWrittenQuestionsDate;
    @CCD(label = " ", searchable = false)
    private String sdoWrittenQuestionsDigitalPortalTxt;
    @CCD(label = " ", searchable = false)
    private String sdoRepliesTxt;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoRepliesDate;
    @CCD(label = " ", searchable = false)
    private String sdoRepliesDigitalPortalTxt;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoServiceOfOrderTxt;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### a. Instruction of the expert", searchable = false, typeOverride = FieldType.Label)
  private String sdoInstructionOfTheExpertLbl;
  @CCD(label = "### b. Expert report", searchable = false, typeOverride = FieldType.Label)
  private String sdoExpertReportLbl;
  @CCD(label = "add the report uploaded to the Digital Portal", searchable = false, typeOverride = FieldType.Label)
  private String sdoExpertReportDigitalPortalLbl;
  @CCD(label = "### c. Written questions", searchable = false, typeOverride = FieldType.Label)
  private String sdoWrittenQuestionsLbl;
  @CCD(label = "add the report uploaded to the Digital Portal", searchable = false, typeOverride = FieldType.Label)
  private String sdoWrittenQuestionsDigitalPortalLbl;
  @CCD(label = "### d. Replies", searchable = false, typeOverride = FieldType.Label)
  private String sdoRepliesLbl;
  @CCD(label = "The expert shall", searchable = false, typeOverride = FieldType.Label)
  private String sdoRepliesTheExpertLbl;
  @CCD(label = "and the answers uploaded to the Digital Portal", searchable = false, typeOverride = FieldType.Label)
  private String sdoRepliesDigitalPortalLbl;
  @CCD(label = "### e. Service of order", searchable = false, typeOverride = FieldType.Label)
  private String sdoServiceOfOrderLbl;
  @CCD(
          label = "### f. Expert may apply direct to the court for directions",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String sdoExpertMayApplyMainLbl;
  @CCD(
          label = "The expert may apply direct to the court for directions where necessary under Rule 35.14 of the Civil Procedure Rules.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String sdoExpertMayApplyLbl;
  @CCD(label = "### g. Fees and expenses of the expert", searchable = false, typeOverride = FieldType.Label)
  private String sdoFeesAndExpensesMainLbl;
  @CCD(
          label = "Unless the parties agree in writing or the Court orders otherwise, the fees and expenses of the expert shall be paid by the parties giving instructions for the report equally save that the costs of answering questions shall be paid by the party asking.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String sdoFeesAndExpensesLbl;
  // ==== end synthesised definition-only fields ====
}
