package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.model.querymanagement.LatestQuery;

import java.util.Comparator;
import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

public class CaseQueriesUtil {

    private CaseQueriesUtil() {
        //NO-OP
    }

    public static CaseQueriesCollection getUserQueriesByRole(CaseData caseData, List<String> roles) {
        if (roles.contains("[APPLICANTSOLICITORONE]")) {
            return caseData.getQmApplicantSolicitorQueries();
        } else if (roles.contains("[RESPONDENTSOLICITORONE]")) {
            return caseData.getQmRespondentSolicitor1Queries();
        } else if (roles.contains("[RESPONDENTSOLICITORTWO]")) {
            return caseData.getQmRespondentSolicitor2Queries();
        } else if (roles.contains("[CLAIMANT]")) {
            return caseData.getQmApplicantCitizenQueries();
        } else if (roles.contains("[DEFENDANT]")) {
            return caseData.getQmRespondentCitizenQueries();
        } else {
            throw new IllegalArgumentException("User does have a supported case role for query management.");
        }
    }

    public static LatestQuery getLatestQuery(CaseQueriesCollection usersQueries) {
        return unwrapElements(usersQueries.getCaseMessages()).stream()
            .max(Comparator.comparing(CaseMessage::getCreatedOn))
            .map(latestMessage -> LatestQuery.builder()
                .queryId(latestMessage.getId())
                .isHearingRelated(latestMessage.getIsHearingRelated())
                .isAdditionalQuestion(latestMessage.getCreatedBy().contains("Admin") ? YesOrNo.NO : YesOrNo.YES)
                .build())
            .orElse(null);
    }

}
