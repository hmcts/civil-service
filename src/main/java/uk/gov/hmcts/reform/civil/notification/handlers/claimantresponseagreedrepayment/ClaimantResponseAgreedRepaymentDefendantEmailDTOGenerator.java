package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseagreedrepayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimantResponseAgreedRepaymentDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    protected ClaimantResponseAgreedRepaymentDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        super(notificationsProperties);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isRespondentResponseBilingual() ? notificationsProperties.getRespondentCcjNotificationWelshTemplate()
            : notificationsProperties.getRespondentCcjNotificationTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-agree-repayment-respondent-notification-%s";
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        return properties;
    }
}
