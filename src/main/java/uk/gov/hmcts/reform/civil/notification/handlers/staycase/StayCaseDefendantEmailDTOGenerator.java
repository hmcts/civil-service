package uk.gov.hmcts.reform.civil.notification.handlers.staycase;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

@Component
public class StayCaseDefendantEmailDTOGenerator extends StayCaseEmailDTOGenerator {

    public StayCaseDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties,
        NotificationsSignatureConfiguration configuration, FeatureToggleService featureToggleService) {
        super(notificationsProperties, configuration, featureToggleService);
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.isRespondent1LiP() ? caseData.getRespondent1().getPartyEmail()
            : caseData.getRespondentSolicitor1EmailAddress();
    }

    @Override
    protected boolean isLiP(CaseData caseData) {
        return caseData.isRespondent1LiP();
    }

    @Override
    protected boolean isBilingual(CaseData caseData) {
        return caseData.isRespondentResponseBilingual();
    }

    @Override
    protected String getPartyName(CaseData caseData) {
        return caseData.getRespondent1().getPartyName();
    }

    @Override
    protected String getReferenceTemplate() {
        return "case-stayed-defendant-notification-%s";
    }
}
