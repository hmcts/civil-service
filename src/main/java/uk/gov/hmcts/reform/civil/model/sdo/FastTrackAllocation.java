package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
public class FastTrackAllocation {

    private YesOrNo assignComplexityBand;
    private ComplexityBand band;
    private String reasons;
}
