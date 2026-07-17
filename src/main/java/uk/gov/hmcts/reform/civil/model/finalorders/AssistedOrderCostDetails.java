package uk.gov.hmcts.reform.civil.model.finalorders;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums;

import java.math.BigDecimal;
import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssistedOrderCostDetails {

    @CCD(
            label = " ",
            hint = "For example, to whom/when the cost are reserved (eg 'to the trial judge')",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String detailsRepresentationText;
    @CCD(
            label = " ",
            hint = "For example, 16 4 2021",
            showCondition = "assistedOrderClaimantDefendantFirstDropdown = \"COSTS\"",
            searchable = false
    )
    private LocalDate assistedOrderCostsFirstDropdownDate;
    @CCD(
            label = " ",
            hint = "For example, 16 4 2021",
            showCondition = "assistedOrderAssessmentSecondDropdownList2 = \"YES\" AND assistedOrderClaimantDefendantFirstDropdown = \"SUBJECT_DETAILED_ASSESSMENT\"",
            searchable = false
    )
    private LocalDate assistedOrderAssessmentThirdDropdownDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("makeAnOrderForCostsQOCSYesOrNo")
    private YesOrNo makeAnOrderForCostsYesOrNo;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String besPokeCostDetailsText;
    @CCD(label = " ", searchable = false)
    private CostEnums makeAnOrderForCostsList;
    @CCD(
            label = " ",
            showCondition = "makeAnOrderForCostsList = \"CLAIMANT\" OR makeAnOrderForCostsList = \"DEFENDANT\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FinalOrderMakeAnOrderTopList"
    )
    private CostEnums assistedOrderClaimantDefendantFirstDropdown;
    @CCD(
            label = " ",
            hint = "For example, 145.00",
            showCondition = "assistedOrderClaimantDefendantFirstDropdown = \"COSTS\"",
            searchable = false,
            typeOverride = FieldType.MoneyGBP
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal assistedOrderCostsFirstDropdownAmount;
    @CCD(
            label = " ",
            hint = "For example, 145.00",
            showCondition = "assistedOrderAssessmentSecondDropdownList2 = \"YES\" AND assistedOrderClaimantDefendantFirstDropdown = \"SUBJECT_DETAILED_ASSESSMENT\"",
            searchable = false,
            typeOverride = FieldType.MoneyGBP
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal assistedOrderAssessmentThirdDropdownAmount;
    @CCD(
            label = " ",
            showCondition = "assistedOrderClaimantDefendantFirstDropdown = \"SUBJECT_DETAILED_ASSESSMENT\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FinalOrderMakeAnOrderClaimantSecondList"
    )
    private CostEnums assistedOrderAssessmentSecondDropdownList1;
    @CCD(
            label = " ",
            showCondition = "assistedOrderClaimantDefendantFirstDropdown = \"SUBJECT_DETAILED_ASSESSMENT\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "FinalOrderMakeAnOrderClaimantThirdList"
    )
    private CostEnums assistedOrderAssessmentSecondDropdownList2;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Costs reserved", searchable = false, typeOverride = FieldType.Label)
  private String detailsRepresentationLabel;
  @CCD(
          label = "### Please type in any additional detail to follow 'Costs reserved' (Optional)",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String detailsRepresentationSecondLabel;
  @CCD(
          label = "### Make an order for detailed costs/summary costs \n ### The",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String makeAnOrderForCostsLabel;
  @CCD(
          label = "### shall pay the defendant's",
          showCondition = "assistedOrderClaimantDefendantFirstDropdown = \"COSTS\" AND  makeAnOrderForCostsList = \"CLAIMANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String assistedOrderClaimantDefendantFirstDropdownLabelType1;
  @CCD(
          label = "### shall pay the defendant's costs",
          showCondition = "assistedOrderClaimantDefendantFirstDropdown = \"SUBJECT_DETAILED_ASSESSMENT\" AND makeAnOrderForCostsList = \"CLAIMANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String assistedOrderClaimantDefendantFirstDropdownLabelType2;
  @CCD(
          label = "### shall pay the claimant's",
          showCondition = "assistedOrderClaimantDefendantFirstDropdown = \"COSTS\" AND  makeAnOrderForCostsList = \"DEFENDANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String assistedOrderDefendantFirstDropdownLabelType1;
  @CCD(
          label = "### shall pay the claimant's costs",
          showCondition = "assistedOrderClaimantDefendantFirstDropdown = \"SUBJECT_DETAILED_ASSESSMENT\" AND makeAnOrderForCostsList = \"DEFENDANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String assistedOrderDefendantFirstDropdownLabelType2;
  @CCD(
          label = "### in the sum of £",
          showCondition = "assistedOrderClaimantDefendantFirstDropdown = \"COSTS\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String assistedOrderCostsFirstDropdownAmountLabel;
  @CCD(
          label = "### Such sum shall be made by 4pm on",
          showCondition = "assistedOrderClaimantDefendantFirstDropdown = \"COSTS\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String assistedOrderCostsFirstDropdownDateLabel;
  @CCD(
          label = " ### Interim payment required?",
          showCondition = "assistedOrderClaimantDefendantFirstDropdown = \"SUBJECT_DETAILED_ASSESSMENT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String assistedOrderAssessmentSecondDropdownListLabel;
  @CCD(
          label = "### The claimant shall pay £",
          showCondition = "assistedOrderAssessmentSecondDropdownList2 = \"YES\" AND assistedOrderClaimantDefendantFirstDropdown = \"SUBJECT_DETAILED_ASSESSMENT\" AND makeAnOrderForCostsList = \"CLAIMANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String assistedOrderClaimantThirdDropdownLabel;
  @CCD(
          label = "### The defendant shall pay £",
          showCondition = "assistedOrderAssessmentSecondDropdownList2 = \"YES\" AND assistedOrderClaimantDefendantFirstDropdown = \"SUBJECT_DETAILED_ASSESSMENT\" AND makeAnOrderForCostsList = \"DEFENDANT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String assistedOrderDefendantThirdDropdownLabel;
  @CCD(
          label = "on account of costs \n ### Such sum shall be made by 4pm on",
          showCondition = "assistedOrderAssessmentSecondDropdownList2 = \"YES\" AND assistedOrderClaimantDefendantFirstDropdown = \"SUBJECT_DETAILED_ASSESSMENT\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String assistedOrderAssessmentThirdDropdownDateLabel;
  @CCD(label = "### Does the paying party have QOCS protection?", searchable = false, typeOverride = FieldType.Label)
  private String makeAnOrderForCostsQOCSLabel;
  @CCD(
          label = "Other than by way of permitted set off, there shall be no enforcement of any costs assessed under this order without permission of the court",
          showCondition = "makeAnOrderForCostsQOCSYesOrNo = \"Yes\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String makeAnOrderForCostsQOCSText;
  @CCD(label = "### Bespoke costs order", searchable = false, typeOverride = FieldType.Label)
  private String besPokeCostDetailsLabel;
  @CCD(label = "### Please insert cost order you wish to make", searchable = false, typeOverride = FieldType.Label)
  private String besPokeCostDetailsSecondLabel;
  // ==== end synthesised definition-only fields ====
}
