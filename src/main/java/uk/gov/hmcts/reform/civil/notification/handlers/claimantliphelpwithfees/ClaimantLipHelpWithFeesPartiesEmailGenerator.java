package uk.gov.hmcts.reform.civil.notification.handlers.claimantliphelpwithfees;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimantLipHelpWithFeesPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimantLipHelpWithFeesPartiesEmailGenerator(
            ClaimantLipHelpWithFeesEmailDTOGenerator claimantEmailDTOGenerator
    ) {
        super(List.of(claimantEmailDTOGenerator));
    }
}