package uk.gov.hmcts.reform.civil.notification.handlers.notifyjudgmentvarieddeterminationofmeans;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getApplicantLegalOrganizationName;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getDefendantNameBasedOnCaseType;

@Component
public class JudgmentVariedDeterminationOfMeansAppSolOneEmailDTOGenerator extends EmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "claimant-judgment-varied-determination-of-means-%s";

    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    public JudgmentVariedDeterminationOfMeansAppSolOneEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            OrganisationService organisationService
    ) {
        this.notificationsProperties = notificationsProperties;
        this.organisationService = organisationService;
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return nonNull(caseData.getApplicantSolicitor1UserDetails().getEmail());
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getNotifyClaimantJudgmentVariedDeterminationOfMeansTemplate();
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
        properties.put(LEGAL_ORG_NAME, getApplicantLegalOrganizationName(caseData, organisationService));
        properties.put(DEFENDANT_NAME, getDefendantNameBasedOnCaseType(caseData));
        return properties;
    }

}
