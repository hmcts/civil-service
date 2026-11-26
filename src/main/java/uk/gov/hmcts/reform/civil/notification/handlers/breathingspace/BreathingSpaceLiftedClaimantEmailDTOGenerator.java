package uk.gov.hmcts.reform.civil.notification.handlers.breathingspace;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class BreathingSpaceLiftedClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "breathing-space-lifted-lip-notification-%s";

    private final BreathingSpaceEmailHelper helper;
    private final NotificationsProperties notificationsProperties;

    public BreathingSpaceLiftedClaimantEmailDTOGenerator(BreathingSpaceEmailHelper helper, NotificationsProperties notificationsProperties) {
        this.helper = helper;
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return super.getShouldNotify(caseData) && StringUtils.isNotBlank(getEmailAddress(caseData));
    }

    @Override
    public String getEmailAddress(CaseData caseData) {
        return caseData.getApplicant1().getPartyEmail();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
            ? notificationsProperties.getNotifyLiPApplicantBreathingSpaceLiftedWelsh()
            : notificationsProperties.getNotifyLiPApplicantBreathingSpaceLifted();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return helper.addProperties(caseData);
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        properties.put(PARTY_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        return properties;
    }
}
