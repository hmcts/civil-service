package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimContinuingOnlineSpecApplicantEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimContinuingOnlineSpecApplicantEmailGenerator(
            ClaimContinuingOnlineSpecApplicantPartyEmailDTOGenerator applicantGen
    ) {
        super(List.of(applicantGen));
    }
}
