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
public class SdoR2SmallClaimsPPI {

    @CCD(label = "The Defendant(s) shall by", searchable = false)
    private LocalDate ppiDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String text;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "#### Defendant(s) shall send to the claimant(s)", searchable = false, typeOverride = FieldType.Label)
  private String title;
  // ==== end synthesised definition-only fields ====
}
