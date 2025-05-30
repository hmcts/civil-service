package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimContinuingOnlineSpecRespondentPartyEmailDTOGenerator extends EmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "claim-continuing-online-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final PinInPostConfiguration pipInPostConfiguration;

    public ClaimContinuingOnlineSpecRespondentPartyEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            PinInPostConfiguration pipInPostConfiguration
    ) {
        this.notificationsProperties = notificationsProperties;
        this.pipInPostConfiguration = pipInPostConfiguration;
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return caseData.getRespondent1() != null
                && caseData.getRespondent1().getPartyEmail() != null;
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getRespondent1().getPartyEmail();
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
    public Map<String, String> addProperties(CaseData caseData) {
        Map<String, String> props = new HashMap<>();
        props.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
        props.put(CLAIMANT_NAME,   getPartyNameBasedOnType(caseData.getApplicant1()));
        props.put(ISSUED_ON,        formatLocalDate(caseData.getIssueDate(), DATE));
        props.put(RESPOND_URL,     pipInPostConfiguration.getRespondToClaimUrl());
        props.put(CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference());
        props.put(CLAIM_16_DIGIT_NUMBER,  caseData.getCcdCaseReference().toString());
        props.put(PIN,              caseData.getRespondent1PinToPostLRspec().getAccessCode());
        props.put(RESPONSE_DEADLINE,
                formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE));
        props.put(FRONTEND_URL,     pipInPostConfiguration.getCuiFrontEndUrl());
        return props;
    }

    @Override
    protected Map<String, String> addCustomProperties(
            Map<String, String> properties,
            CaseData caseData
    ) {
        return properties;
    }
}
