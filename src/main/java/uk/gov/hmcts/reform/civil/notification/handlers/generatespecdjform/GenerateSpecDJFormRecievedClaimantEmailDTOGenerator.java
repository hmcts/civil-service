package uk.gov.hmcts.reform.civil.notification.handlers.generatespecdjform;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class GenerateSpecDJFormRecievedClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_RECEIVED = "default-judgment-applicant-received-notification-%s";
    private final NotificationsProperties notificationsProperties;

    public GenerateSpecDJFormRecievedClaimantEmailDTOGenerator(NotificationsProperties notificationsProperties) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
            ? notificationsProperties.getApplicantLiPDefaultJudgmentRequestedBilingualTemplate()
            : notificationsProperties.getApplicantLiPDefaultJudgmentRequested();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_RECEIVED;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        super.addCustomProperties(properties, caseData);
        properties.put(APPLICANT_ONE_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        properties.put(CLAIM_NUMBER, caseData.getLegacyCaseReference());
        properties.put(DEFENDANT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        return properties;
    }
}
