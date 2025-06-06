package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lipvliptolrvlipandlipvlrtolrvlr;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimantLipRepresentedAllPartiesEmailDTOGenerator extends AllPartiesEmailGenerator {

    public ClaimantLipRepresentedAllPartiesEmailDTOGenerator(NewApplicantSolEmailDTOGenerator applicantSolEmailDTOGenerator,
                                                             ClaimantLipEmailDTOGenerator claimantLipEmailDTOGenerator,
                                                             DefendantLipEmailDTOGenerator defendantLipEmailDTOGenerator,
                                                             ClaimantLipRepresentedRespSolOneEmailGenerator respSolOneEmailGenerator) {
        super(List.of(applicantSolEmailDTOGenerator,
              claimantLipEmailDTOGenerator,
              defendantLipEmailDTOGenerator,
                      respSolOneEmailGenerator));
    }
}
