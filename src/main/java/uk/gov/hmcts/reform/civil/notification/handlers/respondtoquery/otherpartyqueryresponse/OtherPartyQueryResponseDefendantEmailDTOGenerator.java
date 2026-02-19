package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.RespondToQueryHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

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
        return respondToQueryHelper.addLipOtherPartyProperties(
            properties,
            caseData,
            caseData.getRespondent1().getPartyName()
        );
    }
}
