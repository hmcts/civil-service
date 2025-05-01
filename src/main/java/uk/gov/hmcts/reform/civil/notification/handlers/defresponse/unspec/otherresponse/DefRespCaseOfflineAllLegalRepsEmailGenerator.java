package uk.gov.hmcts.reform.civil.notification.handlers.defresponse.unspec.otherresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllLegalRepsEmailGenerator;

@Component
public class DefRespCaseOfflineAllLegalRepsEmailGenerator extends AllLegalRepsEmailGenerator {

    public DefRespCaseOfflineAllLegalRepsEmailGenerator(
        DefRespCaseOfflineAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        DefRespCaseOfflineRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        DefRespCaseOfflineRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator
    ) {
        super(appSolOneEmailDTOGenerator,
            respSolOneEmailDTOGenerator,
            respSolTwoEmailDTOGenerator);
    }

}
