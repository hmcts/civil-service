package uk.gov.hmcts.reform.civil.service.mediation;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.math.BigDecimal;

public abstract class MediationCSVService {

    private static final String SITE_ID = "5";
    private static final String CASE_TYPE = "1";
    private static final String CHECK_LIST = "4";
    private static final String PARTY_STATUS = "5";

    public String generateCSVContent(CaseData caseData) {
        MediationParams mediationParams = getMediationParams(caseData);
        return getCSVContent(mediationParams);
    }

    private String getCSVContent(MediationParams params) {
        String[] headers = {"SITE_ID", "CASE_NUMBER", "CASE_TYPE", "AMOUNT", "PARTY_TYPE", "COMPANY_NAME",
            "CONTACT_NAME", "CONTACT_NUMBER", "CHECK_LIST", "PARTY_STATUS", "CONTACT_EMAIL", "PILOT"};
        CaseData data = params.getCaseData();
        String totalClaimAmount = data.getTotalClaimAmount().toString();
        String[] claimantData = {
            SITE_ID, data.getLegacyCaseReference(), CASE_TYPE, totalClaimAmount,
            data.getApplicant1().getType().toString(), getCsvCompanyName(data.getApplicant1()),
            getCsvContactNameForApplicant(params), getContactNumberForApplicant(params),
            CHECK_LIST, PARTY_STATUS, getContactEmailForApplicant(params),
            isPilot(data.getTotalClaimAmount())
        };

        String[] respondentData = {
            SITE_ID, data.getLegacyCaseReference(), CASE_TYPE, totalClaimAmount,
            data.getRespondent1().getType().toString(), getCsvCompanyName(data.getRespondent1()),
            getCsvContactNameForDefendant(params), getContactNumberForDefendant(params),
            CHECK_LIST, PARTY_STATUS, getContactEmailForDefendant(params),
            isPilot(data.getTotalClaimAmount())
        };

        return generateCSVRow(headers)
            + generateCSVRow(claimantData)
            + generateCSVRow(respondentData);
    }

    protected abstract ApplicantContactDetails getApplicantContactDetails();

    protected abstract DefendantContactDetails getDefendantContactDetails();

    protected abstract MediationParams getMediationParams(CaseData caseData);

    private String getCsvContactNameForApplicant(MediationParams params) {
        return getApplicantContactDetails().getApplicantContactName(params);
    }

    private String getContactEmailForApplicant(MediationParams params) {
        return getApplicantContactDetails().getApplicantContactEmail(params);
    }

    private String getContactNumberForApplicant(MediationParams params) {
        return getApplicantContactDetails().getApplicantContactNumber(params);
    }

    private String getContactEmailForDefendant(MediationParams params) {
        return getDefendantContactDetails().getDefendantContactEmail(params);
    }

    private String getCsvContactNameForDefendant(MediationParams params) {
        return getDefendantContactDetails().getDefendantContactNumber(params);
    }

    private String getContactNumberForDefendant(MediationParams params) {
        return getDefendantContactDetails().getDefendantContactNumber(params);
    }

    private String isPilot(BigDecimal amount) {
        return amount.compareTo(new BigDecimal(10000)) < 0 ? "Yes" : "No";
    }

    private String generateCSVRow(String[] row) {
        StringBuilder builder = new StringBuilder();

        for (String s : row) {
            builder.append(s).append(",");
        }
        builder.append("\n");

        return builder.toString();
    }

    protected String getCsvCompanyName(Party party) {
        return (party.isCompany() || party.isOrganisation()) ? party.getPartyName() : null;
    }

}
