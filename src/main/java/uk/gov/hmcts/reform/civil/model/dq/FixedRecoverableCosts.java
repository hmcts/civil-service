package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.ComplexityBand;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
public class FixedRecoverableCosts {

    private YesOrNo isSubjectToFixedRecoverableCostRegime;
    private ComplexityBand band;
    private YesOrNo complexityBandingAgreed;
    private String reasons;
}
