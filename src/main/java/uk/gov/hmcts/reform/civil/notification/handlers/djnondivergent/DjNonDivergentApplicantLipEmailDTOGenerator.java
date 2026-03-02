package uk.gov.hmcts.reform.civil.notification.handlers.djnondivergent;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

@Component
public class DjNonDivergentApplicantLipEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE_LIP = "dj-non-divergent-applicant-notification-lip-%s";

    private final NotificationsProperties notificationsProperties;

    public DjNonDivergentApplicantLipEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyUpdateTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_LIP;
    }

}
