package uk.gov.hmcts.reform.civil.notification.handlers.generateordercourtofficerorder;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.GenerateOrderNotifyPartiesCourtOfficerOrder;

@Component
@RequiredArgsConstructor
public class GenerateOrderCOODefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    @Setter
    private String taskInfo;

    NotificationsProperties notificationsProperties;

    protected static final String COO_DEFENDANT_REFERENCE_TEMPLATE = "generate-order-notification-%s";

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (caseData.isRespondentResponseBilingual()) {
            if (GenerateOrderNotifyPartiesCourtOfficerOrder.toString().equals(taskInfo)) {
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
