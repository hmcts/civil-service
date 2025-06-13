package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmstoproceed;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.RespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameBasedOnType;

@Component
public class ClaimantResponseConfirmsToProceedRespSolOneEmailDTOGenerator extends RespSolOneEmailDTOGenerator {

    private final ClaimantResponseConfirmsToProceedEmailHelper claimantResponseConfirmsToProceedEmailHelper;

    protected ClaimantResponseConfirmsToProceedRespSolOneEmailDTOGenerator(OrganisationService organisationService,
                                                                           ClaimantResponseConfirmsToProceedEmailHelper claimantResponseConfirmsToProceedEmailHelper) {
        super(organisationService);
        this.claimantResponseConfirmsToProceedEmailHelper = claimantResponseConfirmsToProceedEmailHelper;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return claimantResponseConfirmsToProceedEmailHelper.getTemplate(caseData, false);
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData, true, organisationService));

        if (caseData.getCaseAccessCategory().equals(SPEC_CLAIM)) {
            properties.put(RESPONDENT_NAME, getPartyNameBasedOnType(caseData.getRespondent1()));
            properties.put(APPLICANT_ONE_NAME, getPartyNameBasedOnType(caseData.getApplicant1()));
        }

        return properties;
    }

    @Override
    protected String getReferenceTemplate() {
        return "claimant-confirms-to-proceed-respondent-notification-%s";
    }
}
