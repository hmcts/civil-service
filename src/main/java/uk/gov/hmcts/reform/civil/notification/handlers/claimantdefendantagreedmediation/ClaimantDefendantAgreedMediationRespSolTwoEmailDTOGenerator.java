package uk.gov.hmcts.reform.civil.notification.handlers.claimantdefendantagreedmediation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.shouldSendMediationNotificationDefendant2LRCarm;

@Component
public class ClaimantDefendantAgreedMediationRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    private final FeatureToggleService featureToggleService;

    public ClaimantDefendantAgreedMediationRespSolTwoEmailDTOGenerator(OrganisationService organisationService, NotificationsProperties notificationsProperties,
                                                                       FeatureToggleService featureToggleService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyDefendantLRForMediation();
    }

    @Override
    protected String getReferenceTemplate() {
        return "mediation-agreement-respondent-notification-%s";
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        boolean carmEnabled = featureToggleService.isCarmEnabledForCase(caseData);
        return shouldSendMediationNotificationDefendant2LRCarm(caseData, carmEnabled);
    }
}
