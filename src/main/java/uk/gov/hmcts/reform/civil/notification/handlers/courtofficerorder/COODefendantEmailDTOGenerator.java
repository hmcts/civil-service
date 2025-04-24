package uk.gov.hmcts.reform.civil.notification.handlers.courtofficerorder;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

@Component
@AllArgsConstructor
public class COODefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    NotificationsProperties notificationsProperties;

    protected static final String COO_DEFENDANT_REFERENCE_TEMPLATE = "generate-order-notification-%s";

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (caseData.isRespondentResponseBilingual()) {
            return notificationsProperties.getNotifyLipUpdateTemplateBilingual();
        }
        return notificationsProperties.getNotifyLipUpdateTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return COO_DEFENDANT_REFERENCE_TEMPLATE;
    }
}
