package uk.gov.hmcts.reform.civil.notification.handlers.claimantliphelpwithfees;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.TemplateCommonPropertiesHelper;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

@Component
public class ClaimantLipHelpWithFeesClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "notify-claimant-lip-help-with-fees-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public ClaimantLipHelpWithFeesClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties, TemplateCommonPropertiesHelper templateCommonPropertiesHelper) {
        this.notificationsProperties = notificationsProperties;
        this.templateCommonPropertiesHelper = templateCommonPropertiesHelper;
    }

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