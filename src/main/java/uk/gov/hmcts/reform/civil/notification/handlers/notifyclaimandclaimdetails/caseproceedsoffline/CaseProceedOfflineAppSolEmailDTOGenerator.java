package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.caseproceedsoffline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class CaseProceedOfflineAppSolEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "case-proceeds-in-caseman-applicant-notification-%s";

    protected CaseProceedOfflineAppSolEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(notificationsProperties, organisationService);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getSolicitorCaseTakenOffline();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }
}
