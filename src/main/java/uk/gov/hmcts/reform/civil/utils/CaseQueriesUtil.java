package uk.gov.hmcts.reform.civil.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.QueryCollectionType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.model.querymanagement.LatestQuery;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.civil.enums.DocCategory.CASEWORKER_QUERY_DOCUMENT_ATTACHMENTS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.CLAIMANT_QUERY_DOCUMENTS;
import static java.util.Objects.nonNull;

import static uk.gov.hmcts.reform.civil.enums.DocCategory.CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DEFENDANT_QUERY_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.enums.DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS;
import static uk.gov.hmcts.reform.civil.enums.QueryCollectionType.APPLICANT_SOLICITOR_QUERIES;
import static uk.gov.hmcts.reform.civil.enums.QueryCollectionType.RESPONDENT_SOLICITOR_ONE_QUERIES;
import static uk.gov.hmcts.reform.civil.enums.QueryCollectionType.RESPONDENT_SOLICITOR_TWO_QUERIES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPClaimant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isLIPDefendant;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Slf4j
public class CaseQueriesUtil {

    private static final String UNSUPPORTED_ROLE_ERROR = "Unsupported case role for query management.";

    private CaseQueriesUtil() {
        //NO-OP
    }

    public static List<CaseMessage> getUserQueriesCreatedByUser(CaseData caseData, String createdBy) {
        if (caseData.getQueries() != null) {
            return unwrapElements(caseData.getQueries().getCaseMessages()).stream()
                .filter(m -> m.getCreatedBy().equals(createdBy)).collect(Collectors.toList());
        }
        throw new IllegalArgumentException("Unsupported action for no queries on case");
    }

    //ToDo: Remove this and all its usages after public queries release.
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

    //ToDo: Remove this and all its usages after public queries release.
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
        if (caseData.getQueries() != null) {
            latestQueries.add(caseData.getQueries().latest());
        }
        //ToDo: Remove after public queries release
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

    //ToDo: Remove this and all its usages after public queries release.
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

    //ToDo: Remove this and all its usages after public queries release.
    public static DocCategory getQueryDocumentCategory(QueryCollectionType collectionType) {
        return switch(collectionType) {
            case APPLICANT_SOLICITOR_QUERIES -> CLAIMANT_QUERY_DOCUMENTS;
            case RESPONDENT_SOLICITOR_ONE_QUERIES, RESPONDENT_SOLICITOR_TWO_QUERIES -> DEFENDANT_QUERY_DOCUMENTS;
            default -> null;
        };
    }

    public static DocCategory getQueryAttachmentsDocumentCategory(QueryCollectionType collectionType) {
        return switch (collectionType) {
            case APPLICANT_SOLICITOR_QUERIES -> CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS;
            case RESPONDENT_SOLICITOR_ONE_QUERIES, RESPONDENT_SOLICITOR_TWO_QUERIES ->
                DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS;
            default -> null;
        };
    }

    public static List<String> getUserRoleForQuery(CaseData caseData,
                                                   CoreCaseUserService coreCaseUserService, String queryId) {
        if (caseData.getQueries() != null) {
            String createdBy = unwrapElements(caseData.getQueries().getCaseMessages()).stream()
                .filter(m -> m.getId().equals(queryId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No query found for queryId " + queryId))
                .getCreatedBy();
            return coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), createdBy);
        }
        //ToDo: Remove below after public queries release
        CaseMessage query = getQueryById(caseData, queryId);
        String createdBy = query.getCreatedBy();
        return coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), createdBy);
    }

    //ToDo: Remove this and all its usages after public queries release.
    public static void updateQueryCollectionPartyName(List<String> roles, MultiPartyScenario scenario, CaseData.CaseDataBuilder builder) {
        CaseData caseData = builder.build();
        String partyName = getQueryCollectionPartyName(roles, scenario);

        if (isApplicantSolicitor(roles)) {
            builder.qmApplicantSolicitorQueries(updateQueryCollectionPartyName(caseData.getQmApplicantSolicitorQueries(), partyName));
        } else if (isRespondentSolicitorOne(roles)) {
            builder.qmRespondentSolicitor1Queries(updateQueryCollectionPartyName(caseData.getQmRespondentSolicitor1Queries(), partyName));
        } else if (isRespondentSolicitorTwo(roles)) {
            builder.qmRespondentSolicitor2Queries(updateQueryCollectionPartyName(caseData.getQmRespondentSolicitor2Queries(), partyName));
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_ROLE_ERROR);
        }
    }

    private static CaseQueriesCollection updateQueryCollectionPartyName(CaseQueriesCollection collection, String partyName) {
        return nonNull(collection) && nonNull(partyName) ? collection.toBuilder().partyName(partyName).build() : collection;
    }

    public static String getQueryCollectionPartyName(List<String> roles, MultiPartyScenario scenario) {
        if (isApplicantSolicitor(roles)) {
            return "Claimant";
        } else if (!scenario.equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) && isRespondentSolicitorOne(roles)) {
            return "Defendant";
        } else if (scenario.equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP) && isRespondentSolicitorOne(roles)) {
            return "Defendant 1";
        } else if (isRespondentSolicitorTwo(roles)) {
            return "Defendant 2";
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_ROLE_ERROR);
        }
    }

    public static CaseMessage getQueryById(CaseData caseData, String queryId) {
        List<CaseMessage> latestQueries = new ArrayList<>();
        if (caseData.getQueries() != null) {
            return unwrapElements(caseData.getQueries().getCaseMessages()).stream()
                .filter(m -> m.getId().equals(queryId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No query found for queryId " + queryId));
        }
        // ToDo: Remove below after public queries release
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

    private static boolean isWelshQuery(CaseData caseData, List<String> roles) {
        String languagePreference = null;

        if (isLIPClaimant(roles)) {
            languagePreference = caseData.getClaimantBilingualLanguagePreference();
        }

        if (isLIPDefendant(roles)) {
            languagePreference = caseData.getDefendantBilingualLanguagePreference();
        }

        return nonNull(languagePreference) && (languagePreference.equals("WELSH") || languagePreference.equals("BOTH"));
    }

    public static LatestQuery buildLatestQuery(CaseMessage latestCaseMessage) {
        return Optional.ofNullable(latestCaseMessage)
            .map(caseMessage -> LatestQuery.builder()
                .queryId(caseMessage.getId())
                .isHearingRelated(caseMessage.getIsHearingRelated())
                .build())
            .orElse(null);
    }

    public static LatestQuery buildLatestQuery(CaseMessage latestCaseMessage, CaseData caseData, List<String> roles) {
        return buildLatestQuery(latestCaseMessage).toBuilder()
            .isWelsh(isWelshQuery(caseData, roles) ? YesOrNo.YES : YesOrNo.NO)
            .build();
    }

    public static void assignCategoryIdToAttachments(CaseMessage latestCaseMessage,
                                                     AssignCategoryId assignCategoryId,
                                                     String docCategoryId) {
        List<Element<Document>> attachments = latestCaseMessage.getAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            for (Element<Document> attachment : attachments) {
                assignCategoryId.assignCategoryIdToDocument(attachment.getValue(), docCategoryId);
            }
        }
    }

    public static void assignCategoryIdToAttachments(CaseMessage latestCaseMessage,
                                                     AssignCategoryId assignCategoryId,
                                                     List<String> roles) {
        String categoryId = getAttachmentsCategoryIdForRole(roles);
        assignCategoryIdToAttachments(latestCaseMessage, assignCategoryId, categoryId);
    }

    public static void assignCategoryIdToCaseworkerAttachments(CaseData caseData,
                                                               CaseMessage latestCaseMessage,
                                                               AssignCategoryId assignCategoryId,
                                                               boolean isPublicQmEnabled) {
        if (!isPublicQmEnabled) {
            CaseQueriesCollection workingCollection = getCollectionByMessage(caseData, latestCaseMessage);
            DocCategory documentCategory = getQueryAttachmentsDocumentCategory(getCollectionType(workingCollection, caseData));
            assignCategoryIdToAttachments(latestCaseMessage, assignCategoryId, documentCategory.getValue());
        } else {
            assignCategoryIdToAttachments(latestCaseMessage, assignCategoryId, CASEWORKER_QUERY_DOCUMENT_ATTACHMENTS.getValue());
        }

    }

    private static String getAttachmentsCategoryIdForRole(List<String> roles) {
        if (isApplicantSolicitor(roles) || isLIPClaimant(roles)) {
            return CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS.getValue();
        } else if (isRespondentSolicitorOne(roles) || isRespondentSolicitorTwo(roles) || isLIPDefendant(roles)) {
            return DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue();
        } else {
            throw new IllegalArgumentException(UNSUPPORTED_ROLE_ERROR);
        }
    }

    public static boolean hasOldQueries(CaseData caseData) {
        return nonNull(caseData.getQmApplicantSolicitorQueries())
            || nonNull(caseData.getQmRespondentSolicitor1Queries())
            || nonNull(caseData.getQmRespondentSolicitor2Queries());
    }

    public static void migrateAllQueries(CaseData.CaseDataBuilder builder) {
        CaseData caseData = builder.build();
        if (hasOldQueries(caseData)) {
            String collectionOwner = "";
            try {
                builder.queries(CaseQueriesCollection.builder()
                                    .partyName("All queries")
                                    .caseMessages(new ArrayList<>())
                                    .build());
                collectionOwner = "Claimant";
                migrateQueries(caseData.getQmApplicantSolicitorQueries(), builder);
                collectionOwner = "Defendant 1";
                migrateQueries(caseData.getQmRespondentSolicitor1Queries(), builder);
                collectionOwner = "Defendant 2";
                migrateQueries(caseData.getQmRespondentSolicitor2Queries(), builder);
            } catch (Exception e) {
                log.error("There was a problem migrating the [{}] queries ", collectionOwner, e);
                // Continue to throw the original error since all queries must be migrated successfully.
                throw e;
            }
        }
    }

    public static void migrateQueries(CaseQueriesCollection collectionToMigrate, CaseData.CaseDataBuilder builder) {
        if (nonNull(collectionToMigrate) && nonNull(collectionToMigrate.getCaseMessages())) {
            CaseData caseData = builder.build();
            log.info("Started to migrate [{}] queries", collectionToMigrate.getPartyName(), caseData.getCcdCaseReference());
            List<Element<CaseMessage>> messages = caseData.getQueries().getCaseMessages();
            messages.addAll(collectionToMigrate.getCaseMessages());
            builder.queries(caseData.getQueries().toBuilder().caseMessages(messages).build());
        }
    }

    public static void clearOldQueryCollections(CaseData.CaseDataBuilder builder) {
        if (hasOldQueries(builder.build())) {
            builder.qmApplicantSolicitorQueries(null);
            builder.qmRespondentSolicitor1Queries(null);
            builder.qmRespondentSolicitor2Queries(null);
        }
    }

    public static void logMigrationSuccess(CaseData caseDataBefore) {
        if (hasOldQueries(caseDataBefore)) {
            Arrays.asList(
                    caseDataBefore.getQmApplicantSolicitorQueries(),
                    caseDataBefore.getQmRespondentSolicitor1Queries(),
                    caseDataBefore.getQmRespondentSolicitor2Queries()
                ).stream().filter(Objects::nonNull)
                .forEach(collection ->
                             log.info(
                                 "Successfully migrated [{}] queries",
                                 collection.getPartyName(), caseDataBefore.getCcdCaseReference()
                             ));
        }
    }
}
