package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

@Component
public class ClaimSubmissionLipClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE =
            "claim-submission-lip-claimant-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public ClaimSubmissionLipClaimantEmailDTOGenerator(
            NotificationsProperties notificationsProperties
    ) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyClaimantLipForClaimSubmissionTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }
}
