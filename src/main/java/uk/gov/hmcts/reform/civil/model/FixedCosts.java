package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FixedCosts {

    @CCD(
            label = "Do you want to claim for fixed commencement costs?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo claimFixedCosts;
    @CCD(
            label = "Amount",
            showCondition = "claimFixedCosts = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.MoneyGBP
    )
    private String fixedCostAmount;
}
