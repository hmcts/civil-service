package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.online.onlyonerespondentrespond;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common.SpecDefRespAppSolOneEmailDTOGenerator;

import java.util.List;

@Component
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
