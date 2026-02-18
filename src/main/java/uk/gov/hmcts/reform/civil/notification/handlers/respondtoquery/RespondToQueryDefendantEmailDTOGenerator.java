package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Component
public class RespondToQueryDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "response-to-query-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final RespondToQueryDateHelper respondToQueryDateHelper;

    public RespondToQueryDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                    RespondToQueryDateHelper respondToQueryDateHelper) {
        this.notificationsProperties = notificationsProperties;
        this.respondToQueryDateHelper = respondToQueryDateHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isRespondentResponseBilingual()
            ? notificationsProperties.getQueryLipWelshPublicResponseReceived()
            : notificationsProperties.getQueryLipPublicResponseReceived();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        String partyName = caseData.getRespondent1().getPartyName();
        properties.put(PARTY_NAME, partyName);
        properties.put(CLAIMANT_V_DEFENDANT, getAllPartyNames(caseData));
        respondToQueryDateHelper.addQueryDateProperty(properties, caseData);
        return properties;
    }
}
