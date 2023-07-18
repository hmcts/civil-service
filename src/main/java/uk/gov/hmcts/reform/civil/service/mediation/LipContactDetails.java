package uk.gov.hmcts.reform.civil.service.mediation;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;

import java.util.Optional;

public abstract class LipContactDetails {

    protected String getCsvCompanyName(Party party) {
        return (party.isCompany() || party.isOrganisation()) ? party.getPartyName() : null;
    }

    protected String getCsvIndividualName(Party party) {
        return (party.isIndividual() || party.isSoleTrader()) ? party.getPartyName() : null;
    }

    protected String getApplicantRepresentativeEmailAddress(CaseData caseData) {
        return Optional.ofNullable(caseData.getApplicantSolicitor1CheckEmail()).map(CorrectEmail::getEmail)
            .orElse(caseData.getApplicantSolicitor1UserDetails().getEmail());
    }

    protected String getRepresentativeContactNumber(Optional<Organisation> organisation) {
        return organisation.map(Organisation::getCompanyNumber).orElse("");
    }
}
