package uk.gov.hmcts.reform.civil.notification.handlers.notifydecisiononreconsiderationrequest;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getClaimantVDefendant;

@Component
public class NotifyDecisionOnReconsiderationRequestClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "reconsideration-upheld-applicant-notification-%s";
    private final NotificationsProperties notificationsProperties;

    public NotifyDecisionOnReconsiderationRequestClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyClaimReconsiderationLRTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        Map<String, String> result = super.addCustomProperties(properties, caseData);
        result.put(CLAIMANT_V_DEFENDANT, getClaimantVDefendant(caseData));
        return result;
    }
}
