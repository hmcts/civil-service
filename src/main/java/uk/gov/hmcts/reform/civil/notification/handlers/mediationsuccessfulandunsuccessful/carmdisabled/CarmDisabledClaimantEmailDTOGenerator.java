package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmdisabled;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.notification.handlers.CamundaProcessIdentifier.MediationSuccessfulNotifyParties;

@Component
@AllArgsConstructor
public class CarmDisabledClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_LIP = "mediation-update-claimant-notification-LIP-%s";

    private final NotificationsProperties notificationsProperties;

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return getEmailTemplateId(caseData, null);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData, String taskId) {
        if (MediationSuccessfulNotifyParties.toString().equals(taskId)) {
            return caseData.isClaimantBilingual()
                ? notificationsProperties.getNotifyApplicantLiPMediationSuccessfulWelshTemplate() :
                notificationsProperties.getNotifyApplicantLiPMediationSuccessfulTemplate();
        }
        return caseData.isClaimantBilingual()
            ? notificationsProperties.getMediationUnsuccessfulClaimantLIPWelshTemplate() :
            notificationsProperties.getMediationUnsuccessfulClaimantLIPTemplate();

    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(Map.of(
                CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
                RESPONDENT_NAME, caseData.getRespondent1().getPartyName(),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference()
        ));
        return properties;
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_LIP;
    }
}
