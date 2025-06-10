package uk.gov.hmcts.reform.civil.notification.handlers.claimantdefendantagreedmediation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class ClaimantDefendantAgreedMediationRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    public ClaimantDefendantAgreedMediationRespSolTwoEmailDTOGenerator(OrganisationService organisationService, NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyDefendantLRForMediation();
    }

    @Override
    protected String getReferenceTemplate() {
        return "mediation-agreement-respondent-notification-%s";
    }
}
