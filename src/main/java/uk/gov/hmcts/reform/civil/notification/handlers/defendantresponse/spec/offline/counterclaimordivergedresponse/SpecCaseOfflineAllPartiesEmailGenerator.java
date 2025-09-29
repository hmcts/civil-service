package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.offline.counterclaimordivergedresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class SpecCaseOfflineAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public SpecCaseOfflineAllPartiesEmailGenerator(
        SpecCaseOfflineAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
        SpecCaseOfflineRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        SpecCaseOfflineRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator,
        SpecCaseOfflineClaimantEmailDTOGenerator claimantEmailDTOGenerator
    ) {
        super(List.of(appSolOneEmailDTOGenerator,
                      respSolOneEmailDTOGenerator,
                      respSolTwoEmailDTOGenerator,
                      claimantEmailDTOGenerator));
    }
}
