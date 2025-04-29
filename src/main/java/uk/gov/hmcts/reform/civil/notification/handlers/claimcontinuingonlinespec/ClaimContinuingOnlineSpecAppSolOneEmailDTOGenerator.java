package uk.gov.hmcts.reform.civil.notification.handlers.claimcontinuingonlinespec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimContinuingOnlineSpecAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    protected static final String REFERENCE_TEMPLATE = "claim-continuing-online-notification-%s";

    private final NotificationsProperties notificationsProperties;

    public ClaimContinuingOnlineSpecAppSolOneEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            OrganisationService organisationService
    ) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return caseData.getRespondent2() != null
                ? notificationsProperties.getClaimantSolicitorClaimContinuingOnline1v2ForSpec()
                : notificationsProperties.getClaimantSolicitorClaimContinuingOnlineForSpec();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(
            Map<String, String> properties,
            CaseData caseData
    ) {
        Map<String, String> updated = super.addCustomProperties(properties, caseData);

        updated.put(
                CLAIM_DETAILS_NOTIFICATION_DEADLINE,
                formatLocalDate(caseData.getRespondent1ResponseDeadline().toLocalDate(), DATE)
        );
        updated.put(ISSUED_ON, formatLocalDate(caseData.getIssueDate(), DATE));
        updated.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));

        if (caseData.getRespondent2() != null) {
            updated.put(RESPONDENT_ONE_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            updated.put(RESPONDENT_TWO_NAME, getPartyNameBasedOnType(caseData.getRespondent2()));
        } else {
            updated.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            updated.put(RESPONSE_DEADLINE, formatLocalDateTime(
                    caseData.getRespondent1ResponseDeadline(), DATE_TIME_AT));
        }

        return updated;
    }
}
