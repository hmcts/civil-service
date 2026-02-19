package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;

@Component
public class RespondToQueryAppSolEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "response-to-query-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final RespondToQueryHelper respondToQueryHelper;

    public RespondToQueryAppSolEmailDTOGenerator(OrganisationService organisationService,
                                                 NotificationsProperties notificationsProperties,
                                                 RespondToQueryHelper respondToQueryHelper) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.respondToQueryHelper = respondToQueryHelper;
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
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getApplicantLegalOrganizationName(caseData, organisationService));
        respondToQueryHelper.addQueryDateProperty(properties, caseData);
        return properties;
    }
}
