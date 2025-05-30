package uk.gov.hmcts.reform.civil.notification.handlers.claimcontinuingonlinespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimContinuingOnlineSpecClaimantEmailDTOGenerator
        extends EmailDTOGenerator
        implements NotificationData {

    private static final String REFERENCE_TEMPLATE = "claim-continuing-online-lip-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public ClaimContinuingOnlineSpecClaimantEmailDTOGenerator(
            NotificationsProperties notificationsProperties
    ) {
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return caseData.isApplicantLiP()
                && caseData.getApplicant1Email() != null;
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getApplicant1Email();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.isClaimantBilingual()
                ? notificationsProperties.getBilingualClaimantClaimContinuingOnlineForSpec()
                : notificationsProperties.getClaimantClaimContinuingOnlineForSpec();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return Map.of(
                RESPONDENT_NAME,      getPartyNameBasedOnType(caseData.getRespondent1()),
                CLAIMANT_NAME,        getPartyNameBasedOnType(caseData.getApplicant1()),
                ISSUE_DATE,           formatLocalDate(caseData.getIssueDate(), DATE),
                CLAIM_REFERENCE_NUMBER, caseData.getLegacyCaseReference(),
                RESPONSE_DEADLINE,    formatLocalDate(
                        caseData.getRespondent1ResponseDeadline().toLocalDate(),
                        DATE
                )
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
