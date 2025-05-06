package uk.gov.hmcts.reform.civil.notification.handlers.claimantliphelpwithfees;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;

@Component
@AllArgsConstructor
public class ClaimantLipHelpWithFeesEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "notify-claimant-lip-help-with-fees-notification-%s";

    private final NotificationsProperties notificationsProperties;

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
                ? notificationsProperties.getNotifyClaimantLipHelpWithFeesWelsh()
                : notificationsProperties.getNotifyClaimantLipHelpWithFees();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }
}