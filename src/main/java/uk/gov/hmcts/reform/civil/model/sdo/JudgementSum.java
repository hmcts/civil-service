package uk.gov.hmcts.reform.civil.model.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
public class JudgementSum {

    @CCD(
            label = "Judgment for the claimant for a sum to be decided by the court",
            hint = "Subject to a deduction of the percentage below",
            searchable = false,
            min = 0,
            max = 100
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Double judgementSum;

    @JsonCreator
    public JudgementSum(@JsonProperty("judgementSum") Double judgementSum) {
        this.judgementSum = judgementSum;
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String label;
  // ==== end synthesised definition-only fields ====
}
