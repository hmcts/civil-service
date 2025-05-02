package uk.gov.hmcts.reform.civil.notification.handlers.defresponse.spec.offline.counterclaimordivergedresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

@Component
public class SpecCaseOfflineAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public SpecCaseOfflineAllPartiesEmailGenerator(
        SpecCaseOfflineAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        SpecCaseOfflineRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        SpecCaseOfflineRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator,
        SpecCaseOfflineClaimantEmailDTOGenerator claimantEmailDTOGenerator
    ) {
        super(appSolOneEmailDTOGenerator,
              respSolOneEmailDTOGenerator,
              respSolTwoEmailDTOGenerator,
              claimantEmailDTOGenerator,
              null);
    }
}
