package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.GenerateOrderNotifyPartiesCourtOfficerOrder;

@Component
@AllArgsConstructor
public class GenerateOrderCOODefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    protected static final String COO_DEFENDANT_REFERENCE_TEMPLATE = "generate-order-notification-%s";

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return getEmailTemplateId(caseData, null);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData, String taskId) {
        if (caseData.isRespondentResponseBilingual()) {
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
        return COO_DEFENDANT_REFERENCE_TEMPLATE;
    }
}
