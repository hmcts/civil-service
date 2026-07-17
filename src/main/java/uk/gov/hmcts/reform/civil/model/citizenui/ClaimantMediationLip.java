package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ClaimantMediationLip {

    @CCD(label = "Mediation Decision", searchable = false)
    private MediationDecision hasAgreedFreeMediation;
    @CCD(
            label = " ",
            showCondition = "hasAgreedFreeMediation = \"DO_NOT_SHOW_IN_UI\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo canWeUseMediationLiP;
    @CCD(label = " ", showCondition = "hasAgreedFreeMediation = \"DO_NOT_SHOW_IN_UI\"", searchable = false)
    private String canWeUseMediationPhoneLiP;
    @CCD(
            label = " ",
            showCondition = "hasAgreedFreeMediation = \"DO_NOT_SHOW_IN_UI\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo mediationDisagreementLiP;
    @CCD(label = " ", showCondition = "hasAgreedFreeMediation = \"DO_NOT_SHOW_IN_UI\"", searchable = false)
    private String noMediationReasonLiP;
    @CCD(label = " ", showCondition = "hasAgreedFreeMediation = \"DO_NOT_SHOW_IN_UI\"", searchable = false)
    private String noMediationOtherReasonLiP;
    @CCD(
            label = " ",
            showCondition = "hasAgreedFreeMediation = \"DO_NOT_SHOW_IN_UI\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo companyTelephoneOptionMediationLiP;
    @CCD(label = " ", showCondition = "hasAgreedFreeMediation = \"DO_NOT_SHOW_IN_UI\"", searchable = false)
    private String companyTelephoneConfirmationMediationLiP;
    @CCD(label = " ", showCondition = "hasAgreedFreeMediation = \"DO_NOT_SHOW_IN_UI\"", searchable = false)
    private String companyTelephoneContactPersonMediationLiP;
    @CCD(label = " ", showCondition = "hasAgreedFreeMediation = \"DO_NOT_SHOW_IN_UI\"", searchable = false)
    private String companyTelephonePhoneNumberMediationLiP;

    @JsonIgnore
    public boolean hasClaimantAgreedToFreeMediation() {
        return MediationDecision.Yes.equals(hasAgreedFreeMediation);
    }

    @JsonIgnore
    public boolean hasClaimantNotAgreedToFreeMediation() {
        return MediationDecision.No.equals(hasAgreedFreeMediation);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "<p>${applicant1.partyName} has automatically been registered to take part in free mediation from HM Courts and Tribunals Service.</p> \n <p>Using a mediator can help settle a dispute without going to court which can be quicker, cheaper and less stressful for all parties.</p>",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String mediationSummary;
  @CCD(
          label = "<p>Find out more about <a href=\"https://www.gov.uk/guidance/small-claims-mediation-service\" rel=\"noreferrer noopener\" target=\"_blank\">free mediation (opens in a new tab)</a>.</p>",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String claimMediationLabel;
  // ==== end synthesised definition-only fields ====
}
