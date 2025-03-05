package uk.gov.hmcts.reform.civil.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
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
    void shouldAssignCategoryIDToAttachments() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3")
            .toBuilder()
            .createdOn(LocalDateTime.now())
            .attachments(wrapElements(
                Document.builder().documentFileName("a").build(),
                Document.builder().documentFileName("b").build()))
            .build();

        CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId);

        List<Document> documents = unwrapElements(caseMessage.getAttachments());

        assertEquals(DocCategory.QUERY_DOCUMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.QUERY_DOCUMENTS.getValue(), documents.get(1).getCategoryID());
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
                                           .createdOn(LocalDateTime.now())
                                           .build(),
                                       CaseMessage.builder()
                                           .createdOn(LocalDateTime.now().minusDays(1))
                                           .build()))
            .build();

        CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
            .partyName("John Smith")
            .roleOnCase("respondent-solicitor-1")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .createdOn(LocalDateTime.now().minusDays(2))
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
                                           .createdOn(LocalDateTime.now().minusDays(2))
                                           .build(),
                                       CaseMessage.builder()
                                           .createdOn(LocalDateTime.now().minusDays(1))
                                           .build()))
            .build();

        CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
            .partyName("John Smith")
            .roleOnCase("respondent-solicitor-1")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .createdOn(LocalDateTime.now())
                                           .build(),
                                       CaseMessage.builder()
                                           .createdOn(LocalDateTime.now().minusMinutes(1))
                                           .build()))
            .build();

        CaseQueriesCollection respondent2Query = CaseQueriesCollection.builder()
            .partyName("Jane Doe")
            .roleOnCase("respondent-solicitor-2")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .createdOn(LocalDateTime.now().minusMinutes(2))
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
                                           .createdOn(LocalDateTime.now().minusDays(2))
                                           .build(),
                                       CaseMessage.builder()
                                           .createdOn(LocalDateTime.now().minusDays(1))
                                           .build()))
            .build();

        CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
            .partyName("John Smith")
            .roleOnCase("respondent-solicitor-1")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .createdOn(LocalDateTime.now().minusHours(2))
                                           .build()))
            .build();

        CaseQueriesCollection respondent2Query = CaseQueriesCollection.builder()
            .partyName("Jane Doe")
            .roleOnCase("respondent-solicitor-2")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .createdOn(LocalDateTime.now().minusMinutes(2))
                                           .build(),
                                       CaseMessage.builder()
                                           .createdOn(LocalDateTime.now())
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
}
