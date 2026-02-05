package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

/**
 * Party-facing view helper that resolves respondent/applicant derived fields for DJ templates.
 */
@Service
public class DjPartyFieldService {

    static final String BOTH_DEFENDANTS = "Both Defendants";

    public String resolveRespondent(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            return caseData.getRespondent1().getPartyName();
        }

        String defendantName = caseData.getDefendantDetails() != null
            && caseData.getDefendantDetails().getValue() != null
            ? caseData.getDefendantDetails().getValue().getLabel()
            : null;

        if (BOTH_DEFENDANTS.equals(defendantName)) {
            return caseData.getRespondent1().getPartyName()
                + ", "
                + caseData.getRespondent2().getPartyName();
        } else if (caseData.getRespondent1() != null
            && defendantName != null
            && defendantName.equals(caseData.getRespondent1().getPartyName())) {
            return caseData.getRespondent1().getPartyName();
        } else {
            return caseData.getRespondent2() != null
                ? caseData.getRespondent2().getPartyName()
                : caseData.getRespondent1().getPartyName();
        }
    }

    public boolean hasApplicantPartyName(CaseData caseData) {
        return caseData.getApplicant1() != null
            && caseData.getApplicant1().getPartyName() != null;
    }
}
