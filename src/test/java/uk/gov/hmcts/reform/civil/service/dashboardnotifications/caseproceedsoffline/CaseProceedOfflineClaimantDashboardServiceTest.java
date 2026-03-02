package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.time.OffsetDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT_WITHOUT_TASK_CHANGES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_LIP_QM_CASE_OFFLINE_OPEN_QUERIES_CLAIMANT;

@ExtendWith(MockitoExtension.class)
class CaseProceedOfflineClaimantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER_TOKEN";
    private CaseProceedOfflineClaimantDashboardService service;

    @Mock
    private DashboardScenariosService dashboardScenariosService;
    @Mock
    private DashboardNotificationService dashboardNotificationService;
    @Mock
    private TaskListService taskListService;
    @Mock
    private DashboardNotificationsParamsMapper mapper;
    @Mock
    private uk.gov.hmcts.reform.civil.service.FeatureToggleService toggleService;

    @BeforeEach
    void setup() {
        CaseProceedOfflineClaimantScenarioService scenarioService =
            new CaseProceedOfflineClaimantScenarioService(toggleService);
        service = new CaseProceedOfflineClaimantDashboardService(
            dashboardScenariosService,
            mapper,
            dashboardNotificationService,
            taskListService,
            scenarioService
        );
    }

    @Nested
    class NotifyCaseProceedOffline {
        @BeforeEach
        void stubMapper() {
            when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
        }

        @Test
        void shouldRecordScenario_whenInvokedForCase() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(1234L)
                .previousCCDState(All_FINAL_ORDERS_ISSUED)
                .build();

            when(toggleService.isPublicQueryManagementEnabled(any())).thenReturn(false);

            service.notifyCaseProceedOffline(caseData, AUTH_TOKEN);

            verifyDeleteNotificationsAndTaskListUpdates(caseData);
            verify(dashboardScenariosService).recordScenarios(
                eq(AUTH_TOKEN),
                eq(SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT_WITHOUT_TASK_CHANGES.getScenario()),
                eq(caseData.getCcdCaseReference().toString()),
                any(ScenarioRequestParams.class)
            );
        }

        @Test
        void shouldRecordQMScenario_whenInvokedForCaseWithOpenApplicantCitizenQuery() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec()
                .includesApplicantCitizenQueryFollowUp(OffsetDateTime.now())
                .build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(1234L)
                .previousCCDState(All_FINAL_ORDERS_ISSUED)
                .build();

            when(toggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);

            service.notifyCaseProceedOffline(caseData, AUTH_TOKEN);

            verifyDeleteNotificationsAndTaskListUpdates(caseData);
            verify(dashboardScenariosService).recordScenarios(
                eq(AUTH_TOKEN),
                eq(SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT_WITHOUT_TASK_CHANGES.getScenario()),
                eq(caseData.getCcdCaseReference().toString()),
                any(ScenarioRequestParams.class)
            );
            verify(dashboardScenariosService).recordScenarios(
                eq(AUTH_TOKEN),
                eq(SCENARIO_AAA6_LIP_QM_CASE_OFFLINE_OPEN_QUERIES_CLAIMANT.getScenario()),
                eq(caseData.getCcdCaseReference().toString()),
                any(ScenarioRequestParams.class)
            );
        }
    }

    @Nested
    class Eligibility {

        @Test
        void shouldBeEligibleForCasemanAndCaseProgressionWhenLipvLip() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1Represented(YesOrNo.NO);
            caseData.setRespondent1Represented(YesOrNo.NO);

            assertTrue(service.eligibleForCasemanState(caseData));
            assertTrue(service.eligibleForCaseProgressionState(caseData));
        }

        @Test
        void shouldBeEligibleForCasemanWhenLipvLr() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1Represented(YesOrNo.NO);
            caseData.setRespondent1Represented(YesOrNo.YES);

            assertTrue(service.eligibleForCasemanState(caseData));
        }
    }

    private void verifyDeleteNotificationsAndTaskListUpdates(CaseData caseData) {
        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            caseData.getCcdCaseReference().toString(),
            "CLAIMANT"
        );
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            caseData.getCcdCaseReference().toString(),
            "CLAIMANT",
            "Applications"
        );
    }
}
