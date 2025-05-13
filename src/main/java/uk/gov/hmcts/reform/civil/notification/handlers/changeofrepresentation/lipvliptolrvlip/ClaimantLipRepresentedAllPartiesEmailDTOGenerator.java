package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolrvlip;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

@Component
public class ClaimantLipRepresentedAllPartiesEmailDTOGenerator extends AllPartiesEmailGenerator {

    public ClaimantLipRepresentedAllPartiesEmailDTOGenerator(NewApplicantSolEmailDTOGenerator applicantSolEmailDTOGenerator,
                                                             ClaimantLipEmailDTOGenerator claimantLipEmailDTOGenerator,
                                                             DefendantLipEmailDTOGenerator defendantLipEmailDTOGenerator) {
        super(applicantSolEmailDTOGenerator,
              null,
              null,
              claimantLipEmailDTOGenerator,
              defendantLipEmailDTOGenerator);
    }
}
