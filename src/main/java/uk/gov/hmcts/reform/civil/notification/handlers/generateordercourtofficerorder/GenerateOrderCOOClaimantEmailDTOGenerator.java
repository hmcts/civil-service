package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.GenerateOrderNotifyPartiesCourtOfficerOrder;

@Component
public class GenerateOrderCOOClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    protected static final String COO_CLAIMANT_REFERENCE_TEMPLATE = "generate-order-notification-%s";

    protected GenerateOrderCOOClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        super(notificationsProperties);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return getEmailTemplateId(caseData, null);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData, String taskId) {
        if (caseData.isClaimantBilingual()) {
            if (GenerateOrderNotifyPartiesCourtOfficerOrder.toString().equals(taskId)) {
                return notificationsProperties.getNotifyLipUpdateTemplateBilingual();
            } else {
                return notificationsProperties.getOrderBeingTranslatedTemplateWelsh();
            }
        }
        return notificationsProperties.getNotifyLipUpdateTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return COO_CLAIMANT_REFERENCE_TEMPLATE;
    }
}
