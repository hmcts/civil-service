package uk.gov.hmcts.reform.civil.notification.handlers.trialready;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

@Component
public class TrialReadyClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "other-party-trial-ready-notification-%s";

    protected TrialReadyClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        super(notificationsProperties);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual() ? notificationsProperties.getNotifyLipUpdateTemplateBilingual() :
            notificationsProperties.getNotifyLipUpdateTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }
}
