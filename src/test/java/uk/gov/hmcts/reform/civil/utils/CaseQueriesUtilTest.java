package uk.gov.hmcts.reform.civil.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.model.querymanagement.LatestQuery;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class CaseQueriesUtilTest {

    @Mock
    private CoreCaseUserService coreCaseUserService;

    @InjectMocks
    private AssignCategoryId assignCategoryId;

    @Test
    void shouldReturnNull_WhenCaseMessageIsNull() {
        LatestQuery result = CaseQueriesUtil.buildLatestQuery(null);
        assertNull(result);
    }

    @Test
    void shouldBuildLatestQueryFromCaseMessage() {
        OffsetDateTime createdOn = OffsetDateTime.now();
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3");
        caseMessage.setCreatedOn(createdOn);

        LatestQuery result = CaseQueriesUtil.buildLatestQuery(caseMessage);
        assertEquals("id", result.getQueryId());
        assertNull(result.getIsWelsh());
    }

    @ParameterizedTest
    @CsvSource(value = {
        "CLAIMANT, WELSH, ENGLISH, YES",
        "CLAIMANT, BOTH, ENGLISH, YES",
        "CLAIMANT, ENGLISH, WELSH, NO",
        "CLAIMANT, ENGLISH, ENGLISH, NO",
        "DEFENDANT, ENGLISH, WELSH, YES",
        "DEFENDANT, ENGLISH, BOTH, YES",
        "DEFENDANT, WELSH, ENGLISH, NO",
        "DEFENDANT, ENGLISH, ENGLISH, NO",
        "OTHER_ROLE, NULL, WELSH, NO",
        "OTHER_ROLE, WELSH, NULL, NO",
        "OTHER_ROLE, NULL, NULL, NO"
    }, nullValues = "NULL")
    void shouldBuildLatestQueryFromCaseMessage_withLanguagePreference(String caseRole, String claimantLanguage, String defendantLanguage, YesOrNo expected) {
        OffsetDateTime createdOn = OffsetDateTime.now();
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setClaimantBilingualLanguagePreference(claimantLanguage);
        caseData.setCaseDataLiP(respondentResponseWithLanguagePreference(defendantLanguage));
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3");
        caseMessage.setCreatedOn(createdOn);

        List<String> roles = List.of(caseRole);
        LatestQuery result = CaseQueriesUtil.buildLatestQuery(caseMessage, caseData, roles);

        assertEquals("id", result.getQueryId());
        assertEquals(expected, result.getIsWelsh());
    }

    @Test
    void shouldAssignCategoryIDToAttachments_whenApplicantUploadsAttachment() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3");
        caseMessage.setCreatedOn(OffsetDateTime.now());
        Document doc1 = new Document();
        doc1.setDocumentFileName("a");
        Document doc2 = new Document();
        doc2.setDocumentFileName("b");
        caseMessage.setAttachments(wrapElements(doc1, doc2));

        CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId,
                                                      List.of(CaseRole.APPLICANTSOLICITORONE.toString()));

        List<Document> documents = unwrapElements(caseMessage.getAttachments());

        assertEquals(DocCategory.CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldAssignCategoryIDToAttachments_whenDefendantUploadsAttachment() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3");
        caseMessage.setCreatedOn(OffsetDateTime.now());
        Document doc1 = new Document();
        doc1.setDocumentFileName("a");
        Document doc2 = new Document();
        doc2.setDocumentFileName("b");
        caseMessage.setAttachments(wrapElements(doc1, doc2));

        CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId,
                                                      List.of(CaseRole.DEFENDANT.toString())
        );

        List<Document> documents = unwrapElements(caseMessage.getAttachments());

        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldAssignCategoryIDToAttachments_whenClaimantUploadsAttachment() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3");
        caseMessage.setCreatedOn(OffsetDateTime.now());
        Document doc1 = new Document();
        doc1.setDocumentFileName("a");
        Document doc2 = new Document();
        doc2.setDocumentFileName("b");
        caseMessage.setAttachments(wrapElements(doc1, doc2));

        CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId,
                                                      List.of(CaseRole.CLAIMANT.toString())
        );

        List<Document> documents = unwrapElements(caseMessage.getAttachments());

        assertEquals(DocCategory.CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.CLAIMANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldAssignCategoryIDToAttachments_whenRespondent1UploadsAttachment() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3");
        caseMessage.setCreatedOn(OffsetDateTime.now());
        Document doc1 = new Document();
        doc1.setDocumentFileName("a");
        Document doc2 = new Document();
        doc2.setDocumentFileName("b");
        caseMessage.setAttachments(wrapElements(doc1, doc2));

        CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId,
                                                      List.of(CaseRole.RESPONDENTSOLICITORONE.toString()));

        List<Document> documents = unwrapElements(caseMessage.getAttachments());

        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldAssignCategoryIDToAttachments_whenRespondent2UploadsAttachment() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3");
        caseMessage.setCreatedOn(OffsetDateTime.now());
        Document doc1 = new Document();
        doc1.setDocumentFileName("a");
        Document doc2 = new Document();
        doc2.setDocumentFileName("b");
        caseMessage.setAttachments(wrapElements(doc1, doc2));

        CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId,
                                                      List.of(CaseRole.RESPONDENTSOLICITORTWO.toString()));

        List<Document> documents = unwrapElements(caseMessage.getAttachments());

        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(0).getCategoryID());
        assertEquals(DocCategory.DEFENDANT_QUERY_DOCUMENT_ATTACHMENTS.getValue(), documents.get(1).getCategoryID());
    }

    @Test
    void shouldThrowError_whenUserHasUnsupportedRole() {
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3");
        caseMessage.setCreatedOn(OffsetDateTime.now());
        Document doc1 = new Document();
        doc1.setDocumentFileName("a");
        Document doc2 = new Document();
        doc2.setDocumentFileName("b");
        caseMessage.setAttachments(wrapElements(doc1, doc2));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            CaseQueriesUtil.assignCategoryIdToAttachments(caseMessage, assignCategoryId,
                                                          List.of("New role"))
        );

        assertEquals("Unsupported case role for query management.", exception.getMessage());
    }

    @Test
    void shouldReturnNull_whenNoQueryFoundForId() {
        CaseData caseData = CaseDataBuilder.builder().build();
        CaseQueriesCollection queries = new CaseQueriesCollection();
        queries.setCaseMessages(List.of());
        caseData.setQueries(queries);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            CaseQueriesUtil.getQueryById(caseData, "1")
        );

        assertEquals("No query found for queryId 1", exception.getMessage());
    }

    @Test
    void shouldReturnQuery_whenQueryFoundForId() {
        CaseMessage msg1 = new CaseMessage();
        msg1.setId("1");
        CaseMessage msg2 = new CaseMessage();
        msg2.setId("2");
        CaseMessage msg3 = new CaseMessage();
        msg3.setId("3");
        CaseQueriesCollection queries = new CaseQueriesCollection();
        queries.setCaseMessages(wrapElements(msg1, msg2, msg3));
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1L);
        caseData.setQueries(queries);

        CaseMessage latestQuery = CaseQueriesUtil.getQueryById(caseData, "2");

        assertThat(latestQuery).isEqualTo(queries.getCaseMessages().get(1).getValue());
    }

    @Test
    void shouldReturnApplicantRole_whenQueryFoundForId() {
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));
        CaseMessage applicantMsg = new CaseMessage();
        applicantMsg.setId("1");
        CaseQueriesCollection applicantQuery = new CaseQueriesCollection();
        applicantQuery.setRoleOnCase("applicant-solicitor");
        applicantQuery.setCaseMessages(wrapElements(applicantMsg));

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1L);
        caseData.setQueries(applicantQuery);

        List<String> roles = CaseQueriesUtil.getUserRoleForQuery(caseData, coreCaseUserService, "1");

        assertThat(roles).containsOnly(CaseRole.APPLICANTSOLICITORONE.toString());
    }

    @Test
    void shouldReturnRespondent1Role_whenQueryFoundForId() {
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.toString()));

        CaseMessage respondent1Msg = new CaseMessage();
        respondent1Msg.setId("2");
        CaseQueriesCollection respondent1Query = new CaseQueriesCollection();
        respondent1Query.setRoleOnCase("respondent-solicitor-1");
        respondent1Query.setCaseMessages(wrapElements(respondent1Msg));

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1L);
        caseData.setQueries(respondent1Query);

        List<String> roles = CaseQueriesUtil.getUserRoleForQuery(caseData, coreCaseUserService, "2");

        assertThat(roles).containsOnly(CaseRole.RESPONDENTSOLICITORONE.toString());
    }

    @Test
    void shouldReturnRespondent2Role_whenQueryFoundForId() {
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.toString()));

        CaseMessage respondent2Msg = new CaseMessage();
        respondent2Msg.setId("3");
        CaseQueriesCollection respondent2Query = new CaseQueriesCollection();
        respondent2Query.setRoleOnCase("respondent-solicitor-2");
        respondent2Query.setCaseMessages(wrapElements(respondent2Msg));
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1L);
        caseData.setQueries(respondent2Query);

        List<String> roles = CaseQueriesUtil.getUserRoleForQuery(caseData, coreCaseUserService, "3");

        assertThat(roles).containsOnly(CaseRole.RESPONDENTSOLICITORTWO.toString());
    }

    @Test
    void shouldUpdateApplicantSolicitorQueries_WhenRoleIsApplicantSolicitor() {
        List<String> roles = List.of("APPLICANTSOLICITORONE");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_ONE;
        CaseData caseData = CaseDataBuilder.builder().build();
        CaseQueriesCollection applicantQueries = new CaseQueriesCollection();
        applicantQueries.setPartyName("Old Name");
        caseData.setQmApplicantSolicitorQueries(applicantQueries);

        CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, caseData);

        assertEquals("Claimant", caseData.getQmApplicantSolicitorQueries().getPartyName());
    }

    @Test
    void shouldUpdateRespondentSolicitor1Queries_WhenRoleIsRespondentSolicitor1() {
        List<String> roles = List.of("RESPONDENTSOLICITORONE");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_ONE;
        CaseData caseData = CaseDataBuilder.builder().build();
        CaseQueriesCollection respondent1Queries = new CaseQueriesCollection();
        respondent1Queries.setPartyName("Old Name");
        caseData.setQmRespondentSolicitor1Queries(respondent1Queries);

        CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, caseData);

        assertEquals("Defendant", caseData.getQmRespondentSolicitor1Queries().getPartyName());
    }

    @Test
    void shouldUpdateRespondentSolicitor1Queries_WhenRoleIsRespondentSolicitor1_1v2Diff() {
        List<String> roles = List.of("RESPONDENTSOLICITORONE");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
        CaseData caseData = CaseDataBuilder.builder().build();
        CaseQueriesCollection respondent1Queries = new CaseQueriesCollection();
        respondent1Queries.setPartyName("Old Name");
        caseData.setQmRespondentSolicitor1Queries(respondent1Queries);

        CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, caseData);

        assertEquals("Defendant 1", caseData.getQmRespondentSolicitor1Queries().getPartyName());
    }

    @Test
    void shouldUpdateRespondentSolicitor2Queries_WhenRoleIsRespondentSolicitor2() {
        List<String> roles = List.of("RESPONDENTSOLICITORTWO");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
        CaseData caseData = CaseDataBuilder.builder().build();
        CaseQueriesCollection respondent2Queries = new CaseQueriesCollection();
        respondent2Queries.setPartyName("Old Name");
        caseData.setQmRespondentSolicitor2Queries(respondent2Queries);

        CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, caseData);

        assertEquals("Defendant 2", caseData.getQmRespondentSolicitor2Queries().getPartyName());
    }

    @Test
    void shouldUpdateRespondentSolicitor1Queries_WhenRoleIsRespondentSolicitor1_1v2Same() {
        List<String> roles = List.of("RESPONDENTSOLICITORONE");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
        CaseData caseData = CaseDataBuilder.builder().build();
        CaseQueriesCollection respondent1Queries = new CaseQueriesCollection();
        respondent1Queries.setPartyName("Old Name");
        caseData.setQmRespondentSolicitor1Queries(respondent1Queries);

        CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, caseData);

        assertEquals("Defendant", caseData.getQmRespondentSolicitor1Queries().getPartyName());
    }

    @Test
    void shouldThrowException_WhenRoleIsUnsupported() {
        List<String> roles = List.of("UNSUPPORTEDROLE");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_ONE;
        CaseData caseData = CaseDataBuilder.builder().build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, caseData)
        );

        assertEquals("Unsupported case role for query management.", exception.getMessage());
    }

    private CaseMessage buildCaseMessage(String id, String subject) {
        CaseMessage caseMessage = new CaseMessage();
        caseMessage.setId(id);
        caseMessage.setSubject(subject);
        caseMessage.setName("John Doe");
        caseMessage.setBody("Sample body text");
        caseMessage.setAttachments(List.of());
        caseMessage.setIsHearingRelated(NO);
        caseMessage.setHearingDate(LocalDate.now());
        caseMessage.setCreatedOn(OffsetDateTime.now());
        caseMessage.setCreatedBy("System");
        return caseMessage;
    }

    @Test
    void shouldMigrateAllQueries_whenOldQueriesExist() {
        CaseMessage appMsg = new CaseMessage();
        appMsg.setId("app1");
        appMsg.setCreatedOn(OffsetDateTime.now());
        List<Element<CaseMessage>> applicantMessages = wrapElements(appMsg);
        CaseMessage res1Msg = new CaseMessage();
        res1Msg.setId("res1");
        res1Msg.setCreatedOn(OffsetDateTime.now().plusHours(1));
        List<Element<CaseMessage>> respondent1Messages = wrapElements(res1Msg);
        CaseQueriesCollection applicantQueries = new CaseQueriesCollection();
        applicantQueries.setCaseMessages(applicantMessages);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setQmApplicantSolicitorQueries(applicantQueries);
        CaseQueriesCollection respondent1Queries = new CaseQueriesCollection();
        respondent1Queries.setCaseMessages(respondent1Messages);
        caseData.setQmRespondentSolicitor1Queries(respondent1Queries);
        CaseQueriesCollection respondent2Queries = new CaseQueriesCollection();
        CaseMessage res2Msg = new CaseMessage();
        res2Msg.setId("res2");
        res2Msg.setCreatedOn(OffsetDateTime.now().plusHours(2));
        List<Element<CaseMessage>> respondent2Messages = wrapElements(res2Msg);
        respondent2Queries.setCaseMessages(respondent2Messages);
        caseData.setQmRespondentSolicitor2Queries(respondent2Queries);

        CaseQueriesUtil.migrateAllQueries(caseData);

        assertThat(caseData.getQueries()).isNotNull();
        assertThat(caseData.getQueries().getPartyName()).isEqualTo("All queries");
        assertThat(caseData.getQueries().getCaseMessages()).hasSize(3);
        assertThat(unwrapElements(caseData.getQueries().getCaseMessages()).stream().map(CaseMessage::getId))
            .containsExactlyInAnyOrder("app1", "res1", "res2");
    }

    @Test
    void shouldNotMigrateQueries_whenNoOldQueriesExist() {
        CaseData caseData = CaseDataBuilder.builder().build();

        CaseQueriesUtil.migrateAllQueries(caseData);

        assertThat(caseData.getQueries()).isNull();
    }

    @Test
    void shouldNotConsumeOriginalException() {
        CaseData caseData = CaseDataBuilder.builder().build();
        CaseMessage caseMsg = new CaseMessage();
        CaseQueriesCollection applicantQueries = new CaseQueriesCollection();
        applicantQueries.setCaseMessages(List.of(element(caseMsg)));
        caseData.setQmApplicantSolicitorQueries(applicantQueries);

        try (MockedStatic<CaseQueriesUtil> mockedStatic = mockStatic(CaseQueriesUtil.class)) {
            NullPointerException expectedException = new NullPointerException("Simulated migration failure");
            mockedStatic.when(() -> CaseQueriesUtil.migrateAllQueries(any(CaseData.class)))
                .thenCallRealMethod();
            mockedStatic.when(() -> CaseQueriesUtil.hasOldQueries(any(CaseData.class)))
                .thenCallRealMethod();
            mockedStatic.when(() -> CaseQueriesUtil.migrateQueries(any(CaseQueriesCollection.class), any(CaseData.class)))
                .thenThrow(expectedException);

            NullPointerException thrownException = assertThrows(NullPointerException.class, () ->
                CaseQueriesUtil.migrateAllQueries(caseData));

            assertThat(thrownException).isEqualTo(expectedException);
        }
    }

    @Test
    void shouldClearOldQueryCollections_whenOldQueriesExist() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setQmApplicantSolicitorQueries(new CaseQueriesCollection());
        caseData.setQmRespondentSolicitor1Queries(new CaseQueriesCollection());
        caseData.setQmRespondentSolicitor2Queries(new CaseQueriesCollection());

        CaseQueriesUtil.clearOldQueryCollections(caseData);

        assertThat(caseData.getQmApplicantSolicitorQueries()).isNull();
        assertThat(caseData.getQmRespondentSolicitor1Queries()).isNull();
        assertThat(caseData.getQmRespondentSolicitor2Queries()).isNull();
    }

    @Test
    void shouldNotClearOldQueryCollections_whenNoOldQueriesExist() {
        CaseData caseData = CaseDataBuilder.builder().build();

        CaseQueriesUtil.clearOldQueryCollections(caseData);

        assertThat(caseData.getQmApplicantSolicitorQueries()).isNull();
        assertThat(caseData.getQmRespondentSolicitor1Queries()).isNull();
        assertThat(caseData.getQmRespondentSolicitor2Queries()).isNull();
    }

    @Test
    void shouldNotError_whenOldQueriesExistWhileLoggingMigrationSuccess() {
        CaseMessage appMsg = new CaseMessage();
        appMsg.setId("app1");
        appMsg.setCreatedOn(OffsetDateTime.now());
        List<Element<CaseMessage>> applicantMessages = wrapElements(appMsg);
        CaseMessage res1Msg = new CaseMessage();
        res1Msg.setId("res1");
        res1Msg.setCreatedOn(OffsetDateTime.now().plusHours(1));
        List<Element<CaseMessage>> respondent1Messages = wrapElements(res1Msg);
        CaseQueriesCollection applicantQueries = new CaseQueriesCollection();
        applicantQueries.setCaseMessages(applicantMessages);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setQmApplicantSolicitorQueries(applicantQueries);
        CaseQueriesCollection respondent1Queries = new CaseQueriesCollection();
        respondent1Queries.setCaseMessages(respondent1Messages);
        caseData.setQmRespondentSolicitor1Queries(respondent1Queries);
        CaseQueriesCollection respondent2Queries = new CaseQueriesCollection();
        CaseMessage res2Msg = new CaseMessage();
        res2Msg.setId("res2");
        res2Msg.setCreatedOn(OffsetDateTime.now().plusHours(2));
        List<Element<CaseMessage>> respondent2Messages = wrapElements(res2Msg);
        respondent2Queries.setCaseMessages(respondent2Messages);
        caseData.setQmRespondentSolicitor2Queries(respondent2Queries);

        CaseQueriesUtil.logMigrationSuccess(caseData);
    }

    @Test
    void shouldNotError_whenNoOldQueriesExistWhileLoggingMigrationSuccess() {
        CaseQueriesUtil.logMigrationSuccess(CaseDataBuilder.builder().build());
    }

    private CaseDataLiP respondentResponseWithLanguagePreference(String languagePreference) {
        RespondentLiPResponse respondentResponse = new RespondentLiPResponse();
        respondentResponse.setRespondent1ResponseLanguage(languagePreference);
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setRespondent1LiPResponse(respondentResponse);
        return caseDataLiP;
    }
}
