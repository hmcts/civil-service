package uk.gov.hmcts.reform.civil.service.dashboardnotifications.caseproceedsoffline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.dashboard.services.DashboardNotificationService;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;
import uk.gov.hmcts.reform.dashboard.services.TaskListService;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT_WITHOUT_TASK_CHANGES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_LIP_QM_CASE_OFFLINE_OPEN_QUERIES_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT_FAST_TRACK;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class CaseProceedOfflineDefendantDashboardServiceTest {

    private static final String AUTH_TOKEN = "BEARER_TOKEN";
    private CaseProceedOfflineDefendantDashboardService service;

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
        CaseProceedOfflineDefendantScenarioService scenarioService =
            new CaseProceedOfflineDefendantScenarioService(toggleService);
        service = new CaseProceedOfflineDefendantDashboardService(
            dashboardScenariosService,
            mapper,
            toggleService,
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
        void shouldRecordScenario_whenInvokedWithoutCaseProgression() {
            List<Element<GeneralApplication>> gaApplications = wrapElements(
                GeneralApplication.builder().caseLink(CaseLink.builder().caseReference("12345678").build()).build()
            );
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .ccdCaseReference(1234L)
                .generalApplications(gaApplications)
                .previousCCDState(AWAITING_APPLICANT_INTENTION)
                .build();

            when(toggleService.isPublicQueryManagementEnabled(any())).thenReturn(false);

            service.notifyCaseProceedOffline(caseData, AUTH_TOKEN);

            verifyDeleteNotificationsAndTaskListUpdates(caseData);
            verify(dashboardScenariosService).recordScenarios(
                eq(AUTH_TOKEN),
                eq(SCENARIO_AAA6_CASE_PROCEED_IN_CASE_MAN_DEFENDANT_WITHOUT_TASK_CHANGES.getScenario()),
                eq(caseData.getCcdCaseReference().toString()),
                any()
            );
            verify(dashboardScenariosService).recordScenarios(
                eq(AUTH_TOKEN),
                eq(SCENARIO_AAA6_GENERAL_APPLICATION_INITIATE_APPLICATION_INACTIVE_DEFENDANT.getScenario()),
                eq(caseData.getCcdCaseReference().toString()),
                any()
            );
            verify(dashboardScenariosService).recordScenarios(
                eq(AUTH_TOKEN),
                eq(SCENARIO_AAA6_GENERAL_APPLICATION_AVAILABLE_DEFENDANT.getScenario()),
                eq(caseData.getCcdCaseReference().toString()),
                any()
            );
        }

        @Test
        void shouldRecordScenario_whenCaseProgressionEnabledAndActiveJudgment() {
            when(toggleService.isPublicQueryManagementEnabled(any())).thenReturn(false);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .ccdCaseReference(5555L)
                .respondent1Represented(YesOrNo.NO)
                .activeJudgment(new JudgmentDetails())
                .previousCCDState(CaseState.All_FINAL_ORDERS_ISSUED)
                .build();

            service.notifyCaseProceedOffline(caseData, AUTH_TOKEN);

            verifyDeleteNotificationsAndTaskListUpdates(caseData);
            verify(dashboardScenariosService).recordScenarios(
                eq(AUTH_TOKEN),
                eq(SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT.getScenario()),
                eq(caseData.getCcdCaseReference().toString()),
                any()
            );
        }

        @Test
        void shouldRecordScenario_whenFastTrackActiveJudgment() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec().build().toBuilder()
                .ccdCaseReference(9999L)
                .respondent1Represented(YesOrNo.NO)
                .activeJudgment(new JudgmentDetails())
                .responseClaimTrack(AllocatedTrack.FAST_CLAIM.toString())
                .previousCCDState(CaseState.All_FINAL_ORDERS_ISSUED)
                .build();

            service.notifyCaseProceedOffline(caseData, AUTH_TOKEN);

            verify(dashboardScenariosService).recordScenarios(
                eq(AUTH_TOKEN),
                eq(SCENARIO_AAA6_UPDATE_CASE_PROCEED_IN_CASE_MAN_DEFENDANT_FAST_TRACK.getScenario()),
                eq(caseData.getCcdCaseReference().toString()),
                any()
            );
        }

        @Test
        void shouldRecordQueryScenario_whenCitizenQueryAwaitingResponse() {
            when(toggleService.isPublicQueryManagementEnabled(any())).thenReturn(true);

            CaseData caseData = CaseDataBuilder.builder().atStateRespondentFullAdmissionSpec()
                .includesRespondentCitizenQueryFollowUp(OffsetDateTime.now())
                .build().toBuilder()
                .respondent1Represented(YesOrNo.NO)
                .ccdCaseReference(1111L)
                .previousCCDState(CaseState.All_FINAL_ORDERS_ISSUED)
                .build();

            service.notifyCaseProceedOffline(caseData, AUTH_TOKEN);

            verify(dashboardScenariosService).recordScenarios(
                eq(AUTH_TOKEN),
                eq(SCENARIO_AAA6_LIP_QM_CASE_OFFLINE_OPEN_QUERIES_DEFENDANT.getScenario()),
                eq(caseData.getCcdCaseReference().toString()),
                any()
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
        void shouldBeEligibleForCasemanAndCaseProgressionWhenLrVLip() {
            CaseData caseData = CaseDataBuilder.builder().build();
            caseData.setApplicant1Represented(YesOrNo.YES);
            caseData.setRespondent1Represented(YesOrNo.NO);

            assertTrue(service.eligibleForCasemanState(caseData));
            assertTrue(service.eligibleForCaseProgressionState(caseData));
        }
    }

    private void verifyDeleteNotificationsAndTaskListUpdates(CaseData caseData) {
        verify(dashboardNotificationService).deleteByReferenceAndCitizenRole(
            caseData.getCcdCaseReference().toString(),
            "DEFENDANT"
        );
        verify(taskListService).makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            caseData.getCcdCaseReference().toString(),
            "DEFENDANT",
            "Applications"
        );
    }
}
