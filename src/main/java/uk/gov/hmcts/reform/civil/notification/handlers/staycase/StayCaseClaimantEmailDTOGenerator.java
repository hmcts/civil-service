package uk.gov.hmcts.reform.civil.notification.handlers.staycase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Component
public class StayCaseClaimantEmailDTOGenerator extends StayCaseEmailDTOGenerator {

    public StayCaseClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties,
        NotificationsSignatureConfiguration configuration, FeatureToggleService featureToggleService) {
        super(notificationsProperties, configuration, featureToggleService);
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.isApplicantLiP() ? caseData.getClaimantUserDetails().getEmail()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    @Override
    protected boolean isLiP(CaseData caseData) {
        return caseData.isApplicantLiP();
    }

    @Override
    protected boolean isBilingual(CaseData caseData) {
        return caseData.isClaimantBilingual();
    }

    @Override
    protected String getPartyName(CaseData caseData) {
        return caseData.getApplicant1().getPartyName();
    }

    @Override
    protected String getReferenceTemplate() {
        return "case-stayed-claimant-notification-%s";
    }
}
