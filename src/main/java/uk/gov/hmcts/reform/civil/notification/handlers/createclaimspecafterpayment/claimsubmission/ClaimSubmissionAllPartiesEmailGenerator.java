package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment.claimsubmission;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimSubmissionAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimSubmissionAllPartiesEmailGenerator(
            ClaimSubmissionClaimantEmailDTOGenerator claimantGenerator
    ) {
        super(List.of(claimantGenerator));
    }
}
