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

    public String generateCSVContent(CaseData caseData, boolean isR2FlagEnabled) {
        MediationParams mediationParams = getMediationParams(caseData);
        return getCSVContent(mediationParams, isR2FlagEnabled);
    }

    private String getCSVContent(MediationParams params, boolean isR2FlagEnabled) {
        CaseData data = params.getCaseData();
        ApplicantContactDetails applicantContactDetails = getApplicantContactDetails();
        DefendantContactDetails defendantContactDetails = getDefendantContactDetails();
        String totalClaimAmount = data.getTotalClaimAmount().toString();
        String[] claimantData = {
            SITE_ID, data.getLegacyCaseReference(), CASE_TYPE, totalClaimAmount,
            CLAIMANT, getCsvCompanyName(data.getApplicant1()),
            applicantContactDetails.getApplicantContactName(params), applicantContactDetails.getApplicantContactNumber(params),
            CHECK_LIST, PARTY_STATUS, applicantContactDetails.getApplicantContactEmail(params),
            isPilot(data.getTotalClaimAmount())
        };

        String[] respondentData = {
            SITE_ID, data.getLegacyCaseReference(), CASE_TYPE, totalClaimAmount,
            RESPONDENT, getCsvCompanyName(data.getRespondent1()),
            defendantContactDetails.getDefendantContactName(params), defendantContactDetails.getDefendantContactNumber(params),
            CHECK_LIST, PARTY_STATUS, defendantContactDetails.getDefendantContactEmail(params),
            isPilot(data.getTotalClaimAmount())
        };

        if (isR2FlagEnabled) {
            return generateCSVRow(claimantData, isWelshFlag(data.isBilingual()), isR2FlagEnabled)
                    + generateCSVRow(respondentData, isWelshFlag(data.isRespondentResponseBilingual()), isR2FlagEnabled);
        }

        return generateCSVRow(claimantData, null, false)
                + generateCSVRow(respondentData, null, false);
    }

    protected abstract ApplicantContactDetails getApplicantContactDetails();

    protected abstract DefendantContactDetails getDefendantContactDetails();

    protected abstract MediationParams getMediationParams(CaseData caseData);

    private String isPilot(BigDecimal amount) {
        return amount.compareTo(new BigDecimal(10000)) < 0 ? "Yes" : "No";
    }

    private String generateCSVRow(String[] row, String bilingualFlag, boolean isR2FlagEnabled) {
        StringBuilder builder = new StringBuilder();

        for (String s : row) {
            builder.append(s).append(",");
        }
        if (isR2FlagEnabled) {
            builder.append(bilingualFlag).append(",");
        }
        builder.append("\n");

        return builder.toString();
    }

    protected String getCsvCompanyName(Party party) {
        return (party.isCompany() || party.isOrganisation()) ? party.getPartyName() : null;
    }

    private String isWelshFlag(boolean isBilingualFlag) {
        return isBilingualFlag ? "Yes" : "No";
    }
}
