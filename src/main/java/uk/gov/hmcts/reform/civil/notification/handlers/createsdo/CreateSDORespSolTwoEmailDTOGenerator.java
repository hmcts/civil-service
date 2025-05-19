package uk.gov.hmcts.reform.civil.notification.handlers.createsdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class CreateSDORespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private final FeatureToggleService featureToggleService;

    private static final String REFERENCE_TEMPLATE = "create-sdo-respondent-2-notification-%s";

    protected CreateSDORespSolTwoEmailDTOGenerator(NotificationsProperties notificationsProperties, OrganisationService organisationService, FeatureToggleService featureToggleService) {
        super(notificationsProperties, organisationService);
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (caseData.getCaseAccessCategory() == CaseCategory.SPEC_CLAIM) {
            if (caseData.isRespondentResponseBilingual()) {
                return notificationsProperties.getSdoOrderedSpecBilingual();
            } else if (featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed(caseData.getCaseManagementLocation().getBaseLocation())) {
                return notificationsProperties.getSdoOrderedSpecEa();
            }
            return notificationsProperties.getSdoOrderedSpec();
        }
        return notificationsProperties.getSdoOrdered();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }
}
