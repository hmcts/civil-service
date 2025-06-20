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
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.QueryCollectionType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.model.querymanagement.LatestQuery;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.QueryCollectionType.APPLICANT_SOLICITOR_QUERIES;
import static uk.gov.hmcts.reform.civil.enums.QueryCollectionType.RESPONDENT_SOLICITOR_ONE_QUERIES;
import static uk.gov.hmcts.reform.civil.enums.QueryCollectionType.RESPONDENT_SOLICITOR_TWO_QUERIES;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class CaseQueriesUtilTest {

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @InjectMocks
    private AssignCategoryId assignCategoryId;

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
        OffsetDateTime createdOn = OffsetDateTime.now();
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
            .createdOn(OffsetDateTime.now())
            .attachments(wrapElements(
                Document.builder().documentFileName("a").build(),
                Document.builder().documentFileName("b").build()))
            .build();

        CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId,
                                                      List.of(CaseRole.APPLICANTSOLICITORONE.toString()));

        List<Document> documents = unwrapElements(caseMessage.getAttachments());

        assertEquals(DocCategory.CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldAssignCategoryIDToAttachments_whenDefendantUploadsAttachment() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3")
            .toBuilder()
            .createdOn(OffsetDateTime.now())
            .attachments(wrapElements(
                Document.builder().documentFileName("a").build(),
                Document.builder().documentFileName("b").build()
            ))
            .build();

        CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId,
                                                      List.of(CaseRole.DEFENDANT.toString())
        );

        List<Document> documents = unwrapElements(caseMessage.getAttachments());

        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldAssignCategoryIDToAttachments_whenClaimantUploadsAttachment() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3")
            .toBuilder()
            .createdOn(OffsetDateTime.now())
            .attachments(wrapElements(
                Document.builder().documentFileName("a").build(),
                Document.builder().documentFileName("b").build()
            ))
            .build();

        CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId,
                                                      List.of(CaseRole.CLAIMANT.toString())
        );

        List<Document> documents = unwrapElements(caseMessage.getAttachments());

        assertEquals(DocCategory.CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldAssignCategoryIDToAttachments_whenRespondent1UploadsAttachment() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3")
            .toBuilder()
            .createdOn(OffsetDateTime.now())
            .attachments(wrapElements(
                Document.builder().documentFileName("a").build(),
                Document.builder().documentFileName("b").build()))
            .build();

        CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId,
                                                      List.of(CaseRole.RESPONDENTSOLICITORONE.toString()));

        List<Document> documents = unwrapElements(caseMessage.getAttachments());

        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldAssignCategoryIDToAttachments_whenRespondent2UploadsAttachment() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3")
            .toBuilder()
            .createdOn(OffsetDateTime.now())
            .attachments(wrapElements(
                Document.builder().documentFileName("a").build(),
                Document.builder().documentFileName("b").build()))
            .build();

        CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId,
                                                      List.of(CaseRole.RESPONDENTSOLICITORTWO.toString()));

        List<Document> documents = unwrapElements(caseMessage.getAttachments());

        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldThrowError_whenUserHasUnsupportedRole() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3")
            .toBuilder()
            .createdOn(OffsetDateTime.now())
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

        CaseQueriesUtil.assignCategoryIdToCaseworkerAttachments(caseData, queries.get(1).getValue(), assignCategoryId, false);

        List<Document> documents = unwrapElements(queries.get(1).getValue().getAttachments());

        assertEquals(DocCategory.CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(1).getCategoryID());
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

        CaseQueriesUtil.assignCategoryIdToCaseworkerAttachments(caseData, queries.get(1).getValue(), assignCategoryId, false);

        List<Document> documents = unwrapElements(queries.get(1).getValue().getAttachments());

        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(1).getCategoryID());
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

        CaseQueriesUtil.assignCategoryIdToCaseworkerAttachments(caseData, queries.get(1).getValue(), assignCategoryId, false);

        List<Document> documents = unwrapElements(queries.get(1).getValue().getAttachments());

        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(1).getCategoryID());
    }
    @Test
    void shouldAssignCategoryIDToAttachments_whenCaseworker_QmLip_isOn() {
        List<Element<CaseMessage>> queries = buildCaseMessageWithFollowUpQuery();
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1L)
            .qmRespondentSolicitor2Queries(CaseQueriesCollection.builder()
                                               .caseMessages(queries)
                                               .build())
            .build();

        CaseQueriesUtil.assignCategoryIdToCaseworkerAttachments(caseData, queries.get(1).getValue(), assignCategoryId, true);

        List<Document> documents = unwrapElements(queries.get(1).getValue().getAttachments());

        assertEquals(DocCategory.CASEWORKER_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.CASEWORKER_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldReturnNull_whenNoQueriesExist() {
        CaseData caseData = CaseData.builder().build();

        CaseMessage latestQuery = CaseQueriesUtil.getLatestQuery(caseData);

        assertThat(latestQuery).isNull();
    }

    @Test
    void shouldReturnQuery_whenApplicantRaisesLastQuery() {
        CaseQueriesCollection applicantQuery = CaseQueriesCollection.builder()
            .partyName("John Doe")
            .roleOnCase("applicant-solicitor")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .createdOn(OffsetDateTime.now())
                                           .build(),
                                       CaseMessage.builder()
                                           .createdOn(OffsetDateTime.now().minusDays(1))
                                           .build()))
            .build();

        CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
            .partyName("John Smith")
            .roleOnCase("respondent-solicitor-1")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .createdOn(OffsetDateTime.now().minusDays(2))
                                           .build()))
            .build();

        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(applicantQuery)
            .qmRespondentSolicitor1Queries(respondent1Query)
            .build();

        CaseMessage latestQuery = CaseQueriesUtil.getLatestQuery(caseData);

        assertThat(latestQuery).isEqualTo(applicantQuery.getCaseMessages().get(0).getValue());
    }

    @Test
    void shouldReturnQuery_whenRespondent1RaisesLastQuery() {
        CaseQueriesCollection applicantQuery = CaseQueriesCollection.builder()
            .partyName("John Doe")
            .roleOnCase("applicant-solicitor")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .createdOn(OffsetDateTime.now().minusDays(2))
                                           .build(),
                                       CaseMessage.builder()
                                           .createdOn(OffsetDateTime.now().minusDays(1))
                                           .build()))
            .build();

        CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
            .partyName("John Smith")
            .roleOnCase("respondent-solicitor-1")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .createdOn(OffsetDateTime.now())
                                           .build(),
                                       CaseMessage.builder()
                                           .createdOn(OffsetDateTime.now().minusMinutes(1))
                                           .build()))
            .build();

        CaseQueriesCollection respondent2Query = CaseQueriesCollection.builder()
            .partyName("Jane Doe")
            .roleOnCase("respondent-solicitor-2")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .createdOn(OffsetDateTime.now().minusMinutes(2))
                                           .build()))
            .build();

        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(applicantQuery)
            .qmRespondentSolicitor1Queries(respondent1Query)
            .qmRespondentSolicitor2Queries(respondent2Query)
            .build();

        CaseMessage latestQuery = CaseQueriesUtil.getLatestQuery(caseData);

        assertThat(latestQuery).isEqualTo(respondent1Query.getCaseMessages().get(0).getValue());
    }

    @Test
    void shouldReturnQuery_whenRespondent2RaisesLastQuery() {
        CaseQueriesCollection applicantQuery = CaseQueriesCollection.builder()
            .partyName("John Doe")
            .roleOnCase("applicant-solicitor")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .createdOn(OffsetDateTime.now().minusDays(2))
                                           .build(),
                                       CaseMessage.builder()
                                           .createdOn(OffsetDateTime.now().minusDays(1))
                                           .build()))
            .build();

        CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
            .partyName("John Smith")
            .roleOnCase("respondent-solicitor-1")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .createdOn(OffsetDateTime.now().minusHours(2))
                                           .build()))
            .build();

        CaseQueriesCollection respondent2Query = CaseQueriesCollection.builder()
            .partyName("Jane Doe")
            .roleOnCase("respondent-solicitor-2")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .createdOn(OffsetDateTime.now().minusMinutes(2))
                                           .build(),
                                       CaseMessage.builder()
                                           .createdOn(OffsetDateTime.now())
                                           .build()))
            .build();

        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(applicantQuery)
            .qmRespondentSolicitor1Queries(respondent1Query)
            .qmRespondentSolicitor2Queries(respondent2Query)
            .build();

        CaseMessage latestQuery = CaseQueriesUtil.getLatestQuery(caseData);

        assertThat(latestQuery).isEqualTo(respondent2Query.getCaseMessages().get(1).getValue());
    }

    @Test
    void shouldReturnNull_whenNoQueryFoundForId() {
        CaseData caseData = CaseData.builder().build();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            CaseQueriesUtil.getQueryById(caseData, "1")
        );
        assertEquals("No query found for queryId 1", exception.getMessage());
    }

    @Test
    void shouldReturnApplicantQuery_whenQueryFoundForId() {
        CaseQueriesCollection applicantQuery = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("1")
                                           .build(),
                                       CaseMessage.builder()
                                           .id("4")
                                           .build()))
            .build();

        CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("2")
                                           .build()))
            .build();

        CaseQueriesCollection respondent2Query = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("3")
                                           .build()))
            .build();
        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(applicantQuery)
            .qmRespondentSolicitor1Queries(respondent1Query)
            .qmRespondentSolicitor2Queries(respondent2Query).build();

        CaseMessage latestQuery = CaseQueriesUtil.getQueryById(caseData, "1");

        assertThat(latestQuery).isEqualTo(applicantQuery.getCaseMessages().get(0).getValue());
    }

    @Test
    void shouldReturnRespondent1Query_whenQueryFoundForId() {
        CaseQueriesCollection applicantQuery = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("1")
                                           .build(),
                                       CaseMessage.builder()
                                           .id("4")
                                           .build()))
            .build();

        CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("2")
                                           .build()))
            .build();

        CaseQueriesCollection respondent2Query = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("3")
                                           .build()))
            .build();
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1L)
            .qmApplicantSolicitorQueries(applicantQuery)
            .qmRespondentSolicitor1Queries(respondent1Query)
            .qmRespondentSolicitor2Queries(respondent2Query).build();

        CaseMessage latestQuery = CaseQueriesUtil.getQueryById(caseData, "2");

        assertThat(latestQuery).isEqualTo(respondent1Query.getCaseMessages().get(0).getValue());
    }

    @Test
    void shouldReturnRespondent2Query_whenQueryFoundForId() {
        CaseQueriesCollection applicantQuery = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("1")
                                           .build()))
            .build();

        CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("2")
                                           .build()))
            .build();

        CaseQueriesCollection respondent2Query = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("3")
                                           .build(),
                                       CaseMessage.builder()
                                           .id("4")
                                           .build()))
            .build();
        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(applicantQuery)
            .qmRespondentSolicitor1Queries(respondent1Query)
            .qmRespondentSolicitor2Queries(respondent2Query).build();

        CaseMessage latestQuery = CaseQueriesUtil.getQueryById(caseData, "4");

        assertThat(latestQuery).isEqualTo(respondent2Query.getCaseMessages().get(1).getValue());
    }

    @Test
    void shouldReturnApplicantRole_whenQueryFoundForId() {
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));
        CaseQueriesCollection applicantQuery = CaseQueriesCollection.builder()
            .roleOnCase("applicant-solicitor")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("1")
                                           .build()))
            .build();

        CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
            .roleOnCase("respondent-solicitor-1")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("2")
                                           .build()))
            .build();

        CaseQueriesCollection respondent2Query = CaseQueriesCollection.builder()
            .roleOnCase("respondent-solicitor-2")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("3")
                                           .build()))
            .build();
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1L)
            .qmApplicantSolicitorQueries(applicantQuery)
            .qmRespondentSolicitor1Queries(respondent1Query)
            .qmRespondentSolicitor2Queries(respondent2Query).build();

        List<String> roles = CaseQueriesUtil.getUserRoleForQuery(caseData, coreCaseUserService, "1");

        assertThat(roles).containsOnly(CaseRole.APPLICANTSOLICITORONE.toString());
    }

    @Test
    void shouldReturnRespondent1Role_whenQueryFoundForId() {
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.toString()));
        CaseQueriesCollection applicantQuery = CaseQueriesCollection.builder()
            .roleOnCase("applicant-solicitor")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("1")
                                           .build()))
            .build();

        CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
            .roleOnCase("respondent-solicitor-1")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("2")
                                           .build()))
            .build();

        CaseQueriesCollection respondent2Query = CaseQueriesCollection.builder()
            .roleOnCase("respondent-solicitor-2")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("3")
                                           .build()))
            .build();
        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(applicantQuery)
            .ccdCaseReference(1L)
            .qmRespondentSolicitor1Queries(respondent1Query)
            .qmRespondentSolicitor2Queries(respondent2Query).build();

        List<String> roles = CaseQueriesUtil.getUserRoleForQuery(caseData, coreCaseUserService, "2");

        assertThat(roles).containsOnly(CaseRole.RESPONDENTSOLICITORONE.toString());
    }

    @Test
    void shouldReturnRespondent2Role_whenQueryFoundForId() {
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.toString()));
        CaseQueriesCollection applicantQuery = CaseQueriesCollection.builder()
            .roleOnCase("applicant-solicitor")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("1")
                                           .build()))
            .build();

        CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
            .roleOnCase("respondent-solicitor-1")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("2")
                                           .build()))
            .build();

        CaseQueriesCollection respondent2Query = CaseQueriesCollection.builder()
            .roleOnCase("respondent-solicitor-2")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("3")
                                           .build()))
            .build();
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1L)
            .qmApplicantSolicitorQueries(applicantQuery)
            .qmRespondentSolicitor1Queries(respondent1Query)
            .qmRespondentSolicitor2Queries(respondent2Query).build();

        List<String> roles = CaseQueriesUtil.getUserRoleForQuery(caseData, coreCaseUserService, "3");

        assertThat(roles).containsOnly(CaseRole.RESPONDENTSOLICITORTWO.toString());
    }

    @Test
    void shouldUpdateApplicantSolicitorQueries_WhenRoleIsApplicantSolicitor() {
        List<String> roles = List.of("APPLICANTSOLICITORONE");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_ONE;
        CaseData.CaseDataBuilder builder = CaseData.builder()
            .qmApplicantSolicitorQueries(CaseQueriesCollection.builder().partyName("Old Name").build());

        CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, builder);
        CaseData updatedCaseData = builder.build();

        assertEquals("Claimant", updatedCaseData.getQmApplicantSolicitorQueries().getPartyName());
    }

    @Test
    void shouldUpdateRespondentSolicitor1Queries_WhenRoleIsRespondentSolicitor1() {
        List<String> roles = List.of("RESPONDENTSOLICITORONE");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_ONE;
        CaseData.CaseDataBuilder builder = CaseData.builder()
            .qmRespondentSolicitor1Queries(CaseQueriesCollection.builder().partyName("Old Name").build());

        CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, builder);
        CaseData updatedCaseData = builder.build();

        assertEquals("Defendant", updatedCaseData.getQmRespondentSolicitor1Queries().getPartyName());
    }

    @Test
    void shouldUpdateRespondentSolicitor1Queries_WhenRoleIsRespondentSolicitor1_1v2Diff() {
        List<String> roles = List.of("RESPONDENTSOLICITORONE");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
        CaseData.CaseDataBuilder builder = CaseData.builder()
            .qmRespondentSolicitor1Queries(CaseQueriesCollection.builder().partyName("Old Name").build());

        CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, builder);
        CaseData updatedCaseData = builder.build();

        assertEquals("Defendant 1", updatedCaseData.getQmRespondentSolicitor1Queries().getPartyName());
    }

    @Test
    void shouldUpdateRespondentSolicitor2Queries_WhenRoleIsRespondentSolicitor2() {
        List<String> roles = List.of("RESPONDENTSOLICITORTWO");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
        CaseData.CaseDataBuilder builder = CaseData.builder()
            .qmRespondentSolicitor2Queries(CaseQueriesCollection.builder().partyName("Old Name").build());

        CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, builder);
        CaseData updatedCaseData = builder.build();

        assertEquals("Defendant 2", updatedCaseData.getQmRespondentSolicitor2Queries().getPartyName());
    }

    @Test
    void shouldUpdateRespondentSolicitor1Queries_WhenRoleIsRespondentSolicitor1_1v2Same() {
        List<String> roles = List.of("RESPONDENTSOLICITORONE");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
        CaseData.CaseDataBuilder builder = CaseData.builder()
            .qmRespondentSolicitor1Queries(CaseQueriesCollection.builder().partyName("Old Name").build());

        CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, builder);
        CaseData updatedCaseData = builder.build();

        assertEquals("Defendant", updatedCaseData.getQmRespondentSolicitor1Queries().getPartyName());
    }

    @Test
    void shouldThrowException_WhenRoleIsUnsupported() {
        List<String> roles = List.of("UNSUPPORTEDROLE");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_ONE;
        CaseData.CaseDataBuilder builder = CaseData.builder();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, builder)
        );

        assertEquals("Unsupported case role for query management.", exception.getMessage());
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
            .createdOn(OffsetDateTime.now())
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
                .createdOn(OffsetDateTime.now())
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
                .createdOn(OffsetDateTime.now())
                .createdBy("System")
                .parentId("id")
                .build());
    }

    @Test
    void shouldReturnMatchingCollection_WhenMessageExistsInApplicantSolicitorQueries() {
        OffsetDateTime createdOn = OffsetDateTime.now();
        CaseMessage caseMessage = buildCaseMessage("id", "Query 1")
            .toBuilder()
            .createdOn(createdOn)
            .build();

        CaseQueriesCollection applicantSolicitorQueries = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(caseMessage))
            .build();

        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(applicantSolicitorQueries)
            .build();

        CaseQueriesCollection result = CaseQueriesUtil.getCollectionByMessage(caseData, caseMessage);

        assertEquals(applicantSolicitorQueries, result);
    }

    @Test
    void shouldReturnMatchingCollection_WhenMessageExistsInRespondentSolicitor1Queries() {
        OffsetDateTime createdOn = OffsetDateTime.now();
        CaseMessage caseMessage = buildCaseMessage("id", "Query 2")
            .toBuilder()
            .createdOn(createdOn)
            .build();

        CaseQueriesCollection respondentSolicitor1Queries = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(caseMessage))
            .build();

        CaseData caseData = CaseData.builder()
            .qmRespondentSolicitor1Queries(respondentSolicitor1Queries)
            .build();

        CaseQueriesCollection result = CaseQueriesUtil.getCollectionByMessage(caseData, caseMessage);

        assertEquals(respondentSolicitor1Queries, result);
    }

    @Test
    void shouldReturnMatchingCollection_WhenMessageExistsInRespondentSolicitor2Queries() {
        OffsetDateTime createdOn = OffsetDateTime.now();
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3")
            .toBuilder()
            .createdOn(createdOn)
            .build();

        CaseQueriesCollection respondentSolicitor2Queries = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(caseMessage))
            .build();

        CaseData caseData = CaseData.builder()
            .qmRespondentSolicitor2Queries(respondentSolicitor2Queries)
            .build();

        CaseQueriesCollection result = CaseQueriesUtil.getCollectionByMessage(caseData, caseMessage);

        assertEquals(respondentSolicitor2Queries, result);
    }

    @Test
    void shouldReturnNull_WhenMessageDoesNotExistInAnyCollection() {
        OffsetDateTime createdOn = OffsetDateTime.now();
        CaseMessage caseMessage = buildCaseMessage("id", "Query 4")
            .toBuilder()
            .createdOn(createdOn)
            .build();

        CaseData caseData = CaseData.builder().build();

        CaseQueriesCollection result = CaseQueriesUtil.getCollectionByMessage(caseData, caseMessage);

        assertNull(result);
    }

    @Test
    void shouldReturnNull_WhenCaseMessagesAreNull() {
        OffsetDateTime createdOn = OffsetDateTime.now();
        CaseMessage caseMessage = buildCaseMessage("id", "Query 5")
            .toBuilder()
            .createdOn(createdOn)
            .build();

        CaseQueriesCollection emptyCollection = CaseQueriesCollection.builder().build();

        CaseData caseData = CaseData.builder()
            .build();

        CaseQueriesCollection result = CaseQueriesUtil.getCollectionByMessage(caseData, caseMessage);

        assertNull(result);
    }

    @Test
    void shouldReturnClaimantQueryDocuments_whenCollectionTypeIsApplicantSolicitorQueries() {
        DocCategory result = CaseQueriesUtil.getQueryDocumentCategory(APPLICANT_SOLICITOR_QUERIES);

        assertEquals(DocCategory.CLAIMANT_QUERY_DOCUMENTS, result);
    }

    @Test
    void shouldReturnDefendantQueryDocuments_whenCollectionTypeIsRespondentSolicitorOneQueries() {
        DocCategory result = CaseQueriesUtil.getQueryDocumentCategory(RESPONDENT_SOLICITOR_ONE_QUERIES);

        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENTS, result);
    }

    @Test
    void shouldReturnDefendantQueryDocuments_whenCollectionTypeIsRespondentSolicitorTwoQueries() {
        DocCategory result = CaseQueriesUtil.getQueryDocumentCategory(RESPONDENT_SOLICITOR_TWO_QUERIES);

        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENTS, result);
    }

    @Test
    void shouldReturnApplicantSolicitorQueries_whenQueriesMatchApplicantSolicitorCollection() {
        CaseQueriesCollection applicantQueries = CaseQueriesCollection.builder().partyName("app 1").build();
        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(applicantQueries)
            .build();

        QueryCollectionType result = CaseQueriesUtil.getCollectionType(applicantQueries, caseData);

        assertEquals(APPLICANT_SOLICITOR_QUERIES, result);
    }

    @Test
    void shouldReturnRespondentSolicitorOneQueries_whenQueriesMatchRespondentSolicitorOneCollection() {
        CaseQueriesCollection respondent1Queries = CaseQueriesCollection.builder().partyName("res 1").build();
        CaseData caseData = CaseData.builder()
            .qmRespondentSolicitor1Queries(respondent1Queries)
            .build();

        QueryCollectionType result = CaseQueriesUtil.getCollectionType(respondent1Queries, caseData);

        assertEquals(RESPONDENT_SOLICITOR_ONE_QUERIES, result);
    }

    @Test
    void shouldReturnRespondentSolicitorTwoQueries_whenQueriesMatchRespondentSolicitorTwoCollection() {
        CaseQueriesCollection respondent2Queries = CaseQueriesCollection.builder().partyName("res 2").build();
        CaseData caseData = CaseData.builder()
            .qmRespondentSolicitor2Queries(respondent2Queries)
            .build();

        QueryCollectionType result = CaseQueriesUtil.getCollectionType(respondent2Queries, caseData);

        assertEquals(RESPONDENT_SOLICITOR_TWO_QUERIES, result);
    }

    @Test
    void shouldReturnNull_whenQueriesDoNotMatchAnyCollection() {
        CaseQueriesCollection someQueries = CaseQueriesCollection.builder().build();
        CaseData caseData = CaseData.builder().build();

        QueryCollectionType result = CaseQueriesUtil.getCollectionType(someQueries, caseData);

        assertNull(result);
    }

}
