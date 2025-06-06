package uk.gov.hmcts.reform.civil.notification.handlers.createlipclaim;

import java.util.List;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

@Component
public class ApplicantClaimSubmittedAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ApplicantClaimSubmittedAllPartiesEmailGenerator(
            ApplicantClaimSubmittedClaimantEmailDTOGenerator applicantGenerator
    ) {
        super(List.of(applicantGenerator));
    }
}
