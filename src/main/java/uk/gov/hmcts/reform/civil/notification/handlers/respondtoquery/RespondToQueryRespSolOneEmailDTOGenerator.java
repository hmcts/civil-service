package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@Component
public class RespondToQueryRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "response-to-query-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final RespondToQueryDateHelper respondToQueryDateHelper;

    public RespondToQueryRespSolOneEmailDTOGenerator(OrganisationService organisationService,
                                                     NotificationsProperties notificationsProperties,
                                                     RespondToQueryDateHelper respondToQueryDateHelper) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.respondToQueryDateHelper = respondToQueryDateHelper;
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
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData, true, organisationService));
        respondToQueryDateHelper.addQueryDateProperty(properties, caseData);
        return properties;
    }
}
