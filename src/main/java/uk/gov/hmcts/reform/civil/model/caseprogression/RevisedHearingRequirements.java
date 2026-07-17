package uk.gov.hmcts.reform.civil.model.caseprogression;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RevisedHearingRequirements {

    @CCD(
            label = "Has anything changed to the support or adjustments you wish the court and the judge to consider for you, or a witness who will give evidence on your behalf?",
            hint = "Check your previous answers in 'Directions questionnaire form' under 'Claim documents'",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo revisedHearingRequirements;
    @CCD(
            label = "What support do you, your clients, experts or witnesses need?",
            hint = "For Example, client requires a courtroom with step free access.",
            showCondition = "revisedHearingRequirements = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String revisedHearingComments;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Hearing Requirements", searchable = false, typeOverride = FieldType.Label)
  private String startLabel;
  @CCD(
          label = "You are reminded that this information will be shared with all other parties.",
          showCondition = "revisedHearingRequirements = \"Yes\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String endLabel;
  @CCD(label = "****", searchable = false, typeOverride = FieldType.Label)
  private String endLine;
  // ==== end synthesised definition-only fields ====
}
