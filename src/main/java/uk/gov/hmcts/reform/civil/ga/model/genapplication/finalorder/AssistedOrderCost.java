package uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.AssistedOrderCostDropdownList;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class AssistedOrderCost {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal costAmount;
    private LocalDate costPaymentDeadLine;
    private YesOrNo isPartyCostProtection;
    private LocalDate assistedOrderCostsFirstDropdownDate;
    private LocalDate assistedOrderAssessmentThirdDropdownDate;
    private YesOrNo makeAnOrderForCostsYesOrNo;
    private AssistedOrderCostDropdownList makeAnOrderForCostsList;
    private AssistedOrderCostDropdownList assistedOrderCostsMakeAnOrderTopList;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal assistedOrderCostsFirstDropdownAmount;
    private AssistedOrderCostDropdownList assistedOrderAssessmentSecondDropdownList1;
    private AssistedOrderCostDropdownList assistedOrderAssessmentSecondDropdownList2;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal assistedOrderAssessmentThirdDropdownAmount;

    @JsonCreator
    AssistedOrderCost(@JsonProperty("costAmount") BigDecimal costAmount,
                               @JsonProperty("costPaymentDeadLine") LocalDate costPaymentDeadLine,
                               @JsonProperty("isPartyCostProtection") YesOrNo isPartyCostProtection,
                      @JsonProperty("assistedOrderCostsFirstDropdownDate") LocalDate assistedOrderCostsFirstDropdownDate,
                      @JsonProperty("assistedOrderAssessmentThirdDropdownDate") LocalDate assistedOrderAssessmentThirdDropdownDate,
                      @JsonProperty("makeAnOrderForCostsQOCSYesOrNo") YesOrNo makeAnOrderForCostsYesOrNo,
                      @JsonProperty("makeAnOrderForCostsList")  AssistedOrderCostDropdownList makeAnOrderForCostsList,
                      @JsonProperty("assistedOrderClaimantDefendantFirstDropdown") AssistedOrderCostDropdownList assistedOrderCostsMakeAnOrderTopList,
                      @JsonProperty("assistedOrderCostsFirstDropdownAmount") BigDecimal assistedOrderCostsFirstDropdownAmount,
                      @JsonProperty("assistedOrderAssessmentSecondDropdownList1") AssistedOrderCostDropdownList assistedOrderAssessmentSecondDropdownList1,
                      @JsonProperty("assistedOrderAssessmentSecondDropdownList2") AssistedOrderCostDropdownList assistedOrderAssessmentSecondDropdownList2,
                      @JsonProperty("assistedOrderAssessmentThirdDropdownAmount")BigDecimal assistedOrderAssessmentThirdDropdownAmount
    ) {

        this.costAmount = costAmount;
        this.costPaymentDeadLine = costPaymentDeadLine;
        this.isPartyCostProtection = isPartyCostProtection;
        this.makeAnOrderForCostsYesOrNo = makeAnOrderForCostsYesOrNo;
        this.makeAnOrderForCostsList = makeAnOrderForCostsList;
        this.assistedOrderCostsFirstDropdownDate = assistedOrderCostsFirstDropdownDate;
        this.assistedOrderAssessmentThirdDropdownDate = assistedOrderAssessmentThirdDropdownDate;
        this.assistedOrderCostsMakeAnOrderTopList = assistedOrderCostsMakeAnOrderTopList;
        this.assistedOrderCostsFirstDropdownAmount = assistedOrderCostsFirstDropdownAmount;
        this.assistedOrderAssessmentSecondDropdownList1 = assistedOrderAssessmentSecondDropdownList1;
        this.assistedOrderAssessmentSecondDropdownList2 = assistedOrderAssessmentSecondDropdownList2;
        this.assistedOrderAssessmentThirdDropdownAmount = assistedOrderAssessmentThirdDropdownAmount;
    }

    private BigDecimal toPounds() {
        return MonetaryConversions.penniesToPounds(this.assistedOrderCostsFirstDropdownAmount);
    }

    public String formatCaseAmountToPounds() {
        return "£" + this.toPounds();
    }
}
