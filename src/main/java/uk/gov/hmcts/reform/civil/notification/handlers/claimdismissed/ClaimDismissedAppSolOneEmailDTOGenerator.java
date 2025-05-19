package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class ClaimDismissedAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    protected static final String CLAIM_DISMISSED_APPLICANT_NOTIFICATION_REFERENCE_TEMPLATE = "claim-dismissed-applicant-notification-%s";
    private final ClaimDismissedEmailHelper claimDismissedEmailHelper;

    public ClaimDismissedAppSolOneEmailDTOGenerator(OrganisationService organisationService,
                                                    ClaimDismissedEmailHelper claimDismissedEmailHelper) {
        super(organisationService);
        this.claimDismissedEmailHelper = claimDismissedEmailHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return claimDismissedEmailHelper.getTemplateId(caseData);
    }

    @Override
    protected String getReferenceTemplate() {
        return CLAIM_DISMISSED_APPLICANT_NOTIFICATION_REFERENCE_TEMPLATE;
    }

}
