package uk.gov.hmcts.reform.civil.notification.handlers.trialready;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

@Component
public class TrialReadyDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "other-party-trial-ready-notification-%s";

    private final NotificationsProperties notificationsProperties;

    protected TrialReadyDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isRespondentResponseBilingual() ? notificationsProperties.getNotifyLipUpdateTemplateBilingual() :
            notificationsProperties.getNotifyLipUpdateTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }
}
