package uk.gov.hmcts.reform.civil.model.finalorders;

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
    private String claimantCostStandardText;
    private LocalDate claimantCostStandardDate;
    private YesOrNo claimantCostStandardProtectionOption;
    private String defendantCostStandardText;
    private LocalDate defendantCostStandardDate;
    private YesOrNo defendantCostStandardProtectionOption;
    private String claimantCostSummarilyText;
    private LocalDate claimantCostSummarilyDate;
    private YesOrNo claimantCostSummarilyProtectionOption;
    private String defendantCostSummarilyText;
    private LocalDate defendantCostSummarilyDate;
    private YesOrNo defendantCostSummarilyProtectionOption;
    private String besPokeCostDetailsText;
}
