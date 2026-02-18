package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Component
public class RespondToQueryClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "response-to-query-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final RespondToQueryDateHelper respondToQueryDateHelper;

    public RespondToQueryClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                   RespondToQueryDateHelper respondToQueryDateHelper) {
        this.notificationsProperties = notificationsProperties;
        this.respondToQueryDateHelper = respondToQueryDateHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
            ? notificationsProperties.getQueryLipWelshPublicResponseReceived()
            : notificationsProperties.getQueryLipPublicResponseReceived();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        String partyName = caseData.getApplicant1().getPartyName();
        properties.put(PARTY_NAME, partyName);
        properties.put(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
        respondToQueryDateHelper.addQueryDateProperty(properties, caseData);
        return properties;
    }
}
