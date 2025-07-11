package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponsecui;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class DefendantResponseCUIClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private final NotificationsProperties notificationsProperties;

    private static final String REFERENCE_TEMPLATE = "defendant-response-applicant-notification-%s";

    public DefendantResponseCUIClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyLiPClaimantDefendantResponded();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
        properties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        return properties;
    }

    public Boolean getShouldNotify(CaseData caseData) {
        return caseData.isApplicantLiP() && !caseData.isRespondentResponseBilingual() ? Boolean.TRUE : Boolean.FALSE;
    }
}
