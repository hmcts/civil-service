package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.getLegalOrganizationNameForRespondent;

@AllArgsConstructor
public abstract class RespSolTwoEmailDTOGenerator extends EmailDTOGenerator {

    protected final OrganisationService organisationService;

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getRespondentSolicitor2EmailAddress();
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        boolean isRespondent1 = false;
        properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, getLegalOrganizationNameForRespondent(caseData,
            isRespondent1, organisationService));
        return properties;
    }

    public Boolean getShouldNotify(CaseData caseData) {
        return isOneVTwoTwoLegalRep(caseData) && !caseData.isRespondent2LiP();
    }
}
