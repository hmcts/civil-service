package uk.gov.hmcts.reform.civil.ga.service.dashboardnotifications;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaDashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GaDashboardScenarioServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private DashboardApiClient dashboardApiClient;

    @Mock
    private GaDashboardNotificationsParamsMapper mapper;

    @Test
    void shouldRecordPrimaryExtraAndAdditionalScenarios() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .ccdCaseReference(123456L)
            .build();
        HashMap<String, Object> params = new HashMap<>();
        when(mapper.mapCaseDataToParams(caseData)).thenReturn(params);

        TestScenarioService service = new TestScenarioService(
            dashboardApiClient,
            mapper,
            "Scenario.Primary",
            "Scenario.Extra",
            Map.of(
                "Scenario.Additional", true,
                "Scenario.Skipped", false,
                "", true
            ),
            true,
            true
        );

        service.trigger(caseData, AUTH_TOKEN);

        ScenarioRequestParams expectedParams = ScenarioRequestParams.builder().params(params).build();
        String caseReference = caseData.getCcdCaseReference().toString();

        verify(dashboardApiClient).recordScenario(caseReference, "Scenario.Primary", AUTH_TOKEN, expectedParams);
        verify(dashboardApiClient).recordScenario(caseReference, "Scenario.Extra", AUTH_TOKEN, expectedParams);
        verify(dashboardApiClient).recordScenario(caseReference, "Scenario.Additional", AUTH_TOKEN, expectedParams);
        verifyNoMoreInteractions(dashboardApiClient);
        assertTrue(service.beforeCalled());
    }

    private static class TestScenarioService extends GaDashboardScenarioService {

        private final String scenario;
        private final String extraScenario;
        private final Map<String, Boolean> scenarios;
        private final boolean recordScenario;
        private final boolean recordExtraScenario;
        private boolean beforeCalled;

        TestScenarioService(DashboardApiClient dashboardApiClient,
                            GaDashboardNotificationsParamsMapper mapper,
                            String scenario,
                            String extraScenario,
                            Map<String, Boolean> scenarios,
                            boolean recordScenario,
                            boolean recordExtraScenario) {
            super(dashboardApiClient, mapper);
            this.scenario = scenario;
            this.extraScenario = extraScenario;
            this.scenarios = scenarios;
            this.recordScenario = recordScenario;
            this.recordExtraScenario = recordExtraScenario;
        }

        void trigger(GeneralApplicationCaseData caseData, String authToken) {
            recordScenario(caseData, authToken);
        }

        boolean beforeCalled() {
            return beforeCalled;
        }

        @Override
        protected String getScenario(GeneralApplicationCaseData caseData) {
            return scenario;
        }

        @Override
        protected String getExtraScenario() {
            return extraScenario;
        }

        @Override
        protected Map<String, Boolean> getScenarios(GeneralApplicationCaseData caseData) {
            return scenarios;
        }

        @Override
        protected boolean shouldRecordScenario(GeneralApplicationCaseData caseData) {
            return recordScenario;
        }

        @Override
        protected boolean shouldRecordExtraScenario(GeneralApplicationCaseData caseData) {
            return recordExtraScenario;
        }

        @Override
        protected void beforeRecordScenario(GeneralApplicationCaseData caseData, String authToken) {
            beforeCalled = true;
        }
    }
}
