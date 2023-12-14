package uk.gov.hmcts.reform.civil.model.finalorders;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.finalorders.CostEnums;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AssistedOrderCostDetails {

    private String detailsRepresentationText;
    private LocalDate assistedOrderCostsFirstDropdownDate;
    private LocalDate assistedOrderAssessmentThirdDropdownDate;
    @JsonProperty("makeAnOrderForCostsQOCSYesOrNo")
    private YesOrNo makeAnOrderForCostsYesOrNo;
    private String besPokeCostDetailsText;
    private CostEnums makeAnOrderForCostsList;
    private CostEnums assistedOrderClaimantDefendantFirstDropdown;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal assistedOrderCostsFirstDropdownAmount;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal assistedOrderAssessmentThirdDropdownAmount;
    private CostEnums assistedOrderAssessmentSecondDropdownList1;
    private CostEnums assistedOrderAssessmentSecondDropdownList2;

}
