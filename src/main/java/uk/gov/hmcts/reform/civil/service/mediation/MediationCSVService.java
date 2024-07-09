package uk.gov.hmcts.reform.civil.service.mediation;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.math.BigDecimal;

public abstract class MediationCSVService {

    private static final String SITE_ID = "5";
    private static final String CASE_TYPE = "1";
    private static final String CHECK_LIST = "4";
    private static final String PARTY_STATUS = "5";
    private static final String CLAIMANT = "1";
    private static final String RESPONDENT = "2";

    public String generateCSVContent(CaseData caseData) {
        MediationParams mediationParams = getMediationParams(caseData);
        return getCSVContent(mediationParams);
    }

    private String getCSVContent(MediationParams params) {
        CaseData data = params.getCaseData();
        ApplicantContactDetails applicantContactDetails = getApplicantContactDetails();
        DefendantContactDetails defendantContactDetails = getDefendantContactDetails();
        String totalClaimAmount = data.getTotalClaimAmount().toString();
        String[] claimantData = {
            SITE_ID, CASE_TYPE, CHECK_LIST, PARTY_STATUS, data.getLegacyCaseReference(), totalClaimAmount, CLAIMANT,
            getCsvCompanyName(data.getApplicant1()), applicantContactDetails.getApplicantContactName(params),
            applicantContactDetails.getApplicantContactNumber(params),
            applicantContactDetails.getApplicantContactEmail(params),
            isPilot(data.getTotalClaimAmount())
        };

        String[] respondentData = {
            SITE_ID, CASE_TYPE, CHECK_LIST, PARTY_STATUS, data.getLegacyCaseReference(), totalClaimAmount, RESPONDENT,
            getCsvCompanyName(data.getRespondent1()), defendantContactDetails.getDefendantContactName(params),
            defendantContactDetails.getDefendantContactNumber(params),
            defendantContactDetails.getDefendantContactEmail(params),
            isPilot(data.getTotalClaimAmount())
        };
        return generateCSVRow(claimantData)
            + generateCSVRow(respondentData);
    }

    protected abstract ApplicantContactDetails getApplicantContactDetails();

    protected abstract DefendantContactDetails getDefendantContactDetails();

    protected abstract MediationParams getMediationParams(CaseData caseData);

    private String isPilot(BigDecimal amount) {
        return amount.compareTo(new BigDecimal(10000)) < 0 ? "Yes" : "No";
    }

    private String generateCSVRow(String[] row) {
        StringBuilder builder = new StringBuilder();

        for (String rowValue : row) {
            builder.append(rowValue).append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("\r\n");

        return builder.toString();
    }

    protected String getCsvCompanyName(Party party) {
        return (party.isCompany() || party.isOrganisation()) ? party.getPartyName() : null;
    }
}
