package uk.gov.hmcts.reform.civil.notification.handlers.claimcontinuingonlinespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimContinuingOnlineSpecEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimContinuingOnlineSpecEmailGenerator(
            ClaimContinuingOnlineSpecAppSolOneEmailDTOGenerator appGen,
            ClaimContinuingOnlineSpecRespSolOneEmailDTOGenerator respOneGen,
            ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator respTwoGen
    ) {
        super(List.of(appGen, respOneGen, respTwoGen
        ));
    }
}
