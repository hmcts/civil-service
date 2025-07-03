package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment.claimcontinuingonline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimContinuingOnlineSpecDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "claim-continuing-online-notification-%s";
    private final NotificationsProperties notificationsProperties;
    private final PinInPostConfiguration pinInPostConfiguration;

    public ClaimContinuingOnlineSpecDefendantEmailDTOGenerator(
            NotificationsProperties notificationsProperties, PinInPostConfiguration pinInPostConfiguration
    ) {
        this.notificationsProperties = notificationsProperties;
        this.pinInPostConfiguration = pinInPostConfiguration;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getRespondentDefendantResponseForSpec();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties,
                                                      CaseData caseData) {
        properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        properties.put(CLAIMANT_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        properties.put(ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE));
        properties.put(RESPOND_URL, pinInPostConfiguration.getRespondToClaimUrl());
        properties.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
        properties.put(CLAIM_16_DIGIT_NUMBER, caseData.getCcdCaseReference().toString());
        properties.put(PIN, caseData.getRespondent1PinToPostLRspec().getAccessCode());
        properties.put(RESPONSE_DEADLINE,
                formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE));
        properties.put(FRONTEND_URL, pinInPostConfiguration.getCuiFrontEndUrl());
        return properties;
    }
}
