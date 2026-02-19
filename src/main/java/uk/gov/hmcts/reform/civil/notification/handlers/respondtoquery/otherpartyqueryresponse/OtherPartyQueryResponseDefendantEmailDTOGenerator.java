package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.RespondToQueryHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getAllPartyNames;

@Component
public class OtherPartyQueryResponseDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "other-party-response-to-query-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final RespondToQueryHelper respondToQueryHelper;

    public OtherPartyQueryResponseDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                            RespondToQueryHelper respondToQueryHelper) {
        this.notificationsProperties = notificationsProperties;
        this.respondToQueryHelper = respondToQueryHelper;
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
        respondToQueryHelper.addCustomProperties(properties, caseData, partyName, true);
        return properties;
    }
}
