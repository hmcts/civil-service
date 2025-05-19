package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

@Component
public class ClaimDismissedRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private final ClaimDismissedEmailHelper claimDismissedEmailHelper;
    protected static final String REFERENCE_TEMPLATE_RESPONDENT_FOR_CLAIM_DISMISSED = "claim-dismissed-respondent-notification-%s";

    public ClaimDismissedRespSolTwoEmailDTOGenerator(OrganisationService organisationService,
                                                     ClaimDismissedEmailHelper claimDismissedEmailHelper) {
        super(organisationService);
        this.claimDismissedEmailHelper = claimDismissedEmailHelper;
    }

    @Override
    public String getEmailTemplateId(CaseData caseData) {
        return claimDismissedEmailHelper.getTemplateId(caseData); //NOSONAR
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_RESPONDENT_FOR_CLAIM_DISMISSED;
    }

    @Override
    protected Boolean getShouldNotify(CaseData caseData) {
        return claimDismissedEmailHelper.isValidForRespondentEmail(caseData); //NOSONAR
    }
}
