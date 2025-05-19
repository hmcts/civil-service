package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class ClaimDismissedRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private final ClaimDismissedEmailTemplater claimDismissedEmailTemplater;
    protected static final String REFERENCE_TEMPLATE_RESPONDENT_FOR_CLAIM_DISMISSED = "claim-dismissed-respondent-notification-%s";

    public ClaimDismissedRespSolOneEmailDTOGenerator(OrganisationService organisationService, ClaimDismissedEmailTemplater claimDismissedEmailHelper
    ) {
        super(organisationService);
        this.claimDismissedEmailTemplater = claimDismissedEmailHelper;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return claimDismissedEmailTemplater.getTemplateId(caseData);
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_RESPONDENT_FOR_CLAIM_DISMISSED;
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return caseData.isRespondent1LiP() ? Boolean.FALSE : Boolean.TRUE  && caseData.getClaimDismissedDate() != null;
    }

}
