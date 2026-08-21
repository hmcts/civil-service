package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "SmallClaimMediation", generate = true)
@Data
public class SmallClaimMedicalLRspec {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo hasAgreedFreeMediation;

    @JsonCreator
    public SmallClaimMedicalLRspec(@JsonProperty("hasAgreedFreeMediation") YesOrNo hasAgreedFreeMediation) {
        this.hasAgreedFreeMediation = hasAgreedFreeMediation;
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "<p>Find out more about <a href=\"https://www.gov.uk/guidance/small-claims-mediation-service\" rel=\"noreferrer noopener\" target=\"_blank\">free mediation (opens in a new tab)</a>.</p>",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String claimMediationLabel;
  // ==== end synthesised definition-only fields ====
}
