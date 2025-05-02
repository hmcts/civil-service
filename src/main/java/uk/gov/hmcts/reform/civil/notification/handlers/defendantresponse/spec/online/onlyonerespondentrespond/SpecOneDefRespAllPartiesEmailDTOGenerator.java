package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.onlyonerespondentrespond;

import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.common.SpecDefRespAppSolOneEmailDTOGenerator;

public class SpecOneDefRespAllPartiesEmailDTOGenerator extends AllPartiesEmailGenerator {

    public SpecOneDefRespAllPartiesEmailDTOGenerator(
        SpecDefRespAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        SpecOneDefRespRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        SpecOneDefRespRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator
    ) {
        super(appSolOneEmailDTOGenerator,
              respSolOneEmailDTOGenerator,
              respSolTwoEmailDTOGenerator,
              null,
              null);
    }
}
