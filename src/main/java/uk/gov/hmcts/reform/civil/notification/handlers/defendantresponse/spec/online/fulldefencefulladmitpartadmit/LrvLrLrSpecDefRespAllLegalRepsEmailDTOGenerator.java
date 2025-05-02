package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.fulldefencefulladmitpartadmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllLegalRepsEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.common.SpecDefRespAppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.common.SpecDefRespRespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.common.SpecDefRespRespSolTwoEmailDTOGenerator;

@Component
public class LrvLrLrSpecDefRespAllLegalRepsEmailDTOGenerator extends AllLegalRepsEmailGenerator {

    public LrvLrLrSpecDefRespAllLegalRepsEmailDTOGenerator(
        SpecDefRespAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        SpecDefRespRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        SpecDefRespRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator
    ) {
        super(appSolOneEmailDTOGenerator,
              respSolOneEmailDTOGenerator,
              respSolTwoEmailDTOGenerator);
    }
}
