package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolipvlrorlrvliptolrvlr;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

@Component
public class DefRepresentedAllPartiesEmailDTOGenerator extends AllPartiesEmailGenerator {

    public DefRepresentedAllPartiesEmailDTOGenerator(DefRepresentedApplicantSolEmailDTOGenerator applicantSolEmailDTOGenerator,
                                                     DefRepresentedNewRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
                                                     DefRepresentedClaimantLipEmailDTOGenerator claimantLipEmailDTOGenerator,
                                                     DefRepresentedDefendantLipEmailDTOGenerator defendantLipEmailDTOGenerator) {
        super(applicantSolEmailDTOGenerator,
              respSolOneEmailDTOGenerator,
              null,
              claimantLipEmailDTOGenerator,
              defendantLipEmailDTOGenerator);
    }
}
