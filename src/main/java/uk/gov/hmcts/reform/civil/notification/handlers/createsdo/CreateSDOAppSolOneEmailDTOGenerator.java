package uk.gov.hmcts.reform.civil.notification.handlers.createsdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Component
public class CreateSDOAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "create-sdo-applicants-notification-%s";

    protected CreateSDOAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(notificationsProperties, organisationService);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.getCaseAccessCategory().equals(SPEC_CLAIM) ? notificationsProperties.getSdoOrderedSpec() :
            notificationsProperties.getSdoOrdered();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }
}
