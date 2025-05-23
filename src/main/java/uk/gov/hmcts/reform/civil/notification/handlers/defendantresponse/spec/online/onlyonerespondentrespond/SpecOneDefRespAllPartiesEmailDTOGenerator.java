package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.onlyonerespondentrespond;

import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common.SpecDefRespAppSolOneEmailDTOGenerator;

import java.util.List;

public class SpecOneDefRespAllPartiesEmailDTOGenerator extends AllPartiesEmailGenerator {

    public SpecOneDefRespAllPartiesEmailDTOGenerator(
        SpecDefRespAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        SpecOneDefRespRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        SpecOneDefRespRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator
    ) {
        super(
            List.of(appSolOneEmailDTOGenerator,
            respSolOneEmailDTOGenerator,
            respSolTwoEmailDTOGenerator));
    }
}
