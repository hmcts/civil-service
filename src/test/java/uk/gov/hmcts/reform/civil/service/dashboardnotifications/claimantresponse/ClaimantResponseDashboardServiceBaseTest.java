package uk.gov.hmcts.reform.civil.service.dashboardnotifications.claimantresponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.util.HashMap;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimantResponseDashboardServiceBaseTest {

    private static final String AUTH_TOKEN = "auth";
    private static final String SCENARIO = "scenario";

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;

    private TestDashboardService service;

    @BeforeEach
    void setUp() {
        service = new TestDashboardService(
            dashboardScenariosService,
            mapper,
            featureToggleService,
            dashboardNotificationService,
            taskListService
        );
    }

    @Test
    void shouldResolveFirstNonNullScenario() {
        String scenario = service.resolveScenarioForTest(() -> null, () -> SCENARIO, () -> "fallback");

        assertEquals(SCENARIO, scenario);
    }

    @Test
    void shouldResolveNullWhenAllScenariosNull() {
        String scenario = service.resolveScenarioForTest(() -> null, () -> null);

        assertNull(scenario);
    }

    @Test
    void shouldRecordGeneralApplicationScenarioWhenProceedingOffline() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM);
        when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());

        service.recordGeneralApplicationScenarioForTest(caseData);

        verify(dashboardScenariosService).recordScenarios(
            eq(AUTH_TOKEN),
            eq(SCENARIO),
            eq("1234"),
            any(ScenarioRequestParams.class)
        );
    }

    @Test
    void shouldNotRecordGeneralApplicationScenarioWhenNotProceedingOffline() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.recordGeneralApplicationScenarioForTest(caseData);

        verifyNoInteractions(dashboardScenariosService);
    }

    @Test
    void shouldIdentifyMintiScenarioWhenToggleEnabledAndTrackMulti() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setResponseClaimTrack(AllocatedTrack.MULTI_CLAIM.name());

        boolean result = service.isMintiApplicableForTest(caseData);

        assertTrue(result);
    }

    @Test
    void shouldNotIdentifyMintiScenarioWhenToggleDisabled() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setResponseClaimTrack(AllocatedTrack.INTERMEDIATE_CLAIM.name());

        boolean result = service.isMintiApplicableForTest(caseData);

        assertFalse(result);
    }

    @Test
    void shouldIdentifyCarmApplicableWhenToggleEnabled() {
        when(featureToggleService.isCarmEnabledForCase(any())).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().build();

        boolean result = service.isCarmApplicableForTest(caseData);

        assertTrue(result);
    }

    @Test
    void shouldClearSettledCaseNotificationsWhenCaseSettled() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.CASE_SETTLED);

        service.clearSettledCaseNotificationsForTest(caseData);

        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole("1234", "CLAIMANT");
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRole("1234", "CLAIMANT");
    }

    @Test
    void shouldNotClearSettledCaseNotificationsWhenCaseNotSettled() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCcdCaseReference(1234L);
        caseData.setCcdState(CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT);

        service.clearSettledCaseNotificationsForTest(caseData);

        verifyNoInteractions(dashboardNotificationService, taskListService);
    }

    private static class TestDashboardService extends ClaimantResponseDashboardServiceBase {
        TestDashboardService(DashboardScenariosService dashboardScenariosService,
                             DashboardNotificationsParamsMapper mapper,
                             FeatureToggleService featureToggleService,
                             DashboardNotificationService dashboardNotificationService,
                             TaskListService taskListService) {
            super(dashboardScenariosService, mapper, featureToggleService, dashboardNotificationService, taskListService);
        }

        @Override
        protected String getScenario(CaseData caseData) {
            return null;
        }

        String resolveScenarioForTest(Supplier<String> first, Supplier<String> second) {
            return resolveScenario(first, second);
        }

        String resolveScenarioForTest(Supplier<String> first, Supplier<String> second, Supplier<String> third) {
            return resolveScenario(first, second, third);
        }

        void recordGeneralApplicationScenarioForTest(CaseData caseData) {
            recordGeneralApplicationScenarioIfNeeded(caseData, ClaimantResponseDashboardServiceBaseTest.AUTH_TOKEN, ClaimantResponseDashboardServiceBaseTest.SCENARIO);
        }

        boolean isMintiApplicableForTest(CaseData caseData) {
            return isMintiApplicable(caseData);
        }

        boolean isCarmApplicableForTest(CaseData caseData) {
            return isCarmApplicableForMediation(caseData);
        }

        void clearSettledCaseNotificationsForTest(CaseData caseData) {
            clearSettledCaseNotificationsIfNeeded(caseData, "CLAIMANT");
        }
    }
}
