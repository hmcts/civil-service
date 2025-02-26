package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.defendant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;

@ExtendWith(MockitoExtension.class)
class CaseDismissDefendantDashboardNotificationHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private CaseDismissDefendantDashboardNotificationHandler handler;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;

    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @Test
    void shouldRecordScenario_whenInvoked() {
        // Given
        CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .applicant1Represented(YesOrNo.NO)
            .ccdCaseReference(1234L)
            .previousCCDState(AWAITING_APPLICANT_INTENTION).build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId(CaseEvent.CREATE_DASHBOARD_NOTIFICATION_DISMISS_CASE_DEFENDANT.name()).build()
        ).build();
        HashMap<String, Object> scenarioParams = new HashMap<>();
        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

        // When
        handler.handle(params);
        final String caseId = caseData.getCcdCaseReference().toString();
        // Then
        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            caseId,
            "DEFENDANT"
        );
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole(
            caseId,
            "DEFENDANT",
            null
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            DashboardScenarios.SCENARIO_AAA6_DISMISS_CASE_DEFENDANT.getScenario(),
            caseId,
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }
}
