package uk.gov.hmcts.reform.civil.service.citizenui;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@AllArgsConstructor
public class MediationCSVService {

    private static final String SITE_ID = "5";
    private static final String CASE_TYPE = "1";
    private static final String CHECK_LIST = "4";
    private static final String PARTY_STATUS = "5";

    private final OrganisationService organisationService;

    public String generateCSVContent(CaseData data) {
        String [] headers = {"SITE_ID", "CASE_NUMBER", "CASE_TYPE", "AMOUNT", "PARTY_TYPE", "COMPANY_NAME",
            "CONTACT_NAME", "CONTACT_NUMBER", "CHECK_LIST", "PARTY_STATUS", "CONTACT_EMAIL", "PILOT"};

        Optional<Organisation> claimantRepresentativeOrganisation = organisationService.findOrganisationById(data.getApplicantOrganisationId());
        String totalClaimAmount = data.getTotalClaimAmount().toString();
        String [] claimantData = {
            SITE_ID, data.getLegacyCaseReference(), CASE_TYPE, totalClaimAmount,
            data.getApplicant1().getType().toString(), getCsvCompanyName(data.getApplicant1()),
            getApplicantSolicitorCompanyName(claimantRepresentativeOrganisation,data), getRepresentativeContactNumber(claimantRepresentativeOrganisation),
            CHECK_LIST, PARTY_STATUS, getApplicantEmailAddress(data),
            isPilot(data.getTotalClaimAmount())
        };
        Optional<Organisation> defendantRepresentativeOrganisation = getRespondentRepresentativeOrganisation(data);
        String [] respondentData = {
            SITE_ID, data.getLegacyCaseReference(), CASE_TYPE, totalClaimAmount,
            data.getRespondent1().getType().toString(), getCsvCompanyName(data.getRespondent1()),
            getCsvContactNameForDefendant(data, defendantRepresentativeOrganisation), getContactNumberForDefendant(data, defendantRepresentativeOrganisation),
            CHECK_LIST, PARTY_STATUS, getContactEmailForDefendant(data),
            isPilot(data.getTotalClaimAmount())
        };

        return generateCSVRow(headers)
            + generateCSVRow(claimantData)
            + generateCSVRow(respondentData);
    }

    private String isPilot(BigDecimal amount) {
        return amount.compareTo(new BigDecimal(10000)) < 0 ? "Yes" : "No";
    }

    private String generateCSVRow(String [] row) {
        StringBuilder builder = new StringBuilder();

        for (String s : row) {
            builder.append(s).append(",");
        }
        builder.append("\n");

        return builder.toString();
    }

    private Optional<Organisation> getRespondentRepresentativeOrganisation (CaseData caseData) {
        if(caseData.isRespondent1LiP()){
            return Optional.empty();
        }
        return organisationService.findOrganisationById(caseData.getRespondent1OrganisationId());
    }

    private String getCsvCompanyName(Party party) {
        return (party.isCompany() || party.isOrganisation()) ? party.getPartyName() : null;
    }

    private String getCsvContactNameForDefendant(CaseData caseData,  Optional<Organisation> defendantRepresentativeOrganisation) {
       return defendantRepresentativeOrganisation.map(Organisation::getName).orElse(getCsvIndividualName(caseData.getRespondent1()));
    }

    private String getContactNumberForDefendant(CaseData caseData, Optional<Organisation> organisation) {
        return caseData.isRespondent1LiP()? caseData.getRespondent1().getPartyPhone() : getRepresentativeContactNumber(organisation);
    }

    private String getContactEmailForDefendant(CaseData caseData){
        return caseData.isRespondent1LiP()? caseData.getRespondent1().getPartyEmail() : caseData.getRespondentSolicitor1EmailAddress();
    }

    private String getCsvIndividualName(Party party) {
        return (party.isIndividual() || party.isSoleTrader()) ? party.getPartyName() : null;
    }

    private String getApplicantSolicitorCompanyName(Optional<Organisation> organisation, CaseData caseData) {
        return organisation.map(Organisation::getName).orElse(caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName());
    }

    private String getRepresentativeContactNumber(Optional<Organisation> organisation) {
        return organisation.map(Organisation::getCompanyNumber).orElse("");
    }

    private String getApplicantEmailAddress(CaseData caseData) {
        return Optional.ofNullable(caseData.getApplicantSolicitor1CheckEmail()).map(CorrectEmail::getEmail)
            .orElse(caseData.getApplicantSolicitor1UserDetails().getEmail());
    }
}
