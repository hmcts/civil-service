package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2FastTrackAltDisputeResolution {

    @CCD(label = " ", searchable = false)
    private List<IncludeInOrderToggle> includeInOrderToggle;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "At all stages, the parties must consider settling this litigation by any means of Alternative Dispute Resolution. This includes round table conferences, early neutral evaluation, mediation and arbitration. Any party not engaging in any such means proposed by another must upload to the Digital Portal a witness statement giving reasons within 21 days of receipt of that proposal. That witness statement must not be shown to the trial judge until questions of costs arise.",
          showCondition = "includeInOrderToggle = \"INCLUDE\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label;
  // ==== end synthesised definition-only fields ====
}
