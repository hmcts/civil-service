package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FixedCosts {

    private YesOrNo claimFixedCosts;
    private String fixedCostAmount;
}
