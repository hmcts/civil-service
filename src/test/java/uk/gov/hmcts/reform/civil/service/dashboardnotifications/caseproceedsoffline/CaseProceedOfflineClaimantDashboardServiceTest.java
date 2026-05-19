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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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
        lenient().when(mapper.mapCaseDataToParams(any())).thenReturn(new HashMap<>());
    }

    @Nested
    class NotifyCaseProceedOffline {

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
            ScenarioRequestParams params = new ScenarioRequestParams(new HashMap<>());
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

        @Test
        void shouldRecordScenario_whenInvokedForLipVLRCaseInCaseProgression() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(1234L)
                .previousCCDState(uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION)
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
        void shouldRecordScenario_whenInvokedForLipVLipCaseInCaseProgression() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(1234L)
                .previousCCDState(uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION)
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
        void shouldRecordScenario_whenInvokedForLipVLRCaseInJudicialReferral() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(1234L)
                .previousCCDState(uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL)
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
        void shouldRecordScenario_whenInvokedForLipVLRCaseInCasemanState() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(1234L)
                .previousCCDState(uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION)
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
        void shouldRecordScenario_whenInvokedForLipVLipCaseInCasemanState() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .ccdCaseReference(1234L)
                .previousCCDState(uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION)
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
        void shouldNotRecordPrimaryScenario_whenInvokedForNonLipCase() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.YES)
                .ccdCaseReference(1234L)
                .previousCCDState(uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION)
                .build();

            when(toggleService.isPublicQueryManagementEnabled(any())).thenReturn(false);

            service.notifyCaseProceedOffline(caseData, AUTH_TOKEN);

            // Primary scenario should NOT be recorded
            verify(dashboardScenariosService, org.mockito.Mockito.never()).recordScenarios(
                eq(AUTH_TOKEN),
                eq(SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT_WITHOUT_TASK_CHANGES.getScenario()),
                eq(caseData.getCcdCaseReference().toString()),
                any(ScenarioRequestParams.class)
            );
            // But additional scenarios might still be recorded as they don't have the Lip check in this service
            verify(dashboardScenariosService).recordScenarios(
                eq(AUTH_TOKEN),
                eq("Scenario.AAA6.GeneralApplication.InitiateApplication.Inactive.Claimant"),
                eq(caseData.getCcdCaseReference().toString()),
                any(ScenarioRequestParams.class)
            );
        }

        @Test
        void shouldNotRecordPrimaryScenario_whenInvokedForNonLipCaseInCaseProgression() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.YES)
                .ccdCaseReference(1234L)
                .previousCCDState(uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION)
                .build();

            when(toggleService.isPublicQueryManagementEnabled(any())).thenReturn(false);

            service.notifyCaseProceedOffline(caseData, AUTH_TOKEN);

            // Primary scenario should NOT be recorded
            verify(dashboardScenariosService, org.mockito.Mockito.never()).recordScenarios(
                eq(AUTH_TOKEN),
                eq(SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT_WITHOUT_TASK_CHANGES.getScenario()),
                eq(caseData.getCcdCaseReference().toString()),
                any(ScenarioRequestParams.class)
            );
        }

        @Test
        void shouldNotRecordPrimaryScenario_whenInvokedForLipvLRCaseInCaseProgression_ButNotOneVOne() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.NO)
                .addApplicant2(YesOrNo.YES)
                .ccdCaseReference(1234L)
                .previousCCDState(uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION)
                .build();

            when(toggleService.isPublicQueryManagementEnabled(any())).thenReturn(false);

            service.notifyCaseProceedOffline(caseData, AUTH_TOKEN);

            // Primary scenario should NOT be recorded because it is 2v1, not 1v1
            verify(dashboardScenariosService, org.mockito.Mockito.never()).recordScenarios(
                eq(AUTH_TOKEN),
                eq(SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_CLAIMANT_WITHOUT_TASK_CHANGES.getScenario()),
                eq(caseData.getCcdCaseReference().toString()),
                any(ScenarioRequestParams.class)
            );
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

    @Nested
    class EligibleForCaseProgressionState {

        @Test
        void shouldReturnTrue_whenLipVLipOneVOne() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .applicant1Represented(YesOrNo.NO)
                .build();

            boolean result = service.eligibleForCaseProgressionState(caseData);

            org.junit.jupiter.api.Assertions.assertTrue(result);
        }

        @Test
        void shouldReturnTrue_whenLipVLROneVOne() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.NO)
                .build();

            boolean result = service.eligibleForCaseProgressionState(caseData);

            org.junit.jupiter.api.Assertions.assertTrue(result);
        }

        @Test
        void shouldReturnFalse_whenNotOneVOne() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.YES)
                .applicant1Represented(YesOrNo.NO)
                .addApplicant2(YesOrNo.YES)
                .build();

            boolean result = service.eligibleForCaseProgressionState(caseData);

            org.junit.jupiter.api.Assertions.assertFalse(result);
        }
    }
}
