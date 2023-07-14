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
        String [] claimantData = {
            SITE_ID, data.getLegacyCaseReference(), CASE_TYPE, data.getTotalClaimAmount().toString(),
            data.getApplicant1().getType().toString(), getApplicantSolicitorCompanyName(claimantRepresentativeOrganisation,data),
            data.getApplicant1().getPartyName(), getRepresentativeContactNumber(claimantRepresentativeOrganisation),
            CHECK_LIST, PARTY_STATUS, getApplicantEmailAddress(data),
            isPilot(data.getTotalClaimAmount())
        };

        String [] respondentData = {
            SITE_ID, data.getLegacyCaseReference(), CASE_TYPE, data.getTotalClaimAmount().toString(),
            data.getRespondent1().getType().toString(), getCsvCompanyNameForDefendant(data.getRespondent1()),
            getCsvIndividualName(data.getRespondent1()), data.getRespondent1().getPartyPhone(),
            CHECK_LIST, PARTY_STATUS, data.getRespondent1().getPartyEmail(),
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

    private String getApplicantSolicitorCompanyName(Optional<Organisation> organisation, CaseData caseData) {
        return organisation.map(Organisation::getName).orElse(caseData.getApplicantSolicitor1ClaimStatementOfTruth().getName());
    }

    private String getRepresentativeContactNumber(Optional<Organisation> organisation) {
       return organisation.map(Organisation::getCompanyNumber).orElse("");
    }

    private String getApplicantEmailAddress(CaseData caseData) {
        return Optional.ofNullable(caseData.getApplicantSolicitor1CheckEmail()).map(CorrectEmail::getEmail).orElse("");
    }

    private String getCsvCompanyNameForDefendant(CaseData caseData) {
        if(caseData.isRespondent1LiP()){
            return caseData.getRespondent1().getPartyName();
        }

    }

    private String getCsvIndividualName(Party party) {
       return party.getPartyName();
    }
}
