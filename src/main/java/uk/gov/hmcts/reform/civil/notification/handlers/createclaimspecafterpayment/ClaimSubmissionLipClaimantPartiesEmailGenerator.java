package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimSubmissionLipClaimantPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimSubmissionLipClaimantPartiesEmailGenerator(
            ClaimSubmissionLipClaimantEmailDTOGenerator claimantGenerator
    ) {
        super(List.of(claimantGenerator));
    }
}
