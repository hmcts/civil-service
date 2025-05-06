package uk.gov.hmcts.reform.civil.notification.handlers;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

public abstract class DefendantEmailDTOGenerator extends EmailDTOGenerator {

    public DefendantEmailDTOGenerator(NotificationsSignatureConfiguration configuration,
                                      FeatureToggleService featureToggleService) {
        super(configuration, featureToggleService);
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
