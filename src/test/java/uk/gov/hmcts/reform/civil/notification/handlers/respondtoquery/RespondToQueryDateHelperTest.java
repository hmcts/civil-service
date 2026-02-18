package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.QUERY_DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class RespondToQueryDateHelperTest {

    @Mock
    private QueryManagementCamundaService runtimeService;
    @Mock
    private CoreCaseUserService coreCaseUserService;

    private RespondToQueryDateHelper helper;

    @BeforeEach
    void setUp() {
        helper = new RespondToQueryDateHelper(runtimeService, coreCaseUserService);
    }

    @Test
    void shouldAddLatestFollowUpQueryDate() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseData caseData = createCaseData(now);
        when(runtimeService.getProcessVariables("process-id"))
            .thenReturn(QueryManagementVariables.builder().queryId("response-id").build());
        when(coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), "LR"))
            .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));

        Map<String, String> properties = new HashMap<>();

        helper.addQueryDateProperty(properties, caseData);

        assertThat(properties).containsEntry(QUERY_DATE, formatLocalDate(now.minusDays(1).toLocalDate(), DATE));
    }

    @Test
    void shouldFallbackToParentQueryDateWhenNoFollowUp() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseQueriesCollection caseQueries = new CaseQueriesCollection();
        caseQueries.setCaseMessages(wrapElements(
            createCaseMessage("parent-id", "LR", now.minusDays(3), null),
            createCaseMessage("response-id", "admin", now, "parent-id")
        ));

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
            .queries(caseQueries)
            .businessProcess(new BusinessProcess().setProcessInstanceId("process-id"))
            .ccdCaseReference(1234567890L)
            .build();

        when(runtimeService.getProcessVariables("process-id"))
            .thenReturn(QueryManagementVariables.builder().queryId("response-id").build());
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

        return CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
            .queries(caseQueries)
            .businessProcess(new BusinessProcess().setProcessInstanceId("process-id"))
            .ccdCaseReference(1234567890L)
            .build();
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
