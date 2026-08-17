package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2ScheduleOfLoss {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoR2ScheduleOfLossClaimantText;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoR2ScheduleOfLossClaimantDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String sdoR2ScheduleOfLossDefendantText;
    @CCD(label = " ", searchable = false)
    private LocalDate sdoR2ScheduleOfLossDefendantDate;
    @CCD(label = "Claim for future pecuniary loss", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isClaimForPecuniaryLoss;
    @CCD(
            label = " ",
            showCondition = "isClaimForPecuniaryLoss = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String sdoR2ScheduleOfLossPecuniaryLossTxt;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Claimant", searchable = false, typeOverride = FieldType.Label)
  private String sdoR2ScheduleOfLossClaimantLabel;
  @CCD(label = "### Defendant(s)", searchable = false, typeOverride = FieldType.Label)
  private String sdoR2ScheduleOfLossDefendantLabel;
  // ==== end synthesised definition-only fields ====
}
