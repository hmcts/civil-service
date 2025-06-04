package uk.gov.hmcts.reform.civil.notification.handlers.caseproceedsincaseman;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Component
public class CaseProceedsInCasemanClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "case-proceeds-in-caseman-applicant-notification-%s";

    private final FeatureToggleService featureToggleService;

    protected CaseProceedsInCasemanClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties, FeatureToggleService featureToggleService) {
        super(notificationsProperties);
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual() ? notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate() :
            notificationsProperties.getClaimantLipClaimUpdatedTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return caseData.isLipvLipOneVOne() && featureToggleService.isLipVLipEnabled() ? Boolean.TRUE : Boolean.FALSE;
    }
}
