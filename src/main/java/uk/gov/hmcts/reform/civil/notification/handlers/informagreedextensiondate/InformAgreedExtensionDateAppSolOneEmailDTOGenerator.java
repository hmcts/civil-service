package uk.gov.hmcts.reform.civil.notification.handlers.informagreedextensiondate;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

@Component
public class InformAgreedExtensionDateAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "agreed-extension-date-applicant-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected InformAgreedExtensionDateAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                                  OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getClaimantSolicitorAgreedExtensionDate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        super.addCustomProperties(properties, caseData);
        InformAgreedExtensionDateNotificationDataHelper.addCommonProperties(properties, caseData);
        return properties;
    }
}
