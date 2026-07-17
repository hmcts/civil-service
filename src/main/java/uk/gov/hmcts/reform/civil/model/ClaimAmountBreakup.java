package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import net.minidev.json.annotate.JsonIgnore;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ClaimAmountBreakup {

    @CCD(ignore = true)
    private ClaimAmountBreakupDetails value;
    @JsonIgnore
    private String id;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "What you are claiming for",
          hint = "Briefly explain each item, for example: broken tiles, roof damage.",
          searchable = false
  )
  private String claimReason;
  @CCD(label = "Amount", searchable = false, typeOverride = FieldType.MoneyGBP)
  private String claimAmount;
  // ==== end synthesised definition-only fields ====
}