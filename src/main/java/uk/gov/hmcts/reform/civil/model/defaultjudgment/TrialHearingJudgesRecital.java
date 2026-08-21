package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "TrialHearingJudgesRecitalDJ", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TrialHearingJudgesRecital {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String input;

    @CCD(label = " ", searchable = false)
    private String judgeNameTitle;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String label;
  // ==== end synthesised definition-only fields ====
}
