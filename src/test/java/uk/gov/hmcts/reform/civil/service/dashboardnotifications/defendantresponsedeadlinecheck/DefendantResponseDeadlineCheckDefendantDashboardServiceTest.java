package uk.gov.hmcts.reform.civil.service.dashboardnotifications.defendantresponsedeadlinecheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_DEFENDANT_RESPONSE_DEADLINE_PASSED_DEFENDANT;

@ExtendWith(MockitoExtension.class)
class DefendantResponseDeadlineCheckDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;

    @InjectMocks
    private DefendantResponseDeadlineCheckDefendantDashboardService service;

    @BeforeEach
    void setup() {
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordDefendantScenario() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);

        service.notifyDefendantResponseDeadlineCheck(caseData, AUTH_TOKEN);

        verify(mapper).mapCaseDataToParams(caseData);
        verify(dashboardScenariosService).recordScenarios(
            AUTH_TOKEN,
            SCENARIO_AAA6_DEFENDANT_RESPONSE_DEADLINE_PASSED_DEFENDANT.getScenario(),
            "1234",
            ScenarioRequestParams.builder().params(new HashMap<>()).build()
        );
    }
}
