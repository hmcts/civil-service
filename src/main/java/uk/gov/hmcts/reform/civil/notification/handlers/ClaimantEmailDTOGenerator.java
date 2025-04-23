
package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;

@AllArgsConstructor
public abstract class ClaimantEmailDTOGenerator extends EmailDTOGenerator {

    @Override
    public String getEmailAddress(CaseData caseData) {
        return caseData.getApplicant1Email();
    }
}
