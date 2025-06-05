package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseagreedsettledpartadmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimantResponseAgreedSettledPartAdmitDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    protected ClaimantResponseAgreedSettledPartAdmitDefendantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        super(notificationsProperties);
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isRespondentResponseBilingual() ? notificationsProperties.getRespondentLipPartAdmitSettleClaimBilingualTemplate()
            : notificationsProperties.getRespondentLipPartAdmitSettleClaimTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-part-admit-settle-respondent-notification-%s";
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        return properties;
    }
}
