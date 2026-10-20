package uk.gov.hmcts.reform.civil.workflow.ccd;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.workflow.WorkflowIntegrationTest;
import uk.gov.hmcts.reform.civil.workflow.ccd.fixture.RaiseQueryFixtures;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.queryManagementRespondQuery;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@SuppressWarnings("java:S5960")
class RespondQueryWorkflowTest extends WorkflowIntegrationTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    @Test
    void shouldRespondToQuerySuccessfully() throws Exception {
        CaseData fixture = buildCaseWithQueryAwaitingResponse();

        startWorkflow(fixture)
            .eventId(queryManagementRespondQuery)
            .aboutToStart()
            .then(result -> assertThat(result.response().getErrors()).isNullOrEmpty())
            .aboutToSubmit()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();

                CaseData caseData = result.caseData();
                assertThat(caseData.getBusinessProcess()).isNotNull();
                assertThat(caseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
                assertThat(caseData.getBusinessProcess().getCamundaEvent())
                    .isEqualTo(queryManagementRespondQuery.name());

                CaseMessage latest = caseData.getQueries().latest();
                assertThat(latest).isNotNull();
                assertThat(latest.getAttachments()).isNotEmpty();
                latest.getAttachments().forEach(attachment ->
                    assertThat(attachment.getValue().getCategoryID())
                        .isEqualTo("CaseWorkerQueryDocumentsDocumentAttachments")
                );
            })
            .submitted()
            .then(result -> assertThat(result.submittedResponse()).isNotNull());
    }

    @Test
    void shouldMigrateOldCollectionsOnAboutToStart() throws Exception {
        CaseData fixture = RaiseQueryFixtures.caseDataWithLegacyCollections();

        startWorkflow(fixture)
            .eventId(queryManagementRespondQuery)
            .aboutToStart()
            .then(result -> {
                assertThat(result.response().getErrors()).isNullOrEmpty();
                CaseData migrated = result.caseData();
                assertThat(migrated.getQueries()).isNotNull();
                assertThat(migrated.getQueries().getPartyName()).isEqualTo("All queries");
                List<CaseMessage> allMessages = unwrapElements(migrated.getQueries().getCaseMessages());
                assertThat(allMessages).hasSize(1);
                assertThat(allMessages.get(0).getBody()).isEqualTo("Old question");
            });
    }

    @Test
    void shouldClearLegacyCollectionsOnAboutToSubmit() throws Exception {
        CaseData fixture = buildCaseWithQueryAwaitingResponse();
        fixture.setQmApplicantSolicitorQueries(
            buildLegacyCollection("Claimant", "old-msg")
        );

        startWorkflow(fixture)
            .eventId(queryManagementRespondQuery)
            .aboutToSubmit()
            .then(result -> {
                java.util.Map<String, Object> responseData = result.response().getData();
                assertThat(responseData).doesNotContainKey("qmApplicantSolicitorQueries");
                assertThat(responseData).doesNotContainKey("qmRespondentSolicitor1Queries");
                assertThat(responseData).doesNotContainKey("qmRespondentSolicitor2Queries");
            });
    }

    private static CaseData buildCaseWithQueryAwaitingResponse() {
        
        String rootId = UUID.randomUUID().toString();

        CaseMessage rootQuery = RaiseQueryFixtures.queryMessage(
            "party-user-id", "Query subject", "Original question", false);
        rootQuery.setId(rootId);
        rootQuery.setCreatedOn(OffsetDateTime.now().minusHours(2));

        CaseMessage response = RaiseQueryFixtures.queryMessage(
            "caseworker-id", "Query subject", "Caseworker response", false);
        response.setParentId(rootId);
        response.setCreatedOn(OffsetDateTime.now());

        CaseQueriesCollection queries = new CaseQueriesCollection();
        queries.setPartyName("All queries");
        queries.setCaseMessages(new ArrayList<>(List.of(element(rootQuery), element(response))));
        CaseData base = RaiseQueryFixtures.caseData();
        base.setQueries(queries);
        return base;
    }

    private static CaseQueriesCollection buildLegacyCollection(String partyName, String body) {
        CaseMessage msg = RaiseQueryFixtures.queryMessage("old-user", "Old query", body, false);
        CaseQueriesCollection collection = new CaseQueriesCollection();
        collection.setPartyName(partyName);
        collection.setCaseMessages(new ArrayList<>(List.of(element(msg))));
        return collection;
    }
}
