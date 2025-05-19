package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Component
@AllArgsConstructor
public class ClaimDismissedEmailHelper {

    private final ClaimDismissedEmailTemplater claimDismissedEmailTemplater;

    public boolean isValidForRespondentEmail(CaseData caseData) {
        return isOneVTwoTwoLegalRep(caseData) && caseData.getClaimDismissedDate() != null;
    }

    public String getTemplateId(CaseData caseData) {
        return claimDismissedEmailTemplater.getTemplateId(caseData);
    }

}
