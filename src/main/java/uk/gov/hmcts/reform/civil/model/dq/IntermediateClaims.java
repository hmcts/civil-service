package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
public class IntermediateClaims {

    private YesOrNo isSubjectToFixedRecoverableCostRegime;
    private String band;
    private YesOrNo complexityBandingAgreed;
    private String reasons;
}
