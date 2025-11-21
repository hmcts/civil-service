package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardScenarioServiceTest {

    private static final String PRIMARY_SCENARIO = "PrimaryScenario";
    private static final String EXTRA_SCENARIO = "ExtraScenario";
    private static final String AUTH_TOKEN = "Bearer token";

    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private DashboardScenariosService dashboardScenariosService;

    private TestDashboardScenarioService scenarioService;

    @BeforeEach
    void setup() {
        scenarioService = new TestDashboardScenarioService(dashboardScenariosService, mapper);
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Test
    void shouldRecordPrimaryExtraAndAdditionalScenarios() {
        scenarioService.primaryScenario = PRIMARY_SCENARIO;
        scenarioService.extraScenario = EXTRA_SCENARIO;
        scenarioService.additionalScenarios = Map.of(
            "AdditionalTrue", true,
            "AdditionalFalse", false
        );

        CaseData caseData = CaseData.builder().ccdCaseReference(123L).build();

        scenarioService.record(caseData, AUTH_TOKEN);

        verify(mapper).mapCaseDataToParams(caseData);
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(PRIMARY_SCENARIO),
            eq("123"),
            ArgumentMatchers.any()
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(EXTRA_SCENARIO),
            eq("123"),
            ArgumentMatchers.any()
        );
        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq("AdditionalTrue"),
            eq("123"),
            ArgumentMatchers.any()
        );
        verify(dashboardScenariosService, never()).recordScenarios(
            eq(AUTH_TOKEN),
            eq("AdditionalFalse"),
            any(),
            any()
        );
        assertThat(scenarioService.beforeHookCalled).isTrue();
    }

    @Test
    void shouldSkipScenariosWhenNotEligible() {
        scenarioService.primaryScenario = PRIMARY_SCENARIO;
        scenarioService.extraScenario = EXTRA_SCENARIO;
        scenarioService.shouldRecordPrimary = false;
        scenarioService.shouldRecordExtra = false;
        scenarioService.additionalScenarios = Map.of("Additional", false);

        CaseData caseData = CaseData.builder().build();

        scenarioService.record(caseData, AUTH_TOKEN);

        verify(mapper).mapCaseDataToParams(caseData);
        verifyNoInteractions(dashboardScenariosService);
        assertThat(scenarioService.beforeHookCalled).isFalse();
    }

    private static class TestDashboardScenarioService extends DashboardScenarioService {

        private String primaryScenario;
        private String extraScenario;
        private Map<String, Boolean> additionalScenarios = Map.of();
        private boolean shouldRecordPrimary = true;
        private boolean shouldRecordExtra = true;
        private boolean beforeHookCalled;

        TestDashboardScenarioService(DashboardScenariosService dashboardScenariosService,
                                     DashboardNotificationsParamsMapper mapper) {
            super(dashboardScenariosService, mapper);
        }

        void record(CaseData caseData, String authToken) {
            recordScenario(caseData, authToken);
        }

        @Override
        protected String getScenario(CaseData caseData) {
            return primaryScenario;
        }

        @Override
        protected String getExtraScenario() {
            return extraScenario;
        }

        @Override
        protected Map<String, Boolean> getScenarios(CaseData caseData) {
            return additionalScenarios;
        }

        @Override
        protected boolean shouldRecordScenario(CaseData caseData) {
            return shouldRecordPrimary;
        }

        @Override
        protected boolean shouldRecordExtraScenario(CaseData caseData) {
            return shouldRecordExtra;
        }

        @Override
        protected void beforeRecordScenario(CaseData caseData, String authToken) {
            beforeHookCalled = true;
        }
    }
}
