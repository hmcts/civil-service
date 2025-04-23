
package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@AllArgsConstructor
public abstract class ClaimantEmailDTOGenerator extends EmailDTOGenerator {

    @Override
    public String getEmailAddress(CaseData caseData) {
        return caseData.getApplicant1Email();
    }

    protected Boolean getShouldNotify(CaseData caseData) {
        return caseData.isApplicantLiP() ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(PARTY_NAME, caseData.getApplicant1().getPartyName());
        properties.put(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
        return properties;
    }
}
