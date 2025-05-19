package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Component
public class ClaimDismissedEmailValidator {

    public boolean isValidForEmail(CaseData caseData) {
        return isOneVTwoTwoLegalRep(caseData) && caseData.getClaimDismissedDate() != null;
    }
}
