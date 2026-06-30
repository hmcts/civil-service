package uk.gov.hmcts.reform.civil.notification.handlers.createsdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class CreateSDORespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_RESPONDENT_2 = "create-sdo-respondent-2-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected CreateSDORespSolTwoEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_RESPONDENT_2;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (caseData.getCaseAccessCategory() == CaseCategory.SPEC_CLAIM) {
            if (caseData.isRespondentResponseBilingual()) {
                return notificationsProperties.getSdoOrderedSpecBilingual();
            } else {
                return notificationsProperties.getSdoOrderedSpecEa();
            }
        }
        return notificationsProperties.getSdoOrdered();
    }
}
