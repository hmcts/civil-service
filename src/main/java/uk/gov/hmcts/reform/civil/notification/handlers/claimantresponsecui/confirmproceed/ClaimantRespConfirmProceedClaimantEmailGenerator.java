package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsecui.confirmproceed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailGenerator;

@Component
public class ClaimantRespConfirmProceedClaimantEmailGenerator extends ClaimantEmailGenerator {
    public ClaimantRespConfirmProceedClaimantEmailGenerator(ClaimantRespConfirmProceedClaimantEmailDTOGenerator claimantEmailDTOGenerator) {
        super(claimantEmailDTOGenerator);
    }
}
