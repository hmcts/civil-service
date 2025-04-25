package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

@Component
@AllArgsConstructor
public class GenerateOrderCOOClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    NotificationsProperties notificationsProperties;

    protected static final String COO_CLAIMANT_REFERENCE_TEMPLATE = "generate-order-notification-%s";

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (caseData.isClaimantBilingual()) {
            return notificationsProperties.getNotifyLipUpdateTemplateBilingual();
        }
        return notificationsProperties.getNotifyLipUpdateTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return COO_CLAIMANT_REFERENCE_TEMPLATE;
    }
}
