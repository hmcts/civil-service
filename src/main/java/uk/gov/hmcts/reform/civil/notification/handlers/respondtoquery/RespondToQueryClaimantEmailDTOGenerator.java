package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@Component
public class RespondToQueryClaimantEmailDTOGenerator extends AbstractRespondToQueryClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "response-to-query-notification-%s";

    public RespondToQueryClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                   RespondToQueryHelper respondToQueryHelper) {
        super(notificationsProperties, respondToQueryHelper);
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        respondToQueryHelper.addLipOtherPartyProperties(properties, caseData, caseData.getApplicant1().getPartyName());
        respondToQueryHelper.addQueryDateProperty(properties, caseData);
        return properties;
    }
}
