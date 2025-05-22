package uk.gov.hmcts.reform.civil.notification.handlers.claimdismissed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;

@Component
public class ClaimDismissedRespSolTwoEmailDTOGenerator extends RespSolTwoEmailDTOGenerator {

    private final ClaimDismissedEmailTemplater claimDismissedEmailTemplater;
    protected static final String REFERENCE_TEMPLATE_RESPONDENT_FOR_CLAIM_DISMISSED = "claim-dismissed-respondent-notification-%s";

    public ClaimDismissedRespSolTwoEmailDTOGenerator(OrganisationService organisationService,
                                                     ClaimDismissedEmailTemplater claimDismissedEmailHelper) {
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
        return caseData.getClaimDismissedDate() != null && isOneVTwoTwoLegalRep(caseData);
    }
}
