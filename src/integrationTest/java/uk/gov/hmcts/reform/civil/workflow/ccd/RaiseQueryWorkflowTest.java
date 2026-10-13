package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.RaiseQueryFixtures;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.queryManagementRaiseQuery;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.APPLICANTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.DEFENDANT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORONE;
import static uk.gov.hmcts.reform.civil.handler.callback.user.RaiseQueryCallbackHandler.FOLLOW_UPS_ERROR;
import static uk.gov.hmcts.reform.civil.handler.callback.user.RaiseQueryCallbackHandler.INVALID_CASE_STATE_ERROR;
import static uk.gov.hmcts.reform.civil.handler.callback.user.RaiseQueryCallbackHandler.PUBLIC_QUERIES_PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.user.RaiseQueryCallbackHandler.QM_NOT_ALLOWED_ERROR;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@SuppressWarnings("java:S5960")
class RaiseQueryWorkflowTest extends WorkflowIntegrationTest {

    private static final String TEST_USER_ID = "test-user-id";

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @BeforeEach
    void setUp() {
        when(featureToggleService.isPublicQueryManagementEnabled(org.mockito.ArgumentMatchers.any(CaseData.class)))
            .thenReturn(true);
        when(userService.getUserInfo(anyString()))
            .thenReturn(UserInfo.builder().uid(TEST_USER_ID).sub("solicitor@example.com").build());
    }

    @Test
    void shouldRaiseQuerySuccessfully() throws Exception {
        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(APPLICANTSOLICITORONE.getFormattedName()));

        startWorkflow(RaiseQueryFixtures.caseDataWithNewQuery(false))
            .eventId(queryManagementRaiseQuery)
            .aboutToStart()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty())
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData caseData = result.caseData();
                assertThat(caseData.getQueries()).isNotNull();
                assertThat(caseData.getQueries().getPartyName()).isEqualTo(PUBLIC_QUERIES_PARTY_NAME);

                CaseMessage latest = caseData.getQueries().latest();
                assertThat(latest).isNotNull();
                assertThat(latest.getCreatedBy()).contains(TEST_USER_ID);
                assertThat(latest.getCreatedBy()).contains(APPLICANTSOLICITORONE.getFormattedName());

                assertThat(caseData.getQmLatestQuery()).isNotNull();
                assertThat(caseData.getQmLatestQuery().getQueryId()).isEqualTo(latest.getId());
                assertThat(caseData.getQmLatestQuery().getIsHearingRelated()).isEqualTo(YesOrNo.NO);

                assertThat(caseData.getBusinessProcess()).isNotNull();
                assertThat(caseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
                assertThat(caseData.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(queryManagementRaiseQuery.name());
            })
            .submitted()
            .then(result -> assertThat(result.submittedResponse()).isNotNull());
    }

    @Test
    void shouldBlockRaiseQueryInClosedState() throws Exception {
        startWorkflow(RaiseQueryFixtures.caseDataInClosedState())
            .eventId(queryManagementRaiseQuery)
            .aboutToStart()
            .then(result -> assertThat(result.response().getErrors()).containsExactly(INVALID_CASE_STATE_ERROR));
    }

    @Test
    void shouldBlockRaiseQueryWhenFeatureDisabled() throws Exception {
        when(featureToggleService.isPublicQueryManagementEnabled(org.mockito.ArgumentMatchers.any(CaseData.class)))
            .thenReturn(false);

        startWorkflow(RaiseQueryFixtures.caseData())
            .eventId(queryManagementRaiseQuery)
            .aboutToStart()
            .then(result -> assertThat(result.response().getErrors()).containsExactly(QM_NOT_ALLOWED_ERROR));
    }

    @Test
    void shouldBlockConsecutiveFollowUps() throws Exception {
        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(APPLICANTSOLICITORONE.getFormattedName()));

        String rootId = UUID.randomUUID().toString();

        CaseMessage rootQuery = RaiseQueryFixtures.queryMessage(
            "party-user-id", "Query subject", "Original question", false);
        rootQuery.setId(rootId);
        rootQuery.setCreatedOn(java.time.OffsetDateTime.now().minusHours(3));

        CaseMessage caseworkerResponse = RaiseQueryFixtures.queryMessage(
            "caseworker-id", "Query subject", "Caseworker response", false);
        caseworkerResponse.setParentId(rootId);
        caseworkerResponse.setCreatedOn(java.time.OffsetDateTime.now().minusHours(2));

        CaseMessage firstFollowUp = RaiseQueryFixtures.queryMessage(
            "party-user-id", "Query subject", "First follow-up", false);
        firstFollowUp.setParentId(rootId);
        firstFollowUp.setCreatedOn(java.time.OffsetDateTime.now().minusHours(1));

        // This consecutive follow-up is the "latest" message and makes the thread even (4 msgs)
        CaseMessage consecutiveFollowUp = RaiseQueryFixtures.queryMessage(
            TEST_USER_ID, "Query subject", "Consecutive follow-up", false);
        consecutiveFollowUp.setParentId(rootId);
        consecutiveFollowUp.setCreatedOn(java.time.OffsetDateTime.now());

        CaseQueriesCollection queries = new CaseQueriesCollection();
        queries.setPartyName("All queries");
        queries.setCaseMessages(new ArrayList<>(List.of(
            uk.gov.hmcts.reform.civil.utils.ElementUtils.element(rootQuery),
            uk.gov.hmcts.reform.civil.utils.ElementUtils.element(caseworkerResponse),
            uk.gov.hmcts.reform.civil.utils.ElementUtils.element(firstFollowUp),
            uk.gov.hmcts.reform.civil.utils.ElementUtils.element(consecutiveFollowUp)
        )));
        
        CaseData fixture = RaiseQueryFixtures.caseData();
        fixture.setQueries(queries);

        startWorkflow(fixture)
            .eventId(queryManagementRaiseQuery)
            .aboutToStart()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty())
            .aboutToSubmit()
            .then(result -> assertThat(result.response().getErrors()).containsExactly(FOLLOW_UPS_ERROR));
    }

    @Test
    void shouldAllowFollowUpAfterResponse() throws Exception {
        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(APPLICANTSOLICITORONE.getFormattedName()));

        CaseData fixture = RaiseQueryFixtures.caseDataWithQueryAndResponse();
        String rootId = unwrapElements(fixture.getQueries().getCaseMessages()).stream()
            .filter(m -> m.getParentId() == null)
            .findFirst()
            .map(CaseMessage::getId)
            .orElseThrow();

        CaseMessage followUp = RaiseQueryFixtures.queryMessage(
            TEST_USER_ID, "Query subject", "Follow-up after response", false);
        followUp.setParentId(rootId);
        fixture.getQueries().getCaseMessages()
            .add(uk.gov.hmcts.reform.civil.utils.ElementUtils.element(followUp));

        startWorkflow(fixture)
            .eventId(queryManagementRaiseQuery)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getBusinessProcess().getCamundaEvent())
                    .isEqualTo(queryManagementRaiseQuery.name());
            });
    }

    @Test
    void shouldAssignClaimantCategoryForApplicantSolicitor() throws Exception {
        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(APPLICANTSOLICITORONE.getFormattedName()));

        startWorkflow(RaiseQueryFixtures.caseDataWithNewQuery(false))
            .eventId(queryManagementRaiseQuery)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                CaseMessage latest = result.caseData().getQueries().latest();
                assertThat(latest.getAttachments()).isNotEmpty();
                latest.getAttachments().forEach(attachment ->
                    assertThat(attachment.getValue().getCategoryID())
                        .isEqualTo("ClaimantQueryDocumentAttachments")
                );
            });
    }

    @Test
    void shouldAssignDefendantCategoryForRespondentSolicitor() throws Exception {
        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(RESPONDENTSOLICITORONE.getFormattedName()));

        startWorkflow(RaiseQueryFixtures.caseDataWithNewQuery(false))
            .eventId(queryManagementRaiseQuery)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                CaseMessage latest = result.caseData().getQueries().latest();
                assertThat(latest.getAttachments()).isNotEmpty();
                latest.getAttachments().forEach(attachment ->
                    assertThat(attachment.getValue().getCategoryID())
                        .isEqualTo("DefendantQueryDocumentAttachments")
                );
            });
    }

    @Test
    void shouldAssignDefendantCategoryForLipDefendant() throws Exception {
        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(DEFENDANT.getFormattedName()));

        startWorkflow(RaiseQueryFixtures.caseDataWithNewQuery(false))
            .eventId(queryManagementRaiseQuery)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                CaseMessage latest = result.caseData().getQueries().latest();
                assertThat(latest.getAttachments()).isNotEmpty();
                latest.getAttachments().forEach(attachment ->
                    assertThat(attachment.getValue().getCategoryID())
                        .isEqualTo("DefendantQueryDocumentAttachments")
                );
            });
    }

    @Test
    void shouldClearLegacyQueryCollections() throws Exception {
        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(APPLICANTSOLICITORONE.getFormattedName()));

        CaseData fixture = RaiseQueryFixtures.caseDataWithLegacyCollections();
        CaseMessage newMessage = RaiseQueryFixtures.queryMessage(
            TEST_USER_ID, "New query", "Question after migration", false);
        CaseQueriesCollection queries = new CaseQueriesCollection();
        queries.setPartyName("All queries");
        queries.setCaseMessages(new ArrayList<>(List.of(
            uk.gov.hmcts.reform.civil.utils.ElementUtils.element(newMessage))));
        fixture.setQueries(queries);

        startWorkflow(fixture)
            .eventId(queryManagementRaiseQuery)
            .aboutToStart()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                CaseData migrated = result.caseData();
                assertThat(migrated.getQueries()).isNotNull();
                assertThat(migrated.getQueries().getPartyName()).isEqualTo(PUBLIC_QUERIES_PARTY_NAME);
            })
            .aboutToSubmit()
            .then(result -> {
                java.util.Map<String, Object> responseData = result.response().getData();
                assertThat(responseData).doesNotContainKey("qmApplicantSolicitorQueries");
                assertThat(responseData).doesNotContainKey("qmRespondentSolicitor1Queries");
                assertThat(responseData).doesNotContainKey("qmRespondentSolicitor2Queries");
            });
    }

    @Test
    void shouldSetHearingRelatedFlagOnLatestQuery() throws Exception {
        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(APPLICANTSOLICITORONE.getFormattedName()));

        startWorkflow(RaiseQueryFixtures.caseDataWithNewQuery(true))
            .eventId(queryManagementRaiseQuery)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getQmLatestQuery()).isNotNull();
                assertThat(result.caseData().getQmLatestQuery().getIsHearingRelated())
                    .isEqualTo(YesOrNo.YES);
            });
    }

    @Test
    void shouldSetWelshFlagForLipClaimant() throws Exception {
        when(coreCaseUserService.getUserCaseRoles(anyString(), anyString()))
            .thenReturn(List.of(uk.gov.hmcts.reform.civil.enums.CaseRole.CLAIMANT.getFormattedName()));

        CaseData fixture = RaiseQueryFixtures.caseDataWithNewQuery(false);
        fixture.setClaimantBilingualLanguagePreference("BOTH");

        startWorkflow(fixture)
            .eventId(queryManagementRaiseQuery)
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                assertThat(result.caseData().getQmLatestQuery()).isNotNull();
                assertThat(result.caseData().getQmLatestQuery().getIsWelsh())
                    .isEqualTo(YesOrNo.YES);
            });
    }

    @Test
    void shouldBlockLipQueryWhenLipToggleDisabled() throws Exception {
        when(featureToggleService.isPublicQueryManagementEnabled(org.mockito.ArgumentMatchers.any(CaseData.class)))
            .thenReturn(false);

        startWorkflow(RaiseQueryFixtures.caseDataLip())
            .eventId(queryManagementRaiseQuery)
            .aboutToStart()
            .then(result -> assertThat(result.response().getErrors()).containsExactly(QM_NOT_ALLOWED_ERROR));
    }
}
