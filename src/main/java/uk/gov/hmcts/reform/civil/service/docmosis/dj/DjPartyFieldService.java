package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getRespondent1NameWithLitigiousFriend;
import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getRespondent2NameWithLitigiousFriend;

/**
 * Party-facing view helper that resolves respondent/applicant derived fields for DJ templates.
 */
@Service
public class DjPartyFieldService {

    static final String BOTH_DEFENDANTS = "Both Defendants";

    public String resolveRespondent(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            return getRespondent1NameWithLitigiousFriend(caseData, true);
        }

        String defendantName = caseData.getDefendantDetails() != null
            && caseData.getDefendantDetails().getValue() != null
            ? caseData.getDefendantDetails().getValue().getLabel()
            : null;

        if (BOTH_DEFENDANTS.equals(defendantName)) {

            return getRespondent1NameWithLitigiousFriend(caseData, true)
                + ", "
                + getRespondent2NameWithLitigiousFriend(caseData, true);
        } else if (caseData.getRespondent1() != null
            && defendantName != null
            && defendantName.equals(caseData.getRespondent1().getPartyName())) {
            return getRespondent1NameWithLitigiousFriend(caseData, true);
        } else {
            return caseData.getRespondent2() != null
                ? getRespondent2NameWithLitigiousFriend(caseData, true)
                : getRespondent1NameWithLitigiousFriend(caseData, true);
        }
    }

    public boolean hasApplicantPartyName(CaseData caseData) {
        return caseData.getApplicant1() != null
            && caseData.getApplicant1().getPartyName() != null;
    }
}
