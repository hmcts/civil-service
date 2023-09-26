package uk.gov.hmcts.reform.civil.model.finalorders;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

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
}
