package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.model.querymanagement.LatestQuery;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

public class CaseQueriesUtil {

    private static final String UNSUPPORTED_ROLE_ERROR = "User does have a supported case role for query management.";

    private CaseQueriesUtil() {
        //NO-OP
    }

    public static CaseQueriesCollection getUserQueriesByRole(CaseData caseData, List<String> roles) {
        if (isApplicantSolicitor(roles)) {
            return caseData.getQmApplicantSolicitorQueries();
        } else if (isRespondentSolicitorOne(roles)) {
            return caseData.getQmRespondentSolicitor1Queries();
        } else if (isRespondentSolicitorTwo(roles)) {
            return caseData.getQmRespondentSolicitor2Queries();
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_ROLE_ERROR);
        }
    }

    public static LatestQuery buildLatestQuery(CaseMessage latestCaseMessage) {
        return Optional.ofNullable(latestCaseMessage)
            .map(caseMessage -> LatestQuery.builder()
                .queryId(caseMessage.getId())
                .isHearingRelated(caseMessage.getIsHearingRelated())
                .build())
            .orElse(null);
    }
}
