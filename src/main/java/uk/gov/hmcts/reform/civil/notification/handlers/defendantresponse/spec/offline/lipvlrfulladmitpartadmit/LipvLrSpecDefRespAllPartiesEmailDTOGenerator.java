package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.offline.lipvlrfulladmitpartadmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common.SpecDefRespClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common.SpecDefRespRespSolOneEmailDTOGenerator;

@Component
public class LipvLrSpecDefRespAllPartiesEmailDTOGenerator extends AllPartiesEmailGenerator {

    public LipvLrSpecDefRespAllPartiesEmailDTOGenerator(
        SpecDefRespClaimantEmailDTOGenerator claimantEmailDTOGenerator,
        SpecDefRespRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator
    ) {
        super(null,
              respSolOneEmailDTOGenerator,
              null,
              claimantEmailDTOGenerator,
              null);
    }
}
