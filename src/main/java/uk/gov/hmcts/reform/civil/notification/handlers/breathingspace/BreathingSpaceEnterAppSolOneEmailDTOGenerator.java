package uk.gov.hmcts.reform.civil.notification.handlers.breathingspace;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

@Component
public class BreathingSpaceEnterAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private final BreathingSpaceEmailHelper helper;
    private final NotificationsProperties notificationsProperties;

    public BreathingSpaceEnterAppSolOneEmailDTOGenerator(OrganisationService organisationService,
        BreathingSpaceEmailHelper helper, NotificationsProperties notificationsProperties) {
        super(organisationService);
        this.helper = helper;
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return !caseData.isApplicant1NotRepresented() && caseData.getApplicantSolicitor1UserDetails() != null;
    }

    @Override
    public String getEmailAddress(CaseData caseData) {
        return caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getBreathingSpaceEnterApplicantEmailTemplate();
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-confirms-not-to-proceed-respondent-notification-%s";
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return helper.addProperties(caseData);
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, helper.getEnterOrganisationName(
            caseData.getApplicant1OrganisationPolicy(), () -> caseData.getApplicantSolicitor1ClaimStatementOfTruth() == null
                ? null : caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName()));
        return properties;
    }
}
