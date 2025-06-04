package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimContinuingOnlineSpecAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimContinuingOnlineSpecAllPartiesEmailGenerator(
            ClaimContinuingOnlineSpecAppSolOneEmailDTOGenerator appGen,
            ClaimContinuingOnlineSpecRespSolOneEmailDTOGenerator respOneGen,
            ClaimContinuingOnlineSpecRespSolTwoEmailDTOGenerator respTwoGen,
            ClaimContinuingOnlineSpecClaimantEmailDTOGenerator claimantGen,
            ClaimContinuingOnlineSpecDefendantEmailDTOGenerator defendantGen
    ) {
        super(List.of(appGen, respOneGen, respTwoGen, claimantGen, defendantGen
        ));
    }
}
