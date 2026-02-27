package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.AbstractRespondToQueryDefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.RespondToQueryHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@Component
public class OtherPartyQueryResponseDefendantEmailDTOGenerator extends AbstractRespondToQueryDefendantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "other-party-response-to-query-notification-%s";

    public OtherPartyQueryResponseDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                            RespondToQueryHelper respondToQueryHelper) {
        super(notificationsProperties, respondToQueryHelper);
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

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return respondToQueryHelper.shouldNotifyOtherPartyLipDefendant(caseData);
    }
}
