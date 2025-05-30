package uk.gov.hmcts.reform.civil.notification.handlers.claimcontinuingonlinespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimContinuingOnlineSpecAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimContinuingOnlineSpecAllPartiesEmailGenerator(
            ClaimContinuingOnlineSpecAppSolOneEmailDTOGenerator appSolOneEmailDTOGenerator,
            ClaimContinuingOnlineSpecRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
            ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator,
            ClaimContinuingOnlineSpecClaimantEmailDTOGenerator claimantEmailDTOGenerator,
            ClaimContinuingOnlineSpecDefendantEmailDTOGenerator defendantEmailDTOGenerator
    ) {
        super(List.of(appSolOneEmailDTOGenerator, respSolOneEmailDTOGenerator, respSolTwoEmailDTOGenerator,
                      claimantEmailDTOGenerator, defendantEmailDTOGenerator
        ));
    }
}
