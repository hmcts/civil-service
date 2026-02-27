package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.AbstractRespondToQueryClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.RespondToQueryHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

@Component
public class OtherPartyQueryResponseClaimantEmailDTOGenerator extends AbstractRespondToQueryClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "other-party-response-to-query-notification-%s";

    public OtherPartyQueryResponseClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties,
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
            caseData.getApplicant1().getPartyName()
        );
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return respondToQueryHelper.shouldNotifyOtherPartyLipClaimant(caseData);
    }
}
