package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.QUERY_DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class RespondToQueryHelperTest {

    @Mock
    private QueryManagementCamundaService runtimeService;
    @Mock
    private CoreCaseUserService coreCaseUserService;

    private RespondToQueryHelper helper;

    @BeforeEach
    void setUp() {
        helper = new RespondToQueryHelper(runtimeService, coreCaseUserService);
    }

    @Test
    void shouldAddLipProperties() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        Map<String, String> properties = new HashMap<>();

        helper.addCustomProperties(properties, caseData, "John Smith", true);

        assertThat(properties)
            .containsEntry(PARTY_NAME, "John Smith")
            .doesNotContainKey(CLAIM_LEGAL_ORG_NAME_SPEC);
    }

    @Test
    void shouldAddLegalRepProperties() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        Map<String, String> properties = new HashMap<>();

        helper.addCustomProperties(properties, caseData, "Signer Name", false);

        assertThat(properties)
            .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name")
            .doesNotContainKey(PARTY_NAME);
    }

    @Test
    void shouldAddLatestFollowUpQueryDate() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseData caseData = createCaseData(now);
        QueryManagementVariables queryManagementVariables = new QueryManagementVariables();
        queryManagementVariables.setQueryId("response-id");
        when(runtimeService.getProcessVariables("process-id"))
            .thenReturn(queryManagementVariables);
        when(coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), "LR"))
            .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));

        Map<String, String> properties = new HashMap<>();

        helper.addQueryDateProperty(properties, caseData);

        assertThat(properties).containsEntry(QUERY_DATE, formatLocalDate(now.minusDays(1).toLocalDate(), DATE));
    }

    @Test
    void shouldReturnResponseQueryContextWhenProcessVariablesAvailable() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseData caseData = createCaseData(now);
        QueryManagementVariables queryManagementVariables = new QueryManagementVariables();
        queryManagementVariables.setQueryId("response-id");
        when(runtimeService.getProcessVariables("process-id"))
            .thenReturn(queryManagementVariables);
        when(coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), "LR"))
            .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));

        Optional<RespondToQueryHelper.ResponseQueryContext> context = helper.getResponseQueryContext(caseData);

        assertThat(context).isPresent();
        assertThat(context.get().getParentQuery().getId()).isEqualTo("parent-id");
        assertThat(context.get().getResponseQuery().getId()).isEqualTo("response-id");
        assertThat(context.get().getRoles()).containsExactly(CaseRole.APPLICANTSOLICITORONE.toString());
    }

    @Test
    void shouldReturnEmptyResponseQueryContextWhenProcessInstanceMissing() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();

        assertThat(helper.getResponseQueryContext(caseData)).isEmpty();
    }

    @Test
    void shouldIdentifyUnspecClaimNotReadyForNotification() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);
        caseData.setCcdState(CaseState.CASE_ISSUED);

        assertThat(helper.isUnspecClaimNotReadyForNotification(
            caseData,
            List.of(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
        )).isTrue();

        assertThat(helper.isUnspecClaimNotReadyForNotification(
            caseData,
            List.of(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
        )).isFalse();
    }

    @Test
    void shouldFallbackToParentQueryDateWhenNoFollowUp() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseQueriesCollection caseQueries = new CaseQueriesCollection();
        caseQueries.setCaseMessages(wrapElements(
            createCaseMessage("parent-id", "LR", now.minusDays(3), null),
            createCaseMessage("response-id", "admin", now, "parent-id")
        ));

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setQueries(caseQueries);
        caseData.setBusinessProcess(new BusinessProcess().setProcessInstanceId("process-id"));
        caseData.setCcdCaseReference(1234567890L);
        QueryManagementVariables queryManagementVariables = new QueryManagementVariables();
        queryManagementVariables.setQueryId("response-id");

        when(runtimeService.getProcessVariables("process-id"))
            .thenReturn(queryManagementVariables);
        when(coreCaseUserService.getUserCaseRoles(any(), any()))
            .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));

        Map<String, String> properties = new HashMap<>();

        helper.addQueryDateProperty(properties, caseData);

        assertThat(properties).containsEntry(QUERY_DATE, formatLocalDate(now.minusDays(3).toLocalDate(), DATE));
    }

    private CaseData createCaseData(OffsetDateTime now) {
        CaseQueriesCollection caseQueries = new CaseQueriesCollection();
        caseQueries.setCaseMessages(wrapElements(
            createCaseMessage("parent-id", "LR", now.minusDays(2), null),
            createCaseMessage("response-id", "admin", now, "parent-id"),
            createCaseMessage("follow-up", "LR", now.minusDays(1), "parent-id")
        ));

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setQueries(caseQueries);
        caseData.setBusinessProcess(new BusinessProcess().setProcessInstanceId("process-id"));
        caseData.setCcdCaseReference(1234567890L);
        return caseData;
    }

    private CaseMessage createCaseMessage(String id, String createdBy, OffsetDateTime createdOn, String parentId) {
        CaseMessage caseMessage = new CaseMessage();
        caseMessage.setId(id);
        caseMessage.setCreatedBy(createdBy);
        caseMessage.setCreatedOn(createdOn);
        caseMessage.setParentId(parentId);
        return caseMessage;
    }
}
