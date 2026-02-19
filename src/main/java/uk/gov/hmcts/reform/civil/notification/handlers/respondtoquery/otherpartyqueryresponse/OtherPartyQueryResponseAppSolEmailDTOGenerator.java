package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.AbstractRespondToQueryAppSolEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.RespondToQueryHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Component
public class OtherPartyQueryResponseAppSolEmailDTOGenerator extends AbstractRespondToQueryAppSolEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "other-party-response-to-query-notification-%s";

    public OtherPartyQueryResponseAppSolEmailDTOGenerator(OrganisationService organisationService,
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
        String orgName = getApplicantLegalOrganizationName(caseData, organisationService);
        respondToQueryHelper.addCustomProperties(properties, caseData, orgName, false);
        return properties;
    }
}
