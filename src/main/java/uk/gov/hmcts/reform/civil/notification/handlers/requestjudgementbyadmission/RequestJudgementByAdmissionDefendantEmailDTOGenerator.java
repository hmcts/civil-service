package uk.gov.hmcts.reform.civil.notification.handlers.requestjudgementbyadmission;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class RequestJudgementByAdmissionDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    protected RequestJudgementByAdmissionDefendantEmailDTOGenerator(
        NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isRespondentResponseBilingual()
            ? notificationsProperties.getRespondentCcjNotificationWelshTemplate()
            : notificationsProperties.getRespondentCcjNotificationTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "request-judgement-by-admission-respondent-notification-%s";
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return caseData.isRespondent1LiP() && !caseData.isLipvLipOneVOne();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
        return properties;
    }
}
