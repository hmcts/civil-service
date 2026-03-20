package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondatespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

@Component
public class InformAgreedExtensionDateSpecRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "agreed-extension-date-applicant-notification-spec-%s";

    private final NotificationsProperties notificationsProperties;

    protected InformAgreedExtensionDateSpecRespSolTwoEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                                       OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getRespondentSolicitorAgreedExtensionDateForSpec();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        super.addCustomProperties(properties, caseData);
        InformAgreedExtensionDateSpecNotificationDataHelper.addRespondentSolicitorProperties(properties, caseData);
        return properties;
    }
}
