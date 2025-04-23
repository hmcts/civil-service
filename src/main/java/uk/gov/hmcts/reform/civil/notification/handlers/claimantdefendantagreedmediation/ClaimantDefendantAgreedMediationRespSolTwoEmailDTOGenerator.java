package uk.gov.hmcts.reform.civil.notification.handlers.claimantdefendantagreedmediation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class ClaimantDefendantAgreedMediationRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {
    private final FeatureToggleService featureToggleService;

    public ClaimantDefendantAgreedMediationRespSolTwoEmailDTOGenerator(OrganisationService organisationService, NotificationsProperties notificationsProperties,
                                                                       FeatureToggleService featureToggleService) {
        super(notificationsProperties, organisationService);
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return featureToggleService.isCarmEnabledForCase(caseData) ? notificationsProperties.getNotifyDefendantLRForMediation() :
            notificationsProperties.getNotifyRespondentLRMediationAgreementTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "mediation-agreement-respondent-notification-%s";
    }
}
