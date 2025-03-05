package uk.gov.hmcts.reform.civil.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.model.querymanagement.LatestQuery;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class CaseQueriesUtilTest {

    @InjectMocks
    private AssignCategoryId assignCategoryId;
    @Mock
    private CoreCaseUserService coreCaseUserService;

    @Test
    void shouldReturnApplicantSolicitorQueries_WhenRoleIsApplicantSolicitor() {
        CaseQueriesCollection applicantSolicitorQueries = CaseQueriesCollection.builder()
            .partyName("John Doe")
            .roleOnCase("applicant-solicitor")
            .build();

        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(applicantSolicitorQueries)
            .build();

        CaseQueriesCollection result = CaseQueriesUtil.getUserQueriesByRole(caseData, List.of("APPLICANTSOLICITORONE"));

        assertEquals(applicantSolicitorQueries, result);
    }

    @Test
    void shouldReturnRespondentSolicitor1Queries_WhenRoleIsRespondentSolicitor1() {
        CaseQueriesCollection respondentSolicitor1Queries = CaseQueriesCollection.builder()
            .partyName("Jane Smith")
            .roleOnCase("respondent-solicitor-1")
            .build();

        CaseData caseData = CaseData.builder()
            .qmRespondentSolicitor1Queries(respondentSolicitor1Queries)
            .build();

        CaseQueriesCollection result = CaseQueriesUtil.getUserQueriesByRole(caseData, List.of("RESPONDENTSOLICITORONE"));

        assertEquals(respondentSolicitor1Queries, result);
    }

    @Test
    void shouldReturnRespondentSolicitor2Queries_WhenRoleIsRespondentSolicitor2() {
        CaseQueriesCollection respondentSolicitor2Queries = CaseQueriesCollection.builder()
            .partyName("Jane Smith")
            .roleOnCase("respondent-solicitor-2")
            .build();

        CaseData caseData = CaseData.builder()
            .qmRespondentSolicitor2Queries(respondentSolicitor2Queries)
            .build();

        CaseQueriesCollection result = CaseQueriesUtil.getUserQueriesByRole(caseData, List.of("RESPONDENTSOLICITORTWO"));

        assertEquals(respondentSolicitor2Queries, result);
    }

    @Test
    void shouldReturnNull_WhenCaseMessageIsNull() {
        LatestQuery result = CaseQueriesUtil.buildLatestQuery(null);
        assertNull(result);
    }

    @Test
    void shouldBuildLatestQueryFromCaseMessage() {
        LocalDateTime createdOn = LocalDateTime.now();
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3")
            .toBuilder()
            .createdOn(createdOn)
            .build();

        LatestQuery result = CaseQueriesUtil.buildLatestQuery(caseMessage);

        assertEquals("id", result.getQueryId());
    }

    @Test
    void shouldThrowExceptionForUnsupportedRole() {
        CaseData caseData = CaseData.builder().build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            CaseQueriesUtil.getUserQueriesByRole(caseData, List.of("unsupported-role"))
        );

        assertEquals("Unsupported case role for query management.", exception.getMessage());
    }

    @Test
    void shouldAssignCategoryIDToAttachments_whenApplicantUploadsAttachment() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3")
            .toBuilder()
            .createdOn(LocalDateTime.now())
            .attachments(wrapElements(
                Document.builder().documentFileName("a").build(),
                Document.builder().documentFileName("b").build()))
            .build();

        CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId,
                                                      List.of(CaseRole.APPLICANTSOLICITORONE.toString()));

        List<Document> documents = unwrapElements(caseMessage.getAttachments());

        assertEquals(DocCategory.CLAIMANT_QUERY_DOCUMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.CLAIMANT_QUERY_DOCUMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldAssignCategoryIDToAttachments_whenRespondent1UploadsAttachment() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3")
            .toBuilder()
            .createdOn(LocalDateTime.now())
            .attachments(wrapElements(
                Document.builder().documentFileName("a").build(),
                Document.builder().documentFileName("b").build()))
            .build();

        CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId,
                                                      List.of(CaseRole.RESPONDENTSOLICITORONE.toString()));

        List<Document> documents = unwrapElements(caseMessage.getAttachments());

        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldAssignCategoryIDToAttachments_whenRespondent2UploadsAttachment() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3")
            .toBuilder()
            .createdOn(LocalDateTime.now())
            .attachments(wrapElements(
                Document.builder().documentFileName("a").build(),
                Document.builder().documentFileName("b").build()))
            .build();

        CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId,
                                                      List.of(CaseRole.RESPONDENTSOLICITORTWO.toString()));

        List<Document> documents = unwrapElements(caseMessage.getAttachments());

        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldThrowError_whenUserHasUnsupportedRole() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3")
            .toBuilder()
            .createdOn(LocalDateTime.now())
            .attachments(wrapElements(
                Document.builder().documentFileName("a").build(),
                Document.builder().documentFileName("b").build()))
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId,
                                                          List.of("New role"))
        );

        assertEquals("Unsupported case role for query management.", exception.getMessage());
    }

    @Test
    void shouldAssignCategoryIDToAttachments_whenCaseworkerRespondsToApplicant() {
        List<Element<CaseMessage>> queries = buildCaseMessageWithFollowUpQuery();
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1L)
            .qmApplicantSolicitorQueries(CaseQueriesCollection.builder()
                                               .caseMessages(queries)
                                               .build())
            .build();

        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));

        CaseQueriesUtil.assignCategoryIdToCaseworkerAttachments(caseData, queries.get(1).getValue(), assignCategoryId,
                                                                coreCaseUserService,
                                                                "id");

        List<Document> documents = unwrapElements(queries.get(1).getValue().getAttachments());

        assertEquals(DocCategory.CLAIMANT_QUERY_DOCUMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.CLAIMANT_QUERY_DOCUMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldAssignCategoryIDToAttachments_whenCaseworkerRespondsToRespondent1() {
        List<Element<CaseMessage>> queries = buildCaseMessageWithFollowUpQuery();
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1L)
            .qmRespondentSolicitor1Queries(CaseQueriesCollection.builder()
                                               .caseMessages(queries)
                                               .build())
            .build();

        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.toString()));

        CaseQueriesUtil.assignCategoryIdToCaseworkerAttachments(caseData, queries.get(1).getValue(), assignCategoryId,
                                                                coreCaseUserService,
                                                                "id");

        List<Document> documents = unwrapElements(queries.get(1).getValue().getAttachments());

        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldAssignCategoryIDToAttachments_whenCaseworkerRespondsToRespondent2() {
        List<Element<CaseMessage>> queries = buildCaseMessageWithFollowUpQuery();
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1L)
            .qmRespondentSolicitor2Queries(CaseQueriesCollection.builder()
                                               .caseMessages(queries)
                                               .build())
            .build();

        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.toString()));

        CaseQueriesUtil.assignCategoryIdToCaseworkerAttachments(caseData, queries.get(1).getValue(), assignCategoryId,
                                                      coreCaseUserService,
                                                      "id");

        List<Document> documents = unwrapElements(queries.get(1).getValue().getAttachments());

        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldThrowError_whenParentUserHasUnsupportedRole() {
        List<Element<CaseMessage>> queries = buildCaseMessageWithFollowUpQuery();
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1L)
            .qmRespondentSolicitor2Queries(CaseQueriesCollection.builder()
                                               .caseMessages(queries)
                                               .build())
            .build();

        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of("new role"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            CaseQueriesUtil.assignCategoryIdToCaseworkerAttachments(caseData, queries.get(1).getValue(), assignCategoryId,
                                                          coreCaseUserService,
                                                          "id")
        );

        assertEquals("Unsupported case role for query management.", exception.getMessage());
    }

    @Test
    void shouldReturnNullWhenNoQueries() {
        CaseData caseData = CaseData.builder().build();
        CaseMessage latestQuery = CaseQueriesUtil.getLatestQuery(caseData);

        assertThat(latestQuery).isNull();
    }

    @Test
    void shouldReturnLatestQuery_whenApplicantRaisedLastQuery() {
        LocalDateTime now = LocalDateTime.now();
        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(CaseQueriesCollection.builder()
                                             .caseMessages(wrapElements(
                                                 buildCaseMessageAt("1", "a", now),
                                                 buildCaseMessageAt("2", "b", now.minusDays(1))))
                                             .build())
            .qmRespondentSolicitor1Queries(CaseQueriesCollection.builder()
                                             .caseMessages(wrapElements(
                                                 buildCaseMessageAt("3", "c", now.minusDays(2)),
                                                 buildCaseMessageAt("4", "d", now.minusDays(1))))
                                             .build())
            .build();
        CaseMessage latestQuery = CaseQueriesUtil.getLatestQuery(caseData);

        assertThat(latestQuery).isEqualTo(buildCaseMessageAt("1", "a", now));
    }

    @Test
    void shouldReturnLatestQuery_whenRespondent1RaisedLastQuery() {
        LocalDateTime now = LocalDateTime.now();
        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(CaseQueriesCollection.builder()
                                             .caseMessages(wrapElements(
                                                 buildCaseMessageAt("1", "a", now.minusDays(2)),
                                                 buildCaseMessageAt("2", "b", now.minusDays(1))))
                                             .build())
            .qmRespondentSolicitor1Queries(CaseQueriesCollection.builder()
                                               .caseMessages(wrapElements(
                                                   buildCaseMessageAt("3", "c", now),
                                                   buildCaseMessageAt("4", "d", now.minusDays(1))))
                                               .build())
            .build();
        CaseMessage latestQuery = CaseQueriesUtil.getLatestQuery(caseData);

        assertThat(latestQuery).isEqualTo(buildCaseMessageAt("3", "c", now));
    }

    @Test
    void shouldReturnLatestQuery_whenRespondent2RaisedLastQuery() {
        LocalDateTime now = LocalDateTime.now();
        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(CaseQueriesCollection.builder()
                                             .caseMessages(wrapElements(
                                                 buildCaseMessageAt("1", "a", now.minusDays(2)),
                                                 buildCaseMessageAt("2", "b", now.minusDays(1))))
                                             .build())
            .qmRespondentSolicitor1Queries(CaseQueriesCollection.builder()
                                               .caseMessages(wrapElements(
                                                   buildCaseMessageAt("3", "c", now.minusDays(1)),
                                                   buildCaseMessageAt("4", "d", now.minusDays(1))))
                                               .build())
            .qmRespondentSolicitor2Queries(CaseQueriesCollection.builder()
                                               .caseMessages(wrapElements(
                                                   buildCaseMessageAt("5", "e", now.minusDays(1)),
                                                   buildCaseMessageAt("6", "f", now)))
                                               .build())
            .build();
        CaseMessage latestQuery = CaseQueriesUtil.getLatestQuery(caseData);

        assertThat(latestQuery).isEqualTo(buildCaseMessageAt("6", "f", now));
    }

    private CaseMessage buildCaseMessageAt(String id, String subject, LocalDateTime createdDate) {
        return CaseMessage.builder()
            .id(id)
            .subject(subject)
            .name("John Doe")
            .body("Sample body text")
            .attachments(List.of())
            .isHearingRelated(NO)
            .hearingDate(LocalDate.now())
            .createdOn(createdDate)
            .createdBy("System")
            .build();
    }

    private CaseMessage buildCaseMessage(String id, String subject) {
        return CaseMessage.builder()
            .id(id)
            .subject(subject)
            .name("John Doe")
            .body("Sample body text")
            .attachments(List.of())
            .isHearingRelated(NO)
            .hearingDate(LocalDate.now())
            .createdOn(LocalDateTime.now())
            .createdBy("System")
            .build();
    }

    private List<Element<CaseMessage>> buildCaseMessageWithFollowUpQuery() {
        return wrapElements(
            CaseMessage.builder()
                .id("id")
                .subject("subject")
                .name("John Doe")
                .body("Sample body text")
                .attachments(wrapElements(
                    Document.builder().documentFileName("a").build(),
                    Document.builder().documentFileName("b").build()))
                .isHearingRelated(NO)
                .hearingDate(LocalDate.now())
                .createdOn(LocalDateTime.now())
                .createdBy("System")
                .build(),
            CaseMessage.builder()
                .id("follow-up-id")
                .subject("subject")
                .name("John Doe")
                .body("Sample body text")
                .attachments(wrapElements(
                    Document.builder().documentFileName("c").build(),
                    Document.builder().documentFileName("d").build()))
                .isHearingRelated(NO)
                .hearingDate(LocalDate.now())
                .createdOn(LocalDateTime.now())
                .createdBy("System")
                .parentId("id")
                .build());
    }
}
