package uk.gov.hmcts.reform.civil.model.defaultjudgment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import jakarta.validation.constraints.Future;
import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrialPPI {

    @CCD(label = "The Defendant(s) shall by", searchable = false)
    @Future(message = "The date entered must be in the future")
    private LocalDate ppiDate;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String text;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "#### Defendant(s) shall send to the claimant(s)", searchable = false, typeOverride = FieldType.Label)
  private String title;
  // ==== end synthesised definition-only fields ====
}
