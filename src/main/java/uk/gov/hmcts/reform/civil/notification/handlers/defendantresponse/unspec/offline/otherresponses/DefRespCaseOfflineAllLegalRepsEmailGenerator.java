package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.unspec.offline.otherresponses;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class DefRespCaseOfflineAllLegalRepsEmailGenerator extends AllPartiesEmailGenerator {

    public DefRespCaseOfflineAllLegalRepsEmailGenerator(
        DefRespCaseOfflineAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        DefRespCaseOfflineRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        DefRespCaseOfflineRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator
    ) {
        super(List.of(appSolOneEmailDTOGenerator,
                      respSolOneEmailDTOGenerator,
                      respSolTwoEmailDTOGenerator));
    }

}
