package uk.gov.hmcts.reform.civil.service.citizenui;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.math.BigDecimal;

@Service
public class MediationCSVService {

    private static final String SITE_ID = "5";
    private static final String CASE_TYPE = "1";
    private static final String CHECK_LIST = "4";
    private static final String PARTY_STATUS = "5";

    public String generateCSVContent(CaseData data) {
        String [] headers = {"SITE_ID", "CASE_NUMBER", "CASE_TYPE", "AMOUNT", "PARTY_TYPE", "COMPANY_NAME",
            "CONTACT_NAME", "CONTACT_NUMBER", "CHECK_LIST", "PARTY_STATUS", "CONTACT_EMAIL", "PILOT"};

        String [] claimantData = {
            SITE_ID, data.getLegacyCaseReference(), CASE_TYPE, data.getTotalClaimAmount().toString(),
            data.getApplicant1().getType().toString(), setUpCsvCompanyName(data.getApplicant1()),
            setUpCsvIndividualName(data.getApplicant1()), data.getApplicant1().getPartyPhone(),
            CHECK_LIST, PARTY_STATUS, data.getApplicant1().getPartyEmail(),
            isPilot(data.getTotalClaimAmount())
        };

        String [] respondentData = {
            SITE_ID, data.getLegacyCaseReference(), CASE_TYPE, data.getTotalClaimAmount().toString(),
            data.getRespondent1().getType().toString(), setUpCsvCompanyName(data.getRespondent1()),
            setUpCsvIndividualName(data.getRespondent1()), data.getRespondent1().getPartyPhone(),
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

    private String setUpCsvCompanyName(Party party) {
        return (party.isCompany() || party.isOrganisation()) ? party.getPartyName() : null;
    }

    private String setUpCsvIndividualName(Party party) {
        return (party.isIndividual() || party.isSoleTrader()) ? party.getPartyName() : null;
    }
}
