package uk.gov.hmcts.reform.civil.notification.handlers.takecaseoffline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class CaseTakenOfflineRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "case-taken-offline-respondent-notification-%s";
    private final NotificationsProperties notificationsProperties;

    public CaseTakenOfflineRespSolTwoEmailDTOGenerator(OrganisationService organisationService, NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
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
