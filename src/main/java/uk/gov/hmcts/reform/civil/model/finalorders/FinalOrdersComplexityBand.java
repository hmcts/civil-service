package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalOrdersComplexityBand {

    private YesOrNo assignComplexityBand;
    private ComplexityBand band;
    private String reasons;
}
