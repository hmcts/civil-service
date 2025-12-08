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
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3")
            .toBuilder()
            .createdOn(createdOn)
            .build();

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
        List<String> roles = List.of(caseRole);
        CaseData caseData = CaseData.builder()
            .claimantBilingualLanguagePreference(claimantLanguage)
            .caseDataLiP(respondentResponseWithLanguagePreference(defendantLanguage))
            .build();
        CaseMessage caseMessage = buildCaseMessage("id", "Query 3")
            .toBuilder()
            .createdOn(createdOn)
            .build();

        LatestQuery result = CaseQueriesUtil.buildLatestQuery(caseMessage, caseData, roles);

        assertEquals("id", result.getQueryId());
        assertEquals(expected, result.getIsWelsh());
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
    void shouldReturnNull_whenNoQueryFoundForId() {
        CaseData caseData = CaseData.builder()
            .queries(CaseQueriesCollection.builder().caseMessages(List.of()).build())
            .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            CaseQueriesUtil.getQueryById(caseData, "1")
        );

        assertEquals("No query found for queryId 1", exception.getMessage());
    }

    @Test
    void shouldReturnQuery_whenQueryFoundForId() {
        CaseQueriesCollection queries = CaseQueriesCollection.builder()
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("1")
                                           .build(),
                                       CaseMessage.builder()
                                           .id("2")
                                           .build(),
                                       CaseMessage.builder()
                                           .id("3")
                                           .build()))
            .build();
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1L)
            .queries(queries).build();

        CaseMessage latestQuery = CaseQueriesUtil.getQueryById(caseData, "2");

        assertThat(latestQuery).isEqualTo(queries.getCaseMessages().get(1).getValue());
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

        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1L)
            .queries(applicantQuery)
            .build();

        List<String> roles = CaseQueriesUtil.getUserRoleForQuery(caseData, coreCaseUserService, "1");

        assertThat(roles).containsOnly(CaseRole.APPLICANTSOLICITORONE.toString());
    }

    @Test
    void shouldReturnRespondent1Role_whenQueryFoundForId() {
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.toString()));

        CaseQueriesCollection respondent1Query = CaseQueriesCollection.builder()
            .roleOnCase("respondent-solicitor-1")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("2")
                                           .build()))
            .build();

        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1L)
            .queries(respondent1Query)
            .build();

        List<String> roles = CaseQueriesUtil.getUserRoleForQuery(caseData, coreCaseUserService, "2");

        assertThat(roles).containsOnly(CaseRole.RESPONDENTSOLICITORONE.toString());
    }

    @Test
    void shouldReturnRespondent2Role_whenQueryFoundForId() {
        when(coreCaseUserService.getUserCaseRoles(any(), any())).thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.toString()));

        CaseQueriesCollection respondent2Query = CaseQueriesCollection.builder()
            .roleOnCase("respondent-solicitor-2")
            .caseMessages(wrapElements(CaseMessage.builder()
                                           .id("3")
                                           .build()))
            .build();
        CaseData caseData = CaseData.builder()
            .ccdCaseReference(1L)
            .queries(respondent2Query).build();

        List<String> roles = CaseQueriesUtil.getUserRoleForQuery(caseData, coreCaseUserService, "3");

        assertThat(roles).containsOnly(CaseRole.RESPONDENTSOLICITORTWO.toString());
    }

    @Test
    void shouldUpdateApplicantSolicitorQueries_WhenRoleIsApplicantSolicitor() {
        List<String> roles = List.of("APPLICANTSOLICITORONE");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_ONE;
        CaseData caseData = CaseData.builder()
            .qmApplicantSolicitorQueries(CaseQueriesCollection.builder().partyName("Old Name").build())
            .build();

        CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, caseData);

        assertEquals("Claimant", caseData.getQmApplicantSolicitorQueries().getPartyName());
    }

    @Test
    void shouldUpdateRespondentSolicitor1Queries_WhenRoleIsRespondentSolicitor1() {
        List<String> roles = List.of("RESPONDENTSOLICITORONE");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_ONE;
        CaseData caseData = CaseData.builder()
            .qmRespondentSolicitor1Queries(CaseQueriesCollection.builder().partyName("Old Name").build())
            .build();

        CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, caseData);

        assertEquals("Defendant", caseData.getQmRespondentSolicitor1Queries().getPartyName());
    }

    @Test
    void shouldUpdateRespondentSolicitor1Queries_WhenRoleIsRespondentSolicitor1_1v2Diff() {
        List<String> roles = List.of("RESPONDENTSOLICITORONE");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
        CaseData caseData = CaseData.builder()
            .qmRespondentSolicitor1Queries(CaseQueriesCollection.builder().partyName("Old Name").build())
            .build();

        CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, caseData);

        assertEquals("Defendant 1", caseData.getQmRespondentSolicitor1Queries().getPartyName());
    }

    @Test
    void shouldUpdateRespondentSolicitor2Queries_WhenRoleIsRespondentSolicitor2() {
        List<String> roles = List.of("RESPONDENTSOLICITORTWO");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
        CaseData caseData = CaseData.builder()
            .qmRespondentSolicitor2Queries(CaseQueriesCollection.builder().partyName("Old Name").build())
            .build();

        CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, caseData);

        assertEquals("Defendant 2", caseData.getQmRespondentSolicitor2Queries().getPartyName());
    }

    @Test
    void shouldUpdateRespondentSolicitor1Queries_WhenRoleIsRespondentSolicitor1_1v2Same() {
        List<String> roles = List.of("RESPONDENTSOLICITORONE");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
        CaseData caseData = CaseData.builder()
            .qmRespondentSolicitor1Queries(CaseQueriesCollection.builder().partyName("Old Name").build())
            .build();

        CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, caseData);

        assertEquals("Defendant", caseData.getQmRespondentSolicitor1Queries().getPartyName());
    }

    @Test
    void shouldThrowException_WhenRoleIsUnsupported() {
        List<String> roles = List.of("UNSUPPORTEDROLE");
        MultiPartyScenario scenario = MultiPartyScenario.ONE_V_ONE;
        CaseData caseData = CaseData.builder().build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            CaseQueriesUtil.updateQueryCollectionPartyName(roles, scenario, caseData)
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

    @Test
    void shouldMigrateAllQueries_whenOldQueriesExist() {
        List<Element<CaseMessage>> applicantMessages = wrapElements(CaseMessage.builder().id("app1").createdOn(OffsetDateTime.now()).build());
        List<Element<CaseMessage>> respondent1Messages = wrapElements(CaseMessage.builder().id("res1").createdOn(OffsetDateTime.now().plusHours(1)).build());
        List<Element<CaseMessage>> respondent2Messages = wrapElements(CaseMessage.builder().id("res2").createdOn(OffsetDateTime.now().plusHours(2)).build());

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setQmApplicantSolicitorQueries(CaseQueriesCollection.builder().caseMessages(applicantMessages).build());
        caseData.setQmRespondentSolicitor1Queries(CaseQueriesCollection.builder().caseMessages(respondent1Messages).build());
        caseData.setQmRespondentSolicitor2Queries(CaseQueriesCollection.builder().caseMessages(respondent2Messages).build());

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
        caseData.setQmApplicantSolicitorQueries(CaseQueriesCollection.builder().caseMessages(List.of(element(CaseMessage.builder().build()))).build());

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
        caseData.setQmApplicantSolicitorQueries(CaseQueriesCollection.builder().build());
        caseData.setQmRespondentSolicitor1Queries(CaseQueriesCollection.builder().build());
        caseData.setQmRespondentSolicitor2Queries(CaseQueriesCollection.builder().build());

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
        List<Element<CaseMessage>> applicantMessages = wrapElements(CaseMessage.builder().id("app1").createdOn(OffsetDateTime.now()).build());
        List<Element<CaseMessage>> respondent1Messages = wrapElements(CaseMessage.builder().id("res1").createdOn(OffsetDateTime.now().plusHours(1)).build());
        List<Element<CaseMessage>> respondent2Messages = wrapElements(CaseMessage.builder().id("res2").createdOn(OffsetDateTime.now().plusHours(2)).build());

        CaseData.CaseDataBuilder builder = CaseData.builder()
            .qmApplicantSolicitorQueries(CaseQueriesCollection.builder().caseMessages(applicantMessages).build())
            .qmRespondentSolicitor1Queries(CaseQueriesCollection.builder().caseMessages(respondent1Messages).build())
            .qmRespondentSolicitor2Queries(CaseQueriesCollection.builder().caseMessages(respondent2Messages).build());

        CaseQueriesUtil.logMigrationSuccess(builder.build());
    }

    @Test
    void shouldNotError_whenNoOldQueriesExistWhileLoggingMigrationSuccess() {
        CaseQueriesUtil.logMigrationSuccess(CaseData.builder().build());
    }

    private CaseDataLiP respondentResponseWithLanguagePreference(String languagePreference) {
        return CaseDataLiP.builder()
            .respondent1LiPResponse(
                RespondentLiPResponse.builder().respondent1ResponseLanguage(languagePreference).build()
            ).build();
    }
}
