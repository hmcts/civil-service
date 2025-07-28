package uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_MESSAGES_TASK_CATEGORIES;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_MESSAGES_TO_THE_COURT;

@ExtendWith(MockitoExtension.class)
class MessageTaskCategoryCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private MessageTaskCategoryCallbackHandler handler;
    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setup() {
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);
    }

    @Test
    void shouldRecordScenario_ForAddingApplicationsAndMessagesToTheCourtTask() {
        CaseData caseData = CaseDataBuilder.builder().atStateTrialReadyCheck()
            .applicant1Represented(NO).build();
        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId(CREATE_MESSAGES_TASK_CATEGORIES.name())
                .build()
        ).build();

        HashMap<String, Object> scenarioParams = new HashMap<>();

        when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

        handler.handle(params);
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_APPLICATIONS_TO_THE_COURT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
        verify(dashboardScenariosService).recordScenarios(
            "BEARER_TOKEN",
            SCENARIO_AAA6_MESSAGES_TO_THE_COURT.getScenario(),
            caseData.getCcdCaseReference().toString(),
            ScenarioRequestParams.builder().params(scenarioParams).build()
        );
    }
}
