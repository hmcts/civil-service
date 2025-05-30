package uk.gov.hmcts.reform.civil.notification.handlers.acknowledgeclaimspec;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Component
public class AcknowledgeClaimSpecAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE = "acknowledge-claim-applicant-notification-%s";

    private final NotificationsProperties notificationsProperties;
    private final OrganisationService organisationService;

    public AcknowledgeClaimSpecAppSolOneEmailDTOGenerator(
            NotificationsProperties notificationsProperties,
            OrganisationService organisationService
    ) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.organisationService = organisationService;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getApplicantSolicitorAcknowledgeClaimForSpec();
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        String orgId = caseData.getApplicant1OrganisationPolicy()
                .getOrganisation()
                .getOrganisationID();
        Optional<Organisation> maybeOrg = organisationService.findOrganisationById(orgId);
        String orgName = maybeOrg.map(Organisation::getName)
                .orElse(caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName());
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, orgName);

        String deadline = formatLocalDate(
                caseData.getRespondent1ResponseDeadline().toLocalDate(),
                DATE
        );
        properties.put(RESPONSE_DEADLINE, deadline);

        return properties;
    }
}
