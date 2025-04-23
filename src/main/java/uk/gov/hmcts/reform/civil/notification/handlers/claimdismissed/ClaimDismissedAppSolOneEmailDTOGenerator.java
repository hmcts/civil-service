package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class ClaimDismissedAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    protected static final String CLAIM_DISMISSED_APPLICANT_NOTIFICATION_REFERENCE_TEMPLATE = "claim-dismissed-applicant-notification-%s";
    private final ClaimDismissedEmailTemplater claimDismissedEmailTemplater;

    public ClaimDismissedAppSolOneEmailDTOGenerator(OrganisationService organisationService,
                                                    ClaimDismissedEmailTemplater claimDismissedEmailTemplater) {
        super(organisationService, organisationService);
        this.claimDismissedEmailTemplater = claimDismissedEmailTemplater;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return claimDismissedEmailTemplater.getTemplateId(caseData);
    }

    @Override
    protected String getReferenceTemplate() {
        return CLAIM_DISMISSED_APPLICANT_NOTIFICATION_REFERENCE_TEMPLATE;
    }

}
