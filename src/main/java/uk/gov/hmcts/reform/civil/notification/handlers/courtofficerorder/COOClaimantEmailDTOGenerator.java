package uk.gov.hmcts.reform.civil.notification.handlers.courtofficerorder;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

@Component
@AllArgsConstructor
public class COOClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    NotificationsProperties notificationsProperties;

    private static final String COURT_OFFICE_ORDER_REFERENCE_TEMPLATE = "generate-order-notification-%s";

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (caseData.isApplicantLiP()) {
            if (caseData.isClaimantBilingual()) {
                return notificationsProperties.getNotifyLipUpdateTemplateBilingual();
            }
            return notificationsProperties.getNotifyLipUpdateTemplate();
        } else {
            return notificationsProperties.getGenerateOrderNotificationTemplate();
        }
    }

    @Override
    protected String getReferenceTemplate() {
        return COURT_OFFICE_ORDER_REFERENCE_TEMPLATE;
    }
}
