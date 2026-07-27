package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isTwoVOne;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@AllArgsConstructor
public abstract class RespSolOneEmailDTOGenerator extends EmailDTOGenerator {

    protected static final String DEFENDANTS_TEXT = "'s claim against you";

    protected final OrganisationService organisationService;

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getRespondentSolicitor1EmailAddress();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return getEmailTemplateId(caseData, null);
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        boolean isRespondent1 = true;
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
            isRespondent1, organisationService));
        return properties;
    }

    protected Map<String, String> buildDefendantNotificationProperties(Map<String, String> properties, CaseData caseData) {
        String partyName = caseData.getApplicant1().getPartyName();

        if (isTwoVOne(caseData)) {
            partyName = String.format("%s and %s", partyName, caseData.getApplicant2().getPartyName());
            properties.putAll(Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                    true, organisationService),
                CLAIMANT_NAME_ONE, caseData.getApplicant1().getPartyName(),
                CLAIMANT_NAME_TWO, caseData.getApplicant2().getPartyName(),
                PARTY_NAME, partyName + DEFENDANTS_TEXT
            ));
        } else {
            properties.putAll(Map.of(
                CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
                    true, organisationService),
                CLAIMANT_NAME, caseData.getApplicant1().getPartyName(),
                PARTY_NAME, partyName + DEFENDANTS_TEXT
            ));
        }
        return properties;
    }

    public Boolean getShouldNotify(CaseData caseData) {
        return caseData.isRespondent1LiP() ? Boolean.FALSE : Boolean.TRUE;
    }
}
