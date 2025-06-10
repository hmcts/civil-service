package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponsenotagreedrepayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimantResponseNotAgreedRepaymentDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    protected ClaimantResponseNotAgreedRepaymentDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isRespondentResponseBilingual() ? notificationsProperties.getNotifyDefendantLipWelshTemplate()
            : notificationsProperties.getNotifyDefendantLipTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-reject-repayment-respondent-notification-%s";
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        return properties;
    }
}
