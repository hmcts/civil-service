package uk.gov.hmcts.reform.civil.utils;

import org.assertj.core.util.Strings;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.model.querymanagement.LatestQuery;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.*;

public class CaseQueriesUtil {

    private static final String UNSUPPORTED_ROLE_ERROR = "Unsupported case role for query management.";

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
        } else if (isLIPClaimant(roles)) {
            return caseData.getQmApplicantCitizenQueries();
        } else if (isLIPDefendant(roles)) {
            return caseData.getQmRespondentCitizenQueries();
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_ROLE_ERROR);
        }
    }

    public static LatestQuery buildLatestQuery(CaseMessage latestCaseMessage) {
        return Optional.ofNullable(latestCaseMessage)
            .map(caseMessage -> LatestQuery.builder()
                .queryId(caseMessage.getId())
                .isHearingRelated(caseMessage.getIsHearingRelated())
                .isAdditionalQuestion(isAdditionalQuestion(caseMessage))
                .build())
            .orElse(null);
    }

    private static YesOrNo isAdditionalQuestion(CaseMessage message) {
        if (Strings.isNullOrEmpty(message.getParentId())) {
            return YesOrNo.NO;
        }
        return message.getCreatedBy().contains("Admin") ? YesOrNo.NO : YesOrNo.YES;
    }
}
