package uk.gov.hmcts.reform.civil.notification.handlers;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

public abstract class DefendantEmailDTOGenerator extends EmailDTOGenerator {

    protected DefendantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        super(notificationsProperties);
    }

    @Override
    public String getEmailAddress(CaseData caseData) {
        return caseData.getRespondent1PartyEmail();
    }

    protected Boolean getShouldNotify(CaseData caseData) {
        return caseData.isRespondent1LiP() ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(PARTY_NAME, caseData.getRespondent1().getPartyName());
        properties.put(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
        return properties;
    }
}
