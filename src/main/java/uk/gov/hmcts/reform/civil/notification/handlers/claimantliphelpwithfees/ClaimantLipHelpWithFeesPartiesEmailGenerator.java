package uk.gov.hmcts.reform.civil.notification.handlers.claimantliphelpwithfees;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

@Component
public class ClaimantLipHelpWithFeesPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimantLipHelpWithFeesPartiesEmailGenerator(
            ClaimantLipHelpWithFeesEmailDTOGenerator claimantEmailDTOGenerator
    ) {
        super(null, null, null, claimantEmailDTOGenerator, null);
    }

    @Override
    protected boolean shouldNotifyRespondents(CaseData caseData) {
        return false;
    }
}