package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.utils.PartyUtils.getPartyNameWithLitigiousFriend;

/**
 * Party-facing view helper that resolves respondent/applicant derived fields for DJ templates.
 */
@Service
public class DjPartyFieldService {

    static final String BOTH_DEFENDANTS = "Both Defendants";

    public String resolveRespondent(CaseData caseData) {
        if (caseData.isRespondent1NotRepresented()) {
            return getPartyNameWithLitigiousFriend(caseData.getRespondent1(), caseData.getRespondent1LitigationFriend(), true);
        }

        String defendantName = caseData.getDefendantDetails() != null
            && caseData.getDefendantDetails().getValue() != null
            ? caseData.getDefendantDetails().getValue().getLabel()
            : null;

        if (BOTH_DEFENDANTS.equals(defendantName)) {

            return getPartyNameWithLitigiousFriend(caseData.getRespondent1(), caseData.getRespondent1LitigationFriend(), true)
                + ", "
                + getPartyNameWithLitigiousFriend(caseData.getRespondent2(), caseData.getRespondent2LitigationFriend(), true);
        } else if (caseData.getRespondent1() != null
            && defendantName != null
            && defendantName.equals(caseData.getRespondent1().getPartyName())) {
            return getPartyNameWithLitigiousFriend(caseData.getRespondent1(), caseData.getRespondent1LitigationFriend(), true);
        } else {
            return caseData.getRespondent2() != null
                ? getPartyNameWithLitigiousFriend(caseData.getRespondent2(), caseData.getRespondent2LitigationFriend(), true)
                : getPartyNameWithLitigiousFriend(caseData.getRespondent1(), caseData.getRespondent1LitigationFriend(), true);
        }
    }

    public boolean hasApplicantPartyName(CaseData caseData) {
        return caseData.getApplicant1() != null
            && caseData.getApplicant1().getPartyName() != null;
    }
}
