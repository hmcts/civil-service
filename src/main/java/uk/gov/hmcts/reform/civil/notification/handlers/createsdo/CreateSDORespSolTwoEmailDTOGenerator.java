package uk.gov.hmcts.reform.civil.notification.handlers.createsdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class CreateSDORespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    protected CreateSDORespSolTwoEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(notificationsProperties, organisationService);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return null;
    }

    @Override
    protected String getReferenceTemplate() {
        return null;
    }
}
