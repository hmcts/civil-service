package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.AbstractRespondToQueryRespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.RespondToQueryHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class OtherPartyQueryResponseRespSolTwoEmailDTOGenerator extends AbstractRespondToQueryRespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "other-party-response-to-query-notification-%s";

    public OtherPartyQueryResponseRespSolTwoEmailDTOGenerator(OrganisationService organisationService,
                                                              NotificationsProperties notificationsProperties,
                                                              RespondToQueryHelper respondToQueryHelper) {
        super(organisationService, notificationsProperties, respondToQueryHelper);
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        String orgName = getLegalOrganizationNameForRespondent(caseData, false, organisationService);
        respondToQueryHelper.addCustomProperties(properties, caseData, orgName, false);
        return properties;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return respondToQueryHelper.shouldNotifyOtherPartyRespondentSolicitorTwo(caseData);
    }
}
