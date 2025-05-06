package uk.gov.hmcts.reform.civil.notification.handlers.claimcontinuingonlinespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

@Component
public class ClaimContinuingOnlineSpecEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimContinuingOnlineSpecEmailGenerator(
            ClaimContinuingOnlineSpecAppSolOneEmailDTOGenerator appGen,
            ClaimContinuingOnlineSpecRespSolOneEmailDTOGenerator respOneGen,
            ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator respTwoGen
    ) {
        super(appGen, respOneGen, respTwoGen, null, null);
    }
}
