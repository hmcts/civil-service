package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class OtherPartyQueryResponseRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "other-party-response-to-query-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final OtherPartyQueryResponseHelper otherPartyQueryResponseHelper;

    public OtherPartyQueryResponseRespSolTwoEmailDTOGenerator(OrganisationService organisationService,
                                                              NotificationsProperties notificationsProperties,
                                                              OtherPartyQueryResponseHelper otherPartyQueryResponseHelper) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.otherPartyQueryResponseHelper = otherPartyQueryResponseHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getQueryLrPublicResponseReceived();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        String orgName = getLegalOrganizationNameForRespondent(caseData, false, organisationService);
        otherPartyQueryResponseHelper.addCustomProperties(properties, caseData, orgName, false);
        return properties;
    }
}
