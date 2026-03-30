package uk.gov.hmcts.reform.civil.notification.handlers.notifydecisiononreconsiderationrequest;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_V_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getClaimantVDefendant;

@Component
public class NotifyDecisionOnReconsiderationRequestRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "reconsideration-upheld-applicant-notification-%s";
    private final NotificationsProperties notificationsProperties;

    public NotifyDecisionOnReconsiderationRequestRespSolTwoEmailDTOGenerator(OrganisationService organisationService,
                                                                             NotificationsProperties notificationsProperties) {
        super(organisationService);
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
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getRespondentSolicitor2EmailAddress() != null
            ? caseData.getRespondentSolicitor2EmailAddress()
            : caseData.getRespondentSolicitor1EmailAddress();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        Map<String, String> result = super.addCustomProperties(properties, caseData);
        result.put(CLAIMANT_V_DEFENDANT, getClaimantVDefendant(caseData));
        result.put(PARTY_NAME, caseData.getRespondent2().getPartyName());
        return result;
    }
}
