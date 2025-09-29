package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.fulldefencefulladmitpartadmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common.SpecDefRespAppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common.SpecDefRespRespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common.SpecDefRespRespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common.SpecDefRespClaimantEmailDTOGenerator;

import java.util.List;

@Component
public class LrvLrLrSpecDefRespAllPartiesEmailDTOGenerator extends AllPartiesEmailGenerator {

    public LrvLrLrSpecDefRespAllPartiesEmailDTOGenerator(
        SpecDefRespAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        SpecDefRespRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        SpecDefRespRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator,
        SpecDefRespClaimantEmailDTOGenerator claimantEmailDTOGenerator
    ) {
        super(
            List.of(appSolOneEmailDTOGenerator,
            respSolOneEmailDTOGenerator,
            respSolTwoEmailDTOGenerator,
            claimantEmailDTOGenerator));
    }
}
