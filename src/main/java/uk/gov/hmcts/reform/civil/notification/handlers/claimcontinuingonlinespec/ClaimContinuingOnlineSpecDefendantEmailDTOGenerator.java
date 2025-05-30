package uk.gov.hmcts.reform.civil.notification.handlers.claimcontinuingonlinespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.config.PinInPostConfiguration;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimContinuingOnlineSpecDefendantEmailDTOGenerator
        extends EmailDTOGenerator
        implements NotificationData {

    private static final String REFERENCE_TEMPLATE = "claim-continuing-online-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final PinInPostConfiguration pipInPostConfiguration;

    public ClaimContinuingOnlineSpecDefendantEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            PinInPostConfiguration pipInPostConfiguration
    ) {
        this.notificationsProperties       = notificationsProperties;
        this.pipInPostConfiguration = pipInPostConfiguration;
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return caseData.isRespondent1LiP()
                && caseData.getRespondent1() != null
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
        return Map.of(
                RESPONDENT_NAME,        getPartyNameBasedOnType(caseData.getRespondent1()),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                CLAIM_16_DIGIT_NUMBER,  caseData.getCcdCaseReference().toString(),
                RESPOND_URL,            pipInPostConfiguration.getRespondToClaimUrl(),
                PIN,                    caseData.getRespondent1PinToPostLRspec().getAccessCode(),
                RESPONSE_DEADLINE,      formatLocalDate(
                        caseData.getRespondent1ResponseDeadline()
                                .toLocalDate(),
                        DATE
                ),
                FRONTEND_URL,           pipInPostConfiguration.getCuiFrontEndUrl()
        );
    }

    @Override
    protected Map<String, String> addCustomProperties(
            Map<String, String> properties,
            CaseData caseData
    ) {
        return properties;
    }
}
