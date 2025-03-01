package uk.gov.hmcts.reform.civil.utils;

import org.assertj.core.util.Strings;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.model.querymanagement.LatestQuery;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPClaimant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPDefendant;

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

    public static CaseMessage getLatestQuery(CaseData caseData) {
        List<CaseMessage> latestQueries = new ArrayList<>();
        if (caseData.getQmApplicantSolicitorQueries() != null) {
            latestQueries.add(caseData.getQmApplicantSolicitorQueries().latest());
        }
        if (caseData.getQmRespondentSolicitor1Queries() != null) {
            latestQueries.add(caseData.getQmRespondentSolicitor1Queries().latest());
        }
        if (caseData.getQmRespondentSolicitor2Queries() != null) {
            latestQueries.add(caseData.getQmRespondentSolicitor2Queries().latest());
        }
        return latestQueries.stream().max(Comparator.comparing(CaseMessage::getCreatedOn))
            .orElse(null);
    }

    public static List<String> getUserRoleForQuery(CaseData caseData,
                                                          CoreCaseUserService coreCaseUserService, String queryId) {
        CaseMessage query = getQueryById(caseData, queryId);
        String createdBy = query.getCreatedBy();
        return coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), createdBy);
    }

    public static CaseMessage getQueryById(CaseData caseData, String queryId) {
        List<CaseMessage> latestQueries = new ArrayList<>();
        if (caseData.getQmApplicantSolicitorQueries() != null) {
            latestQueries.addAll(unwrapElements(caseData.getQmApplicantSolicitorQueries().getCaseMessages()));
        }
        if (caseData.getQmRespondentSolicitor1Queries() != null) {
            latestQueries.addAll(unwrapElements(caseData.getQmRespondentSolicitor1Queries().getCaseMessages()));
        }
        if (caseData.getQmRespondentSolicitor2Queries() != null) {
            latestQueries.addAll(unwrapElements(caseData.getQmRespondentSolicitor2Queries().getCaseMessages()));
        }
        return latestQueries.stream().filter(m -> m.getId().equals(queryId)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No query found for queryId " + queryId));
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
