package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantDefendantNotAttending;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TrialNoticeProcedure {

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FinalOrdersClaimantDefendantNotAttending"
    )
    private FinalOrdersClaimantDefendantNotAttending list;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FinalOrdersClaimantDefendantNotAttending"
    )
    private FinalOrdersClaimantDefendantNotAttending listClaimTwo;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FinalOrdersClaimantDefendantNotAttending"
    )
    private FinalOrdersClaimantDefendantNotAttending listDef;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FinalOrdersClaimantDefendantNotAttending"
    )
    private FinalOrdersClaimantDefendantNotAttending listDefTwo;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "${typeRepresentationClaimantOneDynamic}, the claimant, did not attend the hearing, but the Judge was satisfied that they had received notice of the trial and it was reasonable to proceed in their absence.",
          showCondition = "list=\"SATISFIED_REASONABLE_TO_PROCEED\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label1;
  @CCD(
          label = "${typeRepresentationClaimantOneDynamic}, the claimant, did not attend the hearing and, whilst the Judge was satisfied that they had received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.",
          showCondition = "list=\"SATISFIED_NOTICE_OF_TRIAL\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label2;
  @CCD(
          label = "${typeRepresentationClaimantOneDynamic}, the claimant, did not attend the hearing, The Judge was not satisfied that they had received notice of the hearing and determined that it was not reasonable to proceed in their absence.",
          showCondition = "list=\"NOT_SATISFIED_NOTICE_OF_TRIAL\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label3;
  @CCD(
          label = "${typeRepresentationClaimantTwoDynamic}, the claimant, did not attend the hearing, but the Judge was satisfied that they had received notice of the trial and it was reasonable to proceed in their absence.",
          showCondition = "listClaimTwo=\"SATISFIED_REASONABLE_TO_PROCEED\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label1ClaimTwo;
  @CCD(
          label = "${typeRepresentationClaimantTwoDynamic}, the claimant, did not attend the hearing and, whilst the Judge was satisfied that they had received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.",
          showCondition = "listClaimTwo=\"SATISFIED_NOTICE_OF_TRIAL\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label2ClaimTwo;
  @CCD(
          label = "${typeRepresentationClaimantTwoDynamic}, the claimant, did not attend the hearing, The Judge was not satisfied that they had received notice of the hearing and determined that it was not reasonable to proceed in their absence.",
          showCondition = "listClaimTwo=\"NOT_SATISFIED_NOTICE_OF_TRIAL\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label3ClaimTwo;
  @CCD(
          label = "${typeRepresentationDefendantOneDynamic}, the defendant, did not attend the hearing, but the Judge was satisfied that they had received notice of the trial, and it was reasonable to proceed in their absence.",
          showCondition = "listDef=\"SATISFIED_REASONABLE_TO_PROCEED\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label1Def;
  @CCD(
          label = "${typeRepresentationDefendantOneDynamic}, the defendant, did not attend the hearing and, whilst the Judge was satisfied that they had received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.",
          showCondition = "listDef=\"SATISFIED_NOTICE_OF_TRIAL\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label2Def;
  @CCD(
          label = "${typeRepresentationDefendantOneDynamic}, the defendant, did not attend the hearing. The Judge was not satisfied that they had received notice of the hearing and determined that it was not reasonable to proceed in their absence.",
          showCondition = "listDef=\"NOT_SATISFIED_NOTICE_OF_TRIAL\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label3Def;
  @CCD(
          label = "${typeRepresentationDefendantTwoDynamic}, the defendant, did not attend the hearing, but the Judge was satisfied that they had received notice of the trial, and it was reasonable to proceed in their absence.",
          showCondition = "listDefTwo=\"SATISFIED_REASONABLE_TO_PROCEED\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label1DefTwo;
  @CCD(
          label = "${typeRepresentationDefendantTwoDynamic}, the defendant, did not attend the hearing and, whilst the Judge was satisfied that they had received notice of the trial, the Judge was not satisfied that it was reasonable to proceed in their absence.",
          showCondition = "listDefTwo=\"SATISFIED_NOTICE_OF_TRIAL\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label2DefTwo;
  @CCD(
          label = "${typeRepresentationDefendantTwoDynamic}, the defendant, did not attend the hearing. The Judge was not satisfied that they had received notice of the hearing and determined that it was not reasonable to proceed in their absence.",
          showCondition = "listDefTwo=\"NOT_SATISFIED_NOTICE_OF_TRIAL\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String label3DefTwo;
  // ==== end synthesised definition-only fields ====
}
