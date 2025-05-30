package uk.gov.hmcts.reform.civil.notification.handlers.claimantliphelpwithfees;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimantLipHelpWithFeesAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimantLipHelpWithFeesAllPartiesEmailGenerator(
            ClaimantLipHelpWithFeesClaimantEmailDTOGenerator claimantEmailDTOGenerator
    ) {
        super(List.of(claimantEmailDTOGenerator));
    }
}