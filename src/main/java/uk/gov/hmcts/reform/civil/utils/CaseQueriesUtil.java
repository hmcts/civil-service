package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.QueryCollectionType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.model.querymanagement.LatestQuery;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.CLAIMANT_QUERY_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DEFENDANT_QUERY_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.enums.QueryCollectionType.APPLICANT_SOLICITOR_QUERIES;
import static uk.gov.hmcts.reform.civil.enums.QueryCollectionType.RESPONDENT_SOLICITOR_ONE_QUERIES;
import static uk.gov.hmcts.reform.civil.enums.QueryCollectionType.RESPONDENT_SOLICITOR_TWO_QUERIES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPClaimant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPDefendant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

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

    public static CaseQueriesCollection getCollectionByMessage(CaseData caseData, CaseMessage message) {
        return Stream.of(
                caseData.getQmApplicantSolicitorQueries(),
                caseData.getQmRespondentSolicitor1Queries(),
                caseData.getQmRespondentSolicitor2Queries()
            )
            .filter(collection -> nonNull(collection) && nonNull(collection.getCaseMessages()) && collection.getCaseMessages().stream()
                .anyMatch(messageEl -> messageEl.getValue().getCreatedOn().equals(message.getCreatedOn())))
            .findFirst()
            .orElse(null);
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

    public static QueryCollectionType getCollectionType(CaseQueriesCollection queriesCollection, CaseData caseData) {

        if (queriesCollection.isSame(caseData.getQmApplicantSolicitorQueries())) {
            return APPLICANT_SOLICITOR_QUERIES;
        }
        if (queriesCollection.isSame(caseData.getQmRespondentSolicitor1Queries())) {
            return RESPONDENT_SOLICITOR_ONE_QUERIES;
        }
        if (queriesCollection.isSame(caseData.getQmRespondentSolicitor2Queries())) {
            return RESPONDENT_SOLICITOR_TWO_QUERIES;
        }

        return null;
    }

    public static DocCategory getQueryDocumentCategory(QueryCollectionType collectionType) {
        return switch(collectionType) {
            case APPLICANT_SOLICITOR_QUERIES -> CLAIMANT_QUERY_DOCUMENTS;
            case RESPONDENT_SOLICITOR_ONE_QUERIES, RESPONDENT_SOLICITOR_TWO_QUERIES -> DEFENDANT_QUERY_DOCUMENTS;
            default -> null;
        };
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
        if (caseData.getQmApplicantCitizenQueries() != null) {
            latestQueries.addAll(unwrapElements(caseData.getQmApplicantCitizenQueries().getCaseMessages()));
        }
        if (caseData.getQmRespondentCitizenQueries() != null) {
            latestQueries.addAll(unwrapElements(caseData.getQmRespondentCitizenQueries().getCaseMessages()));
        }
        return latestQueries.stream().filter(m -> m.getId().equals(queryId)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No query found for queryId " + queryId));
    }

    public static LatestQuery buildLatestQuery(CaseMessage latestCaseMessage) {
        return Optional.ofNullable(latestCaseMessage)
            .map(caseMessage -> LatestQuery.builder()
                .queryId(caseMessage.getId())
                .isHearingRelated(caseMessage.getIsHearingRelated())
                .build())
            .orElse(null);
    }

    public static void assignCategoryIdToAttachments(CaseMessage latestCaseMessage,
                                                     AssignCategoryId assignCategoryId,
                                                     List<String> roles) {
        String categoryId = getAttachmentsCategoryIdForRole(roles);
        List<Element<Document>> attachments = latestCaseMessage.getAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            for (Element<Document> attachment : attachments) {
                assignCategoryId.assignCategoryIdToDocument(attachment.getValue(), categoryId);
            }
        }
    }

    public static void assignCategoryIdToCaseworkerAttachments(CaseData caseData,
                                                               CaseMessage latestCaseMessage,
                                                               AssignCategoryId assignCategoryId,
                                                               CoreCaseUserService coreCaseUserService,
                                                               String parentQueryId) {
        List<String> roles = getUserRoleForQuery(caseData, coreCaseUserService, parentQueryId);
        assignCategoryIdToAttachments(latestCaseMessage, assignCategoryId, roles);
    }

    private static String getAttachmentsCategoryIdForRole(List<String> roles) {
        if (isApplicantSolicitor(roles)) {
            return DocCategory.CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS.getValue();
        } else if (isRespondentSolicitorOne(roles) || isRespondentSolicitorTwo(roles)) {
            return DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue();
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_ROLE_ERROR);
        }
    }

    private static String getQueryDocumentCategoryIdForRole(List<String> roles) {
        if (isApplicantSolicitor(roles)) {
            return CLAIMANT_QUERY_DOCUMENTS.getValue();
        } else if (isRespondentSolicitorOne(roles) || isRespondentSolicitorTwo(roles)) {
            return DocCategory.DEFENDANT_QUERY_DOCUMENTS.getValue();
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_ROLE_ERROR);
        }
    }
}
