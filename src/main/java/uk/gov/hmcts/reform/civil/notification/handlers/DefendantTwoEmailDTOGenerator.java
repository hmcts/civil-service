package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@AllArgsConstructor
public abstract class DefendantTwoEmailDTOGenerator extends EmailDTOGenerator {

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return caseData.isRespondent2LiP() ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getRespondent2PartyEmail();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(PARTY_NAME, caseData.getRespondent2().getPartyName());
        properties.put(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
        return properties;
    }
}
