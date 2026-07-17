package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersClaimantRepresentationList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersDefendantRepresentationList;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import com.fasterxml.jackson.annotation.JsonProperty;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimantAndDefendantHeard {

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FinalOrdersClaimantRepresentationList"
    )
    private FinalOrdersClaimantRepresentationList typeRepresentationClaimantList;
    @CCD(
            label = " ",
            showCondition = "typeRepresentationClaimantTwoDynamic=\"*\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FinalOrdersClaimantRepresentationList"
    )
    private FinalOrdersClaimantRepresentationList typeRepresentationClaimantListTwo;
    @CCD(label = " ", showCondition = "typeRepresentationClaimantList=\"CLAIMANT_NOT_ATTENDING\"", searchable = false)
    private TrialNoticeProcedure trialProcedureClaimantComplex;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FinalOrdersDefendantRepresentationList"
    )
    private FinalOrdersDefendantRepresentationList typeRepresentationDefendantList;
    @CCD(
            label = " ",
            showCondition = "typeRepresentationDefendantTwoDynamic=\"*\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FinalOrdersDefendantRepresentationList"
    )
    private FinalOrdersDefendantRepresentationList typeRepresentationDefendantTwoList;
    @CCD(label = " ", showCondition = "typeRepresentationDefendantList=\"DEFENDANT_NOT_ATTENDING\"", searchable = false)
    private TrialNoticeProcedure trialProcedureComplex;
    @CCD(
            label = " ",
            showCondition = "typeRepresentationDefendantTwoList=\"DEFENDANT_NOT_ATTENDING\"",
            searchable = false
    )
    private TrialNoticeProcedure trialProcedureDefTwoComplex;
    @CCD(
            label = " ",
            showCondition = "typeRepresentationClaimantListTwo=\"CLAIMANT_NOT_ATTENDING\"",
            searchable = false
    )
    private TrialNoticeProcedure trialProcedClaimTwoComplex;
    @CCD(
            label = " ",
            hint = "For example, the judge heard from Mr Smith, a director of the claimant company and Miss Smith as lay representative for the defendant",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String detailsRepresentationText;
    @CCD(label = " ", showCondition = "typeRepresentationClaimantList=\"DO_NOT_SHOW\"", searchable = false)
    private String typeRepresentationClaimantOneDynamic;
    @CCD(label = " ", showCondition = "typeRepresentationClaimantList=\"DO_NOT_SHOW\"", searchable = false)
    private String typeRepresentationClaimantTwoDynamic;
    @CCD(label = " ", showCondition = "typeRepresentationClaimantList=\"DO_NOT_SHOW\"", searchable = false)
    private String typeRepresentationDefendantOneDynamic;
    @CCD(label = " ", showCondition = "typeRepresentationClaimantList=\"DO_NOT_SHOW\"", searchable = false)
    private String typeRepresentationDefendantTwoDynamic;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "### Claimant: ${typeRepresentationClaimantOneDynamic}",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationClaimantListLabel;
  @CCD(
          label = "Counsel for ${typeRepresentationClaimantOneDynamic}, the claimant",
          showCondition = "typeRepresentationClaimantList=\"COUNSEL_FOR_CLAIMANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationCounselForClaimantList;
  @CCD(
          label = "Solicitor for ${typeRepresentationClaimantOneDynamic}, the claimant",
          showCondition = "typeRepresentationClaimantList=\"SOLICITOR_FOR_CLAIMANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationSolicitorForClaimantList;
  @CCD(
          label = "Costs draftsman for ${typeRepresentationClaimantOneDynamic}, the claimant",
          showCondition = "typeRepresentationClaimantList=\"COST_DRAFTSMAN_FOR_THE_CLAIMANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationCostDraftsmanList;
  @CCD(
          label = "${typeRepresentationClaimantOneDynamic}, the claimant, in person",
          showCondition = "typeRepresentationClaimantList=\"THE_CLAIMANT_IN_PERSON\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationTheClaimantInPersonList;
  @CCD(
          label = "Lay representative for ${typeRepresentationClaimantOneDynamic}, the claimant",
          showCondition = "typeRepresentationClaimantList=\"LAY_REPRESENTATIVE_FOR_THE_CLAIMANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationLayRepresentativeList;
  @CCD(
          label = "Legal executive for ${typeRepresentationClaimantOneDynamic}, the claimant",
          showCondition = "typeRepresentationClaimantList=\"LEGAL_EXECUTIVE_FOR_THE_CLAIMANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationLegalExecutiveList;
  @CCD(
          label = "Solicitor's Agent for ${typeRepresentationClaimantOneDynamic}, the claimant",
          showCondition = "typeRepresentationClaimantList=\"SOLICITORS_AGENT_FOR_THE_CLAIMANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationSolicitorsAgentList;
  @CCD(
          label = "### Claimant: ${typeRepresentationClaimantTwoDynamic}",
          showCondition = "typeRepresentationClaimantTwoDynamic=\"*\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationClaimantListTwoLabel;
  @JsonProperty("CounselForClaimantListTwo")
  @CCD(
          label = "Counsel for ${typeRepresentationClaimantTwoDynamic}, the claimant",
          showCondition = "typeRepresentationClaimantListTwo=\"COUNSEL_FOR_CLAIMANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String counselForClaimantListTwo;
  @JsonProperty("SolicitorForClaimantListTwo")
  @CCD(
          label = "Solicitor for ${typeRepresentationClaimantTwoDynamic}, the claimant",
          showCondition = "typeRepresentationClaimantListTwo=\"SOLICITOR_FOR_CLAIMANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String solicitorForClaimantListTwo;
  @CCD(
          label = "Costs draftsman for ${typeRepresentationClaimantTwoDynamic}, the claimant",
          showCondition = "typeRepresentationClaimantListTwo=\"COST_DRAFTSMAN_FOR_THE_CLAIMANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationCostDraftsmanListTwo;
  @CCD(
          label = "${typeRepresentationClaimantTwoDynamic}, he claimant, in person",
          showCondition = "typeRepresentationClaimantListTwo=\"THE_CLAIMANT_IN_PERSON\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationTheClaimantInPersonListTwo;
  @CCD(
          label = "Lay representative for ${typeRepresentationClaimantTwoDynamic}, the claimant",
          showCondition = "typeRepresentationClaimantListTwo=\"LAY_REPRESENTATIVE_FOR_THE_CLAIMANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationLayRepresentativeListTwo;
  @CCD(
          label = "Legal executive for ${typeRepresentationClaimantTwoDynamic}, the claimant",
          showCondition = "typeRepresentationClaimantListTwo=\"LEGAL_EXECUTIVE_FOR_THE_CLAIMANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationLegalExecutiveListTwo;
  @CCD(
          label = "Solicitor's Agent for ${typeRepresentationClaimantTwoDynamic}, the claimant",
          showCondition = "typeRepresentationClaimantListTwo=\"SOLICITORS_AGENT_FOR_THE_CLAIMANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationSolicitorsAgentListTwo;
  @CCD(
          label = "### Defendant: ${typeRepresentationDefendantOneDynamic}",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationDefendantListLabel;
  @CCD(
          label = "Counsel for ${typeRepresentationDefendantOneDynamic}, the defendant",
          showCondition = "typeRepresentationDefendantList=\"COUNSEL_FOR_DEFENDANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationCounselForDefendantList;
  @CCD(
          label = "Solicitor for ${typeRepresentationDefendantOneDynamic}, the defendant",
          showCondition = "typeRepresentationDefendantList=\"SOLICITOR_FOR_DEFENDANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationSolicitorForDefendantList;
  @CCD(
          label = "Costs draftsman for ${typeRepresentationDefendantOneDynamic}, the defendant",
          showCondition = "typeRepresentationDefendantList=\"COST_DRAFTSMAN_FOR_THE_DEFENDANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationCostDraftsmanDefList;
  @CCD(
          label = "${typeRepresentationDefendantOneDynamic}, the defendant, in person",
          showCondition = "typeRepresentationDefendantList=\"THE_DEFENDANT_IN_PERSON\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationTheDefendantInPersonList;
  @CCD(
          label = "Lay representative for ${typeRepresentationDefendantOneDynamic}, the defendant",
          showCondition = "typeRepresentationDefendantList=\"LAY_REPRESENTATIVE_FOR_THE_DEFENDANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationLayRepresentativeDefList;
  @CCD(
          label = "Legal executive for ${typeRepresentationDefendantOneDynamic}, the defendant",
          showCondition = "typeRepresentationDefendantList=\"LEGAL_EXECUTIVE_FOR_THE_DEFENDANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationLegalExecutiveDefList;
  @CCD(
          label = "Solicitor's Agent for ${typeRepresentationDefendantOneDynamic}, the defendant",
          showCondition = "typeRepresentationDefendantList=\"SOLICITORS_AGENT_FOR_THE_DEFENDANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationSolicitorsAgentDefList;
  @CCD(
          label = "### Defendant: ${typeRepresentationDefendantTwoDynamic}",
          showCondition = "typeRepresentationDefendantTwoDynamic=\"*\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String typeRepresentationDefendantTwoListLabel;
  @JsonProperty("CounselForDefendantTwoList")
  @CCD(
          label = "Counsel for ${typeRepresentationDefendantTwoDynamic}, the defendant",
          showCondition = "typeRepresentationDefendantTwoList=\"COUNSEL_FOR_DEFENDANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String counselForDefendantTwoList;
  @JsonProperty("SolicitorForDefendantTwoList")
  @CCD(
          label = "Solicitor for ${typeRepresentationDefendantTwoDynamic}, the defendant",
          showCondition = "typeRepresentationDefendantTwoList=\"SOLICITOR_FOR_DEFENDANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String solicitorForDefendantTwoList;
  @JsonProperty("CostDraftsmanDefTwoList")
  @CCD(
          label = "Costs draftsman for ${typeRepresentationDefendantTwoDynamic}, the defendant",
          showCondition = "typeRepresentationDefendantTwoList=\"COST_DRAFTSMAN_FOR_THE_DEFENDANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String costDraftsmanDefTwoList;
  @JsonProperty("TheDefendantInPersonTwoList")
  @CCD(
          label = "${typeRepresentationDefendantTwoDynamic}, the defendant, in person",
          showCondition = "typeRepresentationDefendantTwoList=\"THE_DEFENDANT_IN_PERSON\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String theDefendantInPersonTwoList;
  @JsonProperty("LayRepresentativeDefTwoList")
  @CCD(
          label = "Lay representative for ${typeRepresentationDefendantTwoDynamic}, the defendant",
          showCondition = "typeRepresentationDefendantTwoList=\"LAY_REPRESENTATIVE_FOR_THE_DEFENDANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String layRepresentativeDefTwoList;
  @JsonProperty("LegalExecutiveDefTwoList")
  @CCD(
          label = "Legal executive for ${typeRepresentationDefendantTwoDynamic}, the defendant",
          showCondition = "typeRepresentationDefendantTwoList=\"LEGAL_EXECUTIVE_FOR_THE_DEFENDANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String legalExecutiveDefTwoList;
  @JsonProperty("SolicitorsAgentDefTwoList")
  @CCD(
          label = "Solicitor's Agent for ${typeRepresentationDefendantTwoDynamic}, the defendant",
          showCondition = "typeRepresentationDefendantTwoList=\"SOLICITORS_AGENT_FOR_THE_DEFENDANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String solicitorsAgentDefTwoList;
  @CCD(label = "### Enter details of representation", searchable = false, typeOverride = FieldType.Label)
  private String detailsRepresentationLabel;
  // ==== end synthesised definition-only fields ====
}
